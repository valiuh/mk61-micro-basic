package com.valiukh.microbasic.parsers


import com.valiukh.microbasic.*
import com.valiukh.microbasic.ASTNode
import com.valiukh.microbasic.EmptyNode
import com.valiukh.microbasic.EndNode
import com.valiukh.microbasic.ForLoopNode
import com.valiukh.microbasic.GoSubNode
import com.valiukh.microbasic.GotoNode
import com.valiukh.microbasic.IdentifierNode
import com.valiukh.microbasic.IfNode
import com.valiukh.microbasic.InputNode
import com.valiukh.microbasic.LetNode
import com.valiukh.microbasic.LineNumberNode
import com.valiukh.microbasic.OperatorBinaryNode
import com.valiukh.microbasic.OperatorUnaryNode
import com.valiukh.microbasic.Parser
import com.valiukh.microbasic.PrintNode
import com.valiukh.microbasic.StopNode
import com.valiukh.microbasic.SubroutineNode
import com.valiukh.microbasic.UnknownNode

class LLParser : Parser {

    private var tokens: List<Token> = emptyList()
    private var position = 0

    override fun updateTokens(tokens: List<Token>) {
        this.tokens = tokens
        this.position = 0
    }

    override fun parse(): List<ASTNode> {
        val nodes = mutableListOf<ASTNode>()
        while (!isAtEnd()) {
            parseStatement()?.let { node ->
                if (node !is EmptyNode){
                    nodes.add(node)
                } else {
                    position++ // Skip newline
                }
            }
        }
        return nodes
    }

    private fun parseStatement(): ASTNode? {
        val token = peek() ?: return null
        return when (token.type) {
            TokenType.NUMBER -> parseLineNumber()
            TokenType.IDENTIFIER -> parseIdentifierNode()
            TokenType.KEYWORD -> parseKeyword(token)
            TokenType.NEWLINE -> EmptyNode()
            else -> UnknownNode(token.value)
        }
    }

    private fun parseLineNumber(): ASTNode {
        val token = advance()
        return LineNumberNode(lineNumber = token.value)
    }

    private fun parseIdentifierNode(isLet: Boolean = false): ASTNode {
        val variable = advance().value
        consumeOperator()

        val rhs = parseExpression()

        return if (rhs is LiteralNode) {
            createAssignmentNode(
                variable = variable,
                value = rhs.value,
                isLet = isLet
            )
        } else {
            createAssignmentNode(
                variable = variable,
                expression = listOf(rhs),
                isLet = isLet
            )
        }
    }

    private fun parseKeyword(token: Token): ASTNode {
        return when (token.value) {
            Keyword.LET.keyword -> parseLet()
            Keyword.PRINT.keyword -> parsePrint()
            Keyword.INPUT.keyword -> parseInput()
            Keyword.IF.keyword -> parseIf()
            Keyword.FOR.keyword -> parseFor()
            Keyword.GOTO.keyword -> parseGoto()
            Keyword.GOSUB.keyword -> parseGosub()
            Keyword.SUB.keyword -> parseSubroutine()
            Keyword.END.keyword -> advance().let { EndNode(it.value) }
            Keyword.STOP.keyword -> advance().let { StopNode() }
            Keyword.RETURN.keyword -> advance().let { ReturnNode() }
            in BuiltInFunction.entries.map { it.function } -> parseExpression()
            in Operator.entries.map { it.operator } -> parseExpression()
            else -> UnknownNode(token.value)
        }
    }

    private fun parseLet(): ASTNode {
        advance() // Skip LET
        peek()

        return parseIdentifierNode(
            isLet = true
        )

    }

    private fun parsePrint(): ASTNode {
        advance() // Skip PRINT

        return PrintNode(
            expression = parseExpression()
        )
    }

    private fun parseInput(): ASTNode {
        advance() // Skip INPUT

        return InputNode(
            variable = advance().value
        )
    }

    private fun parseIf(): ASTNode {
        advance() // Skip IF

        val condition = parseExpression()

        consumeValue(Keyword.THEN.keyword)

        val body = mutableListOf<ASTNode>()
        val elseBody = mutableListOf<ASTNode>()

        parseBlock(body, Keyword.END.keyword, Keyword.ELSE.keyword)

        if (matchValue(Keyword.ELSE.keyword)) {
            advance() // Skip ELSE
            parseBlock(elseBody, Keyword.END.keyword)
        }

        if (matchValue(Keyword.END.keyword)) {
            advance() // Consume the closing END of the IF block
        }

        return IfNode(
            expression = listOf(condition),
            body = body,
            bodyElse = elseBody
        )
    }

