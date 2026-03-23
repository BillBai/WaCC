# KWaCC Architecture

Detailed architecture reference for AI agents working on this codebase.

## Pipeline

```
.c file
  → C Preprocessor (clang -E -P) → .i file
  → Lexer → token stream + errors
  → Parser (recursive descent + precedence climbing) → AST + errors
  → VariableResolver → resolved AST (unique names, error detection)
  → TackyGen → TACKY IR (three-address code)
  → TackyToAsm → x86-64 pseudo-assembly
  → ReplacePseudo → stack offsets replace pseudo-registers
  → FixupInstructions → fix x86 operand constraints (mem-mem, imm dst)
  → AsmEmitter → .S file → clang assembles/links → executable
  → Cleanup (.i file deleted)
```

## Core Components

| Component | File | Responsibility |
|-----------|------|----------------|
| **CompilerDriver** | `CompilerDriver.kt` | CLI args, mode selection, pipeline orchestration |
| **CPreprocessor** | `CPreprocessor.kt` | Runs `clang`/`gcc` with `-E -P` |
| **Lexer** | `Lexer.kt` | Tokenizes input, tracks line/column, returns `TokenizeResult` |
| **Parser** | `Parser.kt` | Recursive descent + precedence climbing, returns `ParseResult` |
| **AST** | `Ast.kt` | Sealed node hierarchy for C syntax tree |
| **AstVisitor** | `AstVisitor.kt` | Visitor interface for AST traversal |
| **AstPrettyPrinter** | `AstPrettyPrinter.kt` | Readable AST for debugging |
| **VariableResolver** | `VariableResolver.kt` | Semantic analysis: renames variables to unique names, detects duplicate/undefined |
| **UniqueNameGenerator** | `UniqueNameGenerator.kt` | Singleton counter shared between VariableResolver and TackyGen |
| **TackyGen** | `TackyGen.kt` | Lowers AST to three-address code IR (TACKY). Handles control flow via conditional jumps. Uses emit-and-return pattern — see [EMIT_AND_RETURN.md](../docs/EMIT_AND_RETURN.md) |
| **Tacky** | `Tacky.kt` | Sealed node hierarchy for TACKY IR |
| **TackyPrettyPrinter** | `TackyPrettyPrinter.kt` | Readable TACKY IR for debugging |
| **TackyToAsm** | `TackyToAsm.kt` | Converts TACKY IR to x86-64 pseudo-assembly |
| **Asm** | `Asm.kt` | Sealed node hierarchy for x86-64 assembly |
| **AsmAstVisitor** | `AsmAstVisitor.kt` | Visitor interface for ASM AST traversal |
| **AsmAstPrettyPrinter** | `AsmAstPrettyPrinter.kt` | Readable ASM AST for debugging |
| **ReplacePseudo** | `ReplacePseudo.kt` | Pseudo-registers → stack offsets, computes stack size |
| **FixupInstructions** | `FixupInstructions.kt` | Fixes x86 operand constraints (mem-mem, imm dst, cmp imm) using R10/R11 |
| **AsmEmitter** | `AsmEmitter.kt` | Emits AT&T syntax x86-64 assembly text |
| **DiagnosticEngine** | `DiagnosticEngine.kt` | Placeholder for centralized diagnostics |

*All files in `src/main/kotlin/me/billbai/compiler/kwacc/`*

## Supported C Subset (Chapters 1–5 complete, Chapter 6 conditionals in progress)

| Chapter | Features |
|---------|----------|
| **1–2** | Single function, `int`/`void` return types, `return` statements, integer constants |
| **3** | Unary operators (`-`, `~`), binary operators (`+`, `-`, `*`, `/`, `%`) |
| **4** | Logical (`&&`, `||`, `!`), relational (`==`, `!=`, `<`, `>`, `<=`, `>=`) |
| **5** | Local variables, declarations (`int x;`, `int x = 5;`), assignment (`=`, chained), expression/null statements, `BlockItem` wrapper, implicit `return 0` |
| **6** *(in progress)* | `if`/`else` statements, ternary conditional `?:` expression |

## Key Patterns

- **Visitor pattern**: Both AST and ASM AST use typed visitor interfaces for traversal
- **Sealed class hierarchies**: Tokens, AST nodes, TACKY IR, and ASM nodes all use Kotlin sealed classes for exhaustive `when` matching
- **Precedence climbing**: Parser uses precedence climbing for binary expressions (left-associative with `prec + 1`, right-associative with `prec` for `=` and `?:`)
- **Emit-and-return**: TackyGen recursive calls both emit instructions (side effect) and return where the result lives (value). `visit*` = expressions (return value), `emit*` = statements (return Unit). See [EMIT_AND_RETURN.md](../docs/EMIT_AND_RETURN.md)
- **Error handling**: Lexer/parser return result objects; VariableResolver throws `SemanticError`
- **Three-address code**: TACKY IR uses `dst = op(src)` / `dst = src1 op src2` form — at most one operation per instruction
- **UniqueNameGenerator**: Singleton counter ensures no name collisions between semantic analysis and TACKY generation

## Adding a New Chapter (Checklist)

When the book introduces new language features, follow this order:

1. **Token.kt + Lexer.kt** — New tokens (keywords, operators, delimiters)
2. **Ast.kt** — New sealed class nodes (statements, expressions, operators)
3. **AstVisitor.kt** — Add `visitXxx` method for each new node
4. **Parser.kt** — Parse rules (precedence table for operators, new parse methods for statements)
5. **AstPrettyPrinter.kt** — Readable output for new nodes
6. **VariableResolver.kt** — Handle new nodes (resolve variables, detect errors)
7. **TackyGen.kt** — Lower new AST nodes to TACKY IR instructions
8. **Tacky.kt** — New IR nodes only if needed (new instruction types)
9. **TackyToAsm.kt → ReplacePseudo → FixupInstructions → AsmEmitter** — Only update if new TACKY instructions require new ASM patterns
10. **Tests** — Unit tests for new behavior, test C files in `test_code/`

Use the `compiler-reviewer` subagent after implementing to check for missed visitor methods and operand bugs.

## Package Structure

```
src/main/kotlin/me/billbai/compiler/kwacc/     # Main source
src/test/kotlin/me/billbai/compiler/kwacc/     # Tests
test_code/                                      # Test C files
.claude/skills/                                 # Claude Code skills
.claude/agents/                                 # Claude Code subagents
docs_for_agents/                                # AI collaboration docs
docs/                                           # Implementation pattern docs
```

The Gradle entry point is `me.billbai.compiler.kwacc.MainKt`.
