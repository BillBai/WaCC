# KWaCC Architecture

Detailed architecture reference for AI agents working on this codebase.

## Pipeline

1. Input `.c` file -> C preprocessor -> temporary `.i` file
2. Lexer -> token stream + lexical errors
3. Parser -> AST + parse errors
4. (Future) semantic analysis + code generation
5. Temporary `.i` file deleted

## Core Components

| Component | File | Responsibility |
|-----------|------|----------------|
| **CompilerDriver** | `src/main/kotlin/CompilerDriver.kt` | Parses CLI arguments, selects mode, orchestrates pipeline |
| **CPreprocessor** | `src/main/kotlin/CPreprocessor.kt` | Runs `clang`/`gcc` with `-E -P` to produce preprocessed `.i` file |
| **Lexer** | `src/main/kotlin/Lexer.kt` | Tokenizes input, tracks line/column, returns `TokenizeResult` |
| **Parser** | `src/main/kotlin/Parser.kt` | Recursive descent parser returning `ParseResult` and AST |
| **AST** | `src/main/kotlin/AST.kt` | Sealed node hierarchy for syntax tree |
| **ASTVisitor** | `src/main/kotlin/ASTVisitor.kt` | Visitor-based AST traversal |
| **ASTPrettyPrinter** | `src/main/kotlin/ASTPrettyPrinter.kt` | Renders readable AST for debugging |
| **DiagnosticEngine** | `src/main/kotlin/DiagnosticEngine.kt` | Placeholder for centralized diagnostics |

## Supported C Subset (Current)

- Single function definition
- `int`/`void` return types
- `void` parameter list only (no parameters yet)
- `return` statements
- Integer constants and identifiers as expressions

## Key Patterns

- **Error handling**: Lexer and parser return result objects with errors instead of throwing for recoverable issues.
- **Mode-based operation**: The compiler can stop at lex/parse/codegen stages for debugging.
- **Visitor pattern**: AST traversal is separated from AST definitions.

## Package Structure

All code lives under the `me.billbai.compiler.kwacc` package. The Gradle entry point is `MainKt`.

## Compiler Modes

| Flag | Stage | Description |
|------|-------|-------------|
| `--lex` | Lexing only | Outputs token stream |
| `--parse` | Lex + parse | Prints AST |
| `--codegen` | Placeholder | Backend not implemented |
| `-S` | Assembly | Flag wired, backend not implemented |