    private fun parseBlock(target: MutableList<ASTNode>, vararg terminators: String) {
        while (!isAtEnd() && !matchValue(*terminators)) {
            val token = peek() ?: break
            if (token.type == TokenType.NEWLINE) {
                advance()
                continue
            }
            val node = parseStatement() ?: break
            if (node !is EmptyNode && node !is LineNumberNode) {
                target.add(node)
            }
        }
    }

    private fun parseFor(): ASTNode {
        advance() // Skip FOR
        val variable = advanceOrThrow("Expected loop variable").value
        consumeValue("=")
        val start = parseExpression()
        consumeValue(Keyword.TO.keyword)
        val end = parseExpression()

        val step = if (matchValue(Keyword.STEP.keyword)) {
            advance() // Skip STEP
            parseExpression()
        } else LiteralNode("1")

        val body = mutableListOf<ASTNode>()

        // 🚨 SAFELY PARSE LOOP BODY
        while (!isAtEnd()) {
            val token = peek() ?: break

            if (token.value == Keyword.NEXT.keyword) {
                advance() // consume NEXT
                break
            }

            if (token.type == TokenType.NEWLINE) {
                position++ // Skip newline
                continue
            }

            val node = parseStatement()
            if (node != null) {
                body.add(node)
            } else {
                position++ // safeguard
            }
        }

        return ForLoopNode(
            variable = variable,
            start = start,
            end = end,
            step = step,
            body = body.filter { it !is LineNumberNode }
        )
    }


    private fun parseGoto(): ASTNode {
        advance() // Skip GOTO

        return GotoNode(
            line = advance().value.toInt()
        )
    }

    private fun parseGosub(): ASTNode {
        advance() // Skip GOSUB

        return GoSubNode(
            name = advance().value
        )
    }

    private fun parseSubroutine(): ASTNode {
        advance() // Skip SUB
        val name = advance().value
        val body = mutableListOf<ASTNode>()
        while (!isAtEnd() && !matchValue(Keyword.RETURN.keyword)) {
            val token = peek() ?: break
            if (token.type == TokenType.NEWLINE) {
                advance() // Skip newline
                continue
            }
            val node = parseStatement() ?: break
            if (node !is EmptyNode && node !is LineNumberNode) {
                body.add(node)
            }
        }
        if (matchValue(Keyword.RETURN.keyword)) {
            advance() // Skip RETURN
        }

        return SubroutineNode(
            name = name,
            body = body
        )
    }

    // region Expression parsing (recursive descent with precedence)

    private fun parseExpression(): ASTNode = parseLogicalOr()

    private fun parseLogicalOr(): ASTNode {
        var left = parseLogicalXor()
        while (matchValue(Operator.OR.operator)) {
            val op = advance().value
            left = binaryNode(op, left, parseLogicalXor())
        }
        return left
    }

    private fun parseLogicalXor(): ASTNode {
        var left = parseLogicalAnd()
        while (matchValue(Operator.XOR.operator)) {
            val op = advance().value
            left = binaryNode(op, left, parseLogicalAnd())
        }
        return left
    }

    private fun parseLogicalAnd(): ASTNode {
        var left = parseEquality()
        while (matchValue(Operator.AND.operator)) {
            val op = advance().value
            left = binaryNode(op, left, parseEquality())
        }
        return left
    }

    private fun parseEquality(): ASTNode {
        var left = parseComparison()
        while (matchValue(Operator.EQUAL.operator, Operator.NOT_EQUAL.operator)) {
            val op = advance().value
            left = binaryNode(op, left, parseComparison())
        }
        return left
    }

    private fun parseComparison(): ASTNode {
        var left = parseTerm()
        while (
            matchValue(
                Operator.LESS_THAN.operator,
                Operator.LESS_THAN_OR_EQUAL.operator,
                Operator.GREATER_THAN.operator,
                Operator.GREATER_THAN_OR_EQUAL.operator
            )
        ) {
            val op = advance().value
            left = binaryNode(op, left, parseTerm())
        }
        return left
    }

