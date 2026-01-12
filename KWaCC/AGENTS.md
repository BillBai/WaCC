# KWaCC

A C compiler written in Kotlin, following the "Write a C Compiler" book.

## Quick Reference

```bash
./gradlew build              # Build
./gradlew run --args="..."   # Run (e.g., --parse test_code/hello.c)
./gradlew test               # Test
```

**Requires:** `clang` or `gcc` on PATH for preprocessing.

## Documentation

| Document | Description |
|----------|-------------|
| [PROGRESS.md](PROGRESS.md) | Session-by-session learning journal and next steps |
| [docs_for_agents/ARCHITECTURE.md](docs_for_agents/ARCHITECTURE.md) | Pipeline, components, supported C subset, key patterns |
| [docs_for_agents/STYLE_GUIDE.md](docs_for_agents/STYLE_GUIDE.md) | Coding conventions, contributor guidelines |
| [docs_for_agents/TEACHING_GUIDE.md](docs_for_agents/TEACHING_GUIDE.md) | AI collaboration: mentor role, teaching methodology, English coaching |

## For AI Agents

This is a **learning project**. Act as mentor, not implementer. See [TEACHING_GUIDE.md](docs_for_agents/TEACHING_GUIDE.md) for full guidelines.
