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
| **CompilerDriver** | `.../kwacc/CompilerDriver.kt` | Parses CLI arguments, selects mode, orchestrates pipeline |
| **CPreprocessor** | `.../kwacc/CPreprocessor.kt` | Runs `clang`/`gcc` with `-E -P` to produce preprocessed `.i` file |
| **Lexer** | `.../kwacc/Lexer.kt` | Tokenizes input, tracks line/column, returns `TokenizeResult` |
| **Parser** | `.../kwacc/Parser.kt` | Recursive descent parser returning `ParseResult` and AST |
| **AST** | `.../kwacc/AST.kt` | Sealed node hierarchy for C syntax tree |
| **ASTVisitor** | `.../kwacc/ASTVisitor.kt` | Visitor-based AST traversal |
| **ASTPrettyPrinter** | `.../kwacc/ASTPrettyPrinter.kt` | Renders readable AST for debugging |
| **Asm** | `.../kwacc/asm/Asm.kt` | Sealed node hierarchy for assembly (code generation target) |
| **DiagnosticEngine** | `.../kwacc/DiagnosticEngine.kt` | Placeholder for centralized diagnostics |

*Note: `.../kwacc/` = `src/main/kotlin/me/billbai/compiler/kwacc/`*

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

```
src/main/kotlin/me/billbai/compiler/kwacc/     # Main package
src/main/kotlin/me/billbai/compiler/kwacc/asm/ # Assembly AST (code generation)
src/test/kotlin/me/billbai/compiler/kwacc/     # Tests
```

The Gradle entry point is `me.billbai.compiler.kwacc.MainKt`.

## Compiler Modes

| Flag | Stage | Description |
|------|-------|-------------|
| `--lex` | Lexing only | Outputs token stream |
| `--parse` | Lex + parse | Prints AST |
| `--codegen` | Placeholder | Backend not implemented |
| `-S` | Assembly | Flag wired, backend not implemented |
