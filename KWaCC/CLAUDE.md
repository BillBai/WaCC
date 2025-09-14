# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

KWaCC is a C compiler written in Kotlin. The project follows a multi-stage compilation architecture with distinct phases: lexical analysis, parsing, and code generation. The main entry point is through `CompilerDriver` which orchestrates the compilation pipeline.

## Build and Development Commands

### Basic Commands
- **Build**: `./gradlew build`
- **Run**: `./gradlew run --args="[arguments]"`
- **Test**: `./gradlew test`
- **Clean**: `./gradlew clean`

### Running the Compiler
The compiler supports multiple modes via command-line flags:
- `--lex`: Run lexical analysis only
- `--parse`: Run up to parsing stage  
- `--codegen`: Run up to code generation
- `-S`: Emit assembly output

Example: `./gradlew run --args="--lex input.c"`

### Testing
- Run all tests: `./gradlew test`
- Test framework: JUnit 5 with Kotlin test extensions
- Test files are in `src/test/kotlin/`

## Architecture

### Core Components

1. **CompilerDriver** (`src/main/kotlin/CompilerDriver.kt`): Main orchestrator that:
   - Parses command-line arguments
   - Manages compilation phases (lex, parse, codegen)
   - Handles C preprocessing via external clang/gcc
   - Coordinates file I/O and cleanup

2. **CPreprocessor** (`src/main/kotlin/CPreprocessor.kt`): External preprocessor wrapper that:
   - Invokes clang or gcc with `-E -P` flags
   - Handles preprocessed file generation and cleanup
   - Supports both CLANG and GCC backends

3. **Lexer** (`src/main/kotlin/Lexer.kt`): Lexical analyzer that:
   - Tokenizes C source code from InputStream
   - Provides comprehensive error reporting with line/column info
   - Returns structured `TokenizeResult` with tokens and errors
   - Handles error recovery to collect multiple lexical errors

4. **Token** (`src/main/kotlin/Token.kt`): Token type definitions using sealed classes:
   - Supports identifiers, constants, keywords (int, void, return)
   - Punctuation tokens (parentheses, braces, semicolons)
   - Type-safe token hierarchy with proper toString() implementations

5. **DiagnosticEngine** (`src/main/kotlin/DiagnosticEngine.kt`): Currently a stub for future error reporting

### Key Patterns

- **Error Handling**: The lexer uses a `TokenizeResult` wrapper that separates successful tokens from errors, allowing error recovery and multiple error reporting
- **Preprocessing Pipeline**: All input files go through C preprocessing before lexical analysis, with automatic cleanup of temporary files
- **Mode-based Operation**: The compiler can stop at any stage (lex, parse, codegen) for debugging and testing

### File Processing Flow

1. Input file → C Preprocessor → Temporary `.i` file
2. Lexer reads preprocessed file → Tokens + Errors
3. Parser (future) → AST
4. Code generator (future) → Assembly/Object code
5. Cleanup temporary files

## Package Structure

All code is under the `me.billbai.compiler.kwacc` package. The project uses Gradle with Kotlin JVM plugin and targets the main class `MainKt`.