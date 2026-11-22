# The catalog's shape

The rules an exercise in the catalog follows, for anyone changing the `catalog` module or the apps' filters. The terms are in the [glossary](../CONTEXT.md); the admin who edits and publishes the catalog is described in [the admin area](./admin.md#catalog), and phones download it exercise by exercise without an account ([ADR 0030](./adr/0030-the-catalog-changes-by-exercise-revision-not-by-catalog-version.md), [ADR 0033](./adr/0033-the-server-database-is-the-catalog-and-nothing-ships-bundled.md)).

A phone that has never downloaded the catalog, such as a first launch with no connection, shows no exercises yet. Tests use a frozen sample (`catalog-sample`).

A variation group is the slugs of one movement made easier or harder, ordered from easiest to hardest. An exercise is in at most one group, and a retired one stays in its group but isn't shown. Every exercise in a group carries a `variationNote` in every language, the one line the variations sheet shows under its name ("Knees down, a shorter lever").

An exercise's `equipment` lists what it needs, each need a list of alternatives: `[["DUMBBELL", "KETTLEBELL"]]` is a dumbbell or a kettlebell, `[["DUMBBELL"], ["BENCH"]]` is dumbbells and a bench. No need means bodyweight; `BODYWEIGHT` and `MAT` are never needs. Equipment that helps but isn't needed (a mat, a treadmill for a walk) goes in `optionalEquipment`. The Equipment filter shows an exercise when it uses something ticked, needed or optional, and every need has one alternative ticked; bodyweight-only exercises hide while anything is ticked, unless "No equipment" is.

An exercise's optional `aliases` are other names people search with, per language, which a language may leave out. The search reads them like names but never shows them, and an alias can't be empty.
