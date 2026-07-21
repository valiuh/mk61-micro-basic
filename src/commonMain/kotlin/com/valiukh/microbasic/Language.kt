package com.valiukh.microbasic


/**
 * Enum representing the different keywords in MiniBasic.
 */
enum class Keyword(val keyword: String) {
    REM("REM"),
    LET("LET"),
    PRINT("PRINT"),
    INPUT("INPUT"),
    IF("IF"),
    THEN("THEN"),
    ELSE("ELSE"),
    END("END"),
    FOR("FOR"),
    TO("TO"),
    STEP("STEP"),
    NEXT("NEXT"),
    GOTO("GOTO"),
    GOSUB("GOSUB"),
    SUB("SUB"),
    RETURN("RETURN"),
    STOP("STOP")
}

/**
 * Enum representing the different operators in MiniBasic.
 */
enum class Operator(val operator: String) {
    /**
     * Binary operator: add two numbers.
     */
    PLUS("+"),

    /**
     * Binary operator: subtract two numbers.
     */
    MINUS("-"),

    /**
     * Binary operator: multiply two numbers.
     */
    MULTIPLY("*"),

    /**
     * Binary operator: divide two numbers.
     */
    DIVIDE("/"),

    /**
     * Binary operator: raise a number to a power.
     */
    POWER("^"),

    /**
     * Relational operator: equal.
     */
    EQUAL("="),

    /**
     * Relational operator: not equal.
     */
    NOT_EQUAL("<>"),

    /**
     * Relational operator: greater than.
     */
    GREATER_THAN(">"),

    /**
     * Relational operator: less than.
     */
    LESS_THAN("<"),

    /**
     * Relational operator: greater than or equal.
     */
    GREATER_THAN_OR_EQUAL(">="),

    /**
     * Relational operator: less than or equal.
     */
    LESS_THAN_OR_EQUAL("<="),

    /**
     * Logical operator: logical AND.
     */
    AND("AND"),

    /**
     * Logical operator: logical OR.
     */
    OR("OR"),

    /**
     * Logical operator: logical NOT.
     */
    NOT("NOT"),

    /**
     * Logical operator: exclusive OR.
     */
    XOR("XOR")
}

/**
 * Enum representing the built-in mathematical functions in MiniBasic.
 */
enum class BuiltInFunction(val function: String) {
    /**
     * Exponential function: calculates 10^x.
     */
    EXP10("EXP10"),

    /**
     * Exponential function: calculates e^x.
     */
    EXP("EXP"),

    /**
     * Logarithmic function: base-10 logarithm.
     */
    LOG("LOG"),

    /**
     * Logarithmic function: natural logarithm.
     */
    LN("LN"),

    /**
     * Binary function: raises x to the power y.
     */
    POW("POW"),

    /**
     * Trigonometric function: sine of an angle.
     */
    SIN("SIN"),

    /**
     * Trigonometric function: cosine of an angle.
     */
    COS("COS"),

    /**
     * Trigonometric function: tangent of an angle.
     */
    TAN("TAN"),

    /**
     * Trigonometric function: arc sine of a number.
     */
    ASIN("ASIN"),

    /**
     * Trigonometric function: arc cosine of a number.
     */
    ACOS("ACOS"),

    /**
     * Trigonometric function: arc tangent of a number.
     */
    ATN("ATN"),

    /**
     * Numeric function: absolute value.
     */
    ABS("ABS"),

    /**
     * Numeric function: square root.
     */
    SQRT("SQRT"),

    /**
     * Numeric function: square (x²).
     */
    SQR("SQR"),

    /**
     * Numeric function: reciprocal (1/x).
     */
    RECIP("RECIP"),

    /**
     * Numeric function: integer part of a number.
     */
    FLOOR("FLOOR"),

    /**
     * Numeric function: fractional part of a number.
     */
    FRAC("FRAC"),

    /**
     * Numeric function: returns -1, 0, or 1 depending on the sign of x.
     */
    SIGN("SIGN"),

    /**
     * Binary function: returns the larger of two values.
     */
    MAX("MAX"),

    /**
     * Random function: generates a pseudo-random number in the range [0, 1).
     */
    RANDOM("RANDOM"),

    /**
     * Angle conversion: degrees, minutes and fractions of minutes into decimal representation.
     */
    HM_TO_DEG("HM_TO_DEG"),

    /**
     * Angle conversion: decimal representation into degrees, minutes and fractions of minutes.
     */
    DEG_TO_HM("DEG_TO_HM"),

    /**
     * Angle conversion: degrees, minutes, seconds into decimal representation.
     */
    HMS_TO_DEG("HMS_TO_DEG"),

    /**
     * Angle conversion: decimal representation into degrees, minutes and seconds.
     */
    DEG_TO_HMS("DEG_TO_HMS")
}

/**
 * Enum representing the built-in mathematical constants in MiniBasic.
 */
enum class Constant(val constant: String) {
    /**
     * Mathematical constant π.
     */
    PI("PI"),

    /**
     * Euler's number.
     */
    E("E")
}

enum class Bracket(val bracket: String) {
    LEFT("("),
    RIGHT(")")
}

/**
 * Enum representing the delimiters in MiniBasic.
 */
enum class Delimiter(val delimiter: String) {
    /**
     * Separates arguments in multi-argument function calls.
     */
    COMMA(",")
}