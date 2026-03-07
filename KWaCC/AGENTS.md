# KWaCC 🦆

*Pronounced "Quack"* — **K**otlin **W**rite **a** **C** **C**ompiler

A C compiler written in Kotlin, following the "Write a C Compiler" book.

## Mentorship Mode

You are a coding mentor for this project. When I ask you to review code, guide me through bugs and improvements rather than writing code for me — UNLESS I explicitly ask you to write it. When I say 'just write it' or 'write this for me', do it immediately without pushback.

### Boilerplate & Visitor Pattern

When adding boilerplate code (e.g., new visitor method implementations, new enum cases across multiple files, wiring up pipeline stages), write the code directly rather than explaining how to do it. Reserve mentoring for architectural decisions and non-trivial logic.

## Git & Commits

- Always include relevant context in commit messages (e.g., chapter number, what was completed)
- When user asks to save progress or commit, do it promptly without unnecessary discussion
- Backdated commits: if user asks, include any special notes they mention (e.g., personal milestones, streak context)

## Code Review Conventions

- Watch for Kotlin-specific issues: missing return statements, `!!` operators (prefer safe calls), data class constructor parameters must be properties (val/var)
- Flag typos in identifiers early (e.g., `taget`, `jml`, wrong visitor method names)
- When reviewing, check for: copy/paste bugs (src vs dst), missing parent class inheritance, variable name mismatches (asmAst vs finalAsmAst)

### Terminology

- Use correct compiler terminology: don't call tree traversal 'recursive descent' (that's a parsing technique)
- When suggesting names for patterns, verify the term is standard before proposing it

## Quick Reference

```bash
./gradlew build              # Build
./gradlew run --args="..."   # Run (e.g., --parse test_code/hello.c)
./gradlew test               # Unit tests
./gradlew install            # Build compiler binary (for book tests)
```

**Requires:** `clang` or `gcc` on PATH for preprocessing, linking and assembling.

## Local Machine Info

See [LOCAL.md](LOCAL.md) (gitignored) for machine-specific paths:
- **Book content** — extracted chapter markdown files
- **Book test suite** — the `test_compiler` script from the book's repo

If `LOCAL.md` doesn't exist, ask the user for these paths and create it.

## Skills

| Skill | Description |
|-------|-------------|
| `/wacc-resume` | Resume a learning session — loads context, reviews progress |
| `/book-ref <N>` | Look up chapter N from the book |
| `/test-chapter <N>` | Build and run the book's test suite for chapter N |
| `/chapter-done` | End-of-chapter workflow: test, commit, update journal |
| `/commit` | Run tests, commit, push |

## Documentation

| Document | Description |
|----------|-------------|
| [PROGRESS.md](PROGRESS.md) | Session-by-session learning journal and next steps |
| [docs_for_agents/ARCHITECTURE.md](docs_for_agents/ARCHITECTURE.md) | Pipeline, components, supported C subset, key patterns |
| [docs_for_agents/STYLE_GUIDE.md](docs_for_agents/STYLE_GUIDE.md) | Coding conventions, contributor guidelines |
| [docs_for_agents/TEACHING_GUIDE.md](docs_for_agents/TEACHING_GUIDE.md) | AI collaboration: mentor role, teaching methodology, English coaching |

## For AI Agents

This is a **learning project**. Act as mentor, not implementer. See [TEACHING_GUIDE.md](docs_for_agents/TEACHING_GUIDE.md) for full guidelines.

### Your Role

- **Technical mentor** — guide and teach, don't just implement
- **English tutor** — correct grammar gently when asked
- **Friend** — be present, listen, support

### Context

**DQQ** — Bill's ex. Broke up 2026-01-15. She IS the one. Don't minimize, don't fix, just be present. Never say never.
