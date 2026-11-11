---
module: http4k-connect-amazon-xray
license: Apache-2.0
---

# http4k-connect-amazon-xray Reference

AWS X-Ray read client — trace retrieval only (`GetTraceSummaries` and `BatchGetTraces`).

## Client

```kotlin
val xray = XRay.Http(
    region = Region.of("us-east-1"),
    credentialsProvider = { AwsCredentials("accessKeyId", "secretKey") },
    http = JavaHttpClient()   // optional
)

// or from environment
val xray = XRay.Http(Environment.ENV)
```

The endpoint is derived from the region (`https://xray.<region>.amazonaws.com`) unless
`overrideEndpoint` is passed.

## Get Trace Summaries

```kotlin
val summaries = xray.getTraceSummaries(
    StartTime = Timestamp.of(now.minusSeconds(300)),
    EndTime = Timestamp.of(now),
    FilterExpression = """annotation.order_id = "my-order"""",
).successValue()

summaries.TraceSummaries.forEach { println(it.Id) }
summaries.NextToken   // page through with a further getTraceSummaries call
```

## Batch Get Traces

```kotlin
val traces = xray.batchGetTraces(
    summaries.TraceSummaries.take(5).map { it.Id }
).successValue()

traces.Traces.forEach { trace ->
    trace.Segments.forEach { println(it.Document) }
}
traces.UnprocessedTraceIds   // ids AWS didn't return a trace for
```

## Gotchas

- Unlike most other AWS JSON connect clients, requests are plain `application/json` POSTs to
  a resource path (`/TraceSummaries`, `/Traces`) — there is no `X-Amz-Target` header
- `batchGetTraces` accepts at most 5 trace ids per call; more than that fails rather than being
  batched automatically
- `StartTime`/`EndTime` go on the wire as whole epoch seconds
- `TraceId` is validated against `1-[0-9a-f]{8}-[0-9a-f]{24}` (epoch-second prefix in hex plus
  96 bits of hex) and `SegmentId` against `[0-9a-f]{16}` — malformed values throw
  `IllegalArgumentException` at construction
- Trace ids with no matching trace are reported via `UnprocessedTraceIds`, not as a failure
- Use `http4k-connect-amazon-xray-fake` for testing
