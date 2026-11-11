---
module: http4k-connect-openfeature-fake
license: Apache-2.0
---

# http4k-connect-openfeature-fake Reference

In-memory OFREP server for testing code that uses `http4k-connect-openfeature`. Implements the same HTTP contract as a real OpenFeature provider.

## Basic Usage

```kotlin
val fake = FakeOpenFeature()

// Seed a static flag value
fake[FlagKey.of("dark-mode")] = true
fake[FlagKey.of("greeting")] = "Hello"

// Get a client wired to the fake (no server port needed)
val client = fake.client()

val result = client(EvaluateFlag(FlagKey.of("dark-mode"), EvaluationContext()))
// result == Success(EvaluationSuccess(key=dark-mode, value=true, reason=STATIC))
```

## Conditional Rules

Use `rule(...).returns(...)` to return different values based on evaluation context:

```kotlin
fake.rule(FlagKey.of("experiment")) { ctx ->
    ctx.context["user"] == "alice"
} returns "treatment-a"

fake.rule(FlagKey.of("experiment")) { ctx ->
    ctx.context["plan"] == "premium"
} returns "variant-x"
```

The predicate receives the full `EvaluationContext` including all attributes.

## Rule Matching Priority

1. Rules are first filtered to those where `key` matches AND `matches` predicate returns `true`.
2. Of the matching candidates, non-`STATIC` rules are preferred over `STATIC` ones.
3. Within the same category, the first rule added wins.

```kotlin
// STATIC wins only if no conditional rule matches
fake[FlagKey.of("feature")] = false           // STATIC, catches all
fake.rule(FlagKey.of("feature")) { ctx ->
    ctx.context["beta"] == true
} returns true                                 // TARGETING_MATCH, takes priority when matched
```

## FlagRule

```kotlin
data class FlagRule(
    val key: FlagKey,
    val value: Any?,
    val matches: (EvaluationContext) -> Boolean = { true },
    val reason: Reason = TARGETING_MATCH
)
```

Rules can be inserted directly into `fake.rules` for full control:

```kotlin
fake.rules += FlagRule(FlagKey.of("rate"), 99, { ctx -> ctx.context["role"] == "admin" }, Reason.SPLIT)
```

## Bulk Evaluation

`EvaluateAllFlags` returns all keys present in `fake.rules`. Keys with no matching rule for the given context still appear in the result but with `null` value and `null` reason.

## Chaos

`FakeOpenFeature` extends `ChaoticHttpHandler`, enabling controlled failure injection for resilience testing:

```kotlin
val fake = FakeOpenFeature()
fake.start()   // triggers chaos mode if configured via ChaoticHttpHandler
```

## Running as a Standalone Server

The fake can serve on a real port for integration tests:

```kotlin
// In a test with lifecycle management:
val fake = FakeOpenFeature()
val server = fake.asServer(SunHttp(0)).start()
val client = OpenFeature.Http(Uri.of("http://localhost:${server.port()}"))

// Or use WithRunningFake (manages lifecycle):
class MyTest : WithRunningFake(::FakeOpenFeature) {
    // fake is available as `fake`
}
```

Default standalone port: `43778`.

## Gotchas

- **`fake.client()` uses in-process invocation**: No network socket is opened. The client sends requests directly to the fake handler. Safe for unit tests.
- **Bulk endpoint always returns 200**: `EvaluateAllFlags` returns `200 OK` even when no rules match. Individual results have `null` value when unmatched — check `EvaluationResult.errorCode`.
- **Single flag returns 404 on no match**: `EvaluateFlag` returns `404 NOT_FOUND` with `FLAG_NOT_FOUND` error code when no rule matches the key.
- **Rules are mutable and shared**: `FakeOpenFeature.rules` is a `MutableList`. In concurrent tests sharing a fake instance, modify rules before spawning threads or guard access.
- **Default reason for `set()` is `STATIC`**: `fake[key] = value` creates a `FlagRule` with `reason = STATIC` and a catch-all predicate. The `firstMatch` logic still prefers `TARGETING_MATCH` rules over `STATIC` ones.
