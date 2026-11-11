---
module: http4k-connect-ai-typesafe-fake
license: Apache-2.0
---

# http4k-connect-ai-typesafe-fake Reference

In-memory fake of the TypeSafe AI API (`/v1/systemone`, `/v1/models`) for testing.

## Setup

```kotlin
val fake = FakeTypeSafe()
val typeSafe = fake.client()
// Or:
val typeSafe = TypeSafe.Http(ApiKey.of("ignored"), fake)
```

## Scripted Answers

By default the fake uses `FirstCriterionAnswerer` (favours the first criterion of each question; Noul answers 0.5). Supply a `QuestionAnswerer` to script answers, optionally based on the state:

```kotlin
val fake = FakeTypeSafe { state, id, question ->
    val case: SupportCase = state.asA()
    when (id) {
        QuestionId.of("refund_risk") -> Answer.Noul(Probability.of(if (case.orderId > 1000) 0.8 else 0.2))
        else -> FirstCriterionAnswerer(state, id, question)
    }
}
```

Custom models: `FakeTypeSafe(models = listOf(ModelCard(...)))`.

## Running as a Server

```kotlin
val port = fake.start().port()   // ChaoticHttpHandler
```

## Chaos Testing

`FakeTypeSafe` extends `ChaoticHttpHandler`:

```kotlin
fake.returnStatus(Status.SERVICE_UNAVAILABLE)
fake.behave()
```

## Gotchas

- The returned answer must match the question kind (`Choice`, `Score`, `Noul`), otherwise the client throws `WrongAnswerType`.
- Choice answers must use keys from the question's criteria; `Probability`/`Confidence` must be within 0.0..1.0.
- Throwing from the answerer for an unscripted question id is a handy way to fail tests on unexpected questions.
