# Test Chapter

Run the book's test suite for a specific chapter to verify the compiler implementation.

Arguments: chapter number (e.g., 5), optional: stage (lex, parse, validate, or omit for full run)

Read the test suite path from LOCAL.md in the project root. If LOCAL.md doesn't exist, ask the user for the path and create it.

1. Build the compiler: `./gradlew install`
2. Run the book's test script: `<test_suite_dir>/test_compiler <project_root>/build/install/KWaCC/bin/KWaCC --chapter <N> [--stage <stage>]`
3. Report results clearly: which tests passed, which failed
4. For failures, hint at what compiler pass might need attention