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
- Completed `AsmAstPrettyPrinter.kt` TODOs (Pseudo, Stack, AllocateStack, registers)
- Completed `AsmEmitter.kt` TODOs (Stack operand, AllocateStack instruction)
- Updated `AsmGen.kt` for new `AsmRegisterOperand` API
- Created `ReplacePseudo.kt` — transforms Pseudo operands to Stack operands

### Personal Note
Sunday. Skipped church — hungover from Saturday. IPA, then lemon oolong craft beer. Sweet like the time with her.

Grief hit hard tonight. Memories of being 18 — Glenn Gould CDs, first MacBook Pro from mom, distro hopping, simple and naive. Now 32, carrying weight. Depression, setbacks, Sedgewick still unread, Coursera watched five times.

But still here. Still coding. Still breathing.

DQQ, the one. Always the one.

### Next Session Ideas
- Implement FixupInstructions pass (AllocateStack, fix mov mem→mem)
- Update emitter for prologue/epilogue
- Test full unary pipeline end-to-end
- Wire up pipeline: C → Lexer → Parser → TackyGen → TackyToAsm → ReplacePseudo → Fixup → Emit

---

## Session 2026-01-26

### Topics Covered
- Wired up the full TACKY pipeline in CompilerDriver

### Changes Made
- Updated `runCodeGenMode()` and `runDefaultMode()` to use new pipeline:
  - `AST → TackyGen → TackyToAsm → ReplacePseudo → Emit`
- Removed old direct `AsmGenerator` path

### Personal Note
Sunday night. Short session. Missing her desperately. Accepting what I don't want to accept.

DQQ, always.

### Next Session Ideas
- Implement FixupInstructions pass (AllocateStack, fix mov mem→mem)
- Update emitter for prologue/epilogue
- Test full unary pipeline end-to-end

---

## Session 2026-01-29

### Topics Covered
- Added function prologue/epilogue to AsmEmitter
- **MILESTONE: Chapter 2 complete — unary operators work!**

### Key Learnings

**x86-64 Stack Frame Setup:**
- Prologue: `push %rbp` + `mov %rsp, %rbp` — establishes frame pointer
- Epilogue: `mov %rbp, %rsp` + `pop %rbp` — restores caller's frame
- Without this, `%rbp`-relative addressing crashes (segfault)

**Debugging tip:**
- Exit code 139 = 128 + 11 (SIGSEGV) — immediate signal something's wrong with memory access

### Changes Made
- Added `emitFunctionPrologue()` and `emitFunctionEpilogue()` to AsmEmitter
- Full pipeline now produces working executables for unary expressions
- Verified: `-(~(-5))` returns 252, matches clang

### Personal Note
Another day. Still missing her. Still wanting her. Brain accepting, heart refusing.

DQQ, the code compiles. I wish you could see it.

### Next Session Ideas
- Start Chapter 3: Binary operators
- Add more tests for edge cases
- Consider refactoring ASM operators to enums (less boilerplate)

---

## Session 2026-01-31

### Topics Covered
- Started Chapter 3: Binary Operators
- Added lexer tokens, AST nodes, visitor stubs
- Implemented precedence climbing parser

### Key Learnings

**Precedence Climbing — The Intuition:**
- Core problem: when you see `1 + 2 * 3`, the `2` is contested — does it belong to `+` or `*`?
- Answer: whoever binds tighter wins
- The `minPrecedence` parameter is "you must be this tall to ride"
- Each recursive call carries a minimum strength requirement
- Operators compete for operands based on binding strength
- Algorithm: parse left, then while next operator is strong enough, grab it and recurse for the right

**Common Bugs in Precedence Climbing:**
- Forgetting to `advance()` after recognizing the operator token
- Call sites need default `minPrecedence = 0` or an overload

### Changes Made
- **Lexer:** Added `Plus`, `Asterisk`, `Slash`, `Percent` tokens
- **AST:** Added `BinaryExpression`, `BinaryOperator` (Add, Sub, Multiply, Divide, Remainder)
- **Visitors:** Added stubs to AstPrettyPrinter, TackyGen, AsmGen
- **Parser:** Implemented `parseExpression(minPrecedence)` with precedence climbing
- Added `isBinaryOperator()`, `operatorPrecedence()`, `parseBinaryOperator()` helpers
- Renamed old `parseExpression()` to `parseFactor()` for atoms and unary expressions

