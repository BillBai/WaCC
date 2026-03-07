# Book Reference

Look up content from the "Writing a C Compiler" book for the current chapter.

Arguments: chapter number (e.g., 6), optional: specific topic (e.g., "TACKY IR", "parsing", "assembly")

The book is a directory of markdown files (chapter1.md through chapter20.md, plus appendix-A.md and appendix-B.md).
Read the book path from LOCAL.md in the project root. If LOCAL.md doesn't exist, ask the user for the path and create it.

1. Read the requested chapter from the book
2. If a topic is specified, find the relevant section
3. Summarize the key points, expected AST/IR changes, and new language features
4. Relate it to the current KWaCC codebase — which files will need changes