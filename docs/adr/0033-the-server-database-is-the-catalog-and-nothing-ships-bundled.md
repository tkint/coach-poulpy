---
status: accepted
date: 2026-11-15
---

# The server database is the catalog, and nothing ships bundled

Supersedes the bundled catalog of [ADR 0008](./0008-curated-catalog-with-stable-slugs.md), [ADR 0015](./0015-catalog-text-lives-in-translation-files.md), [ADR 0026](./0026-the-server-holds-the-catalog-as-a-draft-and-published-versions.md) and [ADR 0030](./0030-the-catalog-changes-by-exercise-revision-not-by-catalog-version.md) ([#451](https://github.com/tkint/coach-poulpy/issues/451)).

Until now the apps shipped a copy of the catalog (`catalog/data/exercises.json` plus the `catalog` translation files, merged and embedded at build time), the server filled its database from it on first start, and a phone seeded its own database from it. Since the admin ([#439](https://github.com/tkint/coach-poulpy/issues/439)) edits and publishes exercises, the files were a second source that nothing edited by hand and every release had to carry. The maintainer decided the database is the only source.

## The decision

- **The server database holds the catalog.** The admin creates, edits, publishes and retires exercises there. A fresh database starts with no exercises; nothing seeds it.
- **Phones start with no exercises** and download them, as before, from `GET /api/v1/catalog/exercises` (ADR 0030). A phone that has never downloaded the catalog, such as a first launch with no connection, shows no exercises until its first download. An empty state for that case is left to a later issue.
- **Phones that already hold exercises keep them.** Nothing removes what is in a phone's database.
- **The server's workout views read exercise names from the database**, the latest published revision of each exercise, not from a bundled catalog.
- **The `catalog` translation namespace and its merge tool are gone**: exercise text is stored in each exercise as a map from language to string, as the server and phones already hold it. Fink's catalog translations are made in the admin, not in pull requests.
- **Tests use `Catalog.sample`-like fixtures in code**: the `catalog-sample` module holds a frozen copy of the exercises as they were, and only test source sets depend on it.

## Consequences

- A release no longer carries catalog changes; a catalog fix reaches phones as soon as it is published.
- There is no catalog in the repository to review as a diff. The admin's publish review and the database are the record.
- The catalog is only as safe as the database backup; [the server deployment](../deploying-the-server.md) is where that is kept.
- A development database starts empty and needs exercises created in the admin, or a copy of a real database.
