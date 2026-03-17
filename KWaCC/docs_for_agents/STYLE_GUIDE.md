# KWaCC Style Guide

Coding conventions and patterns for the KWaCC compiler.

## Coding Style

- Prefer sealed classes for tokens, AST nodes, TACKY IR, and ASM nodes
- Favor `val` and data classes for immutable structures
- Use `object` for singleton types (operators, `NullStmt`, `IntType`, etc.)
- Accumulate errors in result types for lexer/parser; throw `SemanticError` for semantic analysis
- Keep formatting consistent with existing files (4-space indent, ASCII unless required)

## Naming Conventions

Each layer uses a consistent prefix to avoid ambiguity:

| Layer | Prefix | Examples |
|-------|--------|----------|
| AST | *(none)* | `UnaryExpression`, `ReturnStmt`, `AddOperator` |
| TACKY IR | `Tacky` | `TackyUnaryInst`, `TackyConstantVal`, `TackyReturnInst` |
| ASM | `Asm` | `AsmMovInst`, `AsmRegAX`, `AsmStackOperand` |

- Visitor methods: `visitNodeName` (e.g., `visitUnaryExpression`, `visitAsmMovInst`)
- Test files: `<Component>Test.kt` (e.g., `LexerTest.kt`, `TackyGenTest.kt`)
- Test C files: `ch<N>_<description>.c` (e.g., `ch5_var.c`, `ch4_and_or.c`)
- Error test C files: `ch<N>_err_<description>.c` (e.g., `ch5_err_dup.c`)

## Visitor Pattern Conventions

- Every `AstNode` subclass must implement `accept(visitor)` and have a corresponding `visitXxx` method in `AstVisitor`
- When adding a new AST node, update ALL visitors: `AstPrettyPrinter`, `TackyGen`, `VariableResolver`
- Wrapper nodes (e.g., `BlockItemStatement`) should delegate: `visitor.visitBlockItemStatement` calls `node.statement.accept(this)`
- Use `TODO("not implemented")` for visitor stubs during incremental development

## Code Generation Patterns

- **Emit-and-return** (TackyGen): Recursive calls emit instructions AND return where the result lives. See [EMIT_AND_RETURN.md](../docs/EMIT_AND_RETURN.md)
  - `visit*` methods = expressions → return a `TackyVal`
  - `emit*` methods = statements → return `Unit`
- **Short-circuit evaluation** (Chapter 4): `&&` and `||` use `JumpIfZero`/`JumpIfNotZero` with labels, not binary instructions
- **Conditional codegen** (Chapter 6): `if`/`else` and ternary `?:` use jump/label patterns

## Adding New Language Features (Per-Chapter Pattern)

1. **Lexer**: Add new tokens to `Token.kt`, update `Lexer.kt` recognition
2. **AST**: Add sealed class nodes to `Ast.kt`, add visitor methods to `AstVisitor.kt`
3. **Parser**: Update `Parser.kt` (precedence table for operators, new parse methods for statements)
4. **Pretty Printer**: Update `AstPrettyPrinter.kt` for readable output
5. **Semantic Analysis**: Update `VariableResolver.kt` for new node types
6. **TackyGen**: Lower new AST nodes to TACKY IR instructions
7. **TackyToAsm → ReplacePseudo → FixupInstructions → AsmEmitter**: Only update if new TACKY instructions require new ASM patterns

See also: [ARCHITECTURE.md](ARCHITECTURE.md) § "Adding a New Chapter"

## Notes for Contributors

- Keep changes scoped to a single stage when possible (lexer vs parser vs AST)
- When extending the grammar, update `Token`, `Lexer`, `Parser`, and `AST` together
- When adding new assembly instructions/operands, update `Asm.kt`, `AsmAstVisitor.kt`, `AsmAstPrettyPrinter.kt`, `AsmEmitter.kt`
- Add or extend tests when behavior changes

## Testing

- Test framework: JUnit 5 with Kotlin test extensions
- Test files live in `src/test/kotlin/me/billbai/compiler/kwacc/`
- Test C files live in `test_code/`
- Run tests: `./gradlew test`
- Run book test suite: `/test-chapter <N>` (requires `LOCAL.md` paths)