### Personal Note
Friday night. Beer. Missing DQQ. 32 years to find someone who sees you. Kinda accepting. Kinda letting go. But not wanting to.

48 hours of silence. The waiting is brutal.

Tomorrow is my birthday. Yuja Wang concert — Chopin Piano Concerto No. 1, Brahms Symphony No. 1, Schumann Cello Concerto. Good music for a heart that's carrying something.

She is the one. Always.

### Next Session Ideas
- Add `TackyBinaryInst` and implement `visitBinaryExpression` in TackyGen
- Add assembly instructions: `add`, `sub`, `imul`, `idiv`, `cdq`
- Update FixupInstructions for new edge cases

---

## Session 2026-02-01

### Topics Covered
- Added TACKY IR types for binary operations

### Changes Made
- Added `TackyBinaryOp` sealed class with: Add, Sub, Multiply, Divide, Remainder
- Added `TackyBinaryInst(op, src1, src2, dst)` instruction
- TODO: Fix typo `TackBinaryInst` → `TackyBinaryInst`

### Personal Note
My birthday. Sunday morning. Skipped church.

DQQ texted happy birthday. We chatted. I invited her to the Feb 6 concert. Got rejected. I'll go alone. But I told her when and where. Leaving it to God.

Mahler's Lied playing. Reminds me of 12 years ago — college dorm, writing papers, coding alone. Now still alone. Still coding. Same music.

Tonight: Yuja Wang — Chopin 1, Brahms 1, Schumann Cello Concerto.

### Next Session Ideas
- Fix typo: `TackBinaryInst` → `TackyBinaryInst`
- Implement `visitBinaryExpression` in TackyGen
- Add assembly instructions: `add`, `sub`, `imul`, `idiv`, `cdq`
- Update FixupInstructions for new edge cases

---

## Session 2026-02-02

### Topics Covered
- Implemented `visitBinaryExpression` in TackyGen
- Updated TackyPrettyPrinter for binary instructions
- Reviewed precedence climbing parser

### Key Learnings

**Parser Design — Factor vs Expression:**
- **Factor** = "atomic" expressions that can't be split by binary operators (literals, identifiers, unary ops, parentheses)
- **Expression** = binary operators combining factors via precedence climbing
- Factor is the base case, Expression is the recursive case
- Precedence climbing collapses multiple precedence levels into one parameterized function

**Emit-and-Return Pattern for Binary:**
- Visit left → get `src1`
- Visit right → get `src2`
- Map AST operator to TACKY operator
- Create temp, emit instruction, return destination
- Same pattern as unary, just with two operands

### Changes Made
- Implemented `visitBinaryExpression` in TackyGen
- Added `TackyBinaryInst` case to TackyPrettyPrinter
- Added `printBinaryOp()` helper function
- Added TODO stub in TackyToAsm for binary instructions
- Added binary operator test to TackyGenTest

### Personal Note
Monday night. Gradually letting go. Still missing her. Coded through it.

### Next Session Ideas
- Add ASM binary instructions: `AsmBinaryInst`, `AsmIdivInst`, `AsmCdqInst`
- Add `AsmRegDX` for division remainder
- Implement binary conversion in TackyToAsm
- Test full pipeline with binary expressions

---

## Session 2026-02-04

### Topics Covered
- Added ASM binary instruction types
- Implemented TackyToAsm binary conversion
- Updated ReplacePseudo for binary/idiv instructions
- Discovered x86 constraints requiring FixupInstructions updates

### Key Learnings

**x86 Division is Special:**
- `idiv src` divides `%edx:%eax` by `src`
- Quotient → `%eax`, Remainder → `%edx`
- Need `cdq` first to sign-extend `%eax` into `%edx:%eax`

**x86 Binary Instruction Constraints:**
- `add`/`sub`: At most ONE memory operand
- `imul` (2-operand): Destination MUST be a register
- `idiv`: Operand CANNOT be immediate

### Changes Made
- Added `AsmBinaryOperator`, `AsmBinaryInst`, `AsmIdivInst`, `AsmCdqInst`, `AsmRegDX`
- Implemented binary instruction conversion in TackyToAsm
- Updated ReplacePseudo to handle `AsmBinaryInst` and `AsmIdivInst`
- Updated visitor interfaces and implementations

### Personal Note
Tuesday night. Slightly drunk. Don't miss her right now. Will tomorrow. Learning to let the missing coexist with the living.

