package com.valiukh.microbasic

/**
 * Enum representing the different types of tokens.
 */
enum class TokenType {
    NUMBER,
    IDENTIFIER,
    KEYWORD,
    OPERATOR,
    BRACKET,
    DELIMITER,
    NEWLINE,
    EOF
}

/**
 * Data class representing a token with a type and value.
 *
 * @property type The type of the token.
 * @property value The value of the token.
 */
data class Token(val type: TokenType, val value: String)

/**
 * A lexer for tokenizing MiniBasic source code.
 */
class Lexer {

    private var position = 0

    /**
     * Tokenizes the input string into a list of tokens.
     *
     * @param input The input string to tokenize.
     * @return A list of tokens.
     */
    fun tokenize(input: String): List<Token> {
        position = 0
        val tokens = mutableListOf<Token>()
        while (position < input.length) {
            val char = input[position]
            when {
                char.isNewLine() -> {
                    tokens.add(Token(TokenType.NEWLINE, "\\n"))
                    position++
                }
                char.isWhitespace() -> position++
                char.isDigit() -> tokens.add(readNumber(input))
                char.isIdentifierStart() -> readIdentifierOrKeyword(input)?.let { tokens.add(it) }
                char.isOperator() -> tokens.add(readOperator(input))
                char.isBracket() -> tokens.add(readBracket(input))
                char.isDelimiter() -> tokens.add(readDelimiter(input))
                else -> throw IllegalArgumentException("Unknown character: $char")
            }
        }
        tokens.add(Token(TokenType.EOF, "EOF"))
        return tokens
    }

    /**
     * Reads a number token from the input string.
     *
     * @param input The input string.
     * @return A number token.
     */
    private fun readNumber(input: String): Token {
        val start = position
        while (position < input.length && input[position].isDigit()) position++
        if (position + 1 < input.length && input[position] == '.' && input[position + 1].isDigit()) {
            position++ // Consume the decimal point.
            while (position < input.length && input[position].isDigit()) position++
        }
        return Token(TokenType.NUMBER, input.substring(start, position))
    }

    /**
     * Reads an identifier or keyword token from the input string.
     *
     * @param input The input string.
     * @return An identifier or keyword token.
     */
    private fun readIdentifierOrKeyword(input: String): Token? {
        val start = position
        while (position < input.length && input[position].isIdentifierPart()) position++
        val value = input.substring(start, position)
        return when {
            isKeyword(value) -> {
                val keyword = canonicalKeyword(value)
                if (keyword == Keyword.REM.keyword) {
                    skipComment(input)
                    null
                } else {
                    Token(TokenType.KEYWORD, keyword)
                }
            }
            isWordOperator(value) -> Token(TokenType.OPERATOR, canonicalWordOperator(value))
            else -> Token(TokenType.IDENTIFIER, value)
        }
    }

    /**
     * Skips the remainder of a REM comment up to, but not including, the next newline.
     *
     * @param input The input string.
     */
    private fun skipComment(input: String) {
        while (position < input.length && input[position] != '\n') position++
    }

    /**
     * Reads an operator token from the input string.
     *
     * @param input The input string.
     * @return An operator token.
     */
    private fun readOperator(input: String): Token {
        if (position + 1 < input.length) {
            val twoChar = input.substring(position, position + 2)
            if (Operator.entries.any { it.operator == twoChar }) {
                position += 2
                return Token(TokenType.OPERATOR, twoChar)
            }
        }
        val operator = input[position++].toString()
        return Token(TokenType.OPERATOR, operator)
    }

    /**
     * Reads a bracket token from the input string.
     *
     * @param input The input string.
     * @return A bracket token.
     */
    private fun readBracket(input: String): Token {
        val bracket = input[position++].toString()
        return Token(TokenType.BRACKET, bracket)
    }

    /**
     * Reads a delimiter token from the input string.
     *
     * @param input The input string.
     * @return A delimiter token.
     */
    private fun readDelimiter(input: String): Token {
        val delimiter = input[position++].toString()
        return Token(TokenType.DELIMITER, delimiter)
    }

    /**
     * Checks if the character is an operator.
     *
     * @return True if the character is an operator, false otherwise.
     */
    private fun Char.isOperator() =
        Operator.entries.any { it.operator == this.toString() }

    /**
     * Checks if the character is a newline.
     *
     * @return True if the character is a newline, false otherwise.
     */
    private fun Char.isNewLine() = this == '\n'

    /**
     * Checks if the character is a whitespace.
     *
     * @return True if the character is a whitespace, false otherwise.
     */
    private fun Char.isBracket() = Bracket.entries.any { it.bracket == this.toString() }

    /**
     * Checks if the character is a delimiter.
     *
     * @return True if the character is a delimiter, false otherwise.
     */
    private fun Char.isDelimiter() = Delimiter.entries.any { it.delimiter == this.toString() }

    /**
     * Checks if the given term is a keyword.
     *
     * @param term The term to check.
     * @return True if the term is a keyword, false otherwise.
     */
    private fun isKeyword(term: String) =
        Keyword.entries.any { it.keyword.equals(term, ignoreCase = true) }

    /**
     * Returns the canonical (uppercase) spelling of a keyword.
     *
     * @param term The term to resolve.
     * @return The canonical keyword spelling.
     */
    private fun canonicalKeyword(term: String) =
        Keyword.entries.first { it.keyword.equals(term, ignoreCase = true) }.keyword

    /**
     * Checks if the given term is a word operator (AND, OR, NOT, XOR).
     *
     * @param term The term to check.
     * @return True if the term is a word operator, false otherwise.
     */
    private fun isWordOperator(term: String) =
        Operator.entries.any { it.operator.all(Char::isLetter) && it.operator.equals(term, ignoreCase = true) }

    /**
     * Returns the canonical (uppercase) spelling of a word operator.
     *
     * @param term The term to resolve.
     * @return The canonical operator spelling.
     */
    private fun canonicalWordOperator(term: String) =
        Operator.entries.first { it.operator.equals(term, ignoreCase = true) }.operator

    /**
     * Checks if the character can start an identifier.
     *
     * @return True if the character is a letter or underscore, false otherwise.
     */
    private fun Char.isIdentifierStart() = isLetter() || this == '_'

    /**
     * Checks if the character can appear inside an identifier.
     *
     * @return True if the character is a letter, digit, or underscore, false otherwise.
     */
    private fun Char.isIdentifierPart() = isLetterOrDigit() || this == '_'

}