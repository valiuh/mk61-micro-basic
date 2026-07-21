package com.valiukh.microbasic.pre_code_generation

import com.valiukh.microbasic.IdentifierNode
import com.valiukh.microbasic.LetNode
import com.valiukh.microbasic.LiteralNode
import com.valiukh.microbasic.OperatorBinaryNode
import com.valiukh.microbasic.PrintNode
import com.valiukh.microbasic.RegisterAllocationException
import com.valiukh.microbasic.RegisterAllocator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RegisterAllocatorTest {

    private val allocator = RegisterAllocator()

    @Test
    fun `assigns registers in declaration order`() {
        val ast = listOf(
            LetNode(variable = "a", value = "5"),
            LetNode(variable = "counter", value = "10"),
            LetNode(variable = "result", value = "0")
        )

        val registers = allocator.allocate(ast)

        assertEquals(mapOf("a" to '0', "counter" to '1', "result" to '2'), registers)
    }

    @Test
    fun `assigns a register only once per distinct variable`() {
        val ast = listOf(
            LetNode(variable = "a", value = "5"),
            IdentifierNode(variable = "a", value = "6"),
            LetNode(variable = "b", value = "1")
        )

        val registers = allocator.allocate(ast)

        assertEquals(mapOf("a" to '0', "b" to '1'), registers)
    }

    @Test
    fun `includes variables referenced inside expressions`() {
        // LET result = a + b
        val ast = listOf(
            LetNode(variable = "a", value = "1"),
            LetNode(variable = "b", value = "2"),
            LetNode(
                variable = "result",
                expression = listOf(
                    OperatorBinaryNode(operator = "+", leftValue = "a", rightValue = "b")
                )
            )
        )

        val registers = allocator.allocate(ast)

        assertEquals(mapOf("a" to '0', "b" to '1', "result" to '2'), registers)
    }

    @Test
    fun `follows first-appearance order even when a variable first appears in a use`() {
        // LET a = b  ->  'a' is declared first, 'b' is referenced first
        val ast = listOf(
            LetNode(variable = "a", value = "b"),
            LetNode(variable = "b", value = "3")
        )

        val registers = allocator.allocate(ast)

        assertEquals(mapOf("a" to '0', "b" to '1'), registers)
    }

    @Test
    fun `numeric literals do not consume registers`() {
        val ast = listOf(
            LetNode(variable = "a", value = "42"),
            PrintNode(expression = LiteralNode("100"))
        )

        val registers = allocator.allocate(ast)

        assertEquals(mapOf("a" to '0'), registers)
    }

    @Test
    fun `allocates exactly all fifteen registers`() {
        val ast = (0 until 15).map { LetNode(variable = "v$it", value = "0") }

        val registers = allocator.allocate(ast)

        assertEquals(15, registers.size)
        assertTrue(registers.values.toSet() == RegisterAllocator.REGISTERS.toSet())
    }

    @Test
    fun `throws when more than fifteen variables are required`() {
        val ast = (0 until 16).map { LetNode(variable = "v$it", value = "0") }

        assertFailsWith<RegisterAllocationException> {
            allocator.allocate(ast)
        }
    }
}
