package com.valiukh.microbasic

import com.valiukh.virtualmachine.Command

/**
 * Translates a semantically valid MiniBasic AST into a list of MK-61 virtual machine
 * program steps.
 *
 * Each element of the returned list is a single VM step, exactly as consumed by
 * [com.valiukh.virtualmachine.Mk61.uploadProgram] once the steps are joined
 * with newlines.
 *
 * Design notes tied to the MK-61 architecture:
 * - Jump targets are absolute step indices, so control flow is generated against symbolic
 *   labels first and resolved to indices in a second pass.
 * - Control commands (jumps, subroutine calls) and their address occupy two steps; memory
 *   commands (`X→П` / `П→X`) and their register occupy one space-separated step.
 * - The evaluation stack (X, Y, Z, T) is only four levels deep and never spills; number
 *   entry and register recall overwrite X without lifting, so `B↑` is emitted explicitly to
 *   preserve operands.
 * - Register names are emitted in lower case (`0`–`9`, `a`–`e`) to match the VM register
 *   file, even though [RegisterAllocator] reports the upper-case `A`–`E`.
 *
 * @property registers An optional, pre-computed `variable -> register` allocation. When
 *   empty, the generator allocates registers itself through [RegisterAllocator].
 */
class CodeGenerator(private val registers: Map<String, Char> = emptyMap()) {

    /** A single item in the intermediate representation, before address resolution. */
    private sealed class Ir {
        /** One program step whose text is already final (number, operator, memory command…). */
        data class Insn(val text: String) : Ir()

        /** A control command plus a symbolic address; expands to two program steps. */
        data class Jump(val command: String, val label: String) : Ir()

        /** A zero-width marker that records the absolute address of the next instruction. */
        data class Label(val name: String) : Ir()
    }

    private val ir = mutableListOf<Ir>()
    private var allocation: Map<String, Char> = emptyMap()
    private var labelSeq = 0

    /**
     * Generates MK-61 program steps for the given [ast].
     *
     * @param ast The MiniBasic abstract syntax tree to translate.
     * @return The MK-61 program as an ordered list of VM steps.
     */
    fun generate(ast: List<ASTNode>): List<String> {
        ir.clear()
        labelSeq = 0
        allocation = registers.ifEmpty { RegisterAllocator().allocate(ast) }

        val subroutines = ast.filterIsInstance<SubroutineNode>()

        // Main program: subroutine definitions are hoisted, never emitted inline.
        ast.forEach { node -> if (node !is SubroutineNode) emitStatement(node) }

        // Terminate the main program so execution halts and never falls into subroutines.
        if ((ir.lastOrNull() as? Ir.Insn)?.text != Command.STOP.commandMnemonics) {
            ir.add(Ir.Insn(Command.STOP.commandMnemonics))
        }

        // Hoisted subroutine bodies.
        subroutines.forEach { emitSubroutine(it) }

        return resolve()
    }

    // region Statements

    private fun emitStatement(node: ASTNode) {
        when (node) {
            is LetNode -> emitAssignment(node.variable, node.value, node.expression)
            is IdentifierNode -> emitAssignment(node.variable, node.value, node.expression)
            is PrintNode -> compileExpression(node.expression)
            is InputNode -> ir.add(Ir.Insn(store(node.variable)))
            is IfNode -> emitIf(node)
            is ForLoopNode -> emitFor(node)
            is GotoNode -> ir.add(Ir.Jump(Command.GOTO.commandMnemonics, lineLabel(node.line)))
            is GoSubNode -> ir.add(Ir.Jump(Command.GOSUB.commandMnemonics, subLabel(node.name)))
            is ReturnNode -> ir.add(Ir.Insn(Command.RTN.commandMnemonics))
            is EndNode -> ir.add(Ir.Insn(Command.STOP.commandMnemonics))
            is StopNode -> ir.add(Ir.Insn(Command.STOP.commandMnemonics))
            is LineNumberNode -> node.lineNumber.toIntOrNull()?.let { ir.add(Ir.Label(lineLabel(it))) }
            is EmptyNode -> Unit
            else -> throw IllegalArgumentException("Unsupported statement: $node")
        }
    }

    private fun emitAssignment(variable: String, value: String?, expression: List<ASTNode>) {
        compileExpression(operandOf(value, expression))
        ir.add(Ir.Insn(store(variable)))
    }

