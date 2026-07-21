package com.valiukh.microbasic.lexer

import com.valiukh.microbasic.Lexer
import com.valiukh.microbasic.Token
import com.valiukh.microbasic.TokenType
import kotlin.test.Test

private object Assert {
    fun <T> assertEquals(expected: T, actual: T) = kotlin.test.assertEquals(expected, actual)
}

class LexerTest {

    @Test
    fun `Scenario - LET`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("10 LET x = 5")
        Assert.assertEquals(6, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "10"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[3])
        Assert.assertEquals(Token(TokenType.NUMBER, "5"), tokens[4])
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[5])
    }

    @Test
    fun `Scenario - PRINT`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("20 PRINT x")
        Assert.assertEquals(4, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "20"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "PRINT"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[3])
    }

    @Test
    fun `Scenario - INPUT`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("30 INPUT x")
        Assert.assertEquals(4, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "30"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "INPUT"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[3])
    }

    @Test
    fun `Scenario - GOTO`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("40 GOTO 50")
        Assert.assertEquals(4, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "40"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "GOTO"), tokens[1])
        Assert.assertEquals(Token(TokenType.NUMBER, "50"), tokens[2])
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[3])
    }

    @Test
    fun `Scenario - GOSUB`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("40 GOSUB function")
        Assert.assertEquals(4, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "40"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "GOSUB"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "function"), tokens[2])
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[3])
    }

    @Test
    fun `Scenario - SIN`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("30 LET x = SIN(5)")
        Assert.assertEquals(9, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "30"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[3])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "SIN"), tokens[4])
        Assert.assertEquals(Token(TokenType.BRACKET, "("), tokens[5])
        Assert.assertEquals(Token(TokenType.NUMBER, "5"), tokens[6])
        Assert.assertEquals(Token(TokenType.BRACKET, ")"), tokens[7])
    }

    @Test
    fun `Scenario - +`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("30 LET x = 5 + 10")
        Assert.assertEquals(8, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "30"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[3])
        Assert.assertEquals(Token(TokenType.NUMBER, "5"), tokens[4])
        Assert.assertEquals(Token(TokenType.OPERATOR, "+"), tokens[5])
        Assert.assertEquals(Token(TokenType.NUMBER, "10"), tokens[6])
    }

    @Test
    fun `Scenario - IF`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("40 IF x > 5 THEN GOTO 50 END")
        Assert.assertEquals(10, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "40"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "IF"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, ">"), tokens[3])
        Assert.assertEquals(Token(TokenType.NUMBER, "5"), tokens[4])
        Assert.assertEquals(Token(TokenType.KEYWORD, "THEN"), tokens[5])
        Assert.assertEquals(Token(TokenType.KEYWORD, "GOTO"), tokens[6])
        Assert.assertEquals(Token(TokenType.NUMBER, "50"), tokens[7])
        Assert.assertEquals(Token(TokenType.KEYWORD, "END"), tokens[8])
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[9])
    }

    @Test
    fun `Scenario - IF ELSE`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("40 IF x > 5 THEN GOTO 50 ELSE GOTO 60 END")
        Assert.assertEquals(13, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "40"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "IF"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, ">"), tokens[3])
        Assert.assertEquals(Token(TokenType.NUMBER, "5"), tokens[4])
        Assert.assertEquals(Token(TokenType.KEYWORD, "THEN"), tokens[5])
        Assert.assertEquals(Token(TokenType.KEYWORD, "GOTO"), tokens[6])
        Assert.assertEquals(Token(TokenType.NUMBER, "50"), tokens[7])
        Assert.assertEquals(Token(TokenType.KEYWORD, "ELSE"), tokens[8])
        Assert.assertEquals(Token(TokenType.KEYWORD, "GOTO"), tokens[9])
        Assert.assertEquals(Token(TokenType.NUMBER, "60"), tokens[10])
        Assert.assertEquals(Token(TokenType.KEYWORD, "END"), tokens[11])
    }

    @Test
    fun `Scenario - IF SIN ELSE ATAN`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("40 IF x > 0 THEN SIN(x) ELSE ATAN(x) END")
        Assert.assertEquals(17, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "40"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "IF"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, ">"), tokens[3])
        Assert.assertEquals(Token(TokenType.NUMBER, "0"), tokens[4])
        Assert.assertEquals(Token(TokenType.KEYWORD, "THEN"), tokens[5])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "SIN"), tokens[6])
        Assert.assertEquals(Token(TokenType.BRACKET, "("), tokens[7])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[8])
        Assert.assertEquals(Token(TokenType.BRACKET, ")"), tokens[9])
        Assert.assertEquals(Token(TokenType.KEYWORD, "ELSE"), tokens[10])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "ATAN"), tokens[11])
        Assert.assertEquals(Token(TokenType.BRACKET, "("), tokens[12])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[13])
        Assert.assertEquals(Token(TokenType.BRACKET, ")"), tokens[14])
        Assert.assertEquals(Token(TokenType.KEYWORD, "END"), tokens[15])
    }

    @Test
    fun `Scenario - FOR`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("50 FOR x = 1 TO 10 STEP 2 NEXT")
        Assert.assertEquals(11, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "50"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "FOR"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[3])
        Assert.assertEquals(Token(TokenType.NUMBER, "1"), tokens[4])
        Assert.assertEquals(Token(TokenType.KEYWORD, "TO"), tokens[5])
        Assert.assertEquals(Token(TokenType.NUMBER, "10"), tokens[6])
        Assert.assertEquals(Token(TokenType.KEYWORD, "STEP"), tokens[7])
        Assert.assertEquals(Token(TokenType.NUMBER, "2"), tokens[8])
        Assert.assertEquals(Token(TokenType.KEYWORD, "NEXT"), tokens[9])
    }

    @Test
    fun `Scenario - script`() {
        val miniBasicCode = """
            10 LET first = 10
            20 LET second = 30
            30 LET res = SIN(second)
            40 LET result = first + second
            50 IF result > 0 THEN GOTO 70 END
            60 LET test = 50
            70 STOP
        """.trimIndent()
        val lexer = Lexer()
        val tokens = lexer.tokenize(miniBasicCode)
        Assert.assertEquals(48, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "10"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "first"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[3])
        Assert.assertEquals(Token(TokenType.NUMBER, "10"), tokens[4])
        Assert.assertEquals(Token(TokenType.NEWLINE, "\\n"), tokens[5])
        Assert.assertEquals(Token(TokenType.NUMBER, "20"), tokens[6])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[7])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "second"), tokens[8])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[9])
        Assert.assertEquals(Token(TokenType.NUMBER, "30"), tokens[10])
        Assert.assertEquals(Token(TokenType.NEWLINE, "\\n"), tokens[11])
        Assert.assertEquals(Token(TokenType.NUMBER, "30"), tokens[12])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[13])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "res"), tokens[14])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[15])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "SIN"), tokens[16])
        Assert.assertEquals(Token(TokenType.BRACKET, "("), tokens[17])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "second"), tokens[18])
        Assert.assertEquals(Token(TokenType.BRACKET, ")"), tokens[19])
        Assert.assertEquals(Token(TokenType.NEWLINE, "\\n"), tokens[20])
        Assert.assertEquals(Token(TokenType.NUMBER, "40"), tokens[21])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[22])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "result"), tokens[23])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[24])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "first"), tokens[25])
        Assert.assertEquals(Token(TokenType.OPERATOR, "+"), tokens[26])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "second"), tokens[27])
        Assert.assertEquals(Token(TokenType.NEWLINE, "\\n"), tokens[28])
        Assert.assertEquals(Token(TokenType.NUMBER, "50"), tokens[29])
        Assert.assertEquals(Token(TokenType.KEYWORD, "IF"), tokens[30])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "result"), tokens[31])
        Assert.assertEquals(Token(TokenType.OPERATOR, ">"), tokens[32])
        Assert.assertEquals(Token(TokenType.NUMBER, "0"), tokens[33])
        Assert.assertEquals(Token(TokenType.KEYWORD, "THEN"), tokens[34])
        Assert.assertEquals(Token(TokenType.KEYWORD, "GOTO"), tokens[35])
        Assert.assertEquals(Token(TokenType.NUMBER, "70"), tokens[36])
        Assert.assertEquals(Token(TokenType.KEYWORD, "END"), tokens[37])
        Assert.assertEquals(Token(TokenType.NEWLINE, "\\n"), tokens[38])
        Assert.assertEquals(Token(TokenType.NUMBER, "60"), tokens[39])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[40])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "test"), tokens[41])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[42])
        Assert.assertEquals(Token(TokenType.NUMBER, "50"), tokens[43])
        Assert.assertEquals(Token(TokenType.NEWLINE, "\\n"), tokens[44])
        Assert.assertEquals(Token(TokenType.NUMBER, "70"), tokens[45])
        Assert.assertEquals(Token(TokenType.KEYWORD, "STOP"), tokens[46])
    }

    @Test
    fun `Scenario - decimal number`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("10 LET x = 3.75")
        Assert.assertEquals(6, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "10"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[3])
        Assert.assertEquals(Token(TokenType.NUMBER, "3.75"), tokens[4])
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[5])
    }

    @Test
    fun `Scenario - underscore identifier`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("10 LET total_sum = 5")
        Assert.assertEquals(6, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "10"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "total_sum"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[3])
        Assert.assertEquals(Token(TokenType.NUMBER, "5"), tokens[4])
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[5])
    }

    @Test
    fun `Scenario - not equal operator`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("10 IF a <> b THEN END")
        Assert.assertEquals(8, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "10"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "IF"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "a"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, "<>"), tokens[3])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "b"), tokens[4])
        Assert.assertEquals(Token(TokenType.KEYWORD, "THEN"), tokens[5])
        Assert.assertEquals(Token(TokenType.KEYWORD, "END"), tokens[6])
    }

    @Test
    fun `Scenario - less than or equal operator`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("10 IF a <= b THEN END")
        Assert.assertEquals(8, tokens.size)
        Assert.assertEquals(Token(TokenType.OPERATOR, "<="), tokens[3])
    }

    @Test
    fun `Scenario - greater than or equal operator`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("10 IF a >= b THEN END")
        Assert.assertEquals(8, tokens.size)
        Assert.assertEquals(Token(TokenType.OPERATOR, ">="), tokens[3])
    }

    @Test
    fun `Scenario - word operators`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("10 IF a AND b OR c XOR NOT d THEN END")
        Assert.assertEquals(13, tokens.size)
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "a"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, "AND"), tokens[3])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "b"), tokens[4])
        Assert.assertEquals(Token(TokenType.OPERATOR, "OR"), tokens[5])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "c"), tokens[6])
        Assert.assertEquals(Token(TokenType.OPERATOR, "XOR"), tokens[7])
        Assert.assertEquals(Token(TokenType.OPERATOR, "NOT"), tokens[8])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "d"), tokens[9])
        Assert.assertEquals(Token(TokenType.KEYWORD, "THEN"), tokens[10])
        Assert.assertEquals(Token(TokenType.KEYWORD, "END"), tokens[11])
    }

    @Test
    fun `Scenario - case insensitive keywords`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("10 let x = 5")
        Assert.assertEquals(6, tokens.size)
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
    }

    @Test
    fun `Scenario - case insensitive word operator`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("10 IF a and b THEN END")
        Assert.assertEquals(8, tokens.size)
        Assert.assertEquals(Token(TokenType.OPERATOR, "AND"), tokens[3])
    }

    @Test
    fun `Scenario - REM comment is skipped`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("REM ax2 + bx + c = 0")
        Assert.assertEquals(1, tokens.size)
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[0])
    }

    @Test
    fun `Scenario - REM comment then statement on next line`() {
        val miniBasicCode = """
            REM this is a comment
            10 LET x = 5
        """.trimIndent()
        val lexer = Lexer()
        val tokens = lexer.tokenize(miniBasicCode)
        Assert.assertEquals(7, tokens.size)
        Assert.assertEquals(Token(TokenType.NEWLINE, "\\n"), tokens[0])
        Assert.assertEquals(Token(TokenType.NUMBER, "10"), tokens[1])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[2])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[3])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[4])
        Assert.assertEquals(Token(TokenType.NUMBER, "5"), tokens[5])
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[6])
    }

    @Test
    fun `Scenario - two-argument function with comma`() {
        val lexer = Lexer()
        val tokens = lexer.tokenize("10 LET x = POW(2, 8)")
        Assert.assertEquals(11, tokens.size)
        Assert.assertEquals(Token(TokenType.NUMBER, "10"), tokens[0])
        Assert.assertEquals(Token(TokenType.KEYWORD, "LET"), tokens[1])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "x"), tokens[2])
        Assert.assertEquals(Token(TokenType.OPERATOR, "="), tokens[3])
        Assert.assertEquals(Token(TokenType.IDENTIFIER, "POW"), tokens[4])
        Assert.assertEquals(Token(TokenType.BRACKET, "("), tokens[5])
        Assert.assertEquals(Token(TokenType.NUMBER, "2"), tokens[6])
        Assert.assertEquals(Token(TokenType.DELIMITER, ","), tokens[7])
        Assert.assertEquals(Token(TokenType.NUMBER, "8"), tokens[8])
        Assert.assertEquals(Token(TokenType.BRACKET, ")"), tokens[9])
        Assert.assertEquals(Token(TokenType.EOF, "EOF"), tokens[10])
    }

}
