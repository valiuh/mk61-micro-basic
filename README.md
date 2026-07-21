# Elektronika MK-61 Micro Basic

Micro Basic is a compact, educational programming language created specifically for the virtual machine of the **Elektronika MK-61** programmable RPN calculator.

Unlike classic BASIC implementations that target general-purpose computers, Micro Basic is designed to be translated into executable instructions for the MK-61 virtual machine. Every language construct ultimately corresponds to one or more calculator instructions.

The language intentionally provides a modern and readable BASIC syntax while preserving the hardware limitations of the original calculator. As a result, some language features differ from traditional BASIC implementations in order to match the capabilities of the MK-61 architecture.

So Micro Basic is not intended to be a general-purpose programming language.

Its primary objective is to make development for the Elektronika MK-61 significantly easier without hiding the architectural nature of the original machine. From this point of view, rather than abstracting away the hardware, Micro Basic embraces it.

Understanding the calculator's registers, stack, and execution model allows programmers to write faster and more efficient programs while benefiting from a modern, readable syntax.

Micro Basic is therefore **not an emulator of Microsoft BASIC, GW-BASIC or Sinclair BASIC**, but rather a high-level language that compiles into the instruction set of the MK-61 virtual machine.

Happy coding with Micro Basic!

---

## Artifacts status