    private fun emitSubroutine(node: SubroutineNode) {
        ir.add(Ir.Label(subLabel(node.name)))
        node.body.forEach { emitStatement(it) }
        ir.add(Ir.Insn(Command.RTN.commandMnemonics))
    }

    // endregion

    // region Control flow

    private fun emitIf(node: IfNode) {
        val condition = node.expression.firstOrNull()
            ?: throw IllegalArgumentException("IF requires a condition")
        val hasElse = node.bodyElse.isNotEmpty()
        val elseLabel = newLabel("else")
        val endLabel = newLabel("endif")

        // Jump to the else/end target when the condition is FALSE; fall through when TRUE.
        compileCondition(condition, jumpWhenFalseTo = if (hasElse) elseLabel else endLabel)

        node.body.forEach { emitStatement(it) }

        if (hasElse) {
            ir.add(Ir.Jump(Command.GOTO.commandMnemonics, endLabel))
            ir.add(Ir.Label(elseLabel))
            node.bodyElse.forEach { emitStatement(it) }
        }

        ir.add(Ir.Label(endLabel))
    }

    private fun emitFor(node: ForLoopNode) {
        val topLabel = newLabel("for")
        val endLabel = newLabel("next")
        val descending = isNegativeLiteral(node.step)

        // Initialise the loop variable: variable = start.
        compileExpression(node.start)
        ir.add(Ir.Insn(store(node.variable)))

        ir.add(Ir.Label(topLabel))

        // Continue while (ascending) variable <= end  <=>  end - variable >= 0
        //          while (descending) variable >= end  <=>  variable - end >= 0
        if (descending) {
            ir.add(Ir.Insn(load(node.variable)))
            ir.add(Ir.Insn(Command.PUSH.commandMnemonics))
            compileExpression(node.end)
        } else {
            compileExpression(node.end)
            ir.add(Ir.Insn(Command.PUSH.commandMnemonics))
            ir.add(Ir.Insn(load(node.variable)))
        }
        ir.add(Ir.Insn(Command.SUB.commandMnemonics))
        ir.add(Ir.Jump(Command.NNG.commandMnemonics, endLabel))

        node.body.forEach { emitStatement(it) }

        // Increment: variable = variable + step.
        ir.add(Ir.Insn(load(node.variable)))
        ir.add(Ir.Insn(Command.PUSH.commandMnemonics))
        compileExpression(node.step)
        ir.add(Ir.Insn(Command.ADD.commandMnemonics))
        ir.add(Ir.Insn(store(node.variable)))

        ir.add(Ir.Jump(Command.GOTO.commandMnemonics, topLabel))
        ir.add(Ir.Label(endLabel))
    }

    /**
     * Emits a comparison followed by a conditional jump that transfers control to
     * [jumpWhenFalseTo] when [condition] evaluates to false, and falls through otherwise.
     *
     * Relational operators map to an MK-61 subtraction plus one of the conditional-jump
     * commands, exploiting the VM rule that a conditional jump falls through when its named
     * predicate holds. Any other expression is treated as a truthiness test (`X != 0`).
     */
    private fun compileCondition(condition: ASTNode, jumpWhenFalseTo: String) {
        if (condition is OperatorBinaryNode && isRelational(condition.operator)) {
            val left = operandOf(condition.leftValue, condition.leftExpression)
            val right = operandOf(condition.rightValue, condition.rightExpression)
            val (first, second, command) = relationalPlan(condition.operator, left, right)

            // Compute first - second into X (SUB computes Y - X, so first -> Y, second -> X).
            compileExpression(first)
            ir.add(Ir.Insn(Command.PUSH.commandMnemonics))
            compileExpression(second)
            ir.add(Ir.Insn(Command.SUB.commandMnemonics))
            ir.add(Ir.Jump(command, jumpWhenFalseTo))
        } else {
            compileExpression(condition)
            ir.add(Ir.Jump(Command.NZR.commandMnemonics, jumpWhenFalseTo))
        }
    }

    // endregion

    // region Expressions

    /** Compiles [node] so that its numeric value is left in the X register. */
    private fun compileExpression(node: ASTNode) {
        when (node) {
            is LiteralNode -> compilePrimary(node.value)
            is OperatorUnaryNode -> compileUnary(node)
            is OperatorBinaryNode -> compileBinary(node)
            else -> throw IllegalArgumentException("Unsupported expression: $node")
        }
    }

