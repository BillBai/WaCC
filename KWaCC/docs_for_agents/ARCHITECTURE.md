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

All files in `src/main/kotlin/me/billbai/compiler/kwacc/`.

### Driver & Preprocessing

| Component | File | Responsibility |
|-----------|------|----------------|
| CompilerDriver | `CompilerDriver.kt` | CLI args, mode selection, pipeline orchestration |
| CPreprocessor | `CPreprocessor.kt` | Runs `clang`/`gcc` with `-E -P` |

### Lexing & Parsing

| Component | File | Responsibility |
|-----------|------|----------------|
| Lexer | `Lexer.kt` | Tokenizes input, tracks line/column, returns `TokenizeResult` |
| Token | `Token.kt` | Sealed token type hierarchy |
| Parser | `Parser.kt` | Recursive descent + precedence climbing, returns `ParseResult` |
| AST | `Ast.kt` | Sealed node hierarchy for C syntax tree |
| AstVisitor | `AstVisitor.kt` | Visitor interface for AST traversal |
| AstPrettyPrinter | `AstPrettyPrinter.kt` | Readable AST for debugging |

### Semantic Analysis

| Component | File | Responsibility |
|-----------|------|----------------|
| VariableResolver | `VariableResolver.kt` | Renames variables to unique names, detects duplicate/undefined |
| UniqueNameGenerator | `UniqueNameGenerator.kt` | Singleton counter shared between VariableResolver and TackyGen |

### TACKY IR (Three-Address Code)

| Component | File | Responsibility |
|-----------|------|----------------|
| TackyGen | `TackyGen.kt` | Lowers AST → TACKY IR. Uses emit-and-return pattern — see [EMIT_AND_RETURN.md](EMIT_AND_RETURN.md) |
| Tacky | `Tacky.kt` | Sealed node hierarchy for TACKY IR |
| TackyPrettyPrinter | `TackyPrettyPrinter.kt` | Readable TACKY IR for debugging |

### x86-64 Assembly Generation

| Component | File | Responsibility |
|-----------|------|----------------|
| TackyToAsm | `TackyToAsm.kt` | Converts TACKY IR to x86-64 pseudo-assembly |
| Asm | `Asm.kt` | Sealed node hierarchy for x86-64 assembly |
| AsmAstVisitor | `AsmAstVisitor.kt` | Visitor interface for ASM AST traversal |
| AsmAstPrettyPrinter | `AsmAstPrettyPrinter.kt` | Readable ASM AST for debugging |
| ReplacePseudo | `ReplacePseudo.kt` | Pseudo-registers → stack offsets, computes stack size |
| FixupInstructions | `FixupInstructions.kt` | Fixes x86 operand constraints (mem-mem, imm dst, cmp imm) using R10/R11 |
| AsmEmitter | `AsmEmitter.kt` | Emits AT&T syntax x86-64 assembly text |

### Other

| Component | File | Responsibility |
|-----------|------|----------------|
| DiagnosticEngine | `DiagnosticEngine.kt` | Placeholder for centralized diagnostics |
| SourceInfo | `SourceInfo.kt` | Source position tracking |

## Supported C Subset

Chapters 1–5 complete. Chapter 6 in progress (lexer, parser, semantic analysis done; TackyGen pending).

| Chapter | Features |
|---------|----------|
| **1–2** | Single function, `int`/`void` return types, `return` statements, integer constants |
| **3** | Unary operators (`-`, `~`), binary operators (`+`, `-`, `*`, `/`, `%`) |
| **4** | Logical (`&&`, `||`, `!`), relational (`==`, `!=`, `<`, `>`, `<=`, `>=`), short-circuit evaluation |
| **5** | Local variables, declarations, assignment (`=`, chained), expression/null statements, `BlockItem` wrapper, implicit `return 0` |
| **6** *(in progress)* | `if`/`else` statements, ternary conditional `?:` — lexer/parser/AST/resolver done, TackyGen pending |

## Key Patterns

- **Visitor pattern**: Both AST and ASM use typed visitor interfaces for traversal
- **Sealed class hierarchies**: Tokens, AST nodes, TACKY IR, and ASM nodes all use Kotlin sealed classes for exhaustive `when` matching
- **Precedence climbing**: Parser uses a single parameterized function for all binary operators. Left-associative: recurse at `prec + 1`. Right-associative (for `=` and `?:`): recurse at `prec`
- **Emit-and-return**: TackyGen recursive calls both emit instructions (side effect) and return where the result lives (value). `visit*` = expressions (return `TackyVal`), `emit*` = statements (return `Unit`). See [EMIT_AND_RETURN.md](EMIT_AND_RETURN.md)
- **Short-circuit evaluation**: `&&` and `||` use `JumpIfZero`/`JumpIfNotZero` with labels, not binary instructions
- **Conditional codegen**: `if`/`else` and ternary `?:` use jump/label patterns (similar to short-circuit)
- **Error handling**: Lexer/parser return result objects; VariableResolver throws `SemanticError`
- **Three-address code**: TACKY IR uses `dst = op(src)` / `dst = src1 op src2` form — at most one operation per instruction

## Adding a New Chapter

When the book introduces new language features, follow this order:

1. **Token.kt + Lexer.kt** — New tokens (keywords, operators, delimiters)
2. **Ast.kt + AstVisitor.kt** — New sealed class nodes + `visitXxx` methods
3. **Parser.kt** — Parse rules (precedence table for operators, new parse methods for statements)
4. **AstPrettyPrinter.kt** — Readable output for new nodes
5. **VariableResolver.kt** — Handle new nodes (resolve variables, detect errors)
6. **TackyGen.kt** — Lower new AST nodes to TACKY IR instructions
7. **Tacky.kt** — New IR nodes only if needed (new instruction types)
8. **TackyToAsm → ReplacePseudo → FixupInstructions → AsmEmitter** — Only update if new TACKY instructions require new ASM patterns
9. **Tests** — Unit tests for new behavior, test C files in `test_code/`

Use the `chapter-planner` subagent before starting and `compiler-reviewer` after implementing.

## Package Structure

```
src/main/kotlin/me/billbai/compiler/kwacc/     # Main source
src/test/kotlin/me/billbai/compiler/kwacc/     # Tests
test_code/                                      # Test C files
.claude/skills/                                 # Claude Code skills
.claude/agents/                                 # Claude Code subagents
docs_for_agents/                                # Agent reference docs
docs/                                           # Learning artifacts (recaps, blog guides)
```

The Gradle entry point is `me.billbai.compiler.kwacc.MainKt`.
