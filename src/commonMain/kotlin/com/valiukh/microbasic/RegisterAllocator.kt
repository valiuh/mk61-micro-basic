package com.valiukh.microbasic

/**
 * Thrown when a program requires more registers than the MK-61 provides.
 */
class RegisterAllocationException(message: String) : Exception(message)

/**
 * Allocates MK-61 memory registers to variables.
 *
 * Variables are mapped to registers in the order in
 * which they first appear in the source, independent of their names. The MK-61
 * exposes a fixed set of 15 registers (`0`–`9`, `A`–`E`); requesting more than
 * that is an error.
 *
 * This is a pure, side-effect-free pass: it consumes an AST and produces a
 * `Variable -> Register` map that later stages (e.g. the Code Generator) can use.
 */
class RegisterAllocator {

    fun allocate(ast: List<ASTNode>): Map<String, Char> {
        val ordered = LinkedHashSet<String>()
        ast.forEach { collectVariables(it, ordered) }

        if (ordered.size > REGISTERS.size) {
            throw RegisterAllocationException(
                "Too many variables: ${ordered.size} required, only ${REGISTERS.size} registers available"
            )
        }

        return ordered.withIndex().associate { (index, name) -> name to REGISTERS[index] }
    }

    private fun collectVariables(node: ASTNode, out: MutableSet<String>) {
        when (node) {
            is LetNode -> {
                out.add(node.variable)
                node.value?.let { addIfVariable(it, out) }
                node.expression.forEach { collectVariables(it, out) }
            }

            is IdentifierNode -> {
                out.add(node.variable)
                node.value?.let { addIfVariable(it, out) }
                node.expression.forEach { collectVariables(it, out) }
            }

            is InputNode -> out.add(node.variable)

            is PrintNode -> collectVariables(node.expression, out)

            is IfNode -> {
                node.expression.forEach { collectVariables(it, out) }
                node.body.forEach { collectVariables(it, out) }
                node.bodyElse.forEach { collectVariables(it, out) }
            }

            is ForLoopNode -> {
                out.add(node.variable)
                collectVariables(node.start, out)
                collectVariables(node.end, out)
                collectVariables(node.step, out)
                node.body.forEach { collectVariables(it, out) }
            }

            is SubroutineNode -> node.body.forEach { collectVariables(it, out) }

            is OperatorBinaryNode -> {
                node.leftValue?.let { addIfVariable(it, out) }
                node.rightValue?.let { addIfVariable(it, out) }
                node.leftExpression.forEach { collectVariables(it, out) }
                node.rightExpression.forEach { collectVariables(it, out) }
            }

            is OperatorUnaryNode -> {
                node.value?.let { addIfVariable(it, out) }
                node.expression.forEach { collectVariables(it, out) }
            }

            is LiteralNode -> addIfVariable(node.value, out)

            else -> Unit
        }
    }

    private fun addIfVariable(token: String, out: MutableSet<String>) {
        if (token.toDoubleOrNull() == null) out.add(token)
    }

    companion object {
        /** The fixed MK-61 register file: `0`–`9`, `A`–`E`. */
        val REGISTERS: List<Char> = "0123456789ABCDE".toList()
    }
}