    private fun compilePrimary(token: String) {
        when {
            token.toDoubleOrNull() != null -> ir.add(Ir.Insn(token))
            token == Constant.PI.constant -> ir.add(Ir.Insn(Command.PI.commandMnemonics))
            token == Constant.E.constant -> ir.add(Ir.Insn(Command.E.commandMnemonics))
            else -> ir.add(Ir.Insn(load(token)))
        }
    }

    private fun compileUnary(node: OperatorUnaryNode) {
        val operand = operandOf(node.value, node.expression)
        when (node.operator) {
            Operator.MINUS.operator -> {
                // Negate as 0 - operand (the MK-61 has no dedicated sign-change command).
                ir.add(Ir.Insn(ZERO))
                ir.add(Ir.Insn(Command.PUSH.commandMnemonics))
                compileExpression(operand)
                ir.add(Ir.Insn(Command.SUB.commandMnemonics))
            }

            Operator.NOT.operator -> {
                compileExpression(operand)
                ir.add(Ir.Insn(Command.NOT.commandMnemonics))
            }

            else -> {
                compileExpression(operand)
                ir.add(Ir.Insn(functionCommand(node.operator)))
            }
        }
    }

    private fun compileBinary(node: OperatorBinaryNode) {
        val left = operandOf(node.leftValue, node.leftExpression)
        val right = operandOf(node.rightValue, node.rightExpression)
        val command = binaryCommand(node.operator)

        if (isPowerOperator(node.operator)) {
            // The VM computes X^Y, so the base must land in X and the exponent in Y.
            compileExpression(right)
            ir.add(Ir.Insn(Command.PUSH.commandMnemonics))
            compileExpression(left)
        } else {
            compileExpression(left)
            ir.add(Ir.Insn(Command.PUSH.commandMnemonics))
            compileExpression(right)
        }
        ir.add(Ir.Insn(command))
    }

    // endregion

    // region Address resolution

    private fun resolve(): List<String> {
        // Pass A: compute the absolute address of every label.
        val addresses = mutableMapOf<String, Int>()
        var address = 0
        for (item in ir) {
            when (item) {
                is Ir.Insn -> address += 1
                is Ir.Jump -> address += 2
                is Ir.Label -> addresses[item.name] = address
            }
        }

        // Pass B: emit final steps, resolving jump targets to their addresses.
        val output = mutableListOf<String>()
        for (item in ir) {
            when (item) {
                is Ir.Insn -> output.add(item.text)
                is Ir.Jump -> {
                    val target = addresses[item.label]
                        ?: throw IllegalArgumentException("Unresolved jump target: ${item.label}")
                    output.add(item.command)
                    output.add(target.toString())
                }

                is Ir.Label -> Unit
            }
        }
        return output
    }

    // endregion

    // region Helpers

    private fun operandOf(value: String?, expression: List<ASTNode>): ASTNode =
        if (value != null) LiteralNode(value) else expression.first()

    private fun store(variable: String): String =
        "${Command.MOVT.commandMnemonics} ${registerName(variable)}"

    private fun load(variable: String): String =
        "${Command.MOVF.commandMnemonics} ${registerName(variable)}"

    private fun registerName(variable: String): Char {
        val register = allocation[variable]
            ?: throw IllegalArgumentException("No register allocated for variable '$variable'")
        return register.lowercaseChar()
    }

    private fun newLabel(prefix: String): String = "$prefix#${labelSeq++}"

    private fun lineLabel(line: Int): String = "line#$line"

    private fun subLabel(name: String): String = "sub#$name"

    private fun isRelational(operator: String): Boolean = operator in RELATIONAL_OPERATORS

    private fun isPowerOperator(operator: String): Boolean =
        operator == Operator.POWER.operator || operator == BuiltInFunction.POW.function

    private fun isNegativeLiteral(node: ASTNode): Boolean = when (node) {
        is LiteralNode -> (node.value.toDoubleOrNull() ?: 0.0) < 0.0
        is OperatorUnaryNode ->
            node.operator == Operator.MINUS.operator &&
                node.expression.isEmpty() &&
                node.value?.toDoubleOrNull() != null

        else -> false
    }

