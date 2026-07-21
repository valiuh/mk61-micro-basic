package com.valiukh.microbasic.pre_code_generation

import com.valiukh.microbasic.GoSubNode
import com.valiukh.microbasic.GotoNode
import com.valiukh.microbasic.LetNode
import com.valiukh.microbasic.LineNumberNode
import com.valiukh.microbasic.LiteralNode
import com.valiukh.microbasic.OperatorBinaryNode
import com.valiukh.microbasic.PrintNode
import com.valiukh.microbasic.SemanticAnalyzer
import com.valiukh.microbasic.SemanticError
import com.valiukh.microbasic.SubroutineNode
import com.valiukh.microbasic.UnknownNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SemanticAnalyzerTest {

    private val analyzer = SemanticAnalyzer()

    @Test
    fun `valid program has no errors`() {
        // LET a = 5 : LET b = a : PRINT b
        val ast = listOf(
            LetNode(variable = "a", value = "5"),
            LetNode(variable = "b", value = "a"),
            PrintNode(expression = LiteralNode("b"))
        )

        assertTrue(analyzer.analyze(ast).isEmpty())
    }

    @Test
    fun `reports a variable that is used but never defined`() {
        // PRINT x  (x is never assigned)
        val ast = listOf(
            PrintNode(expression = LiteralNode("x"))
        )

        val errors = analyzer.analyze(ast)

        assertEquals(listOf(SemanticError.UndefinedVariable("x")), errors)
    }

    @Test
    fun `finds an undefined variable inside an expression`() {
        // LET result = a + b   (only 'a' is defined)
        val ast = listOf(
            LetNode(variable = "a", value = "1"),
            LetNode(
                variable = "result",
                expression = listOf(
                    OperatorBinaryNode(operator = "+", leftValue = "a", rightValue = "b")
                )
            )
        )

        val errors = analyzer.analyze(ast)

        assertEquals(listOf(SemanticError.UndefinedVariable("b")), errors)
    }

    @Test
    fun `reports duplicate subroutine declarations`() {
        val ast = listOf(
            SubroutineNode(name = "Square", body = emptyList()),
            SubroutineNode(name = "Square", body = emptyList())
        )

        val errors = analyzer.analyze(ast)

        assertEquals(listOf(SemanticError.DuplicateSubroutine("Square")), errors)
    }

    @Test
    fun `reports a GOSUB to an undefined subroutine`() {
        val ast = listOf(
            GoSubNode(name = "Missing")
        )

        val errors = analyzer.analyze(ast)

        assertEquals(listOf(SemanticError.UnresolvedSubroutine("Missing")), errors)
    }

    @Test
    fun `accepts a GOSUB to a declared subroutine`() {
        val ast = listOf(
            SubroutineNode(name = "Square", body = emptyList()),
            GoSubNode(name = "Square")
        )

        assertTrue(analyzer.analyze(ast).isEmpty())
    }

    @Test
    fun `reports a GOTO to an undefined line number`() {
        val ast = listOf(
            LineNumberNode(lineNumber = "10"),
            GotoNode(line = 99)
        )

        val errors = analyzer.analyze(ast)

        assertEquals(listOf(SemanticError.UnresolvedGotoTarget(99)), errors)
    }

    @Test
    fun `accepts a GOTO to a declared line number`() {
        val ast = listOf(
            LineNumberNode(lineNumber = "10"),
            GotoNode(line = 10)
        )

        assertTrue(analyzer.analyze(ast).isEmpty())
    }

    @Test
    fun `reports unknown statements`() {
        val ast = listOf(
            UnknownNode(name = "FOOBAR")
        )

        val errors = analyzer.analyze(ast)

        assertEquals(listOf(SemanticError.UnknownStatement("FOOBAR")), errors)
    }

    @Test
    fun `reports register overflow when more than fifteen variables are used`() {
        val ast = (0 until 16).map { LetNode(variable = "v$it", value = "0") }

        val errors = analyzer.analyze(ast)

        assertTrue(errors.contains(SemanticError.RegisterOverflow(16, SemanticAnalyzer.REGISTER_COUNT)))
    }

    @Test
    fun `does not report overflow at exactly fifteen variables`() {
        val ast = (0 until 15).map { LetNode(variable = "v$it", value = "0") }

        val errors = analyzer.analyze(ast)

        assertTrue(errors.none { it is SemanticError.RegisterOverflow })
    }
}
