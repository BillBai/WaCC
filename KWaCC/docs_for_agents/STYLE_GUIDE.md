# KWaCC Style Guide

Coding conventions and contributor guidelines.

## Coding Style

- Prefer sealed classes for tokens/AST
- Favor `val` and data classes for immutable structures
- Accumulate errors in result types for normal error cases
- Keep formatting consistent with existing files (4-space indent, ASCII unless required)

## Notes for Contributors

- Keep changes scoped to a single stage when possible (lexer vs parser vs AST)
- When extending the grammar, update `Token`, `Lexer`, `Parser`, and `AST` together
- When adding new assembly instructions/operands, update `Asm.kt`
- Add or extend tests when behavior changes

## Testing

- Test framework: JUnit 5 with Kotlin test extensions
- Test files live in `src/test/kotlin/me/billbai/compiler/kwacc/`
- Run tests: `./gradlew test`
