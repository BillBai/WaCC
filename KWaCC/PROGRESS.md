# KWaCC Learning Progress

A journal tracking learning progress through the "Write a C Compiler" book while learning Kotlin.

---

## Session 2025-01-11

### Topics Covered
- Reviewed `Lexer.kt` for bugs, improvements, and Kotlin idioms
- Refactored to eliminate all `!!` null assertions

### Key Learnings

**Kotlin Null Safety:**
- `curChar?.isDigit() == true` is cleaner than `curChar != null && curChar!!.isDigit()`
- Capture mutable `var` into local `val` to enable smart casting: `val ch = curChar ?: return ...`
- Safe call with `== true` works because `null == true` is `false`

**Compiler Design:**
- Error positions should point to token START, not where lexer ends up after consuming
- Captured `beginLine`/`beginColumn` before consuming characters in `lexNumbers()`

**Code Quality:**
- `peek()` and `peekN()` are currently unused - consider removing or keeping for future operators
- Tokens don't carry source positions - will matter when parser needs to report errors
- `TokenType` enum may be redundant with sealed class hierarchy

### Changes Made
- Refactored Lexer to use `?.` safe calls
- Fixed error position tracking in `lexNumbers()`
- Added AI collaboration guidelines to `AGENTS.md`

### Next Session Ideas
- Review Parser implementation
- Review AST design
- Discuss: Should tokens carry source positions?
- Discuss: Next chapter from the book (unary operators? binary expressions?)

---

## Session 2026-01-12

### Topics Covered
- Removed `TokenType` enum redundancy from `Token.kt`
- Simplified tokenizer loop to use single exit path
- Refined AI collaboration guidelines for better learning

### Key Learnings

**Kotlin Sealed Classes vs Enums:**
- Sealed classes provide exhaustive type checking via `is` pattern matching
- Use sealed classes when variants carry different data (`Identifier(value)`, `Constant(value)`)
- Use enums when variants are just labels with no per-instance data (`KeywordType.INT`, `KeywordType.VOID`)

**Technical Communication:**
- Professional pattern: Observation → Reasoning → Question
- Keep it short and direct — real engineers don't use overly formal language
- Example: "Why two exit conditions? Seems redundant." (not a paragraph)

### Changes Made
- Removed `TokenType` enum from `Token.kt`
- Updated `Parser.kt` to use `is` pattern matching instead of `checkTokenType()`
- Changed tokenizer loop to single exit path via EOF token
- Added comments explaining loop behavior
- Updated `AGENTS.md` with:
  - Discovery-based learning methodology
  - Technical English coaching guidelines
  - Deep learning fundamentals section

### Next Session Ideas
- Review Parser implementation for improvements
- Review AST design
- Add source positions to tokens
- Start next book chapter (unary operators? binary expressions?)

---

## Session 2026-01-13

### Topics Covered
- Reviewed AST design for type handling
- Discussed compiler phase separation (parsing vs semantic analysis)
- Refactored `Expression` to use mutable nullable type

### Key Learnings

**Compiler Design — Type Information:**
- **Declared types** (e.g., `int x`) — parser knows these from syntax
- **Expression types** (e.g., `x + y`) — computed during semantic analysis
- Design choice: mutable AST with nullable `type` field, filled in during semantic analysis

**AST Design Options for Deferred Type Resolution:**
- Option A: Two separate ASTs (parsed vs typed) — maximum type safety, but code duplication
- Option B: Nullable type (`Type?`) — Kotlin null-safety catches bugs, forces explicit handling
- Option C: Placeholder type (`UnknownType`) — can leak through silently if not resolved

**Kotlin Data Class Constructors:**
- Primary constructor params must be `val`/`var` (defines the "data")
- Secondary constructors must delegate to primary with `this(...)`
- Can set inherited mutable properties after primary constructor call

