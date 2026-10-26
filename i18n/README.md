# Translations

Every word a person reads lives here, one JSON file per language and namespace (ADR 0014, ADR 0015). English in `messages/en/` is the source.

- `messages/<locale>/<namespace>.json`: the messages, nested JSON whose path is the message id, in ICU MessageFormat.
- `project.inlang/settings.json`: the inlang project that [Fink](https://fink.inlang.com/) reads, through the next-intl plugin.
- `glossary/<locale>.md`: rules and vocabulary per language; `glossary/en.md` also holds the translator notes.
- `key-map.json`: each Android and iOS string key from before the move, mapped to its message id, for switching the apps over (#99).

## Proofread or translate in Fink

1. Open [Fink](https://fink.inlang.com/) and sign in with a free GitHub account.
2. Open the repository `tkint/coach-poulpy`; Fink finds `i18n/project.inlang`.
3. Pick the language, edit, and send the changes: Fink opens a pull request.

Read `glossary/en.md` and the glossary of your language first.

## Add a language

Add its folder under `messages/` with the three files, its locale to `locales` in `project.inlang/settings.json`, and `glossary/<locale>.md`.
