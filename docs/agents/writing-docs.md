# Writing docs people can read

A page is good when a reader finds what they came for in under a minute and can stop reading at any point without missing something they needed. Every page in `docs/` follows these rules. Agents and humans alike.

## The rules

1. **Open with the point.** The first paragraph says what the page is about and why someone would read it, in two or three sentences. No history, no tracking issue.
2. **One idea per section.** The heading names the idea (`Who can open it`, not `Security`). If a section needs "also", it is two sections.
3. **Say it once, where it belongs.** Other pages link instead of repeating. Never copy what the code, the OpenAPI document or a test already holds: route lists, field lists and status codes go there, and the page links to them.
4. **Explain a term before using it.** Use the words of [`CONTEXT.md`](../../CONTEXT.md). If a word is new to the reader, define it in a clause at its first use, or link it.
5. **Write what and why, not how it is wired.** The reader wants behaviour and the reason for it. Class names and file names appear only when the reader will go looking for them.
6. **Keep paragraphs short.** Three sentences is typical, five is the limit. Use a bullet list for parallel items, a table for a lookup, prose for reasoning. No chains of clauses joined by semicolons.
7. **Fold the reference.** Detail that only some readers need goes in a `<details><summary>…</summary>` block, closed by default. It works on GitHub and on the site.
8. **One audience per page.** Rules and behaviour (business) go on one page, wiring and class names (technical) on another that links back. A reader should never need to skip lines to find theirs. On the site, business pages go in the **Product** menu and technical ones in **Technical** (`site/.vitepress/config.mts`).
9. **Leave out what isn't true yet.** No "what comes next" lists and no work-in-progress notes: those belong in issues and the [roadmap](../roadmap.md).

## Wording

Plain, active and specific: "The server refuses it" beats "It is refused". Name who does what. Prefer a short word to a long one. Write the way you would explain it to a colleague at a whiteboard.

## Before you finish a page

- Could someone say in one sentence what the page is for after reading its first paragraph?
- Is any paragraph longer than five sentences, or any page longer than about 150 lines? Split it or cut it.
- Is anything here also written somewhere else? Keep one, link the other.
- Is every route, status code or field list one the OpenAPI document already has? Delete it and link.

[The admin area](../admin.md) and [Admin internals](../admin-internals.md) are the worked example.
