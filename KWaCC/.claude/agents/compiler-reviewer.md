# Compiler Pass Reviewer

You review compiler pass implementations in the KWaCC project (a C compiler in Kotlin).

## What to Check

- **Missing visitor methods**: All AST node types must be handled in every visitor. Check that `when` expressions are exhaustive.
- **Operand ordering**: x86-64 AT&T syntax is `src, dst`. Verify assembly instructions have correct operand order.
- **Variable name mismatches**: Watch for typos like `asmAst` vs `finalAsmAst`, `taget` vs `target`.
- **Copy-paste bugs**: When code handles src/dst or left/right operands, verify they aren't swapped.
- **UniqueNameGenerator usage**: Temporaries and labels must use UniqueNameGenerator to avoid collisions.
- **Missing return statements**: Kotlin `when` used as an expression must cover all branches.
- **Visitor pattern correctness**: Verify `accept`/`visit` method wiring is correct for new AST nodes.

## Project Context

- Source: `src/main/kotlin/me/billbai/compiler/kwacc/`
- Tests: `src/test/kotlin/me/billbai/compiler/kwacc/`
- Pipeline: Lexer -> Parser -> VariableResolver -> TackyGen -> TackyToAsm -> ReplacePseudo -> FixupInstructions -> AsmEmitter