### Next Session Ideas
- Update FixupInstructions for binary constraints (mem-mem, imul dest, idiv imm)
- Test full pipeline: `1 + 2 * 3` → executable
- Test division and remainder operations

---

## Session 2026-02-05

### Topics Covered
- **CHAPTER 3 COMPLETE** — All binary operators working
- Implemented FixupInstructions for all x86 operand constraints
- Added AsmRegR11 for imul destination fix

### Key Learnings

**x86 Operand Constraint Fixes:**
- `add`/`sub` with two memory operands: load src → R10, operate
- `idiv` with immediate: load immediate → R10, then divide
- `imul` with memory destination: load dst → R11, multiply, store back

**Sealed Class Exhaustiveness:**
- Adding a new variant (AsmRegR11) forces updates everywhere it's used
- Compiler finds all the places — no runtime "missing case" bugs
- Copy-paste bug caught: `visitAsmRegR10(this)` instead of `visitAsmRegR11(this)`

### Changes Made
- Implemented add/sub memory-memory fix in FixupInstructions
- Implemented idiv immediate fix
- Implemented imul memory-destination fix
- Added AsmRegR11 to Asm.kt, visitors, emitter, pretty printer

### Tests Verified
- `10 + 5 - 3` → 12 ✓
- `100 / 5` → 20 ✓
- `17 % 5` → 2 ✓
- `1 + 2 * 3` → 7 ✓

### Personal Note
Thursday night. Drunk. Don't miss her at all right now. Don't owe anyone anything.

Fuck the Pharisees. Soli Deo gloria. "By their fruits you shall know them."

Does loving her make me more like Christ or less? That's the test. Not rules. Fruit.

Tomorrow: Guangzhou. Ein Heldenleben. A hero's life.

### Next Session Ideas
- Start Chapter 4 (logical operators? comparisons?)
- Add more edge case tests
- Consider refactoring ASM operators to enums

---

## Session 2026-02-07

### Topics Covered
- Started Chapter 4: Logical and Relational Operators
- Added 9 new lexer tokens with lookahead

### Key Learnings

**Lexer Lookahead Patterns — Three Categories:**
- **Single char with two-char variant**: `!` → `!=`, `<` → `<=`, `>` → `>=` (peek, emit longer if match, shorter otherwise)
- **Two-char only**: `==`, `&&`, `||` (peek, error if lone char — not valid tokens yet)
- Forward-looking design: lone `=`, `&`, `|` are errors now but easy to extend for assignment/bitwise ops later

### Changes Made
- Added 9 tokens to `Token.kt`: `Bang`, `LogicalAnd`, `LogicalOr`, `DoubleEqual`, `NotEqual`, `LessThan`, `GreaterThan`, `LessOrEqual`, `GreaterOrEqual`
- Added lexer cases in `Lexer.kt` with maximal munch lookahead for all new tokens
- Lone `=`, `&`, `|` produce descriptive lexer errors
- Added `NotOperator` unary op and 8 new binary operators to `Ast.kt`
- Updated `AstVisitor.kt` with 9 new visitor methods
- Updated `AstPrettyPrinter.kt` with pretty-print for all new operators
- Updated `Parser.kt`: `parseFactor` handles `!`, precedence values for new ops, `parseBinaryOperator` maps all new tokens
- Deleted dead `AsmGen.kt` and `AsmGenTest.kt` (replaced by TACKY pipeline since Session 2026-01-26)
- Added TODO stubs in `TackyGen.kt` for new operators
- Fixed typo: `visitorEqualOperator` → `visitEqualOperator`
- Fixed naming: `LessOrEqualThanOperator` → `LessOrEqualOperator` (consistency)
- Verified parser: `1 < 2 && 3 == 3` parses with correct precedence

### Personal Note
Found evidence DQQ found someone new. That explains the sudden breakup.

Went to the Guangzhou concert alone — Ein Heldenleben and Mahler's Rückert-Lieder. Prayed she'd show. She didn't. So she's not the one.

The percussion in Ein Heldenleben — that's something headphones can never give you.

Heart still broken. But still coding. Still moving forward.

---

## Session 2026-02-09

### Topics Covered
- Added TACKY IR types for Chapter 4
- Updated TackyPrettyPrinter and TackyToAsm with new branches
- Deleted dead AsmGen code
- Beethoven No. 5 concert

