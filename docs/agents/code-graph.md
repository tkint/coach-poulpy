# Code graph

The environment every thread runs in has [CodeGraph](https://github.com/colbymchenry/codegraph) installed: a local index of the Kotlin, Swift and TypeScript code (symbols, calls, who depends on what). It answers "how does X work" or "what breaks if I change Y" in one call instead of many greps and file reads.

## Use it

- **Main thread**: call the `codegraph_explore` MCP tool with a question or a symbol or file name. It returns the relevant source, the call paths and the blast radius (callers and tests).
- **Subagents, or if the MCP tool is missing**: run `codegraph explore "<question>"` or `codegraph node <symbol|file>` in Bash. If the repo has no index yet, run `codegraph init` once (about 6 s here).
- Use it to find code and see impact before a change. Still read the files you edit, and still follow `CONTEXT.md` for naming. The graph knows code, not decisions: ADRs and docs stay the source of truth.
- Never commit `.codegraph/`; it ignores itself.

## Where it is set up

- **Install and index**: the environment's setup script installs `codegraph` and runs `codegraph init` in the repo folder, so every new thread starts with an index.
- **MCP server**: the environment's setup script registers it at user level with `claude mcp add --scope user codegraph -- codegraph serve --mcp`. That gives the main thread the `codegraph_explore` tool and keeps the index in sync as files change. `claude mcp list` should show `codegraph` as connected. If the tool is missing in a thread, say so and use the CLI.
- **Why not a repo `.mcp.json`**: a project-scoped server stays "Pending approval" until someone approves it, and a cloud thread cannot answer that prompt, even with `enabledMcpjsonServers` set. A user-level server needs no approval.
- **Copilot cloud agent**: `.github/workflows/copilot-setup-steps.yml` installs `codegraph` and runs `codegraph init`. It has no MCP server, so it uses the CLI.
- To change the version or the install, edit the environment, and the version in `copilot-setup-steps.yml`.
