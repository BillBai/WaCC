# Debug ASM

Trace a C file through every compiler pipeline stage to diagnose codegen issues.

Arguments: path to a .c file (e.g., `test_code/ch5_var.c`)

1. Run each stage in order and display the output:
   - `./gradlew run --args="--lex <file>"` — token stream
   - `./gradlew run --args="--parse <file>"` — AST
   - `./gradlew run --args="--validate <file>"` — resolved AST (semantic analysis)
   - `./gradlew run --args="--codegen <file>"` — ASM AST + assembly text
2. For each stage, briefly note what looks correct vs suspicious
3. If all stages pass, compile to executable: `./gradlew run --args="<file>"`
4. Run the resulting executable and report the exit code: `./<basename without .c>`
5. Summarize: which stage (if any) introduced the bug, and what to investigate
