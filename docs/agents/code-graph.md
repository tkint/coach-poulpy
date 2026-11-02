# Code graph

The environment every thread runs in has [CodeGraph](https://github.com/colbymchenry/codegraph) installed: a local index of the Kotlin, Swift and TypeScript code (symbols, calls, who depends on what). It answers "how does X work" or "what breaks if I change Y" in one call instead of many greps and file reads.

## Use it

- **Main thread**: call the `codegraph_explore` MCP tool with a question or a symbol or file name. It returns the relevant source, the call paths and the blast radius (callers and tests).
- **Subagents, or if the MCP tool is missing**: run `codegraph explore "<question>"` or `codegraph node <symbol|file>` in Bash. If the repo has no index yet, run `codegraph init` once (about 6 s here).
- Use it to find code and see impact before a change. Still read the files you edit, and still follow `CONTEXT.md` for naming. The graph knows code, not decisions: ADRs and docs stay the source of truth.
- Never commit `.codegraph/`; it ignores itself.

## Where it is set up

In the environment's settings, not in the repo, so nothing to install per thread. To change the version or the setup, edit the environment.