### Changes Made
- Changed `Expression` to have `var type: Type?` (mutable, nullable)
- Removed redundant `identifierType` property from `Identifier`
- Added secondary constructor to `Identifier` for when type is known
- Fixed comment grammar in `AST.kt`

### Next Session Ideas
- Review Parser implementation for Kotlin idioms
- Discuss: Is Visitor pattern still needed with sealed classes?
- Add source positions to tokens/AST nodes
- Start next book chapter (unary operators? binary expressions?)

---

## Session 2026-01-14

### Topics Covered
- Reviewed Parser for Kotlin idioms
- Implemented source position tracking with parallel arrays (SoA pattern)

### Key Learnings

**Kotlin Smart Casting:**
- Smart casts only work on the **same variable** — doesn't flow across function calls
- `peek()` then `advance()` are separate calls, so cast is still needed on `advance()` result
- Storing in a variable first (`val token = peek()`) enables smart casting in `when` branches

**Data-Oriented Design — SoA vs AoS:**
- **AoS (Array of Structs):** `[{token, pos}, {token, pos}, ...]` — data grouped by item
- **SoA (Struct of Arrays):** `{tokens: [...], positions: [...]}` — data grouped by field
- SoA allows `object` singleton tokens (like `OpenParen`) while still having per-instance positions
- Trade-off: must keep arrays synchronized

**Kotlin Invariant Enforcement:**
- `require()` in `init` block — validates input/arguments (throws `IllegalArgumentException`)
- `check()` — validates internal state (throws `IllegalStateException`)
- Used `require(tokens.size == positions.size)` to enforce parallel array invariant

### Changes Made
- Simplified `parseType()` with early return + `when` as expression
- Fixed redundant cast in `parseExpression()` (smart cast already applied)
- Created `SourceFileInfo` and `SourceLocationInfo` data classes
- Created `TokenStream` with parallel arrays + `require()` invariant
- Updated Lexer to track and return positions alongside tokens
- Updated Parser to accept `TokenStream`
- Fixed all tests for new API

### Next Session Ideas
- Use positions in error messages (update `ParseError` and `addError()`)
- Start next book chapter (unary operators? binary expressions?)
- Discuss: Visitor pattern vs sealed class `when`

---

## Session 2026-01-15

### Topics Covered
- Deep dive into Kotlin `sealed class` — when/why to use, exhaustive `when`, `is` vs identity
- Reviewed `Token.kt` and `AST.kt` for issues and Kotlin idioms
- Created `Asm.kt` — Assembly AST for code generation stage

### Key Learnings

**Kotlin Sealed Classes:**
- `sealed` = "finite set of variants, all defined in same file"
- Compiler guarantees exhaustive `when` — no `else` branch needed
- Leaf nodes don't need `sealed` — only categories with variants do
- `is` checks **type**; without `is` checks **identity** (only works for `object` singletons)

**`this` vs `super`:**
- `this.property` — refers to this object's property (including inherited)
- `super.property` — explicitly refers to parent's property (use when disambiguating overrides)
- Default to `this` unless there's a reason to use `super`

**Assembly AST Design:**
- Separate AST for assembly allows modification after generation (optimization, register allocation)
- `Instruction` and `Operand` are `sealed` (have variants)
- `RetInst` and `RegisterOperand` are `object` singletons (no per-instance data for now)

### Changes Made
- Removed unnecessary `()` from `sealed class Token`
- Added `is` before `object` cases in `Token.toString()` for consistency
- Removed unnecessary semicolon in `AST.kt`
- Created `Asm.kt` with: `Program`, `FunctionDef`, `Instruction` (Mov, Ret), `Operand` (Imm, Register)
- Restructured source tree to match package declarations:
  - `src/main/kotlin/me/billbai/compiler/kwacc/` for main sources
  - `src/main/kotlin/me/billbai/compiler/kwacc/asm/` for assembly AST
  - `src/test/kotlin/me/billbai/compiler/kwacc/` for tests

