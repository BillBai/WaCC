# Chapters 3 & 4 Recap and Blog Guide

## Part 1: Chapter Recap

### Chapter 3: Binary Operators — The Machine Fights Back

#### Big Ideas

**1. Precedence Climbing Parser**

The most important algorithm in Chapter 3. Collapses all binary operator precedence levels into a single parameterized function.

Core intuition: operators **compete for operands**. In `1 + 2 * 3`, the `2` is contested. `minPrecedence` is the bouncer — "you must bind at least this tightly to claim this operand." `*` (precedence 50) beats `+` (precedence 45), so `*` gets the `2`.

Key points:
- `parseFactor()` is the base case (atoms that can't be split by binary ops)
- `parseExpression(minPrecedence)` recurses with `currentPrecedence + 1`
- Left-associativity falls out naturally (the `+ 1` ensures left-to-right grouping)

**2. TACKY Binary Instructions — Emit-and-Return**

Same pattern as unary, but with two operands:
```
visit left → src1
visit right → src2
create temp, emit instruction, return temp
```

Every expression, no matter how complex, decomposes into: evaluate subparts, combine results, return new temporary.

**3. x86 Operand Constraints**

| Instruction | Constraint | Why |
|-------------|-----------|-----|
| `add`/`sub` | At most one memory operand | CPU can only do one memory access per instruction |
| `imul` (2-op) | Destination must be register | Hardware limitation of the multiply unit |
| `idiv` | Operand can't be immediate | Division circuit needs a register/memory source |

**4. idiv — The Weird One**

- Operates on a 64-bit dividend split across `edx:eax`
- `cdq` sign-extends `eax` into `edx:eax`
- Quotient → `eax`, Remainder → `edx`
- One instruction, two results

**5. FixupInstructions — The "Sorry About x86" Pass**

Exists solely because x86 has irregular operand constraints. Uses scratch registers R10 and R11 to rewrite illegal operand combinations.

#### Book Sections to Mark (Chapter 3)
- Precedence climbing algorithm
- x86 operand encoding constraints
- `idiv` and the division protocol (`cdq` + `edx:eax`)
- FixupInstructions pass description

---

### Chapter 4: Logical and Relational Operators — Your Compiler Learns to Think

#### Big Ideas

**1. Short-Circuiting — The Conceptual Leap**

The most important concept in Chapter 4. Before this, every expression evaluated ALL its subexpressions. `&&` and `||` break this:

```c
0 && expensive_function()   // must NOT call expensive_function
1 || expensive_function()   // must NOT call expensive_function
```

The compiler must generate code that conditionally skips evaluation. This requires labels, conditional jumps, and linear instruction sequences with branches.

Instruction sequence for `a && b`:
```
evaluate a → v1
JumpIfZero v1 → false_label     ← skip b entirely if a is false
evaluate b → v2
JumpIfZero v2 → false_label
Copy 1 → result
Jump → end
false_label:
Copy 0 → result
end:
```

**2. TACKY IR Control Flow Instructions**

Chapter 4 added the first control flow to the IR:
- `TackyCopyInst` — move a value
- `TackyJumpInst` — unconditional jump
- `TackyJumpIfZeroInst` / `TackyJumpIfNotZeroInst` — conditional jumps
- `TackyLabelInst` — jump targets

These are the building blocks of ALL future control flow (if/else, while, for).

**3. x86 Flags Architecture**

```
cmp a, b  →  computes b - a, sets flags, discards result
```

Three flags that matter:
- **ZF** (Zero Flag) — was the result zero?
- **SF** (Sign Flag) — was the result negative?
- **OF** (Overflow Flag) — did signed overflow occur?

**4. Why SF != OF Means b < a**

- Without overflow: SF directly tells you the sign. Negative → b < a.
- With overflow: SF is flipped — the sign is wrong.
- OF = 1 means "SF is lying"
- `SF XOR OF` (i.e., SF != OF) corrects for the lie, giving the true sign.

| Scenario | True result | SF | OF | SF != OF |
|----------|-------------|----|----|----------|
| `5 - 3 = 2` | positive | 0 | 0 | false → b >= a |
| `3 - 5 = -2` | negative | 1 | 0 | true → b < a |
| `-100 - 100 = -200` (overflow) | negative | 0 | 1 | true → b < a |
| `100 - (-100) = 200` (overflow) | positive | 1 | 1 | false → b >= a |

**5. The Cmp + Mov + SetCC Pattern**

Relational operators AND logical `!` all compile to:
```asm
cmpl  <operand1>, <operand2>   # compare, set flags
movl  $0, <dst>                # zero out destination (doesn't touch flags!)
setCC <dst>                    # write 0 or 1 into low byte based on flags
```

`setCC` only writes 1 byte (`%al`). Without zeroing first, upper 24 bits could be garbage.

**6. Assembly Not ≠ Bitwise Not**

- `notl %eax` — bitwise NOT (flips every bit). Used for `~`
- `!x` — logical NOT. At assembly level: `cmp $0, x` + `sete`. It's a comparison, not bitwise.

**7. Lexer Lookahead Patterns**

- Single with two-char variant: `!` → `!=`, `<` → `<=`
- Two-char only: `&&`, `||`, `==`
- Forward-looking errors: lone `=`, `&`, `|` are errors now but easy to extend later

#### Book Sections to Mark (Chapter 4)
- Short-circuit evaluation
- TACKY control flow instructions
- x86 condition codes and flags
- `setCC` and byte-sized operands
- Lowering `&&`/`||` to jumps

---

### The Arc: What Changed Between Chapter 2 and Chapter 4

| | Chapter 2 | Chapter 3 | Chapter 4 |
|---|-----------|-----------|-----------|
| **Parser** | Simple recursive descent | Precedence climbing | + new precedence levels |
| **Code gen** | Tree → linear | Same pattern, more instructions | Non-linear — jumps and labels |
| **TACKY IR** | Unary only | + Binary | + Control flow (jump, label, copy) |
| **x86 knowledge** | mov, ret, neg, not | add, sub, imul, idiv, cdq | cmp, setCC, jmp, jCC |
| **FixupInstructions** | Nothing | mem-mem, imul dest, idiv imm | + cmp mem-mem, cmp imm operand2 |
| **Key insight** | Compiler = tree transform | x86 is irregular and messy | Code gen can skip code |

---

## Part 2: Blog Writing Guide

### Blog 1: Precedence Climbing Algorithm

#### Suggested Structure

**Opening — The Problem**

Start with `1 + 2 * 3`. Naive left-to-right gives `(1 + 2) * 3 = 9`. Wrong. How does a parser know `*` binds tighter?

**The Naive Approaches (and why they're bad)**

1. One function per precedence level — works but 15 nearly-identical functions for C's 15 levels
2. Shunting-yard (Dijkstra) — explicit operator stack, harder to integrate with recursive descent

Sets up why precedence climbing is the sweet spot.

**The Core Insight — Operators Compete for Operands**

In `1 + 2 * 3`, the `2` is contested. `minPrecedence` says "only operators stronger than X can steal my right operand."

Visual diagram:
```
1  +  2  *  3
      ^
      contested!
   + (prec 45) wants it
   * (prec 50) wants it
   * wins → 2 belongs to *
```

**The Algorithm — Hand-Trace First**

Trace `1 + 2 * 3` step by step, showing each recursive call. Then show pseudocode — now it's just formalizing what the reader already understands.

**Left-Associativity — The `+ 1`**

Trace `1 - 2 - 3`:
- With `+ 1`: `(1 - 2) - 3 = -4` ✓
- Without: `1 - (2 - 3) = 2` ✗

For right-associative operators (like `=`), recurse with same precedence.

**Closing — The Elegance**

One function. One parameter. Adding a new operator = adding one table entry.

#### Key Points to Emphasize
- "Contested operand" metaphor
- Hand-trace before pseudocode
- The `+ 1` trick for associativity
- Show actual Kotlin code from KWaCC

---

### Blog 2: cmp/setCC Pattern in x86

#### Suggested Structure

**Opening — The Surprising Question**

"How does `a < b` become 0 or 1 in assembly?" There's no "compare and return boolean" instruction. x86 does it indirectly.

**The Flags Register — The CPU's Scratchpad**

Hidden single-bit flags updated as side effects of arithmetic:
- ZF: result is zero
- SF: result is negative
- OF: signed overflow

Flags are a **side channel** — the CPU does math and records metadata as a bonus.

**cmp — Subtraction in Disguise**

`cmp a, b` = `b - a`, set flags, throw away result. Show flag state for different inputs:
```
cmp $3, $5  →  5 - 3 = 2   →  ZF=0, SF=0, OF=0
cmp $5, $5  →  5 - 5 = 0   →  ZF=1, SF=0, OF=0
cmp $5, $3  →  3 - 5 = -2  →  ZF=0, SF=1, OF=0
```

**setCC — Reading the Flags**

Maps flag combinations to 0/1. Show the full condition code table.

**The Deep Part — Why SF != OF Means Less Than**

This is the star section. Most tutorials skip the "why."

- Without overflow: SF tells the truth
- With overflow: SF lies (sign flips). OF = 1 means "SF is lying"
- SF XOR OF = true sign. It's a 1-bit error correction code.

Use 8-bit examples to show overflow clearly:
- `-100 - 100 = -200` → stored as `+56`. SF=0 (wrong!), OF=1. SF != OF → true → b < a ✓
- `100 - (-100) = 200` → stored as `-56`. SF=1 (wrong!), OF=1. SF != OF → false → b >= a ✓

**The Full Pattern — Why mov $0 Before setCC**

```asm
cmpl  src2, src1     # set flags
movl  $0, dst        # zero ALL 32 bits
sete  dst            # write 1 byte (low 8 bits only)
```

`sete` only writes `%al`. Without `mov $0`, upper 24 bits = garbage. `mov` doesn't touch flags, so safe between `cmp` and `setCC`.

**Closing — The 1970s Design Philosophy**

One `cmp` sets flags, then `setCC`, `jCC`, or `cmovCC` can all read them. One comparison, many consumers. Elegant hardware multiplexing from the Intel 8080 era.

#### Key Points to Emphasize
- Flags register as a **side channel** concept
- `cmp` is subtraction that discards the result
- OF as a **correction bit** for SF — the "aha" moment
- The byte-size trap with `setCC`
- Historical context

---

### General Writing Tips

- **One "aha" per post.** Blog 1: "operators compete for operands." Blog 2: "OF tells you whether SF is lying."
- **Hand-trace examples before showing code.** Let the reader discover the pattern.
- **Diagrams help a lot** — precedence trees, flag state tables.
- **Show actual KWaCC compiler code** as real-world implementation.
