package com.valiukh.microbasic.code_generation

import com.valiukh.microbasic.ASTNode
import com.valiukh.microbasic.CodeGenerator
import com.valiukh.microbasic.LetNode
import com.valiukh.microbasic.LiteralNode
import com.valiukh.microbasic.PrintNode
import com.valiukh.microbasic.dsl.parse
import com.valiukh.microbasic.parsers.LLParser
import com.valiukh.microbasic.dsl.tokenize
import com.valiukh.virtualmachine.Command
import com.valiukh.virtualmachine.Mk61
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies the MiniBasic [CodeGenerator] on two levels:
 *
 * 1. Instruction shape – the exact ordered list of MK-61 steps, proving operand order,
 *    stack lifts and address resolution.
 * 2. Execution – the generated program is run on a real [Mk61] and its registers are
 *    asserted, proving the emitted [Command] mnemonics behave as intended.
 */
class CodeGeneratorTest {

    // Mnemonics pulled from the VM so shape assertions never drift from the real command set.
    private val push = Command.PUSH.commandMnemonics
    private val stop = Command.STOP.commandMnemonics
    private val mul = Command.MUL.commandMnemonics
    private val div = Command.DIV.commandMnemonics
    private val pow = Command.POW.commandMnemonics
    private val pi = Command.PI.commandMnemonics
    private val and = Command.AND.commandMnemonics
    private val goto = Command.GOTO.commandMnemonics

    private fun movt(register: String): String = "${Command.MOVT.commandMnemonics} $register"
    private fun movf(register: String): String = "${Command.MOVF.commandMnemonics} $register"

    private fun compile(source: String): List<String> =
        CodeGenerator().generate(source.tokenize().parse(LLParser()))

    private fun compile(ast: List<ASTNode>): List<String> =
        CodeGenerator().generate(ast)

    private fun execute(program: List<String>): Mk61 =
        Mk61().apply {
            uploadProgram(program.joinToString("\n"))
            calculate()
        }

    // region Instruction shape

    @Test
    fun `LET with a numeric literal stores it into a register`() {
        assertEquals(listOf("5", movt("0"), stop), compile("LET x = 5"))
    }

    @Test
    fun `binary addition lifts the stack between operands`() {
        assertEquals(listOf("5", push, "4", "+", movt("0"), stop), compile("LET x = 5 + 4"))
    }

    @Test
    fun `subtraction keeps left operand in Y and right in X`() {
        assertEquals(listOf("10", push, "4", "-", movt("0"), stop), compile("LET x = 10 - 4"))
    }

    @Test
    fun `parenthesised expression compiles inner operation first`() {
        assertEquals(
            listOf("2", push, "3", "+", push, "4", mul, movt("0"), stop),
            compile("LET x = (2 + 3) * 4")
        )
    }

    @Test
    fun `division maps to the divide command`() {
        assertEquals(listOf("20", push, "4", div, movt("0"), stop), compile("LET x = 20 / 4"))
    }

    @Test
    fun `power operator reverses operands so the base lands in X`() {
        assertEquals(listOf("3", push, "2", pow, movt("0"), stop), compile("LET x = 2 ^ 3"))
    }

    @Test
    fun `POW built-in reverses operands like the power operator`() {
        assertEquals(listOf("3", push, "2", pow, movt("0"), stop), compile("LET x = POW(2, 3)"))
    }

    @Test
    fun `unary function is emitted after its argument`() {
        assertEquals(listOf("30", "sin", movt("0"), stop), compile("LET x = SIN(30)"))
    }

    @Test
    fun `unary minus is compiled as zero minus operand`() {
        assertEquals(listOf("0", push, "5", "-", movt("0"), stop), compile("LET x = -5"))
    }

    @Test
    fun `PI constant maps to the pi command`() {
        assertEquals(listOf(pi, movt("0"), stop), compile("LET x = PI"))
    }

    @Test
    fun `PRINT recalls the variable register into X`() {
        assertEquals(listOf("7", movt("0"), movf("0"), stop), compile("LET a = 7\nPRINT a"))
    }

    @Test
    fun `INPUT stores X into the variable register`() {
        assertEquals(listOf(movt("0"), stop), compile("INPUT a"))
    }

    @Test
    fun `END emits a single stop`() {
        assertEquals(listOf(stop), compile("END"))
    }

    @Test
    fun `STOP emits a single stop`() {
        assertEquals(listOf(stop), compile("STOP"))
    }

    @Test
    fun `logical AND maps to the bitwise and command`() {
        assertEquals(listOf("6", push, "3", and, movt("0"), stop), compile("LET x = 6 AND 3"))
    }