    private fun parseTerm(): ASTNode {
        var left = parseFactor()
        while (matchValue(Operator.PLUS.operator, Operator.MINUS.operator)) {
            val op = advance().value
            left = binaryNode(op, left, parseFactor())
        }
        return left
    }

    private fun parseFactor(): ASTNode {
        var left = parsePower()
        while (matchValue(Operator.MULTIPLY.operator, Operator.DIVIDE.operator)) {
            val op = advance().value
            left = binaryNode(op, left, parsePower())
        }
        return left
    }

    private fun parsePower(): ASTNode {
        val left = parseUnary()
        return if (matchValue(Operator.POWER.operator)) {
            val op = advance().value
            binaryNode(op, left, parsePower()) // Right-associative
        } else {
            left
        }
    }

    private fun parseUnary(): ASTNode {
        if (matchValue(Operator.NOT.operator, Operator.MINUS.operator)) {
            val op = advance().value
            return unaryNode(op, parseUnary())
        }
        return parsePrimary()
    }

    private fun parsePrimary(): ASTNode {
        val token = peek() ?: throw IllegalStateException("Unexpected end of input in expression")

        if (token.value == Bracket.LEFT.bracket) {
            advance() // Skip (
            val expression = parseExpression()
            consumeValue(Bracket.RIGHT.bracket)
            return expression
        }

        if (isBuiltInFunction(token.value)) {
            return parseFunctionCall()
        }

        if (token.type == TokenType.NUMBER || token.type == TokenType.IDENTIFIER) {
            return LiteralNode(advance().value)
        }

        throw IllegalStateException("Unexpected token in expression: '${token.value}'")
    }

    private fun parseFunctionCall(): ASTNode {
        val name = advance().value
        consumeValue(Bracket.LEFT.bracket)
        val first = parseExpression()

        if (matchValue(Delimiter.COMMA.delimiter)) {
            advance() // Skip ,
            val second = parseExpression()
            consumeValue(Bracket.RIGHT.bracket)
            return binaryNode(name, first, second)
        }

        consumeValue(Bracket.RIGHT.bracket)
        return unaryNode(name, first)
    }

    private fun binaryNode(operator: String, left: ASTNode, right: ASTNode): OperatorBinaryNode =
        OperatorBinaryNode(
            operator = operator,
            leftValue = (left as? LiteralNode)?.value,
            rightValue = (right as? LiteralNode)?.value,
            leftExpression = if (left is LiteralNode) emptyList() else listOf(left),
            rightExpression = if (right is LiteralNode) emptyList() else listOf(right)
        )

    private fun unaryNode(operator: String, operand: ASTNode): OperatorUnaryNode =
        OperatorUnaryNode(
            operator = operator,
            value = (operand as? LiteralNode)?.value,
            expression = if (operand is LiteralNode) emptyList() else listOf(operand)
        )

    private fun isBuiltInFunction(value: String): Boolean =
        BuiltInFunction.entries.any { it.function == value }

    // endregion

    private fun createAssignmentNode(
        variable: String,
        expression: List<ASTNode> = emptyList(),
        value: String? = null,
        isLet: Boolean = false
    ): ASTNode {

        return if (isLet)
            LetNode(
                variable = variable,
                value = value,
                expression = expression
            )
        else
            IdentifierNode(
                variable = variable,
                value = value,
                expression = expression
            )
    }

    private fun peek(offset: Int = 0): Token? =
        tokens.getOrNull(position + offset)

    private fun advance(): Token =
        tokens[position++]

    private fun advanceOrThrow(message: String): Token =
        if (!isAtEnd())
            advance()
        else
            throw IllegalStateException(message)

    private fun consumeOperator(): Token {
        val token = advance()
        require(token.type == TokenType.OPERATOR) { "Expected '=' after identifier" }
        return token
    }

    private fun consumeValue(expected: String): Token {
        val token = advance()
        require(token.value == expected) { "Expected '$expected', but got '${token.value}'" }
        return token
    }

    private fun matchValue(vararg expected: String): Boolean {
        return expected.any { peek()?.value == it }
    }

    private fun isAtEnd(): Boolean = peek()?.type == TokenType.EOF
}