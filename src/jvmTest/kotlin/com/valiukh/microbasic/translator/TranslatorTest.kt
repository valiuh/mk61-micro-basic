package com.valiukh.microbasic.translator

import com.valiukh.microbasic.SemanticError
import com.valiukh.microbasic.dsl.allocateMemory
import com.valiukh.microbasic.dsl.checkSemantics
import com.valiukh.microbasic.dsl.generateOrElse
import com.valiukh.microbasic.dsl.parseWithParser
import com.valiukh.microbasic.parsers.LLParser
import com.valiukh.microbasic.dsl.tokenize
import com.valiukh.virtualmachine.Command
import com.valiukh.virtualmachine.Mk61
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * End-to-end translator test: reads a MiniBasic script from the shared
 * `test_scripts` resources, drives it through the whole fluent pipeline
 * (`tokenize -> parseWithParser -> checkSemantics -> allocateMemory -> generateOrElse`)
 * and verifies both the generated MK-61 program and its behavior when executed
 * on a real [Mk61].
 */
class TranslatorTest {

    /** Runs the complete pipeline, failing the test if any stage reports errors. */
    private fun translate(scriptName: String): List<String> =
        loadTestScript(scriptName = scriptName)
            .tokenize()
            .parseWithParser(parser = LLParser())
            .checkSemantics()
            .allocateMemory()
            .generateOrElse { errors -> fail(errors.formatMessages()) }

    private fun List<SemanticError>.formatMessages(): String =
        "Translation failed: ${joinToString { it.message }}"

    private fun execute(program: List<String>): Mk61 =
        Mk61().apply {
            uploadProgram(program.joinToString("\n"))
            calculate()
        }

    @Test
    fun `factorial script translates to the expected MK-61 program`() {
        val program = translate("factorial.mb61")

        // Variables are allocated in order of first appearance: n -> R0, result -> R1, i -> R2.
        val expected = listOf(
            "${Command.MOVT.commandMnemonics} 0",       // INPUT n
            "1",
            "${Command.MOVT.commandMnemonics} 1",       // LET result = 1
            "1",
            "${Command.MOVT.commandMnemonics} 2",       // FOR i = 1 ...
                                                        // loop guard: (n - i) >= 0 ? fall through : jump to PRINT
            "${Command.MOVF.commandMnemonics} 0",
            Command.PUSH.commandMnemonics,
            "${Command.MOVF.commandMnemonics} 2",
            Command.SUB.commandMnemonics,
            Command.NNG.commandMnemonics,
            "23",
                                                        // body: result = result * i
            "${Command.MOVF.commandMnemonics} 1",
            Command.PUSH.commandMnemonics,
            "${Command.MOVF.commandMnemonics} 2",
            Command.MUL.commandMnemonics,
            "${Command.MOVT.commandMnemonics} 1",
                                                        // step: i = i + 1
            "${Command.MOVF.commandMnemonics} 2",
            Command.PUSH.commandMnemonics,
            "1",
            Command.ADD.commandMnemonics,
            "${Command.MOVT.commandMnemonics} 2",
                                                        // NEXT -> back to loop guard
            Command.GOTO.commandMnemonics,
            "5",
            "${Command.MOVF.commandMnemonics} 1",       // PRINT result
            Command.STOP.commandMnemonics               // END
        )

        assertEquals(expected, program)
    }

    @Test
    fun `factorial program computes 0! on the MK-61`() {
        // No value is entered before INPUT, so n = 0 and the loop body never runs: 0! = 1.
        val vm = execute(translate("factorial.mb61"))

        assertEquals(1.0, vm.printX())
        assertEquals(1.0, vm.printX1())
        assertEquals(listOf(1.0, 0.0, 0.0, 0.0), vm.printRegisters())

        assertEquals(0.0, vm.printDataRegister("0")) // n
        assertEquals(1.0, vm.printDataRegister("1")) // result
        assertEquals(1.0, vm.printDataRegister("2")) // i
    }
}