---
module: http4k-connect-ai-typesafe
license: Apache-2.0
---

# http4k-connect-ai-typesafe Reference

TypeSafe AI "System One" client — ask typed questions (choice, score, noul) about a piece of state and get typed answers with probabilities and confidence.

## Client

```kotlin
val typeSafe = TypeSafe.Http(
    apiKey = ApiKey.of(System.getenv("TYPESAFE_API_KEY")),
    http = JavaHttpClient()   // optional
)
```

Requests are sent to `https://api.typesafe.ai` with Bearer auth. All calls return `Result<R, RemoteFailure>` (use `successValue()` in tests/scripts).

## Questions

Three question types; the string factory forms take an `id`, `instructions` and criteria (any object — converted to JSON automatically):

```kotlin
val department = Question.Choice(
    "department", "Which team should handle this",
    mapOf("billing" to "Payment or subscription issues", "technical" to "Bugs or integration problems")
)
val frustration = Question.Score(
    "frustration", "How frustrated the customer appears",
    listOf("Calm", "Frustrated but civil", "Very angry")
)
val isUrgent = Question.Noul("is_urgent", "The message conveys urgency")
```

- `Choice` -> `Answer.Choice(choice, probabilities, confidence)`
- `Score` -> `Answer.Score(score, legend, confidence, probabilities?)`
- `Noul` -> `Answer.Noul(noul: Probability)`

## Asking

```kotlin
// single question, typed answer
val answer: Answer.Score = typeSafe.ask("this is the best product ever", frustration).successValue()

// batch of questions about one state
val answers = typeSafe.systemOne(complaint, department, frustration, isUrgent).successValue()
answers.answerTo(department).choice
answers.answerTo(isUrgent).noul

// models
typeSafe(GetModels).successValue().models
```

The `state` argument can be any object (data class, map, list, string) — it is serialised via `TypeSafeMoshi`. Pass `model = ...` to override the default model.

## Typed criteria

Use `Entry<T>()` to map criteria/legend nodes back to your own types:

```kotlin
data class Severity(val label: String, val description: String)
val impact = Question.Choice("impact", "How badly is the customer affected", levels.associateBy { it.label })

val response = typeSafe.systemOne(state, impact).successValue()
response.answerTo(impact).chosen<Severity>(impact)   // your Severity for the chosen key
Entry<Severity>()(response.answerTo(severity).legend) // Map<String, Severity>
node.asA<Severity>()
```

## Gotchas

- `Question` ids must be non-blank and unique within a `systemOne` call (questions are keyed by id).
- `Probability` and `Confidence` are value types restricted to 0.0..1.0.
- `answerTo(question)` throws `WrongAnswerType` if the API replies with a different answer type than the question kind.
- Question ids are `@Transient` on the wire; the id is only used to key the request/response.
- For testing use `http4k-connect-ai-typesafe-fake`.
