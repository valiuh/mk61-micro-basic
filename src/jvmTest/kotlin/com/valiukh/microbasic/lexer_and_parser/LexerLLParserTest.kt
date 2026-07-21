package com.valiukh.microbasic.lexer_and_parser

import com.valiukh.microbasic.ASTNode
import com.valiukh.microbasic.ForLoopNode
import com.valiukh.microbasic.GoSubNode
import com.valiukh.microbasic.GotoNode
import com.valiukh.microbasic.IdentifierNode
import com.valiukh.microbasic.IfNode
import com.valiukh.microbasic.InputNode
import com.valiukh.microbasic.LetNode
import com.valiukh.microbasic.LiteralNode
import com.valiukh.microbasic.OperatorBinaryNode
import com.valiukh.microbasic.OperatorUnaryNode
import com.valiukh.microbasic.PrintNode
import com.valiukh.microbasic.ReturnNode
import com.valiukh.microbasic.dsl.parse
import com.valiukh.microbasic.parsers.LLParser
import com.valiukh.microbasic.dsl.tokenize
import kotlin.test.Test
import kotlin.test.assertNull

class LexerLLParserTest {
    private fun assert(condition: Boolean) = kotlin.test.assertTrue(condition)
    private val parser = LLParser()

    private fun ASTNode.lit(): String = (this as LiteralNode).value

    @Test
    fun `Scenario - LET`() {
        val script = "10 LET x = 5"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]

