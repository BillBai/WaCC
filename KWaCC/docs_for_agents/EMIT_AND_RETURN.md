# The Emit-and-Return Pattern

A code generation pattern where recursive calls both **emit instructions** (side effect) and **return a value** (where the result lives).

## The Problem

Expressions are nested:

```c
return -(~(-5));
```

AST:
```
Return
  └── Negate
        └── Complement
              └── Negate
                    └── Constant(5)
```

You need to:
1. Generate code for the innermost part first
2. Use that result as input for the next level
3. Keep going up

## The Pattern

Each recursive call does **two things**:

1. **Emit** — adds instructions to a list (side effect)
2. **Return** — gives back *where the result lives* (a value)

## Example: IR Generator

```kotlin
fun visitExpression(expr: Expression): Value {
    return when (expr) {
        is ConstantExpression -> {
            // No emit, just return the constant
            ConstantValue(expr.value)
        }
        is UnaryExpression -> {
            // 1. RECURSE: get where inner result lives
            val srcVal = visitExpression(expr.innerExpression)

            // 2. EMIT: add instruction to list
            val dstVal = TempValue(makeTmp())
            instructions.add(UnaryInst(op, srcVal, dstVal))

            // 3. RETURN: tell parent where OUR result lives
            return dstVal
        }
    }
}
```

## Trace: `-(~(-5))`

| Call | Emits | Returns |
|------|-------|---------|
| `visit(Negate(Complement(Negate(5))))` | — | recurses first |
| `visit(Complement(Negate(5)))` | — | recurses first |
| `visit(Negate(5))` | — | recurses first |
| `visit(Constant(5))` | nothing | `Constant(5)` |
| back to `Negate(5)` | `tmp.0 = neg 5` | `tmp.0` |
| back to `Complement(...)` | `tmp.1 = not tmp.0` | `tmp.1` |
| back to outer `Negate(...)` | `tmp.2 = neg tmp.1` | `tmp.2` |

Final instructions:
```
tmp.0 = neg 5
tmp.1 = not tmp.0
tmp.2 = neg tmp.1
return tmp.2
```

## Why This Pattern?

- **Emit** handles the *side effect* (building instruction list)
- **Return** handles the *data flow* (where did the result go?)
- Together they flatten nested trees into linear instruction sequences
- LLVM's IRBuilder uses the same pattern

## Visit vs Emit (LLVM Convention)

- **Expressions** → `visit`, returns a value (produces a result)
- **Statements** → `emit`, returns Unit (side effect only)

This distinction matters because expressions always produce a value, statements don't.