---
license: Apache-2.0
module: http4k-ai-llm-typesafe
---

# http4k-ai-llm-typesafe Reference

`Decide` implementation (from `http4k-ai-llm-core`) backed by the TypeSafe AI "System One" API.

## Construction

```kotlin
val decide: Decide = Decide.TypeSafe(ApiKey.of("..."), http = JavaHttpClient())   // http optional

val response = decide(
    DecideRequest(
        "The export button does nothing",
        mapOf(
            "severity" to Question.Score("How severe is this?", listOf(Question.Score.Level("Cosmetic", "Appearance only"), Question.Score.Level("Fully blocked"))),
            "is_bug" to Question.YesNo("This describes a defect")
        ),
        JevModels.JevLatest
    )
).orThrow { error(it) }
```

## Testing

```kotlin
class FakeTypeSafeDecideTest : DecideContract {
    override val decide = Decide.TypeSafe(ApiKey.of("fake"), FakeTypeSafe())
    override val model = JevModels.JevLatest
}
```

Customise answers by passing an answerer lambda to `FakeTypeSafe { state, id, question -> ... }` (default `FirstCriterionAnswerer`). See `connect-ai-typesafe-fake`.

## Gotchas

- Text only: any non-`Content.Text` state returns `Failure(LLMError.Internal)`. Multiple text parts are joined with newlines.
- Score `Level`s are sent as criteria strings: `"label: description"` (or just `label`).
- `Question.YesNo` maps to TypeSafe's `Noul` question; the answer is `Answer.YesNo(probability)`.