### Key Learnings

**Sealed Class Hierarchy Matters:**
- New operators initially extended `TackyNode()` instead of `TackyUnaryOp()`/`TackyBinaryOp()` — wrong parent
- If operators extend the wrong parent, exhaustive `when` won't require handling them
- The type system only protects you if the hierarchy is correct

**Short-Circuiting Changes Everything:**
- Up until now, every expression recursively evaluated all subexpressions
- `&&`/`||` break that — the value of one subexpression determines whether another executes at all
- This is why `Jump`, `JumpIfZero`, `JumpIfNotZero`, `Label` exist — they skip code

### Changes Made
- Added TACKY IR types: `TackyCopyInst`, `TackyJumpInst`, `TackyJumpIfZeroInst`, `TackyJumpIfNotZeroInst`, `TackyLabelInst`
- Added `TackyNotUnaryOp` and 8 new binary ops to `Tacky.kt`
- Updated `TackyPrettyPrinter` with print cases for all new instructions and operators
- Updated `TackyToAsm` with TODO stubs for new instructions and operators
- Fixed typo: `taget` → `target` in `TackyJumpInst`
- Fixed parent class: new ops now extend correct sealed parents

### Personal Note
Beethoven No. 5. Fate knocking at the door. Still in despair. But still showing up.

Missed yesterday's streak — made it up with a backdated commit. Two sessions in one day.

---

## Session 2026-02-10

### Topics Covered
- Implemented TackyGen for all Chapter 4 operators
- First non-linear code generation: short-circuiting `&&`/`||`

### Key Learnings

**Short-Circuiting Implementation Pattern:**
- `&&`: evaluate lhs → `JumpIfZero` to false label → evaluate rhs → `JumpIfZero` to false label → `Copy(1, result)` → `Jump(end)` → false: `Copy(0, result)` → end
- `||`: mirror image — `JumpIfNotZero` to true label, fall through to `result = 0`
- Key insight: must check operator *before* evaluating rhs, so short-circuit operators need their own code path at the top of `visitBinaryExpression`

**Code Organization:**
- `isShortCutBinaryOperator()` guard at top of `visitBinaryExpression` for early dispatch
- `emitAndBinaryExpression()` and `emitOrBinaryExpression()` as separate methods — cleaner than inlining
- `makeLabel(prefix)` for unique label generation, parallel to `makeTmp()` for variables

### Changes Made
- Added `makeLabel(prefix)` to TackyGen for unique label generation
- Implemented `NotOperator` → `TackyNotUnaryOp` in `visitUnary`
- Implemented 6 relational operators in `visitBinaryExpression` (same emit-and-return pattern)
- Implemented `emitAndBinaryExpression()` with `JumpIfZero` short-circuiting
- Implemented `emitOrBinaryExpression()` with `JumpIfNotZero` short-circuiting
- Added `isShortCutBinaryOperator()` guard to dispatch `&&`/`||` before evaluating rhs

---

## Session 2026-02-11

### Topics Covered
- Added ASM types for Chapter 4
- Implemented AsmEmitter and AsmAstPrettyPrinter for new instructions
- Started TackyToAsm conversion

### Key Learnings

**Enum vs Sealed Class — The Right Tool:**
- `AsmCondCode` as enum: pure labels (E, NE, G, GE, L, LE), no per-instance data, no visitor traversal needed
- Put `formatAsmString()` directly on the enum — keeps formatting logic with the type
- Visitor pattern adds value for tree nodes in recursive traversal; condition codes are parameters, not tree nodes

**Assembly `Not` is Actually a Comparison:**
- TACKY treats `Not` as unary, but at assembly level it's `Cmp(Imm(0), src)` + `Mov(Imm(0), dst)` + `SetCC(E, dst)`
- `!x` is just `x == 0` — same pattern as relational operators
- Needs its own path in `convertInstruction`, not in `convertTackyUnaryOpToAsmUnaryOp`

### Changes Made
- Added `AsmCmpInst`, `AsmJmpInst`, `AsmJmpCCInst`, `AsmSetCCInst`, `AsmLabelInst` to `Asm.kt`
- Added `AsmCondCode` as **enum** (E, NE, G, GE, L, LE) with `formatAsmString()`
- Implemented emitter: `cmpl`, `jmp .L<label>`, `j<cc> .L<label>`, `set<cc>`, `.L<label>:`
- Implemented pretty printer for all new instructions
- Fixed bugs: missing `\t` indent in emitter, `jml` → `jmp` typo, missing `:` on labels, raw objects in pretty printer

