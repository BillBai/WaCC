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

### Next Session Ideas
- Assembly Generation: C AST → Asm AST
- Code Emission: Asm AST → `.s` file
- Wire up source positions to error messages
