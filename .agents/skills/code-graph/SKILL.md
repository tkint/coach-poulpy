---
name: code-graph
description: Explore Coach Poulpy symbols, call paths, dependencies and change impact before modifying code.
---

# Explore the code graph

Follow [`docs/agents/code-graph.md`](../../../docs/agents/code-graph.md).

- Use the `codegraph_explore` MCP tool when available.
- Otherwise use `codegraph explore "<question>"` or `codegraph node <symbol|file>` in the repository shell.
- If the index is missing, initialize it with `codegraph init` when the tool is available.
- Ask focused questions about how a behavior works, where a symbol is used, which callers/tests depend on it, and the blast radius of a proposed change.
- Read the actual source and tests before editing. The graph describes code relationships, not product decisions.
- Read `CONTEXT.md` and relevant ADRs for domain vocabulary and decisions.
- Never commit `.codegraph/`.
