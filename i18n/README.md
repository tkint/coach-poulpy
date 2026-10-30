# Translations

Every word a person reads lives here, one JSON file per language and namespace ([ADR 0014](../docs/adr/0014-text-lives-in-one-json-file-per-language-and-namespace.md), [ADR 0015](../docs/adr/0015-catalog-text-lives-in-translation-files.md)). English in `messages/en/` is the source.

- [`messages/<locale>/<namespace>.json`](messages): the messages, nested JSON whose path is the message id, in ICU MessageFormat.
- `project.inlang/settings.json`: the inlang project that [Fink](https://fink.inlang.com/) reads, through the next-intl plugin.
- `glossary/<locale>.md`: rules and vocabulary per language ([English](glossary/en.md), [français](glossary/fr.md)); the English one also holds the translator notes.
- `typography.mjs`: fixes apostrophes, ellipses and each language’s non-breaking spaces; it runs on `main` after every merge.

## Add or change a string

1. Add the message to `messages/en/<namespace>.json` and `messages/fr/<namespace>.json`, under the group of its screen or area: `app` for Android and iOS screens, `domain` for the words the shared Kotlin gives both apps, `catalog` for exercise text. Ids are `<group>.<camelCaseName>`; arguments are `{name}` for text, `{count, number}` for a whole number and `{count, plural, one {# step} other {# steps}}` for plurals.
2. Read it where it's used. The build generates everything else, nothing generated is committed, and it fails when a shipped language lacks the message or names other arguments:
   - **Android**: `R.string.<id with dots as underscores>` (`home.seeWorkout` is `R.string.home_seeWorkout`), or `R.plurals.…` for a plural, with arguments in order of first appearance in English.
   - **iOS**: the id followed by one format per argument, in the same order: `Text("plan.title \(name)")` reads `plan.title %@`, `L("player.step %lld %lld", index, count)` reads `player.step %lld %lld`.
   - **Shared Kotlin**: `DomainText.<id in camelCase>(language, …)`, for example `DomainText.countRounds(languageCode, 3)`. `Labels` and `PoulpyCoach` wrap these for the view models.
   - **Catalog**: `<slug>.name`, `<slug>.instructions.<n>`, `<slug>.variationNote` and the optional, comma-separated `<slug>.aliases`. Changing them raises the catalog `version`; CI fails a pull request that changes them without.
3. A message translators could misread gets a note in [the English rules and notes](glossary/en.md), never in the JSON.

On the phones a plural can only branch on its language's forms (`one`, `many`, `other`…); an exact branch like `=0` works in `domain` only.

## Proofread or translate in Fink

1. Open [Fink](https://fink.inlang.com/) and sign in with a free GitHub account.
2. Open the repository `tkint/coach-poulpy`; Fink finds `i18n/project.inlang`.
3. Pick the language, edit, and send the changes: Fink opens a pull request.

Read the [English rules and notes](glossary/en.md) and the glossary of your language ([français](glossary/fr.md)) first. Type plain apostrophes and spaces: the repository fixes the typography after each merge.

## Add a language

Add its folder under `messages/` with the three files, its locale to `locales` in `project.inlang/settings.json`, and `glossary/<locale>.md`.
