# Translations

Every word a person reads lives here, one JSON file per language and namespace ([ADR 0014](../docs/adr/0014-text-lives-in-one-json-file-per-language-and-namespace.md), [ADR 0015](../docs/adr/0015-catalog-text-lives-in-translation-files.md)). English in `messages/en/` is the source.

- [`messages/<locale>/<namespace>.json`](messages): the messages, nested JSON whose path is the message id, in ICU MessageFormat.
- `project.inlang/settings.json`: the inlang project that [Fink](https://fink.inlang.com/) reads, through the next-intl plugin.
- `glossary/<locale>.md`: rules and vocabulary per language ([English](glossary/en.md), [français](glossary/fr.md)); the English one also holds the translator notes.
- `typography.mjs`: fixes apostrophes, ellipses and each language’s non-breaking spaces; it runs on `main` after every merge.
- `key-map.json`: each Android and iOS string key from before the move, mapped to its message id, for switching the apps over (#99).

## Proofread or translate in Fink

1. Open [Fink](https://fink.inlang.com/) and sign in with a free GitHub account.
2. Open the repository `tkint/coach-poulpy`; Fink finds `i18n/project.inlang`.
3. Pick the language, edit, and send the changes: Fink opens a pull request.

Read the [English rules and notes](glossary/en.md) and the glossary of your language ([français](glossary/fr.md)) first. Type plain apostrophes and spaces: the repository fixes the typography after each merge.

## Add a language

Add its folder under `messages/` with the three files, its locale to `locales` in `project.inlang/settings.json`, and `glossary/<locale>.md`.
