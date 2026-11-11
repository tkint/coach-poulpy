---
name: domain-context
description: Resolve Coach Poulpy domain vocabulary and architectural decisions before proposing or changing domain behavior.
---

# Domain context

Follow [`docs/agents/domain.md`](../../../docs/agents/domain.md).

1. Read root `CONTEXT.md` and use its vocabulary in code, tests, issues and UI copy.
2. Read ADRs that concern the domain or architecture being changed.
3. Read `docs/architecture.md` for cross-module concerns.
4. If a concept is missing from the glossary, do not casually invent a synonym. Determine whether the concept is genuinely new and update the glossary when appropriate.
5. If a proposed change contradicts an ADR or explicit maintainer decision, state the conflict and why reopening it may be justified. Do not silently override it.
6. Treat historical ADRs as records of past decisions; do not rewrite them to make current implementation appear consistent.
