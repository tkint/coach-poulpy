---
license: Apache-2.0
module: http4k-bridge-vertx
---

# http4k-bridge-vertx Reference

Bridge http4k handlers into Vert.x.

## Vert.x Handler

```kotlin
val handler: (RoutingContext) -> Unit = VertxToHttp4kHandler(myApp)
```

## Router Integration

```kotlin
// Add as fallback to a Vert.x router
router.fallbackToHttp4k(myApp)

// Which is equivalent to:
router.route("/*").handler(VertxToHttp4kHandler(myApp))
```

## How It Works

- Returns a `(RoutingContext) -> Unit` function
- Streams the request body via `QueueInputStream` with Vert.x backpressure — the request is paused immediately, then resumed chunk by chunk as the http4k handler reads from the stream
- The blocking http4k handler runs inside `ctx.vertx().executeBlocking()` so it does not block Vert.x's event loop
- Response bodies are streamed back via chunked `write()` calls rather than a single `end(buffer)`

## Gotchas

- **Streaming body**: The adapter wires Vert.x `dataHandler`/`endHandler`/`exceptionHandler` to a `QueueInputStream`. The handler receives the body as it arrives — it does not wait for the full body before being invoked. Handlers that call `bodyString()` will block until the stream ends, which is correct behaviour.
- **`executeBlocking`, not `blockingHandler`**: `fallbackToHttp4k` uses a standard `handler()` entry point with `executeBlocking()` internally, not `blockingHandler()`. The distinction matters for Vert.x ordering guarantees — results are delivered in order by default.
- **Chunked response**: Response bodies are written in chunks; the full body is never held in memory at once. Very large responses are safe.
