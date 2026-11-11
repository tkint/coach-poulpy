---
module: http4k-ops-openfeature
license: Apache-2.0
---

# http4k-ops-openfeature Reference

Integrates OpenFeature feature flags into http4k server handlers. Provides a server filter that evaluates all flags in bulk on each request, lenses that extract typed flag values from the request, and an OpenFeature SDK provider for use with the standard OpenFeature Java SDK.

## Dependencies

Requires `http4k-connect-openfeature` for the `OpenFeature` client.

## Server Filter

`ServerFilters.PopulateOpenFeatureContext` evaluates all flags in bulk at the start of each request and attaches the results to the request for downstream lenses to read.

```kotlin
val openFeatureFilter = ServerFilters.PopulateOpenFeatureContext(
    client = openFeatureClient,
    toContext = { request ->
        EvaluationContext(
            TargetingKey.of(request.header("X-User-Id") ?: "anonymous"),
            "plan" to request.header("X-Plan")
        )
    }
)

val app = openFeatureFilter.then(myHandler)
```

The filter stores results in `OPENFEATURE_CONTEXT_KEY` on the request. If bulk evaluation fails, the filter recovers silently and attaches an empty flag map — lenses then return their defaults.

## Flag Lenses

Define lenses outside the handler (evaluated once, not per request):

```kotlin
val darkMode    = OpenFeatureFlag.boolean().defaulted("dark-mode", false)
val greeting    = OpenFeatureFlag.string().optional("greeting")
val maxItems    = OpenFeatureFlag.int().required("max-items")
val threshold   = OpenFeatureFlag.double().defaulted("threshold", 0.5)
val sessionMax  = OpenFeatureFlag.long().defaulted("session-max-ms", 3_600_000L)
```

Use lenses inside handlers:

```kotlin
val handler: HttpHandler = { request ->
    val enabled = darkMode(request)       // Boolean — falls back to false
    val msg     = greeting(request)       // String? — null if flag absent or error
    val limit   = maxItems(request)       // Int — throws LensFailure if absent
    Response(OK).body("Dark: $enabled, Limit: $limit")
}
```

`PopulateOpenFeatureContext` must be upstream of any handler that uses OpenFeature lenses.

## Lens Variants

| Method | Return type | Behaviour when flag absent/error |
|---|---|---|
| `defaulted(key, default)` | `T` | Returns `default` |
| `optional(key)` | `T?` | Returns `null` |
| `required(key)` | `T` | Throws `LensFailure` |

All variants support `.map { }` to transform the coerced value:

```kotlin
val maxItemsDoubled = OpenFeatureFlag.int().defaulted("max-items", 10).map { it * 2 }
```

## OpenFeature SDK Provider

`Http4kOpenFeatureProvider` bridges http4k's `OpenFeature` client to the standard OpenFeature Java SDK's `FeatureProvider` interface:

```kotlin
val provider = Http4kOpenFeatureProvider(openFeatureClient)

// Register with the OpenFeature SDK
OpenFeatureAPI.getInstance().setProvider(provider)
```

The provider converts SDK `EvaluationContext` to http4k's `ConnectEvaluationContext` (mapping `targetingKey` and all attributes). It supports `boolean`, `string`, `integer`, `double`, and `object` evaluation methods.

## OpenFeatureSnapshot

The filter attaches an `OpenFeatureSnapshot` to each request:

```kotlin
data class OpenFeatureSnapshot(
    val context: EvaluationContext,
    val flags: Map<FlagKey, EvaluationResult>
)

// Retrieve directly if needed
val snapshot: OpenFeatureSnapshot? = OPENFEATURE_CONTEXT_KEY(request)
```

## Testing

Use `FakeOpenFeature` from `http4k-connect-openfeature-fake` as the client:

```kotlin
val fake = FakeOpenFeature()
fake[FlagKey.of("dark-mode")] = true

val app = ServerFilters.PopulateOpenFeatureContext(fake.client()) { req ->
    EvaluationContext(TargetingKey.of(req.header("X-User") ?: "anon"))
}.then { request ->
    val enabled = darkMode(request)
    Response(OK).body(enabled.toString())
}

// No server needed — test the handler directly
assertThat(app(Request(GET, "/").header("X-User", "alice")).bodyString(), equalTo("true"))
```

## Gotchas

- **Filter must precede all flag lenses**: Lenses read from `OPENFEATURE_CONTEXT_KEY`. Without `PopulateOpenFeatureContext` upstream, `required` lenses throw `LensFailure` and `defaulted` lenses return their defaults.
- **Bulk evaluation, not per-lens**: The filter evaluates ALL flags once per request. Individual lenses only read from the cached snapshot — they do not trigger additional network calls.
- **Evaluation failures are silent**: If bulk evaluation fails (network error, provider unavailable), the snapshot contains an empty flags map. Design flag-gated code to tolerate all flags returning defaults.
- **Type coercion is strict**: `OpenFeatureFlag.int()` casts `EvaluationResult.value` as `Int`. If the flag value is a `Double` or `String`, the lens treats the flag as absent (returns default/null/throws). Ensure flag types in the provider match lens types in code.
- **SDK provider type coercion loses precision**: The `Http4kOpenFeatureProvider` converts all numeric attribute values to `Double` when mapping to http4k's context format. Attributes passed as `Long` or `Int` arrive as `Double`.
- **`OPENFEATURE_CONTEXT_KEY` is nullable**: Accessing the key before `PopulateOpenFeatureContext` runs returns `null`. Guard with `?.` or ensure the filter is always in the chain.