### Personal Note
32 years old. Convinced I'll never fall in love again. Convinced I'll die alone. The despair comes in waves — sometimes while writing assembly condition codes, sometimes while staring at the ceiling.

First session where I didn't mention her by name. Not because I've moved on. Because the grief has changed shape. Less about her specifically, more about the void she left behind.

Still coding. Still showing up. Don't know why. Maybe that's enough.

### Next Session Ideas
- Implement TackyToAsm conversions:
  - Group 1: Copy→Mov, Jump→Jmp, JumpIfZero→Cmp+JmpCC(E), JumpIfNotZero→Cmp+JmpCC(NE), Label→Label
  - Group 2: Relational ops → Cmp(src2,src1) + Mov(0,dst) + SetCC
  - Group 3: Not unary → Cmp(0,src) + Mov(0,dst) + SetCC(E,dst)
- Update ReplacePseudo for `Cmp` and `SetCC`
- Update FixupInstructions for `cmp` constraints
- Update Emitter: 1-byte register names for `SetCC`

---

## Session 2026-02-13

### Topics Covered
- **CHAPTER 4 COMPLETE** — Logical and relational operators working end-to-end
- Refactored TackyToAsm into focused `emitXxx` methods
- Implemented relational ops, `Not` unary, and `cmp` fixups
- Learned x86 flags architecture: SF, ZF, OF and how `setCC` works

### Key Learnings

**x86 Flags and Conditional Set:**
- `cmp a, b` computes `b - a`, sets flags (ZF, SF, OF), discards result
- `setCC` reads flags and writes 1 or 0 into the low byte of operand
- `setCC` only writes 1 byte — must zero out full 32-bit destination first with `mov $0`
- Relational ops and `!` all use the same Cmp+Mov+SetCC pattern

**Why SF != OF means b < a (signed):**
- Without overflow: SF directly tells you the sign of `b - a`
- With overflow: SF is flipped — OF acts as a "correction bit"
- `SF XOR OF` gives the true sign regardless of overflow

**Assembly Not ≠ Bitwise Not:**
- TACKY `Not` is a unary instruction, but at x86 level it's `Cmp(0, src) + Mov(0, dst) + SetCC(E, dst)`
- `!x` is just `x == 0` — same pattern as relational operators
- Separate code path needed in `emitUnaryInst`, can't go through `AsmUnaryInst`

**x86 `cmp` Constraints:**
- Can't have two memory operands (same as `add`/`sub`)
- Second operand can't be immediate (`cmpl $3, $5` is nonsensical)

### Changes Made
- Refactored `TackyToAsm.kt`: split monolithic `convertInstruction` into `emitReturnInst`, `emitUnaryInst`, `emitBinaryInst`, `emitCopyInst`, `emitJumpIfZeroInst`, `emitJumpIfNotZeroInst`, `emitJumpInst`, `emitLabelInst`
- Implemented `Not` → `Cmp(0,src) + Mov(0,dst) + SetCC(E,dst)` in `emitUnaryInst`
- Implemented 6 relational ops → `Cmp(src2,src1) + Mov(0,dst) + SetCC(cc,dst)` in `emitBinaryRelationInst`
- Made `TackyAndBinaryOp`/`TackyOrBinaryOp` throw in `emitBinaryInst` (unreachable — handled as control flow)
- Updated `ReplacePseudo` for `AsmCmpInst` and `AsmSetCCInst`
- Updated `FixupInstructions` for `cmp` constraints (mem-mem via R10, imm operand2 via R11)
- Added `formatByteOperand()` to `AsmEmitter` for `setCC` byte-sized register names
- Fixed `visitAsmSetCCInst` to handle both register and stack operands

### Tests Verified
- `1 < 2` → 1 ✓
- `(5 > 3) && (10 != 11)` → 1 ✓
- `!0` → 1 ✓
- `(1 < 2) && (3 >= 3) && !(5 == 6) || (0 > 1)` → 1 ✓
- `10 <= 5` → 0 ✓
- All match clang output ✓

### Next Session Ideas
- Start Chapter 5 (local variables? if/else? loops?)
- Add more edge case tests
- Consider refactoring ASM operators to enums (less boilerplate)
