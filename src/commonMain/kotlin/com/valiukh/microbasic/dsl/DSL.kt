package com.valiukh.microbasic.dsl

import com.valiukh.microbasic.ASTNode
import com.valiukh.microbasic.CodeGenerator
import com.valiukh.microbasic.Lexer
import com.valiukh.microbasic.Parser
import com.valiukh.microbasic.RegisterAllocationException
import com.valiukh.microbasic.RegisterAllocator
import com.valiukh.microbasic.SemanticAnalyzer
import com.valiukh.microbasic.SemanticError
import com.valiukh.microbasic.Token

fun String.tokenize(): List<Token> = Lexer().tokenize(this)

fun List<ASTNode>.generate(): List<String> = CodeGenerator().generate(this)

/**
 * A MiniBasic program flowing through the compilation pipeline.
 *
 * It carries the parsed [ast] together with any [errors] accumulated by the
 * semantic and memory-allocation stages and the resolved [registers], so the
 * stages can be chained fluently:
 *
 * ```
 * val mk61Code = miniBasicCode
 *     .tokenize()
 *     .parseWithParser(LLParser())
 *     .checkSemantics()   // optional
 *     .allocateMemory()
 *     .generateOrElse { errors -> /* report */ }
 * ```
 *
 * @property ast The parsed program.
 * @property errors Problems detected so far; a non-empty list stops code generation.
 * @property registers The `variable -> register` allocation, empty until [allocateMemory] runs.
 */
class MiniBasicProgram internal constructor(
    val ast: List<ASTNode>,
    val errors: List<SemanticError> = emptyList(),
    val registers: Map<String, Char> = emptyMap(),
)

/** Parses tokens with the given [parser] into an AST. */
fun List<Token>.parse(parser: Parser): List<ASTNode> {
    parser.updateTokens(this)
    return parser.parse()
}

/** Parses tokens with the given [parser] and starts a fluent compilation pipeline. */
fun List<Token>.parseWithParser(parser: Parser): MiniBasicProgram =
    MiniBasicProgram(parse(parser))

/**
 * Optional pipeline step that runs [com.valiukh.microbasic.SemanticAnalyzer] and records any problems
 * it finds. Once errors are present the step is a no-op, so it is safe to chain.
 */
fun MiniBasicProgram.checkSemantics(): MiniBasicProgram {
    if (errors.isNotEmpty()) return this
    val found = SemanticAnalyzer().analyze(ast)
    return MiniBasicProgram(ast, errors + found, registers)
}

/**
 * Allocates MK-61 registers to the program's variables using [RegisterAllocator].
 *
 * A [com.valiukh.microbasic.RegisterAllocationException] is captured as a [SemanticError] instead of being
 * thrown, so the failure surfaces through [generateOrElse].
 */
fun MiniBasicProgram.allocateMemory(): MiniBasicProgram {
    if (errors.isNotEmpty()) return this
    return try {
        MiniBasicProgram(ast, errors, RegisterAllocator().allocate(ast))
    } catch (_: RegisterAllocationException) {
        val found = SemanticAnalyzer().analyze(ast)
        MiniBasicProgram(ast, errors + found, registers)
    }
}

/**
 * Generates the MK-61 program, or invokes [onError] with the accumulated errors
 * and returns an empty list when the semantic or memory-allocation stages failed.
 *
 * @param onError Callback invoked with the collected [SemanticError]s on failure.
 */
fun MiniBasicProgram.generateOrElse(
    onError: (errors: List<SemanticError>) -> Unit = {}
): List<String> {
    if (errors.isNotEmpty()) {
        onError(errors)
        return emptyList()
    }
    return CodeGenerator(registers).generate(ast)
}