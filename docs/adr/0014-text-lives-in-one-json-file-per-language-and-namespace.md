---
status: accepted
date: 2026-10-26
---

# Text lives in one JSON file per language and namespace

Every word a person reads, on any surface, is written once in `i18n/messages/<locale>/<namespace>.json`. English is the source; French is the first translation. The namespaces are `app` (Android and iOS screens), `domain` (muscles, equipment, units and the other words the shared Kotlin used to hold in `Labels`), `catalog` (exercise text, see ADR 0015), and later `web` and `api`. Message ids are `<group>.<camelCaseName>`, the group being the screen or area (`library.sortPlanned`, `muscle.chest`), stored as nested JSON, so no id is the prefix of another; catalog ids follow ADR 0015. Plurals and placeholders are ICU MessageFormat with named arguments.

Until now a string lived in four places (Android `values` and `values-fr`, iOS `en.lproj` and `fr.lproj`) or in Kotlin code (`Labels`, the catalog's `{en, fr}` pairs), and a third language meant touching all of them. Native speakers who don't code could not proofread any of it. One file per language lets them work in [Fink](https://fink.inlang.com/), which reads the files through the inlang project in `i18n/project.inlang` and opens pull requests from a free GitHub account.

Android and iOS keep their native string files, but they are generated, never committed: a Gradle task (registered in `build-logic`, implemented in `tools/i18n`) turns the JSON into `strings.xml`, `.strings` and `.stringsdict` under `build/generated`, rewriting named ICU arguments as positional ones in order of first appearance. Xcode already runs Gradle to build the shared framework, so no build needs Node. The same task writes a Kotlin source holding the `domain` text, which the shared code looks up by id instead of the hand-written `Labels`, the way ADR 0012 generates the theme from one file.

The server words only what it sends itself (emails, notifications, exports) from the `api` namespace, and returns errors as codes the clients translate.

## Consequences

- A new string is added to `i18n/messages/en` and `i18n/messages/fr`; a new language is one folder of files plus its locale in the inlang settings.
- `LocalizedText` becomes a map from language to text that falls back to English, so the model no longer names its languages.
- Every message id carries a translator note, since Fink shows nothing else; per-language glossaries live in `i18n/glossary/<locale>.md`.
- CI fails when a message is missing in a locale the app ships.

## Considered options

- **Keep native files as the source**: no proofreading tool reads Android, iOS and the catalog together, and the web and server would need a fourth copy.
- **Read the JSON at runtime in shared Kotlin**: one code path, but plural rules and formatting would need an ICU implementation in Kotlin; the platforms already have one.
- **A Node generator beside `design/build-tokens.mjs`**: would put Node in the iOS and Android builds, which today need only the JDK.