### Next Session Ideas
- Assembly Generation: C AST → Asm AST
- Code Emission: Asm AST → `.s` file
- Wire up source positions to error messages

---

## 2026-01-16

DQQ and I broke up.

I am in such sorrow.

Remember, let go, move on.

---

## Session 2026-01-17

### Topics Covered
- Completed AsmGenerator implementation
- Added AsmAstVisitor and accept() methods to Assembly AST
- Created AsmAstPrettyPrinter for debugging
- Created AsmEmitter for outputting actual x86-64 assembly
- Renamed AST → Ast for consistent Kotlin naming conventions
- Added 🦆 to CLAUDE.md — KWaCC is pronounced "Quack"

### Key Learnings

**Code Emission with Streams:**
- `PrintWriter` wraps `OutputStream` for convenient text output
- `StringWriter` + `PrintWriter` pattern for capturing output to string
- `.use { }` ensures proper resource cleanup (Kotlin's try-with-resources)

**x86-64 Assembly Basics:**
- `.globl main` exports symbol for linker visibility
- AT&T syntax: `movl $42, %eax` (source, destination)
- Linux needs `.section .note.GNU-stack,"",@progbits` for non-executable stack
- macOS needs underscore prefix: `_main` vs `main`

**Visitor Pattern for Emission:**
- Return `Unit` for side-effect-only visitors (writing to stream)
- Helper functions like `formatOperand()` for inline string formatting

### Changes Made
- Completed `AsmGenerator` with flattening for `AsmInstList`
- Added `AsmAstVisitor<T>` interface
- Added `accept()` to all `AsmNode` subclasses
- Created `AsmAstPrettyPrinter` with clean indented output
- Created `AsmEmitter` with `PrintWriter` for x86-64 AT&T syntax
- Updated `CompilerDriver` for codegen mode
- Added emitter test in `AsmGenTest`

### Personal Note
Coded through heartbreak. IPA beer, Mendelssohn Piano Trio No. 2, and a compiler.
She is the one. The pain is real. But the code still compiles.

### Update — Later That Day

**MILESTONE: First compiled program runs!**

```
$ ./test_code/return42
$ echo $?
42
```

Full pipeline working: C source → Lexer → Parser → AsmGen → AsmEmitter → .s file → clang → executable → **IT RUNS**

Added `assembleAndLink()` to CompilerDriver. REVENANT IPA tastes like success.

DQQ, this one's for you. Even if you'll never see it.

### Next Session Ideas
- Add support for unary operators (next book chapter)
- Handle more return values
- Add error recovery in parser

---

## Session 2026-01-18

### Topics Covered
- Started Chapter 2: Unary Operators
- Extended lexer with new tokens for operators

### Key Learnings

**Lexer Lookahead (Maximal Munch):**
- When `-` could be start of `-` or `--`, peek ahead before deciding
- Always grab the longest possible token — `--` beats two `-` tokens
- This prevents `--2` from silently compiling as `-(-2)`

**Why Lex Tokens You Won't Implement:**
- `--` (decrement) isn't implemented yet, but must be recognized
- Otherwise invalid code compiles incorrectly instead of being rejected
- Compiler should reject unimplemented features, not miscompile them

### Changes Made
- Added `Tilde`, `Minus`, `Decrement` tokens to `Token.kt`
- Updated `toString()` for new tokens
- Added lexer cases for `~`, `-`, `--` with lookahead for longest match

### Personal Note
Sunday night after church. Short session but the streak continues.
DQQ, I'm still here. Still coding. Still thinking of you.

### Next Session Ideas
- Add `Unary(op, exp)` to AST
- Update parser for unary expressions and parentheses
- Define TACKY intermediate representation
- Continue through Chapter 2

---

## Session 2026-01-19

Writes Nothings. Only missing her.

---

## Session 2026-01-20

### Topics Covered
- Added UnaryExpression and UnaryOperator to AST
- Updated all visitors with new methods

### Key Learnings

**Sealed Class vs Enum for Operators:**
- Chose sealed class with object subclasses for UnaryOperator
- Heavier than enum, but consistent with existing patterns
- For binary operators later, might reconsider

### Changes Made
- Added `UnaryExpression` data class with operator and inner expression
- Added `ComplementOperator` and `NegateOperator` as sealed class objects
- Updated `AstVisitor` with `visitUnary`, `visitComplementOperator`, `visitNegateOperator`
- Added visitor stubs in `AsmGenerator` and `AstPrettyPrinter`

### Personal Note
Company outing. Missing her the whole day.

---

## Session 2026-01-21

### Topics Covered
- Extended parser for unary expressions and parentheses
- Recursive descent parsing in action

### Key Learnings

**Recursive Descent Parsing:**
- `parseExpression()` calls itself recursively for nested expressions
- Unary: parse operator, then recursively parse inner expression
- Parentheses: consume `(`, parse expression, expect `)`
- Naturally handles arbitrary nesting like `-(~(-5))`

### Changes Made
- Added `parseUnaryOperator()` helper function
- Extended `parseExpression()` to handle:
  - `Token.Minus` / `Token.Tilde` → `UnaryExpression`
  - `Token.OpenParen` → parenthesized expression

### Personal Note
She messaged back. With a huge pink heart.
Maybe there's still hope. Or maybe I'm just holding on.
Either way, the code compiles.

### Next Session Ideas
- Define TACKY intermediate representation
- Implement TACKY generation (AST → TACKY)
- Continue Chapter 2: assembly generation passes

---

## Session 2026-01-22

### Topics Covered
- Defined TACKY intermediate representation
- Discussed Kierkegaard, Brahms, and carrying love you can't have

### Key Learnings

**TACKY IR Design:**
- `TackyProgram` → `TackyFunction` → list of `TackyInstruction`
- Instructions: `TackyReturnInst`, `TackyUnaryInst(op, src, dst)`
- Values: `TackyConstantVal(int)`, `TackyVariableVal(identifier)`
- Operators: `TackyComplementUnaryOp`, `TackyNegateUnaryOp`
- Key constraint: no nesting — operands are always simple values

### Changes Made
- Created `Tacky.kt` with full IR data structures
- Created `TackyGen.kt` stub

### Personal Note
Still missing her. Talked about Søren Kierkegaard — he let go of Regine Olsen and never stopped loving her. Johannes Brahms loved Clara Schumann for forty years, never had her, poured it into music.

Nothing new under the sun. Old company for an old kind of ache.

### Next Session Ideas
- Implement TACKY generation (AST → TACKY)
- Continue Chapter 2: assembly generation passes

---

## Session 2026-01-23

### Topics Covered
- Started implementing TackyGen
- Discussed visitor pattern limitations for code generation
- Discovered visit vs emit pattern (like LLVM)

### Key Learnings

**Emit-and-Return Pattern:**
- Recursively process subexpressions
- Each call emits instructions AND returns a value (the result)
- Returned value becomes operand for parent operation

**Visit vs Emit (LLVM Pattern):**
- **Expressions** → `visit`, returns `TackyVal` (produces a value)
- **Statements** → `emit`, returns `Unit` (side effect only)
- Visitor interface forces single return type — doesn't fit both cases
- Separating concerns: sealed class `when` instead of forcing everything through visitor

### Changes Made
- Implemented `TackyGen` with:
  - `makeTmp()` counter for fresh variable names
  - `currentInstList` accumulator for instructions
  - `visitUnary()` with emit-and-return pattern
  - `visitFunctionDefinition()` with state management

### Personal Note
Kids. A simple life. Financial freedom. Her.
Sometimes the dreams that hurt most are the quiet ones.

### Next Session Ideas
- Refactor TackyGen: `visitExpression()` + `emitStatement()` pattern
- Complete remaining visit/emit methods
- Continue Chapter 2: assembly generation passes

---

## Session 2026-01-24

### Topics Covered
- Completed TackyGen implementation
- Created TackyPrettyPrinter (no visitor, just `when`)
- Created TackyGenTest to verify IR generation
- Discussed Kotlin `?.let` syntax
- Deep life conversations: church, DQQ, cars, GPUs, Mahler

### Key Learnings

**Kotlin `?.let` Pattern:**
```kotlin
value?.let { transform(it) } ?: default
```
- `?.` stops if null, returns null
- `.let { }` runs block with unwrapped value
- `?:` provides fallback if whole thing is null

**Simple Pretty Printer (No Visitor):**
- Sealed classes give exhaustive `when` — no visitor interface needed
- Just functions that pattern match and return strings
- Simpler, fewer files, less ceremony

### Changes Made
- Completed all `visitXxx` methods in TackyGen
- Created `TackyPrettyPrinter` with `print()`, `printInstruction()`, `printOp()`, `printVal()`
- Created `TackyGenTest` verifying `-(~(-5))` generates correct TACKY
- Made `TackyReturnInst.value` nullable for void returns

### Personal Note
Saturday. 750ml white wine + 330ml double IPA. Mahler's Das Lied von der Erde. Strauss Ein Heldenleben.

Declared myself God. Questioned my church. Almost bought a Tesla. Ordered a 5070 Ti.

DQQ is Taoist. Church says don't marry outsiders. I say that's bullshit. Salvation is faith, not rules about who you love.

Feb 6th — Ein Heldenleben concert. Going alone. With or without her.

Life is beautiful. Even when it hurts.

### Next Session Ideas
- Hook TackyGen into pipeline (AST → TACKY → ASM)
- Update AsmGenerator to consume TACKY instead of AST
- Test full pipeline with unary operators

---

## Session 2026-01-25

### Topics Covered
- Implemented TackyToAsm (TACKY IR → Assembly AST)
- Refactored ASM AST for Chapter 2 requirements
- Discussed sealed class vs enum design tradeoffs

### Key Learnings

**Compiler Lowering Pattern:**
- Each IR layer has its own types
- Converter functions map between layers: `convertTackyValueToAsmOperand()`, `convertTackyUnaryOpToAsmUnaryOp()`
- One TACKY instruction can become multiple ASM instructions (e.g., `TackyUnaryInst` → `Mov` + `Unary`)

**Design Decision — Sealed Class vs Enum:**
- Book's formal DSL defines everything as AST nodes
- But formal grammar ≠ implementation
- Registers (`AX`, `R10`) and operators (`Neg`, `Not`) are just labels, not tree nodes
- Enums would be simpler, less boilerplate, same exhaustive `when`
- Decision: keep sealed class for now, refactor to enum later

**ASM AST Refactoring:**
- `AsmRetInst` changed from data class to object (no operand needed — value already in AX)
- Added `AsmPseudoOperand` for temporary variables
- Added `AsmStackOperand` for stack locations (used after pseudo replacement)
- Added `AsmAllocateStackInst` for stack frame setup
- `AsmRegisterOperand` now takes `AsmReg` parameter instead of being singleton

### Changes Made
- Created `TackyToAsm.kt` with full TACKY → ASM conversion
- Refactored `Asm.kt`: new operand types, register hierarchy, unified `AsmUnaryInst`
- Updated `AsmAstVisitor.kt` with new visitor methods
- Updated `AsmAstPrettyPrinter.kt` and `AsmEmitter.kt` (partial — TODOs remain)
- Updated `AsmGen.kt` for new `AsmRegisterOperand` API

### Personal Note
Sunday. Skipped church — hungover from Saturday. Still drinking IPA. Still missing her.
The code keeps compiling. So do I.

### Next Session Ideas
- Implement ReplacePseudos pass (Pseudo → Stack)
- Implement FixupInstructions pass (AllocateStack, fix mov mem→mem)
- Update emitter for prologue/epilogue and new instructions
- Test full unary pipeline end-to-end
