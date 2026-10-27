# Translations

Coach Poulpy is written in English first and translated from there. French is the first translation. Every word in the app, from button labels and exercise instructions to Poulpy's lines, can be proofread or translated in a web editor, without installing anything or touching code.

## What you need

- A free [GitHub](https://github.com/signup) account.
- The [English rules and notes](/i18n/glossary/en): how messages are written, and what each unclear message means.
- The glossary of your language, if there is one: [français](/i18n/glossary/fr).

## Proofread or translate in Fink

[Fink](https://fink.inlang.com/) is the editor translators use. It shows every message in English beside your language, and warns you when a placeholder or a plural form is missing.

1. Open [Coach Poulpy in Fink](https://fink.inlang.com/?repo=tkint/coach-poulpy) and sign in with GitHub.
2. Pick your language and edit the messages you want to change.
3. Send your changes. Fink opens a pull request, which is a proposal the team reviews before anything reaches the app.

Your changes show up in the app's next build after the pull request is merged.

## A few things to know

- **Words in braces stay as they are.** `{name}` or `{count, number}` is filled in by the app. You can move it, but don't translate or rename it.
- **Plurals have one sentence per form.** Fink shows a field for each form your language needs, such as one and many in French. `#` stands for the number.
- **Type normally.** A plain apostrophe, three dots and ordinary spaces are fine. Typographic apostrophes, ellipses and your language's non-breaking spaces are added automatically after your changes are merged.
- **Poulpy is a character.** He speaks in the first person, warmly, and never makes anyone feel guilty. His lines are grouped under `poulpy.` in Fink. His voice is described on the [brand](/docs/brand) page.
- **Exercise texts come from the catalog.** Their messages start with the exercise's short name, like `goblet-squat.name`. Search aliases are extra words people might type when searching. They're never shown, so add the words people really use rather than translating the English list.

## Bring a new language

Open an [issue](https://github.com/tkint/coach-poulpy/issues/new) naming the language. The team adds it to Fink, and you can start translating from the English. Start a glossary for the language as you go: its tone (formal or informal), its typography and its key terms, like the [French one](/i18n/glossary/fr).

## How it works

Every message lives in one file per language and per part of the app (app screens, shared labels, exercise catalog). The Android and iOS apps are built from those files, and so will the website and the server. The decisions behind this are [ADR 0014](/docs/adr/0014-text-lives-in-one-json-file-per-language-and-namespace) and [ADR 0015](/docs/adr/0015-catalog-text-lives-in-translation-files). The files themselves are in the [repository](https://github.com/tkint/coach-poulpy/tree/main/i18n).
