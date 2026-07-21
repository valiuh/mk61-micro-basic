package com.valiukh.microbasic

/**
 * AST – Abstract Syntax Tree
 *
 */
sealed class ASTNode

/**
 * Represents a STOP statement in the AST.
 */
class StopNode() : ASTNode()

/**
 * Represents a RETURN statement in the AST.
 */
class ReturnNode() : ASTNode()

/**
 * Represents a literal primary (number or identifier reference) inside an expression.
 *
 * @property value The raw literal or identifier text.
 */
data class LiteralNode(val value: String) : ASTNode()

/**
 * Represents an empty statement in the AST.
 */
class EmptyNode() : ASTNode()

/**
 * Represents a LET statement in the AST.
 *
 * @property variable The variable being assigned.
 * @property expression The expression being assigned to the variable.
 */
data class LetNode(
    val variable: String,
    val value: String? = null,
    val expression: List<ASTNode> = emptyList()
) : ASTNode()

/**
 * Represents a Identifier statement in the AST.
 *
 * @property variable The variable being assigned.
 * @property expression The expression being assigned to the variable.
 */
data class IdentifierNode(
    val variable: String,
    val value: String? = null,
    val expression: List<ASTNode> = emptyList()
) : ASTNode()

/**
 * Represents a PRINT statement in the AST.
 *
 * @property expression The expression to print.
 */
data class PrintNode(val expression: ASTNode) : ASTNode()

/**
 * Represents an INPUT statement in the AST.
 *
 * @property variable The variable to store the input.
 */
data class InputNode(val variable: String) : ASTNode()

/**
 * Represents an IF statement in the AST.
 *
 * @property variable The variable to compare.
 * @property operator The compare operator.
 * @property value The value to compare.
 * @property body The list of statements in the body of the IF statement.
 * @property bodyElse The list of statements in the body of the ELSE statement.
 */
data class IfNode(
    val expression: List<ASTNode> = emptyList(),
    val body: List<ASTNode>,
    val bodyElse: List<ASTNode>,
) : ASTNode()

/**
 * Represents a FOR loop statement in the AST.
 *
 * @property variable The loop variable.
 * @property start The starting value of the loop variable.
 * @property end The ending value of the loop variable.
 * @property step The step value for the loop variable.
 * @property body The list of statements in the loop body.
 */
data class ForLoopNode(
    val variable: String,
    val start: ASTNode,
    val end: ASTNode,
    val step: ASTNode,
    val body: List<ASTNode>
) : ASTNode()

/**
 * Represents a GOTO statement in the AST.
 *
 * @property line The line number to jump to.
 */
data class GotoNode(val line: Int) : ASTNode()

/**
 * Represents a GOSUB statement in the AST.
 *
 * @property name The name of the subroutine to call.
 */
data class GoSubNode(val name: String) : ASTNode()

/**
 * Represents an END statement in the AST.
 *
 * @property line The line number where the program ends.
 */
data class EndNode(val str: String) : ASTNode()

/**
 * Represents a SUBROUTINE statement in the AST.
 *
 * @property name The name of the subroutine.
 * @property body The list of statements in the subroutine.
 */
data class SubroutineNode(
    val name: String,
    val body: List<ASTNode>
) : ASTNode()

/**
 * Represents a Unary Operator statement in the AST.
 *
 * @property operator The operator being used.
 * @property value The value being assigned.
 * @property expression The expression to which operator should be applied.
 */
data class OperatorUnaryNode(
    val operator: String,
    val value: String? = null,
    val expression: List<ASTNode> = emptyList(),
) : ASTNode()

/**
 * Represents a Binary Operator statement in the AST.
 *
 * @property operator The operator being used.
 * @property value The value being assigned.
 * @property leftExpression The left expression to which operator should be applied.
 * @property rightExpression The right expression to which operator should be applied.
 */
data class OperatorBinaryNode(
    val operator: String,
    val leftValue: String? = null,
    val rightValue: String? = null,
    val leftExpression: List<ASTNode> = emptyList(),
    val rightExpression: List<ASTNode> = emptyList(),
) : ASTNode()

/**
 * Represents a new line number statement in the AST.
 */
data class LineNumberNode(
    val lineNumber: String
) : ASTNode()

/**
 * Represents an unknown statement in the AST.
 *
 * @property name The name of the unknown statement.
 */
data class UnknownNode(
    val name: String
) : ASTNode()

interface Parser {

    fun updateTokens(tokens: List<Token>)

    fun parse(): List<ASTNode>

}