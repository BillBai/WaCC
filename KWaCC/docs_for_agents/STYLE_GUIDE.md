# KWaCC Style Guide

Coding conventions for the KWaCC compiler.

## Coding Style

- Prefer sealed classes for tokens, AST nodes, TACKY IR, and ASM nodes
- Favor `val` and data classes for immutable structures
- Use `object` for singleton types (operators, `NullStmt`, `IntType`, etc.)
- Accumulate errors in result types for lexer/parser; throw `SemanticError` for semantic analysis
- 4-space indent, ASCII unless required

## Naming Conventions

Each layer uses a consistent prefix to avoid ambiguity:

| Layer | Prefix | Examples |
|-------|--------|----------|
| AST | *(none)* | `UnaryExpression`, `ReturnStmt`, `AddOperator` |
| TACKY IR | `Tacky` | `TackyUnaryInst`, `TackyConstantVal`, `TackyReturnInst` |
| ASM | `Asm` | `AsmMovInst`, `AsmRegAX`, `AsmStackOperand` |

Other naming patterns:
- Visitor methods: `visitNodeName` (e.g., `visitUnaryExpression`, `visitAsmMovInst`)
- Test files: `<Component>Test.kt` (e.g., `LexerTest.kt`, `TackyGenTest.kt`)
- Test C files: `ch<N>_<description>.c` (e.g., `ch5_var.c`, `ch4_and_or.c`)
- Error test C files: `ch<N>_err_<description>.c` (e.g., `ch5_err_dup.c`)

## Visitor Pattern

- Every `AstNode` subclass must implement `accept(visitor)` with a corresponding `visitXxx` in `AstVisitor`
- When adding a new AST node, update ALL visitors: `AstPrettyPrinter`, `TackyGen`, `VariableResolver`
- Wrapper nodes (e.g., `BlockItemStatement`) should delegate: `visitor.visitBlockItemStatement` calls `node.statement.accept(this)`
- Use `TODO("not implemented")` for visitor stubs during incremental development

## Code Generation Patterns

- **Emit-and-return** (TackyGen): Recursive calls emit instructions AND return where the result lives. See [EMIT_AND_RETURN.md](EMIT_AND_RETURN.md)
  - `visit*` methods = expressions → return a `TackyVal`
  - `emit*` methods = statements → return `Unit`
- **Short-circuit evaluation** (Chapter 4): `&&` and `||` use `JumpIfZero`/`JumpIfNotZero` with labels, not binary instructions
- **Conditional codegen** (Chapter 6): `if`/`else` and ternary `?:` use jump/label patterns

## Assembly Conventions

- When adding new assembly instructions/operands, update: `Asm.kt`, `AsmAstVisitor.kt`, `AsmAstPrettyPrinter.kt`, `AsmEmitter.kt`
- x86-64 AT&T syntax: operand order is `src, dst`

## Testing

- Framework: JUnit 5 with Kotlin test extensions
- Test source: `src/test/kotlin/me/billbai/compiler/kwacc/`
- Test C files: `test_code/`
- Run tests: `./gradlew test`
- Run book test suite: `/test-chapter <N>` (requires `LOCAL.md` paths)

For the full pipeline overview and per-chapter implementation checklist, see [ARCHITECTURE.md](ARCHITECTURE.md).