        val letNode = result as LetNode
        assert(letNode.variable == "x")
        assert(letNode.value == "5")
    }

    @Test
    fun `Scenario - LET with unar expression`() {
        val script = "10 LET x = SIN(30)"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is LetNode)

        val letNode = result as LetNode
        assert(letNode.variable == "x")
        assertNull(letNode.value)

        val expressions = letNode.expression
        assert(expressions.size == 1)

        val expression = expressions[0]
        assert(expression is OperatorUnaryNode)

        val operatorUnaryNode = expression as OperatorUnaryNode
        assert(operatorUnaryNode.operator == "SIN")
        assert(operatorUnaryNode.value == "30")
    }

    @Test
    fun `Scenario - LET with binary expression`() {
        val script = "10 LET x = 5 + 5"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is LetNode)

        val letNode = result as LetNode
        assert(letNode.variable == "x")
        assertNull(letNode.value)

        val expressions = letNode.expression
        assert(expressions.size == 1)
        val expression = expressions[0]
        assert(expression is OperatorBinaryNode)

        val operatorBinaryNode = expression as OperatorBinaryNode
        assert(operatorBinaryNode.operator == "+")
        assert(operatorBinaryNode.leftValue == "5")
        assert(operatorBinaryNode.rightValue == "5")
    }

    @Test
    fun `Scenario - x = 5`() {
        val script = "10 x = 5"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]

        val node = result as IdentifierNode
        assert(node.variable == "x")
        assert(node.value == "5")
    }

    @Test
    fun `Scenario - x = 5 + 5`() {
        val script = "10 x = 5 + 5"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is IdentifierNode)

        val node = result as IdentifierNode
        assert(node.variable == "x")
        assertNull(node.value)

        val expressions = node.expression
        assert(expressions.size == 1)
        val expression = expressions[0]
        assert(expression is OperatorBinaryNode)

        val operatorBinaryNode = expression as OperatorBinaryNode
        assert(operatorBinaryNode.operator == "+")
        assert(operatorBinaryNode.leftValue == "5")
        assert(operatorBinaryNode.rightValue == "5")
    }

    @Test
    fun `Scenario - x = SIN(30)`() {
        val script = "10 x = SIN(30)"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is IdentifierNode)

        val node = result as IdentifierNode
        assert(node.variable == "x")
        assertNull(node.value)

        val expressions = node.expression
        assert(expressions.size == 1)

        val expression = expressions[0]
        assert(expression is OperatorUnaryNode)

        val operatorUnaryNode = expression as OperatorUnaryNode
        assert(operatorUnaryNode.operator == "SIN")
        assert(operatorUnaryNode.value == "30")
    }

    @Test
    fun `Scenario - PRINT`() {
        val script = "20 PRINT x"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]

        val printNode = result as PrintNode
        assert(printNode.expression.lit() == "x")
    }

    @Test
    fun `Scenario - INPUT`() {
        val script = "30 INPUT x"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]

        val inputNode = result as InputNode
        assert(inputNode.variable == "x")
    }

    @Test
    fun `Scenario - GOTO`() {
        val script = "40 GOTO 50"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is GotoNode)

        val gotoNode = result as GotoNode
        assert(gotoNode.line == 50)
    }

    @Test
    fun `Scenario - GOSUB`() {
        val script = "40 GOSUB function"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is GoSubNode)

        val gotoSubNode = result as GoSubNode
        assert(gotoSubNode.name == "function")
    }

    @Test
    fun `Scenario - IF`() {
        val script = "40 IF x > 5 THEN GOTO 50 END"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)
        val result = abstractSyntaxTree[1]
        assert(result is IfNode)

        val ifNode = result as IfNode
        assert(ifNode.expression.size == 1)

        val expression = ifNode.expression[0]
        assert(expression is OperatorBinaryNode)

        val operatorBinaryNode = expression as OperatorBinaryNode
        assert(operatorBinaryNode.leftValue == "x")
        assert(operatorBinaryNode.operator == ">")
        assert(operatorBinaryNode.rightValue == "5")
        assert(ifNode.body.size == 1)

        val bodyNode = ifNode.body[0]
        assert(bodyNode is GotoNode)

        val gotoNode = bodyNode as GotoNode
        assert(gotoNode.line == 50)
    }

    @Test
    fun `Scenario - IF ELSE`() {
        val script = "40 IF x > 5 THEN GOTO 50 ELSE GOTO 60 END"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)
        val result = abstractSyntaxTree[1]
        assert(result is IfNode)

        assert(result is IfNode)

        val ifNode = result as IfNode
        assert(ifNode.expression.size == 1)

        val expression = ifNode.expression[0]
        assert(expression is OperatorBinaryNode)

        val operatorBinaryNode = expression as OperatorBinaryNode
        assert(operatorBinaryNode.leftValue == "x")
        assert(operatorBinaryNode.operator == ">")
        assert(operatorBinaryNode.rightValue == "5")
        assert(ifNode.body.size == 1)

        val bodyNode = ifNode.body[0]
        assert(bodyNode is GotoNode)

        val ifGotoNode = bodyNode as GotoNode
        assert(ifGotoNode.line == 50)

        val elseNode = ifNode.bodyElse[0]
        assert(elseNode is GotoNode)

        val elseGotoNode = elseNode as GotoNode
        assert(elseGotoNode.line == 60)
    }

    @Test
    fun `Scenario - IF with multiple expressions`() {
        val script = "40 IF x > 5 AND y < 10 THEN GOTO 50 END"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)
        val result = abstractSyntaxTree[1]
        assert(result is IfNode)

        val ifNode = result as IfNode
        assert(ifNode.body.size == 1)

        val bodyNode = ifNode.body[0]
        assert(bodyNode is GotoNode)

        val gotoNode = bodyNode as GotoNode
        assert(gotoNode.line == 50)

        val expressions = ifNode.expression
        assert(expressions.size == 1)

        val expression = expressions[0]
        assert(expression is OperatorBinaryNode)

        val operatorBinaryNode = expression as OperatorBinaryNode
        assert(operatorBinaryNode.operator == "AND")
        assertNull(operatorBinaryNode.leftValue)
        assertNull(operatorBinaryNode.rightValue)

        val leftExpression = operatorBinaryNode.leftExpression
        assert(leftExpression.size == 1)
        val leftExpressionNode = leftExpression[0]
        assert(leftExpressionNode is OperatorBinaryNode)

        val leftOperatorBinaryNode = leftExpressionNode as OperatorBinaryNode
        assert(leftOperatorBinaryNode.operator == ">")
        assert(leftOperatorBinaryNode.leftValue == "x")
        assert(leftOperatorBinaryNode.rightValue == "5")

        val rightExpression = operatorBinaryNode.rightExpression
        assert(rightExpression.size == 1)
        val rightExpressionNode = rightExpression[0]
        assert(rightExpressionNode is OperatorBinaryNode)

        val rightOperatorBinaryNode = rightExpressionNode as OperatorBinaryNode
        assert(rightOperatorBinaryNode.operator == "<")
        assert(rightOperatorBinaryNode.leftValue == "y")
        assert(rightOperatorBinaryNode.rightValue == "10")
    }

    @Test
    fun `Scenario - FOR empty body`() {
        val script = "50 FOR i = 1 TO 10 STEP 2 NEXT"
        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is ForLoopNode)

        val forNode = result as ForLoopNode
        assert(forNode.variable == "i")
        assert(forNode.start.lit() == "1")
        assert(forNode.end.lit() == "10")
        assert(forNode.step.lit() == "2")
        assert(forNode.body.isEmpty())
    }

    @Test
    fun `Scenario - FOR with body`() {
        val script = """
            50 FOR i = 1 TO 10 STEP 2
            60 PRINT i
            70 NEXT
        """.trimIndent()

        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is ForLoopNode)

        val forNode = result as ForLoopNode
        assert(forNode.variable == "i")
        assert(forNode.start.lit() == "1")
        assert(forNode.end.lit() == "10")
        assert(forNode.step.lit() == "2")
        assert(forNode.body.size == 1)

        val bodyNode = forNode.body[0]
        assert(bodyNode is PrintNode)

        val printNode = bodyNode as PrintNode
        assert(printNode.expression.lit() == "i")
    }

    @Test
    fun `Scenario - FOR unar expression body`() {
        val script = """
            50 FOR i = 1 TO 10 STEP 2
            60 LET x = SIN(i)
            70 NEXT
        """.trimIndent()

        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is ForLoopNode)

        val forNode = result as ForLoopNode
        assert(forNode.variable == "i")
        assert(forNode.start.lit() == "1")
        assert(forNode.end.lit() == "10")
        assert(forNode.step.lit() == "2")
        assert(forNode.body.size == 1)

        val bodyNode = forNode.body[0]
        assert(bodyNode is LetNode)

        val letNode = bodyNode as LetNode
        assert(letNode.variable == "x")
        assertNull(letNode.value)

        val expressions = letNode.expression
        assert(expressions.size == 1)

        val expression = expressions[0]
        assert(expression is OperatorUnaryNode)

        val operatorUnaryNode = expression as OperatorUnaryNode
        assert(operatorUnaryNode.operator == "SIN")
        assert(operatorUnaryNode.value == "i")
    }

    @Test
    fun `Scenario - FOR binary expression body`() {
        val script = """
            50 FOR i = 1 TO 10 STEP 2
            60 LET x = i + 5
            70 NEXT
        """.trimIndent()

        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is ForLoopNode)

        val forNode = result as ForLoopNode
        assert(forNode.variable == "i")
        assert(forNode.start.lit() == "1")
        assert(forNode.end.lit() == "10")
        assert(forNode.step.lit() == "2")
        assert(forNode.body.size == 1)

        val bodyNode = forNode.body[0]
        assert(bodyNode is LetNode)

        val letNode = bodyNode as LetNode
        assert(letNode.variable == "x")
        assertNull(letNode.value)

        val expressions = letNode.expression
        assert(expressions.size == 1)

        val expression = expressions[0]
        assert(expression is OperatorBinaryNode)

        val operatorBinaryNode = expression as OperatorBinaryNode
        assert(operatorBinaryNode.leftValue == "i")
        assert(operatorBinaryNode.operator == "+")
        assert(operatorBinaryNode.rightValue == "5")
    }

    @Test
    fun `Scenario - FOR with multiple body`() {
        val script = """
            50 FOR i = 1 TO 10 STEP 2
            60 LET x = i + 5
            70 PRINT x
            80 NEXT
        """.trimIndent()

        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is ForLoopNode)

        val forNode = result as ForLoopNode
        assert(forNode.variable == "i")
        assert(forNode.start.lit() == "1")
        assert(forNode.end.lit() == "10")
        assert(forNode.step.lit() == "2")
        assert(forNode.body.size == 2)

        val bodyNode1 = forNode.body[0]
        assert(bodyNode1 is LetNode)

        val letNode = bodyNode1 as LetNode
        assert(letNode.variable == "x")
        assertNull(letNode.value)

        val expressions = letNode.expression
        assert(expressions.size == 1)

        val expression = expressions[0]
        assert(expression is OperatorBinaryNode)

        val operatorBinaryNode = expression as OperatorBinaryNode
        assert(operatorBinaryNode.leftValue == "i")
        assert(operatorBinaryNode.operator == "+")
        assert(operatorBinaryNode.rightValue == "5")

        val bodyNode2 = forNode.body[1]
        assert(bodyNode2 is PrintNode)

        val printNode = bodyNode2 as PrintNode
        assert(printNode.expression.lit() == "x")
    }

    @Test
    fun `Scenario - FOR with nested FOR`() {
        val script = """
            50 FOR i = 1 TO 10 STEP 2
            60 FOR j = 1 TO 10 STEP 2
            70 PRINT j
            80 NEXT
            90 NEXT
        """.trimIndent()

        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 2)

        val result = abstractSyntaxTree[1]
        assert(result is ForLoopNode)

        val forNode = result as ForLoopNode
        assert(forNode.variable == "i")
        assert(forNode.start.lit() == "1")
        assert(forNode.end.lit() == "10")
        assert(forNode.step.lit() == "2")
        assert(forNode.body.size == 1)

        val bodyNode = forNode.body[0]
        assert(bodyNode is ForLoopNode)

        val nestedForNode = bodyNode as ForLoopNode
        assert(nestedForNode.variable == "j")
        assert(nestedForNode.start.lit() == "1")
        assert(nestedForNode.end.lit() == "10")
        assert(nestedForNode.step.lit() == "2")
        assert(nestedForNode.body.size == 1)

        val nestedBodyNode = nestedForNode.body[0]
        assert(nestedBodyNode is PrintNode)

        val printNode = nestedBodyNode as PrintNode
        assert(printNode.expression.lit() == "j")
    }

    @Test
    fun `Scenario - simple code`() {
        val script = """
            10 LET first = 10
            20 LET second = 30
            30 LET result = first + second
            40 IF result > 0 THEN GOTO 60 END
            50 LET test = 50
        """.trimIndent()

        val abstractSyntaxTree = script
            .tokenize()
            .parse(parser)

        assert(abstractSyntaxTree.size == 10)
        val letNode1 = abstractSyntaxTree[1] as LetNode
        assert(letNode1.variable == "first")
        assert(letNode1.value == "10")

        val letNode2 = abstractSyntaxTree[3] as LetNode
        assert(letNode2.variable == "second")
        assert(letNode2.value == "30")

        val letNode3 = abstractSyntaxTree[5] as LetNode
        assert(letNode3.variable == "result")
        assert(letNode3.expression.size == 1)

        val operatorBinaryNode = letNode3.expression[0] as OperatorBinaryNode
        assert(operatorBinaryNode.operator == "+")
        assert(operatorBinaryNode.leftValue == "first")
        assert(operatorBinaryNode.rightValue == "second")

        val ifNode = abstractSyntaxTree[7] as IfNode
        assert(ifNode.expression.size == 1)

        val operatorBinaryNode2 = ifNode.expression[0] as OperatorBinaryNode
        assert(operatorBinaryNode2.operator == ">")
        assert(operatorBinaryNode2.leftValue == "result")
        assert(operatorBinaryNode2.rightValue == "0")

        assert(ifNode.body.size == 1)
        val gotoNode = ifNode.body[0] as GotoNode
        assert(gotoNode.line == 60)

        val letNode4 = abstractSyntaxTree[9] as LetNode
        assert(letNode4.variable == "test")
        assert(letNode4.value == "50")
    }

    @Test
    fun `Scenario - LET with operator precedence`() {
        val script = "10 LET x = 2 + 3 * 4"
        val letNode = script.tokenize().parse(parser)[1] as LetNode

        assert(letNode.variable == "x")
        assertNull(letNode.value)

        val add = letNode.expression[0] as OperatorBinaryNode
        assert(add.operator == "+")
        assert(add.leftValue == "2")

        val mul = add.rightExpression[0] as OperatorBinaryNode
        assert(mul.operator == "*")
        assert(mul.leftValue == "3")
        assert(mul.rightValue == "4")
    }

    @Test
    fun `Scenario - LET with parentheses`() {
        val script = "10 LET x = (a + b) * (c - d)"
        val letNode = script.tokenize().parse(parser)[1] as LetNode

        val mul = letNode.expression[0] as OperatorBinaryNode
        assert(mul.operator == "*")

        val left = mul.leftExpression[0] as OperatorBinaryNode
        assert(left.operator == "+")
        assert(left.leftValue == "a")
        assert(left.rightValue == "b")

        val right = mul.rightExpression[0] as OperatorBinaryNode
        assert(right.operator == "-")
        assert(right.leftValue == "c")
        assert(right.rightValue == "d")
    }

    @Test
    fun `Scenario - LET with unary minus`() {
        val script = "10 LET x = -b"
        val letNode = script.tokenize().parse(parser)[1] as LetNode

        val neg = letNode.expression[0] as OperatorUnaryNode
        assert(neg.operator == "-")
        assert(neg.value == "b")
    }

    @Test
    fun `Scenario - LET with two-argument function`() {
        val script = "10 LET x = POW(2, 8)"
        val letNode = script.tokenize().parse(parser)[1] as LetNode

        val pow = letNode.expression[0] as OperatorBinaryNode
        assert(pow.operator == "POW")
        assert(pow.leftValue == "2")
        assert(pow.rightValue == "8")
    }

    @Test
    fun `Scenario - LET with nested function argument`() {
        val script = "10 LET x = SIN(a + b)"
        val letNode = script.tokenize().parse(parser)[1] as LetNode

        val sin = letNode.expression[0] as OperatorUnaryNode
        assert(sin.operator == "SIN")
        assertNull(sin.value)

        val arg = sin.expression[0] as OperatorBinaryNode
        assert(arg.operator == "+")
        assert(arg.leftValue == "a")
        assert(arg.rightValue == "b")
    }

    @Test
    fun `Scenario - IF with three logical conditions`() {
        val script = "10 IF a > 0 AND b > 0 AND c > 0 THEN GOTO 50 END"
        val ifNode = script.tokenize().parse(parser)[1] as IfNode

        val outerAnd = ifNode.expression[0] as OperatorBinaryNode
        assert(outerAnd.operator == "AND")

        // Left-associative: ((a>0 AND b>0) AND c>0)
        val innerAnd = outerAnd.leftExpression[0] as OperatorBinaryNode
        assert(innerAnd.operator == "AND")

        val rightCond = outerAnd.rightExpression[0] as OperatorBinaryNode
        assert(rightCond.operator == ">")
        assert(rightCond.leftValue == "c")
        assert(rightCond.rightValue == "0")
    }

    @Test
    fun `Scenario - IF with NOT condition`() {
        val script = "10 IF NOT flag THEN GOTO 50 END"
        val ifNode = script.tokenize().parse(parser)[1] as IfNode

        val not = ifNode.expression[0] as OperatorUnaryNode
        assert(not.operator == "NOT")
        assert(not.value == "flag")
    }

    @Test
    fun `Scenario - IF with XOR condition`() {
        val script = "10 IF a XOR b THEN GOTO 50 END"
        val ifNode = script.tokenize().parse(parser)[1] as IfNode

        val xor = ifNode.expression[0] as OperatorBinaryNode
        assert(xor.operator == "XOR")
        assert(xor.leftValue == "a")
        assert(xor.rightValue == "b")
    }

    @Test
    fun `Scenario - RETURN standalone`() {
        val script = "10 RETURN"
        val node = script.tokenize().parse(parser)[1]
        assert(node is ReturnNode)
    }

    @Test
    fun `Scenario - PRINT negative literal`() {
        val script = "10 PRINT -1"
        val printNode = script.tokenize().parse(parser)[1] as PrintNode

        val neg = printNode.expression as OperatorUnaryNode
        assert(neg.operator == "-")
        assert(neg.value == "1")
    }

    @Test
    fun `Scenario - FOR with variable bounds`() {
        val script = "10 FOR i = 1 TO n NEXT"
        val forNode = script.tokenize().parse(parser)[1] as ForLoopNode

        assert(forNode.variable == "i")
        assert(forNode.start.lit() == "1")
        assert(forNode.end.lit() == "n")
        assert(forNode.step.lit() == "1")
    }

    @Test
    fun `Scenario - FOR with negative step`() {
        val script = "10 FOR i = 10 TO 1 STEP -1 NEXT"
        val forNode = script.tokenize().parse(parser)[1] as ForLoopNode

        assert(forNode.start.lit() == "10")
        assert(forNode.end.lit() == "1")

        val step = forNode.step as OperatorUnaryNode
        assert(step.operator == "-")
        assert(step.value == "1")
    }

}