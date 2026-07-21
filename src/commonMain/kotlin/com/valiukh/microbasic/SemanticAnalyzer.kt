package com.valiukh.microbasic

/**
 * A semantic problem detected in an AST.
 *
 * Errors are returned instead of thrown so that all problems in a program can be
 * reported at once, before any code is generated.
 */
sealed class SemanticError {

    abstract val message: String

    /** The program needs more registers than the MK-61 provides. */
    data class RegisterOverflow(val required: Int, val available: Int) : SemanticError() {
        override val message: String =
            "Too many variables: $required required, only $available registers available"
    }

    /** A variable is read but never assigned anywhere in the program. */
    data class UndefinedVariable(val name: String) : SemanticError() {
        override val message: String = "Variable '$name' is used but never defined"
    }

    /** A `GOSUB` targets a subroutine that does not exist. */
    data class UnresolvedSubroutine(val name: String) : SemanticError() {
        override val message: String = "GOSUB targets undefined subroutine '$name'"
    }

    /** The same subroutine name is declared more than once. */
    data class DuplicateSubroutine(val name: String) : SemanticError() {
        override val message: String = "Subroutine '$name' is declared more than once"
    }

    /** A `GOTO` targets a line number that does not exist. */
    data class UnresolvedGotoTarget(val line: Int) : SemanticError() {
        override val message: String = "GOTO targets undefined line number $line"
    }

    /** An unrecognized statement survived parsing. */
    data class UnknownStatement(val name: String) : SemanticError() {
        override val message: String = "Unknown statement: '$name'"
    }
}

/**
 * Validates that an AST is *meaningful*, not merely syntactically valid.
 *
 * The analyzer never throws for program-level problems; it returns a list of
 * [SemanticError]. An empty list means the program is semantically valid.
 *
 * Checks performed:
 * - register overflow (more variables than the MK-61 can hold),
 * - variables used but never defined,
 * - duplicate subroutine declarations,
 * - `GOSUB` to an undefined subroutine,
 * - `GOTO` to an undefined line number,
 * - unknown statements left over from parsing.
 */
class SemanticAnalyzer {

    fun analyze(ast: List<ASTNode>): List<SemanticError> {
        val errors = mutableListOf<SemanticError>()

        val defined = LinkedHashSet<String>()
        val used = LinkedHashSet<String>()
        val subroutines = mutableListOf<String>()
        val lineNumbers = mutableSetOf<Int>()

        ast.forEach { collect(it, defined, used, subroutines, lineNumbers) }

        // Duplicate subroutines.
        subroutines.groupingBy { it }.eachCount()
            .filterValues { it > 1 }
            .keys
            .forEach { errors.add(SemanticError.DuplicateSubroutine(it)) }

        // Variables used but never assigned anywhere.
        used.filterNot { it in defined }
            .forEach { errors.add(SemanticError.UndefinedVariable(it)) }

        // Unresolved GOTO / GOSUB targets and leftover unknown statements.
        val declaredSubroutines = subroutines.toSet()
        ast.forEach { validateReferences(it, declaredSubroutines, lineNumbers, errors) }

        // Register overflow.
        val allVariables = LinkedHashSet<String>().apply {
            addAll(defined)
            addAll(used)
        }
        if (allVariables.size > REGISTER_COUNT) {
            errors.add(SemanticError.RegisterOverflow(allVariables.size, REGISTER_COUNT))
        }

        return errors
    }

    private fun collect(
        node: ASTNode,
        defined: MutableSet<String>,
        used: MutableSet<String>,
        subroutines: MutableList<String>,
        lineNumbers: MutableSet<Int>
    ) {
        when (node) {
            is LetNode -> {
                defined.add(node.variable)
                node.value?.let { addUse(it, used) }
                node.expression.forEach { collectExpression(it, used) }
            }

            is IdentifierNode -> {
                defined.add(node.variable)
                node.value?.let { addUse(it, used) }
                node.expression.forEach { collectExpression(it, used) }
            }

            is InputNode -> defined.add(node.variable)

            is PrintNode -> collectExpression(node.expression, used)

            is IfNode -> {
                node.expression.forEach { collectExpression(it, used) }
                node.body.forEach { collect(it, defined, used, subroutines, lineNumbers) }
                node.bodyElse.forEach { collect(it, defined, used, subroutines, lineNumbers) }
            }

            is ForLoopNode -> {
                defined.add(node.variable)
                collectExpression(node.start, used)
                collectExpression(node.end, used)
                collectExpression(node.step, used)
                node.body.forEach { collect(it, defined, used, subroutines, lineNumbers) }
            }

            is SubroutineNode -> {
                subroutines.add(node.name)
                node.body.forEach { collect(it, defined, used, subroutines, lineNumbers) }
            }

            is LineNumberNode -> node.lineNumber.toIntOrNull()?.let { lineNumbers.add(it) }

            else -> Unit
        }
    }

    private fun collectExpression(node: ASTNode, used: MutableSet<String>) {
        when (node) {
            is LiteralNode -> addUse(node.value, used)

            is OperatorBinaryNode -> {
                node.leftValue?.let { addUse(it, used) }
                node.rightValue?.let { addUse(it, used) }
                node.leftExpression.forEach { collectExpression(it, used) }
                node.rightExpression.forEach { collectExpression(it, used) }
            }

            is OperatorUnaryNode -> {
                node.value?.let { addUse(it, used) }
                node.expression.forEach { collectExpression(it, used) }
            }

            else -> Unit
        }
    }

    private fun validateReferences(
        node: ASTNode,
        subroutines: Set<String>,
        lineNumbers: Set<Int>,
        errors: MutableList<SemanticError>
    ) {
        when (node) {
            is GotoNode ->
                if (node.line !in lineNumbers) errors.add(SemanticError.UnresolvedGotoTarget(node.line))

            is GoSubNode ->
                if (node.name !in subroutines) errors.add(SemanticError.UnresolvedSubroutine(node.name))

            is UnknownNode -> errors.add(SemanticError.UnknownStatement(node.name))

            is IfNode -> {
                node.body.forEach { validateReferences(it, subroutines, lineNumbers, errors) }
                node.bodyElse.forEach { validateReferences(it, subroutines, lineNumbers, errors) }
            }

            is ForLoopNode ->
                node.body.forEach { validateReferences(it, subroutines, lineNumbers, errors) }

            is SubroutineNode ->
                node.body.forEach { validateReferences(it, subroutines, lineNumbers, errors) }

            else -> Unit
        }
    }

    private fun addUse(token: String, used: MutableSet<String>) {
        if (token.toDoubleOrNull() == null) used.add(token)
    }

    companion object {
        /** Number of MK-61 registers available (`0`–`9`, `A`–`E`). */
        const val REGISTER_COUNT: Int = 15
    }
}
