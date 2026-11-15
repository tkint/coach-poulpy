---
status: accepted
date: 2026-11-14
---

# The shared web components say their own words

The components of `web/ui` ([ADR 0023](./0023-the-websites-share-one-vue-and-daisyui-design-system.md)) used to receive every word as a prop: `close-label`, `show-label`, `previous-label`, the stepper's `less-label`. Each caller translated the same "Previous page" again (`web.json` held it twice, "Show password" three times), and a new caller could forget one and ship an unlabeled button.

Now a component that speaks for itself, such as the label of its own close, previous or clear button, says it from `i18n/messages/<locale>/ui.json`, the way the phones read their generated strings ([ADR 0014](./0014-text-lives-in-one-json-file-per-language-and-namespace.md)). `web/ui/src/i18n.ts` imports the JSON as it is, like the website's `t()`; no generator and no Node step is added, and CI already fails a locale that lacks a message.

## What it means

- A new namespace, `ui` (ids `ui.<camelCaseName>`), holds only words a component says by itself. Words about a screen's content stay in `app` and `web`, and domain words (muscles, equipment, body areas) stay in `domain`, which the body map will read for its area names.
- The language is the browser's when we ship it, English otherwise (`uiLocale`); `setUiLocale` switches it, and the workshop's toolbar uses it.
- The label props stay as optional overrides: a caller that needs other words for one case still passes them. Callers dropping them where the default says the same is a separate change.
- Text that depends on the page (a title, a field's label, a placeholder) remains a prop: the component can't know it.

## Considered options

- **Keep props**: nothing to build, but the duplication and the forgotten label stay.
- **Install a vue-i18n plugin**: one more runtime and an app-level setup for a dozen short messages; the website already reads the JSON directly.
- **Generate typed functions like `DomainText`**: worthwhile if the namespace grows past plain messages; plain lookups are enough while it has no arguments or plurals.
