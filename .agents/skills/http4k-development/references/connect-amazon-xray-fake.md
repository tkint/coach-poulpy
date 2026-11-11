---
module: http4k-connect-amazon-xray-fake
license: Apache-2.0
---

# http4k-connect-amazon-xray-fake Reference

In-memory fake AWS X-Ray for testing trace retrieval.

## Setup

```kotlin
val fakeXRay = FakeXRay()
val client = fakeXRay.client()
```

## Custom Configuration

```kotlin
val traces = Storage.InMemory<StoredTrace>()

val fakeXRay = FakeXRay(
    traces = traces,
    region = Region.of("ldn-north-1"),
    clock = Clock.systemUTC()
)
```

## Test Contracts

```kotlin
class FakeXRayTest : XRayContract, FakeAwsContract {
    override val http = FakeXRay()
}
```

## Seeding Stored State

```kotlin
val traceId = TraceId.of("1-5759e988-bd862e3fe1be46a994272793")

fakeXRay.traces[traceId.value] = StoredTrace(
    traceId = traceId,
    startTime = Timestamp.of(1000),
    duration = 0.5,
    annotations = mapOf("order_id" to "abc"),
    segments = listOf(Segment(SegmentId.of("0123456789abcdef"), """{"name":"checkout-api"}""")),
)

fakeXRay.trace(traceId)   // read it back directly
```

## Chaos Testing

```kotlin
fakeXRay.returnStatus(Status.SERVICE_UNAVAILABLE)
fakeXRay.behave()
```

## Gotchas

- Extends `ChaoticHttpHandler`
- Only a narrow filter expression subset is evaluated: `annotation.<key> = "<value>"`. Anything
  else (service filters, fault/error filters, boolean combinators) is refused with
  `InvalidRequestException` rather than silently matched against every trace
- `getTraceSummaries` filters by `startTime` falling in `[StartTime, EndTime)` and paginates
  with a `NextToken` that's just a numeric offset into the sorted match list (page size 100)
- `batchGetTraces` refuses more than 5 trace ids with `InvalidRequestException`, matching the
  real service's limit
- Unrequested/unknown trace ids come back in `UnprocessedTraceIds` rather than causing a failure
- State lives in a `Storage<StoredTrace>` — pass your own `Storage.InMemory()` to seed or
  inspect traces directly via `fakeXRay.trace(traceId)`