    /**
     * Plans a relational comparison as `(first, second, command)` such that computing
     * `first - second` and issuing [command] falls through exactly when the relation holds.
     */
    private fun relationalPlan(
        operator: String,
        left: ASTNode,
        right: ASTNode
    ): Triple<ASTNode, ASTNode, String> = when (operator) {
        Operator.EQUAL.operator -> Triple(left, right, Command.ZRO.commandMnemonics)
        Operator.NOT_EQUAL.operator -> Triple(left, right, Command.NZR.commandMnemonics)
        Operator.LESS_THAN.operator -> Triple(left, right, Command.NEG.commandMnemonics)
        Operator.GREATER_THAN_OR_EQUAL.operator -> Triple(left, right, Command.NNG.commandMnemonics)
        Operator.GREATER_THAN.operator -> Triple(right, left, Command.NEG.commandMnemonics)
        Operator.LESS_THAN_OR_EQUAL.operator -> Triple(right, left, Command.NNG.commandMnemonics)
        else -> throw IllegalArgumentException("Unsupported relational operator: $operator")
    }

    private fun binaryCommand(operator: String): String = when (operator) {
        Operator.PLUS.operator -> Command.ADD.commandMnemonics
        Operator.MINUS.operator -> Command.SUB.commandMnemonics
        Operator.MULTIPLY.operator -> Command.MUL.commandMnemonics
        Operator.DIVIDE.operator -> Command.DIV.commandMnemonics
        Operator.POWER.operator -> Command.POW.commandMnemonics
        Operator.AND.operator -> Command.AND.commandMnemonics
        Operator.OR.operator -> Command.OR.commandMnemonics
        Operator.XOR.operator -> Command.XOR.commandMnemonics
        BuiltInFunction.POW.function -> Command.POW.commandMnemonics
        BuiltInFunction.MAX.function -> Command.MAX.commandMnemonics
        else -> throw IllegalArgumentException("Unsupported binary operator: $operator")
    }

    private fun functionCommand(function: String): String = when (function) {
        BuiltInFunction.SIN.function -> Command.SIN.commandMnemonics
        BuiltInFunction.COS.function -> Command.COS.commandMnemonics
        BuiltInFunction.TAN.function -> Command.TG.commandMnemonics
        BuiltInFunction.ASIN.function -> Command.ARCSIN.commandMnemonics
        BuiltInFunction.ACOS.function -> Command.ARCCOS.commandMnemonics
        BuiltInFunction.ATN.function -> Command.ARCTG.commandMnemonics
        BuiltInFunction.LOG.function -> Command.LG.commandMnemonics
        BuiltInFunction.LN.function -> Command.NL.commandMnemonics
        BuiltInFunction.EXP.function -> Command.EXP.commandMnemonics
        BuiltInFunction.EXP10.function -> Command.EXP10.commandMnemonics
        BuiltInFunction.ABS.function -> Command.ABS.commandMnemonics
        BuiltInFunction.SQRT.function -> Command.SQRT.commandMnemonics
        BuiltInFunction.SQR.function -> Command.SQR.commandMnemonics
        BuiltInFunction.RECIP.function -> Command.FRAC.commandMnemonics
        BuiltInFunction.FLOOR.function -> Command.FLR.commandMnemonics
        BuiltInFunction.FRAC.function -> Command.FRC.commandMnemonics
        BuiltInFunction.SIGN.function -> Command.SIGN.commandMnemonics
        BuiltInFunction.RANDOM.function -> Command.RANDOM.commandMnemonics
        BuiltInFunction.HM_TO_DEG.function -> Command.HM_TO_DEG.commandMnemonics
        BuiltInFunction.DEG_TO_HM.function -> Command.DEG_TO_HM.commandMnemonics
        BuiltInFunction.HMS_TO_DEG.function -> Command.HMS_TO_DEG.commandMnemonics
        BuiltInFunction.DEG_TO_HMS.function -> Command.DEG_TO_HMS.commandMnemonics
        else -> throw IllegalArgumentException("Unsupported function: $function")
    }

    // endregion

    private companion object {
        const val ZERO = "0"

        val RELATIONAL_OPERATORS = setOf(
            Operator.EQUAL.operator,
            Operator.NOT_EQUAL.operator,
            Operator.LESS_THAN.operator,
            Operator.LESS_THAN_OR_EQUAL.operator,
            Operator.GREATER_THAN.operator,
            Operator.GREATER_THAN_OR_EQUAL.operator
        )
    }
}