### Validation
[![Codemagic microbasic-tests status](https://api.codemagic.io/apps/YOUR_CODEMAGIC_APP_ID/microbasic-tests/status_badge.svg)](https://codemagic.io/app/YOUR_CODEMAGIC_APP_ID/microbasic-tests/latest_build)

### Deployment
[![Codemagic microbasic-publish status](https://api.codemagic.io/apps/YOUR_CODEMAGIC_APP_ID/microbasic-publish/status_badge.svg)](https://codemagic.io/app/YOUR_CODEMAGIC_APP_ID/microbasic-publish/latest_build)

---

## Integration

```toml
[versions]
mk61-micro-basic = "0.1.1"

[libraries]
mk61-micro-basic = { module = "io.github.valiuh.mk61:micro-basic", version.ref = "mk61-micro-basic" }
```

Then consume it in build.gradle.kts:

```kotlin
dependencies {
    implementation(libs.mk61.micro.basic)
}
```

Gradle Groovy DSL:

```groovy
dependencies {
    implementation "io.github.valiuh.mk61:micro-basic:0.1.1"
}
```

Maven:

```xml
<dependency>
    <groupId>io.github.valiuh.mk61</groupId>
    <artifactId>micro-basic</artifactId>
    <version>0.1.1</version>
</dependency>
```

# Micro Basic Kotlin DSL Specification

The Kotlin DSL provides a fluent translation pipeline from Micro Basic source code to MK-61 virtual machine instructions (`List<String>`).  
Each step transforms the program to the next stage of compilation:

```kotlin
private fun translate(scriptName: String): List<String> =
    loadTestScript(scriptName = scriptName)
        .tokenize()
        .parseWithParser(parser = LLParser())
        .checkSemantics() // optional
        .allocateMemory()
        .generateOrElse { errors -> fail(errors.formatMessages()) }

private fun execute(program: List<String>): Mk61 =
    Mk61().apply {
        uploadProgram(program.joinToString("\n"))
        calculate()
    }

val program = translate("factorial.mb61")
val vmState = execute(program)
```

`LLParser` is an implementation of an LL parser and implements the `Parser` interface.  
Because `parseWithParser()` accepts `Parser`, you can provide any parser implementation compatible with that interface.

`.checkSemantics()` is optional. If you skip semantic verification, you can go directly to memory allocation and generation.  
When semantic (or allocation-related semantic) errors are detected, `generateOrElse { errors -> ... }` calls your error handler, for example:

```kotlin
.generateOrElse { errors -> fail(errors.formatMessages()) }
```

If you do not need this error callback style, you can also generate directly from an AST without `generateOrElse`:

```kotlin
val instructions = source
    .tokenize()
    .parse(parser = LLParser())
    .generate()
```

The output of these flows is an MK-61 instruction list that can be uploaded and executed on the virtual machine.

# Micro Basic Language Specification

Micro Basic intentionally differs from traditional BASIC dialects.

| Feature             | Micro Basic |
| ------------------- | --------- |
| Numeric variables   | ✔         |
| Strings             | ✘         |
| Arrays              | ✘         |
| Dynamic memory      | ✘         |
| Heap                | ✘         |
| Local variables     | ✘         |
| Call stack          | ✘         |
| Recursion           | ✘         |
| Register allocation | ✔         |
| RPN code generation | ✔         |
| MK-61 compatible    | ✔         |

### Relationship to the MK-61

Micro Basic should be viewed as a higher-level abstraction over the MK-61 instruction set.

Every language construct eventually becomes one or more calculator instructions.

Examples

| Micro Basic   | Conceptual MK-61 Operation                       |
| ------------- | ------------------------------------------------ |
| `LET a = x`   | Evaluate expression → Store in register          |
| `INPUT a`     | User input → X → Memory Register                 |
| `PRINT a`     | Memory Register → X                              |
| `GOTO`        | Unconditional jump                               |
| `GOSUB`       | Call subroutine                                  |
| `RETURN`      | Return from subroutine                           |
| `IF`          | Conditional branch                               |
| `FOR`         | Counter initialization and conditional branching |

The language therefore remains very close to the hardware while providing a significantly more readable programming model.

### Language Grammar

MiniBasic intentionally keeps its grammar compact.

A simplified grammar is shown below.

```
program

    statement*

statement

    REM
    LET
    INPUT
    PRINT
    IF
    GOTO
    GOSUB
    RETURN
    SUB
    FOR
    NEXT
    STOP
    END

expression

    literal
    variable
    unary-function
    binary-expression
    parenthesized-expression
```

The actual parser may implement additional internal productions required for code generation.


## Program Structure

A Micro Basic program consists of a sequence of statements executed from top to bottom.

Example:

```basic
REM Simple example

LET value = 10

PRINT value

END
```

Statements are generally written one per line.

Keywords are **case-insensitive**.

Line numbers are optional and may be used when explicit addressing is desired.

Comments begin with the `REM` keyword.

---

## Variables

Variables are introduced using the `LET` statement.

```basic
LET counter = 0
LET total = 15
LET result = counter + total
```

### Variable Naming

Variable names may contain:

- uppercase letters
- lowercase letters
- underscores (`_`)
- digits (except as the first character)

Both of the following naming conventions are valid:

```basic
LET counter = 0
LET Counter = 0
LET total_sum = 0
LET currentValue = 10
LET current_value = 10
```

The language is intended to support common naming styles, including:

- camelCase
- PascalCase
- snake_case

---

## Variable Memory Model

Unlike traditional BASIC implementations, Micro Basic does **not** have unlimited variables.

This limitation comes directly from the architecture of the MK-61 calculator.

The calculator contains a fixed number of programmable memory registers.

These registers are used for storing every variable in the program.

Typical register set:

```
0
1
2
3
4
5
6
7
8
9
A
B
C
D
E
```

Because the underlying hardware provides only a fixed amount of register memory, the number of simultaneously existing variables is also limited.

Every variable used anywhere in the program consumes one calculator register.

This includes:

- ordinary variables
- temporary variables
- loop variables
- variables declared inside subroutines

Once all available registers have been assigned, no additional variables can be allocated.

---

## Variable Allocation

Variables are **not assigned to fixed registers by name**.

Instead, the interpreter allocates registers dynamically as variables first appear in the source code.

For example:

```basic
LET a = 5
LET counter = 10
LET result = 0
```

may internally become

| Variable | Calculator Register |
|-----------|---------------------|
| a         | 0 |
| counter   | 1 |
| result    | 2 |

Another program

```basic
LET temperature = 20
LET average = 5
```

may become

| Variable | Calculator Register |
|-----------|---------------------|
| temperature | 0 |
| average     | 1 |

The mapping is determined solely by declaration order, not by variable names.

---

### Register-Oriented Naming

Although variable names are completely free, programs intended for the MK-61 often benefit from naming variables according to their associated register.

For example:

```basic
LET r0 = 0
LET r1 = 0
LET r2 = 0
LET r3 = 0
```

This naming convention makes it easier for programmers to understand how variables are mapped onto the calculator's physical memory.

The compiler is free to allocate registers independently, but using register-oriented names improves readability for developers familiar with the MK-61 architecture.

---

## Variable Lifetime

Micro Basic does not create local variable scopes.

Variables exist in a single global register space shared by the entire program.

Variables declared inside loops or subroutines occupy exactly the same register pool as variables declared in the main program.

For example,

```basic
LET a = 10

SUB Square

    LET temp = a * a

    RETURN
```

Both `a` and `temp` consume calculator registers.

There is no automatic release of registers when leaving a subroutine.

For this reason, programmers are encouraged to reuse variables whenever possible.

---

## Input / Output

Micro Basic provides two high-level input/output statements.

```basic
INPUT value

PRINT value
```

Unlike traditional BASIC systems, these statements **do not represent console input or console output**.

The MK-61 has:

- no text console,
- no character display,
- no keyboard capable of entering strings.

Instead, the calculator provides a numeric keyboard together with the calculator's **X register**, whose value is shown on the display.

MiniBasic therefore interprets `INPUT` and `PRINT` as abstractions over operations involving register **X**.

---

## INPUT

```basic
INPUT value
```

Execution model:

1. The user enters a numeric value using the calculator keyboard.
2. The entered value becomes the current value of register **X**.
3. The generated MK-61 program stores register **X** into the calculator memory register associated with `value`.

Conceptually this is equivalent to

```
User input
      ↓
Register X
      ↓
Memory Register(value)
```

The generated calculator instructions are equivalent to:

```
X → П(register)
```

where `register` is the calculator register assigned to the variable.

---

## PRINT

```basic
PRINT value
```

Execution model:

1. The value stored in the calculator register associated with `value` is loaded into register **X**.
2. The calculator display immediately shows the new value of register X.

Conceptually:

```
Memory Register(value)
          ↓
      Register X
          ↓
      Calculator Display
```

Thus `PRINT` is not a text output operation.

Instead, it is an instruction that makes the numeric value visible by moving it into the calculator's display register.

---

## END and STOP

Micro Basic provides two different execution control statements.

### END

Terminates program execution.

```basic
END
```

### STOP

Temporarily halts execution while preserving the current program state.

Execution may later continue depending on the runtime environment.

```basic
STOP
```

Both statements are translated into their corresponding MK-61 instructions.

---

## Expressions

Micro Basic evaluates arithmetic and logical expressions using standard infix notation.

Examples:

```basic
LET a = 2 + 3
LET b = a * 5
LET c = (a + b) / 2
```

Expressions are translated into Reverse Polish Notation (RPN) instructions suitable for execution on the MK-61 stack machine.

The compiler is responsible for generating the appropriate stack manipulation instructions required by the calculator.

---

## Operators

### Arithmetic Operators

| Operator | Description |
|----------|-------------|
| `+` | Addition |
| `-` | Subtraction |
| `*` | Multiplication |
| `/` | Division |
| `^` | Exponentiation |

Example

```basic
LET result = (a + b) * c
```

---

### Relational Operators

Relational operators return logical values used by conditional statements.

| Operator | Description |
|----------|-------------|
| `=` | Equal |
| `<>` | Not equal |
| `<` | Less than |
| `<=` | Less than or equal |
| `>` | Greater than |
| `>=` | Greater than or equal |

Example

```basic
IF value > 10 THEN
    PRINT value
END
```

---

### Logical Operators

Micro Basic supports logical operations.

| Operator | Description |
|----------|-------------|
| `AND` | Logical AND |
| `OR` | Logical OR |
| `NOT` | Logical NOT |
| `XOR` | Exclusive OR |

Example

```basic
IF a > 0 AND b > 0 THEN
    PRINT a
END
```

---

## Parentheses

Parentheses may be used to control operator precedence.

```basic
LET result = (a + b) * (c - d)
```

The compiler evaluates parenthesized expressions first before translating them into stack operations.

---

## Conditional Execution

Micro Basic provides conditional execution using the `IF` statement.

```basic
IF a > b THEN
    PRINT a
END
```

Optionally,

```basic
IF a > b THEN
    PRINT a
ELSE
    PRINT b
END
```

The compiler generates conditional branch instructions equivalent to the calculator's conditional jump commands.

---

## Unconditional Branching

Micro Basic supports direct program jumps using `GOTO`.

```basic
GOTO 200
```

Unlike higher-level language constructs, `GOTO` transfers execution directly to the specified program address.

The target may be either

- a numeric line number,
- or an internally generated program address.

The generated code is translated directly into the corresponding MK-61 jump instruction.

---

## Subroutines

Micro Basic supports reusable program fragments using subroutines.

Two different syntaxes are available.

---

### GOSUB

```basic
GOSUB 200
```

Calls a subroutine located at an explicit program address.

Execution continues until a matching `RETURN` instruction is encountered.

---

### RETURN

```basic
RETURN
```

Returns execution to the instruction immediately following the corresponding `GOSUB`.

---

### SUB

Micro Basic also supports named subroutines.

```basic
SUB Square

    LET result = value * value

RETURN
```

Unlike `GOSUB`, the programmer specifies a symbolic name rather than a numeric address.

The compiler resolves the symbolic name into the corresponding program address during compilation.

Internally,

```basic
SUB Square
```

is simply another way of defining a jump target.

Therefore,

```basic
GOSUB 250
```

and

```basic
GOSUB Square
```

are conceptually equivalent after compilation.

The first uses an explicit address.

The second uses a symbolic label that the compiler converts into an address.

The purpose of named subroutines is improved readability rather than additional runtime functionality.

---

## Variable Usage Inside Subroutines

Variables declared inside a subroutine are **not local variables**.

For example,

```basic
LET value = 5

SUB Square

    LET temp = value * value

RETURN
```

Both `value` and `temp` occupy calculator memory registers.

The interpreter allocates registers exactly as if both variables had been declared in the main program.

Subroutines do not receive their own register space.

---

## No Local Variables

MiniBasic intentionally does not support local variables.

This limitation comes directly from the architecture of the MK-61 virtual machine.

The calculator has no mechanism for creating temporary storage areas for nested procedure calls.

Consequently,

- every variable belongs to the same global register pool,
- every subroutine shares the same memory,
- register allocation is global for the entire program.

---

## No Call Stack

One of the most important architectural limitations inherited from the MK-61 is the absence of a true call stack.

Unlike modern processors or virtual machines, the MK-61 does not maintain:

- stack frames,
- local variable storage,
- automatic register preservation,
- nested execution contexts.

Every subroutine executes within exactly the same memory environment as the main program.

Because there is no stack frame, the interpreter cannot create an independent address space for a called subroutine.

Instead, all program components operate on the same fixed set of calculator registers.

This explains several language limitations:

- local variables do not exist;
- recursive procedures are not supported;
- every variable contributes to the same register allocation;
- variables declared inside subroutines permanently consume calculator registers.

These restrictions are not language design decisions but direct consequences of the underlying MK-61 hardware architecture.

---

## Loops

MiniBasic supports counted loops using `FOR`.

```basic
FOR i = 1 TO 10

    PRINT i

NEXT
```

---

### STEP

The increment may be explicitly specified.

```basic
FOR i = 0 TO 20 STEP 2

    PRINT i

NEXT
```

Negative increments are also allowed.

```basic
FOR i = 10 TO 1 STEP -1

    PRINT i

NEXT
```

---

### Loop Variables

Loop variables are ordinary program variables.

For example,

```basic
FOR i = 1 TO 10
```

allocates register storage for `i`.

Loop variables are therefore subject to the same memory limitations as every other variable in the language.

They remain part of the global register allocation.

---

## Program Termination

Program execution finishes when either

```basic
END
```

or

```basic
STOP
```

is reached.

`END` terminates execution.

`STOP` suspends execution according to the capabilities of the runtime environment.
---

## Built-in Mathematical Functions

MiniBasic provides a collection of built-in mathematical functions and constants.

Whenever possible, these functions are translated directly into a single MK-61 virtual machine instruction. Functions that do not have a direct hardware equivalent may be translated into a sequence of calculator instructions.

Unless otherwise specified, each function accepts a single numeric argument and returns a numeric result.

### Exponential and Logarithmic Functions

| Function    | Description                                                  |
| ----------- | ------------------------------------------------------------ |
| `EXP10(x)`  | Calculates (10^x).                                           |
| `EXP(x)`    | Calculates (e^x).                                            |
| `LOG(x)`    | Base-10 logarithm.                                           |
| `LN(x)`     | Natural logarithm.                                           |
| `POW(x, y)` | Raises `x` to the power `y`. Equivalent to the `^` operator. |

Example

```basic
LET value = EXP(2)

LET power = POW(2, 8)
```

---

### Trigonometric Functions

| Function  | Description |
| --------- | ----------- |
| `SIN(x)`  | Sine        |
| `COS(x)`  | Cosine      |
| `TAN(x)`  | Tangent     |
| `ASIN(x)` | Arc sine    |
| `ACOS(x)` | Arc cosine  |
| `ATN(x)`  | Arc tangent |

The angle unit (degrees or radians) depends on the current configuration of the virtual machine.

Example

```basic
LET angle = 45

LET x = SIN(angle)

LET y = COS(angle)

PRINT x

PRINT y
```

---

### Numeric Functions

| Function    | Description                                            |
| ----------- | ------------------------------------------------------ |
| `ABS(x)`    | Absolute value                                         |
| `SQRT(x)`   | Square root                                            |
| `SQR(x)`    | Square (`x²`)                                          |
| `RECIP(x)`  | Reciprocal (`1/x`)                                     |
| `FLOOR(x)`  | Integer part of a number                               |
| `FRAC(x)`   | Fractional part of a number                            |
| `SIGN(x)`   | Returns `-1`, `0`, or `1` depending on the sign of `x` |
| `MAX(x, y)` | Returns the larger of two values                       |

Example

```basic
LET root = SQRT(25)

LET square = SQR(5)

LET reciprocal = RECIP(4)

LET integerPart = FLOOR(3.75)

LET fractionalPart = FRAC(3.75)

LET largest = MAX(a, b)
```

---

### Mathematical Constants

MiniBasic provides built-in mathematical constants.

| Constant | Description             |
| -------- | ----------------------- |
| `PI`     | Mathematical constant π |
| `E`      | Euler's number          |

Example

```basic
LET circumference = 2 * PI * radius

LET growth = E ^ x
```

---

### Random Number Generation

| Function   | Description                                            |
| ---------- | ------------------------------------------------------ |
| `RANDOM()` | Generates a pseudo-random number in the range `[0, 1)` |

Example

```basic
LET value = RANDOM()
```

---

### Angle Conversion Functions

The MK-61 instruction set includes several specialized functions for converting between decimal and degree-minute-second representations.

| Function        | Description                                                                            |
| --------------- | -------------------------------------------------------------------------------------- |
| `HM_TO_DEG(x)`  | Converts degrees (hours), minutes and fractions of minutes into decimal representation |
| `DEG_TO_HM(x)`  | Converts decimal representation into degrees (hours), minutes and fractions of minutes |
| `HMS_TO_DEG(x)` | Converts degrees (hours), minutes, seconds into decimal representation                 |
| `DEG_TO_HMS(x)` | Converts decimal representation into degrees (hours), minutes and seconds              |

These functions are primarily useful for scientific and navigation calculations.

---

## Translation to Mk-61 Virtual Machine Instructions Model

Micro Basic programs are **translated** into instructions executed by the MK-61 virtual machine.

Translation consists of several independent stages.

```
Source Code
      │
      ▼
Lexer
      │
      ▼
Parser
      │
      ▼
Abstract Syntax Tree
      │
      ▼
Semantic Analysis
      │
      ▼
Register Allocation
      │
      ▼
Code Generator
      │
      ▼
MK-61 Instructions
```

---

## Lexical Analysis

The lexer converts the input text into a sequence of tokens.

Typical token types include

* keywords
* identifiers
* numbers
* operators
* delimiters
* parentheses

Example

Source

```basic
LET counter = counter + 1
```

Tokens

```
LET
IDENTIFIER(counter)
=
IDENTIFIER(counter)
+
NUMBER(1)
```

---

# Parsing

The parser converts the token stream into an Abstract Syntax Tree (AST).

Example

```basic
LET result = (a + b) * c
```

becomes a tree similar to

```
Assignment

    result

        *

       / \

      +   c

     / \

    a   b
```

The AST is independent of the target hardware and represents only the logical structure of the program.

---

# Semantic Analysis

The semantic analysis phase validates the program.

Typical checks include

* undefined variables,
* duplicate declarations,
* invalid subroutine references,
* invalid loop construction,
* incorrect function usage,
* exceeding the available number of calculator registers.

Errors detected during semantic analysis prevent code generation.

---

# Register Allocation

One of the most important compilation stages is register allocation.

Unlike desktop programming languages, MiniBasic cannot create an unlimited number of variables.

Instead, every variable must be assigned one of the calculator's memory registers.

Allocation follows the order of first appearance.

Example

```basic
LET a = 0
LET b = 1
LET c = 2
```

becomes

| Variable | Register |
| -------- | -------- |
| a        | 0        |
| b        | 1        |
| c        | 2        |

This mapping is maintained throughout the generated program.

If no free registers remain, compilation fails.

---

# Code Generation

The code generator transforms the AST into executable MK-61 instructions.

Example

```basic
LET result = a + b
```

may become conceptually

```
Recall a

Push

Recall b

Add

Store result
```

The exact instruction sequence depends on the optimization strategy and the instruction set supported by the virtual machine.

---

# Example Programs

## Factorial

```basic
10 REM Factorial
20 INPUT n
30 LET result = 1
40 FOR i = 1 TO n
50 LET result = result * i
60 NEXT
70 PRINT result
80 END
```

### Translation to MK-61 Instructions result

| Address | Instruction | Address | Instruction |
|:------:|-------------|:------:|-------------|
| 00 | `X→П 0` | 13 | `П→X 2` |
| 01 | `1`       | 14 | `×`       |
| 02 | `X→П 1`   | 15 | `X→П 1`   |
| 03 | `1`       | 16 | `П→X 2`   |
| 04 | `X→П 2`   | 17 | `B↑`      |
| 05 | `П→X 0`   | 18 | `1`       |
| 06 | `B↑`      | 19 | `+`       |
| 07 | `П→X 2`   | 20 | `X→П 2`   |
| 08 | `-`       | 21 | `БП`      |
| 09 | `X≥0`     | 22 | `5`       |
| 10 | `23`      | 23 | `П→X 1`   |
| 11 | `П→X 1`   | 24 | `С/П`     |
| 12 | `B↑`      |    |           |
---

## Quadratic Equation Solver

```basic
10 REM ax² + bx + c = 0
20 INPUT a
30 INPUT b
40 INPUT c
50 LET d = b * b - 4 * a * c
60 IF d < 0 THEN
70 PRINT -1
80 ELSE
90 LET x1 = (-b + SQRT(d)) / (2 * a)
100 LET x2 = (-b - SQRT(d)) / (2 * a)
110 PRINT x1
120 PRINT x2
130 END
140 END
```

### Translation to MK-61 Instructions result
| Address | Instruction | Address | Instruction | 
|:------:|-------------|:------:|-------------|
| 00 | `X→П 0` | 31 | `П→X 1` |
| 01 | `X→П 1` | 32 | `-` |
| 02 | `X→П 2` | 33 | `B↑` |
| 03 | `П→X 1` | 34 | `П→X 3` |
| 04 | `B↑` | 35 | `√` |
| 05 | `П→X 1` | 36 | `+` |
| 06 | `×` | 37 | `B↑` |
| 07 | `B↑` | 38 | `2` |
| 08 | `4` | 39 | `B↑` |
| 09 | `B↑` | 40 | `П→X 0` |
| 10 | `П→X 0` | 41 | `×` |
| 11 | `×` | 42 | `÷` |
| 12 | `B↑` | 43 | `X→П 4` |
| 13 | `П→X 2` | 44 | `0` |
| 14 | `×` | 45 | `B↑` |
| 15 | `-` | 46 | `П→X 1` |
| 16 | `X→П 3` | 47 | `-` |
| 17 | `П→X 3` | 48 | `B↑` |
| 18 | `B↑` | 49 | `П→X 3` |
| 19 | `0` | 50 | `√` |
| 20 | `-` | 51 | `-` |
| 21 | `X<0` | 52 | `B↑` |
| 22 | `29` | 53 | `2` |
| 23 | `0` | 54 | `B↑` |
| 24 | `B↑` | 55 | `П→X 0` |
| 25 | `1` | 56 | `×` |
| 26 | `-` | 57 | `÷` |
| 27 | `БП` | 58 | `X→П 5` |
| 28 | `61` | 59 | `П→X 4` |
| 29 | `0` | 60 | `П→X 5` |
| 30 | `B↑` | 61 | `С/П` |
---

## Fibonacci Numbers

```basic
10 INPUT n
20 LET a = 0
30 LET b = 1
40 FOR i = 1 TO n
50 PRINT a
60 LET t = a + b
70 LET a = b
80 LET b = t
90 NEXT
100 END
```
### Translation to MK-61 Instructions result

| Address | Instruction | Address | Instruction |
|:------:|-------------|:------:|-------------|
| 00 | `X→П 0` | 16 | `П→X 2` |
| 01 | `0`       | 17 | `+`       |
| 02 | `X→П 1`   | 18 | `X→П 4`   |
| 03 | `1`       | 19 | `П→X 2`   |
| 04 | `X→П 2`   | 20 | `X→П 1`   |
| 05 | `1`       | 21 | `П→X 4`   |
| 06 | `X→П 3`   | 22 | `X→П 2`   |
| 07 | `П→X 0`   | 23 | `П→X 3`   |
| 08 | `B↑`      | 24 | `B↑`      |
| 09 | `П→X 3`   | 25 | `1`       |
| 10 | `-`       | 26 | `+`       |
| 11 | `X≥0`     | 27 | `X→П 3`   |
| 12 | `30`      | 28 | `БП`      |
| 13 | `П→X 1`   | 29 | `7`       |
| 14 | `П→X 1`   | 30 | `С/П`     |
| 15 | `B↑`      |    |           |

---