# AGENTS.md

Guidance for AI agents and collaborators working in this repository.

## Project Overview

KWaCC is a C compiler written in Kotlin. It currently implements a small front-end pipeline (preprocess -> lex -> parse) and prints an AST for debugging.

## Architecture

### Pipeline

1. Input `.c` file -> C preprocessor -> temporary `.i` file
2. Lexer -> token stream + lexical errors
3. Parser -> AST + parse errors
4. (Future) semantic analysis + code generation
5. Temporary `.i` file deleted

### Core Components

- **CompilerDriver** (`src/main/kotlin/CompilerDriver.kt`)
  - Parses CLI arguments, selects mode, and orchestrates the pipeline.
- **CPreprocessor** (`src/main/kotlin/CPreprocessor.kt`)
  - Runs `clang`/`gcc` with `-E -P` to produce a preprocessed `.i` file.
- **Lexer** (`src/main/kotlin/Lexer.kt`)
  - Tokenizes input, tracks line/column, returns `TokenizeResult`.
- **Parser** (`src/main/kotlin/Parser.kt`)
  - Recursive descent parser returning `ParseResult` and an AST.
- **AST + Visitor** (`src/main/kotlin/AST.kt`, `src/main/kotlin/ASTVisitor.kt`)
  - Sealed node hierarchy with visitor-based traversal.
- **ASTPrettyPrinter** (`src/main/kotlin/ASTPrettyPrinter.kt`)
  - Renders a readable AST for debugging.
- **DiagnosticEngine** (`src/main/kotlin/DiagnosticEngine.kt`)
  - Placeholder for centralized diagnostics.

### Supported C Subset (Current)

- Single function definition
- `int`/`void` return types
- `void` parameter list only (no parameters yet)
- `return` statements
- Integer constants and identifiers as expressions

### Key Patterns

- **Error handling**: Lexer and parser return result objects with errors instead of throwing for recoverable issues.
- **Mode-based operation**: The compiler can stop at lex/parse/codegen stages for debugging.
- **Visitor pattern**: AST traversal is separated from AST definitions.

### Package Structure

All code lives under the `me.billbai.compiler.kwacc` package. The Gradle entry point is `MainKt`.

## Coding Style

- Prefer sealed classes for tokens/AST.
- Favor `val` and data classes for immutable structures.
- Accumulate errors in result types for normal error cases.
- Keep formatting consistent with existing files (4-space indent, ASCII unless required).

## Build, Run, Test

- **Build**: `./gradlew build`
- **Run**: `./gradlew run --args="[arguments]"`
- **Test**: `./gradlew test`
- **Clean**: `./gradlew clean`

### Compiler Modes

- `--lex`: Lexing only
- `--parse`: Lex + parse (prints AST)
- `--codegen`: Placeholder stage
- `-S`: Emit assembly (flag wired, backend not implemented)

Example:

```
./gradlew run --args="--parse test_code/hello.c"
```

### Testing

- Test framework: JUnit 5 with Kotlin test extensions
- Test files live in `src/test/kotlin/`

## External Dependencies

- `clang` (default) or `gcc` must be available on `PATH` for preprocessing.

## Notes for Contributors

- Keep changes scoped to a single stage when possible (lexer vs parser vs AST).
- When extending the grammar, update `Token`, `Lexer`, `Parser`, and `AST` together.
- Add or extend tests under `src/test/kotlin/` when behavior changes.
