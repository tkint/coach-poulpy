---
module: http4k-connect-openfeature
license: Apache-2.0
---

# http4k-connect-openfeature Reference

OpenFeature Remote Evaluation Protocol (OFREP) client. Evaluates feature flags via an HTTP API conforming to the OFREP spec.

## Construction

```kotlin
val client = OpenFeature.Http(
    baseUri = Uri.of("https://flags.example.com"),
    http = JavaHttpClient()   // optional — defaults to JavaHttpClient
)
```

## Actions

### Evaluate a Single Flag

```kotlin
val result: Result<EvaluationSuccess, RemoteFailure> = client(
    EvaluateFlag(
        key = FlagKey.of("dark-mode"),
        context = EvaluationContext(TargetingKey.of("user-123"))
    )
)
```

### Evaluate All Flags

```kotlin
val result: Result<BulkEvaluationSuccess, RemoteFailure> = client(
    EvaluateAllFlags(
        context = EvaluationContext("plan" to "premium", "region" to "eu")
    )
)
```

## EvaluationContext Construction

```kotlin
// Empty context
EvaluationContext()

// With targeting key only
EvaluationContext(TargetingKey.of("user-abc"))

// With targeting key and extra attributes
EvaluationContext(TargetingKey.of("user-abc"), "plan" to "pro", "region" to "us")

// Attributes only (no targeting key)
EvaluationContext("experiment" to "variant-b")
```

## Value Types

`FlagKey` and `TargetingKey` are non-blank string value types. Construct with `.of()`:

```kotlin
FlagKey.of("dark-mode")
TargetingKey.of("user-123")
```

## Caching

Wrap any `OpenFeature` client with TTL-based in-memory caching:

```kotlin
val cached = OpenFeature.Cached(
    delegate = client,
    ttl = Duration.ofMinutes(5),       // default: 1 hour
    clock = Clock.systemUTC()           // injectable for testing
)
```

Single-flag and bulk caches are separate. Cache keys incorporate the serialised evaluation context, so different contexts get independent cache entries.

Custom `Storage` implementations can replace the default in-memory maps:

```kotlin
OpenFeature.Cached(
    delegate = client,
    flagStorage = Storage.InMemory(),
    bulkStorage = Storage.InMemory(),
    ttl = Duration.ofHours(1)
)
```

## Serialisation

`OpenFeatureMoshi` handles JSON marshalling. It registers `FlagKey` and `TargetingKey` as value types. Use it if extending actions or building tooling:

```kotlin
OpenFeatureMoshi.asFormatString(context)   // EvaluationContext → JSON string
```

## Gotchas

- **FlagKey/TargetingKey construction**: Both reject blank strings — `FlagKey.of("")` throws. Prefer value type construction over passing raw strings.
- **`EvaluationSuccess.value` is `Any?`**: Cast to the expected type after evaluation. The OFREP protocol does not carry type information in the flag key.
- **Cached wraps, not replaces**: `OpenFeature.Cached` is a decorator; pass the underlying `Http` client as `delegate`. Both caches default to in-memory storage with no eviction beyond TTL — suitable for low-cardinality contexts only.
- **Distinct context = distinct cache entry**: Contexts are serialised to JSON to form cache keys. Large or high-cardinality contexts (e.g., per-request attributes) defeat caching.
- **`EvaluateAllFlags` 200 with partial errors**: The bulk endpoint returns 200 OK even when individual flags fail. Check `EvaluationResult.errorCode` on each result in `BulkEvaluationSuccess.flags`.
- **`CachedEvaluation` and `CachedBulkEvaluation` are Kotshi-serialisable**: If you provide a persistent `Storage<CachedEvaluation>`, these types must be reachable by the Kotshi adapter factory.