    @Test
    fun `GOTO resolves the target line to an absolute address`() {
        val program = compile("10 LET x = 1\n20 GOTO 40\n30 LET x = 999\n40 LET y = 2")

        assertEquals(
            listOf("1", movt("0"), goto, "6", "999", movt("0"), "2", movt("1"), stop),
            program
        )
    }

    @Test
    fun `registers above nine are emitted in lower case`() {
        val ast = (0..9).map { LetNode(variable = "v$it", value = "0") } +
            LetNode(variable = "v10", value = "100") +
            PrintNode(expression = LiteralNode("v10"))

        val program = compile(ast)

        assertTrue(movt("a") in program, "expected a lower-case 'a' store in $program")
        assertTrue(movf("a") in program, "expected a lower-case 'a' recall in $program")
    }

    // endregion

    // region Execution on the MK-61

    @Test
    fun `subtraction executes with correct operand order`() {
        assertEquals(6.0, execute(compile("LET x = 10 - 4")).printDataRegister("0"))
    }

    @Test
    fun `parenthesised expression evaluates on the stack`() {
        assertEquals(20.0, execute(compile("LET x = (2 + 3) * 4")).printDataRegister("0"))
    }

    @Test
    fun `division executes with correct operand order`() {
        assertEquals(5.0, execute(compile("LET x = 20 / 4")).printDataRegister("0"))
    }

    @Test
    fun `power executes as base to the exponent`() {
        assertEquals(8.0, execute(compile("LET x = 2 ^ 3")).printDataRegister("0"))
    }

    @Test
    fun `POW built-in executes as base to the exponent`() {
        assertEquals(8.0, execute(compile("LET x = POW(2, 3)")).printDataRegister("0"))
    }

    @Test
    fun `unary minus negates the operand`() {
        assertEquals(-5.0, execute(compile("LET x = -5")).printDataRegister("0"))
    }

    @Test
    fun `logical AND executes as a bitwise operation`() {
        assertEquals(2.0, execute(compile("LET x = 6 AND 3")).printDataRegister("0"))
    }

    @Test
    fun `PRINT shows the stored value in X`() {
        assertEquals(7.0, execute(compile("LET a = 7\nPRINT a")).printX())
    }

    @Test
    fun `IF with a true condition executes the body`() {
        val source = "LET a = 5\nLET b = 3\nIF a > b THEN\nLET c = 1\nEND"

        assertEquals(1.0, execute(compile(source)).printDataRegister("2"))
    }

    @Test
    fun `IF with a false condition skips the body`() {
        val source = "LET a = 2\nLET b = 3\nIF a > b THEN\nLET c = 1\nEND"

        assertEquals(0.0, execute(compile(source)).printDataRegister("2"))
    }

    @Test
    fun `IF ELSE executes the else branch when the condition is false`() {
        val source = "LET a = 2\nLET b = 3\nIF a > b THEN\nLET c = 1\nELSE\nLET c = 2\nEND"

        assertEquals(2.0, execute(compile(source)).printDataRegister("2"))
    }

    @Test
    fun `ascending FOR loop accumulates its counter`() {
        val source = "LET sum = 0\nFOR i = 1 TO 5\nLET sum = sum + i\nNEXT"

        assertEquals(15.0, execute(compile(source)).printDataRegister("0"))
    }

    @Test
    fun `descending FOR loop with a negative step accumulates its counter`() {
        val source = "LET sum = 0\nFOR i = 3 TO 1 STEP -1\nLET sum = sum + i\nNEXT"

        assertEquals(6.0, execute(compile(source)).printDataRegister("0"))
    }

    @Test
    fun `GOTO jumps over the skipped code`() {
        val source = "10 LET x = 1\n20 GOTO 40\n30 LET x = 999\n40 LET y = 2"
        val calculator = execute(compile(source))

        assertEquals(1.0, calculator.printDataRegister("0"))
        assertEquals(2.0, calculator.printDataRegister("1"))
    }

    @Test
    fun `GOSUB calls a subroutine and returns after it`() {
        val source = "LET x = 5\nGOSUB Double\nPRINT x\nEND\nSUB Double\nLET x = x + x\nRETURN"
        val calculator = execute(compile(source))

        assertEquals(10.0, calculator.printX())
        assertEquals(10.0, calculator.printDataRegister("0"))
    }

    @Test
    fun `a variable in register a is stored and recalled correctly`() {
        val ast = (0..9).map { LetNode(variable = "v$it", value = "0") } +
            LetNode(variable = "v10", value = "100") +
            LetNode(variable = "reset", value = "0") +
            PrintNode(expression = LiteralNode("v10"))

        assertEquals(100.0, execute(compile(ast)).printX())
    }

    // endregion
}