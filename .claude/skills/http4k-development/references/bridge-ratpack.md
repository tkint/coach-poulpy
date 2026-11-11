---
license: Apache-2.0
module: http4k-bridge-ratpack
---

# http4k-bridge-ratpack Reference

Bridge http4k handlers into Ratpack.

## Ratpack Handler

```kotlin
val handler = RatpackToHttp4kHandler(myApp)
```

## Router Integration

```kotlin
// Add as fallback handler to a Ratpack router
router.fallbackToHttp4k(myApp)
```

## How It Works

- Wraps the http4k handler as a Ratpack `Handler`
- Streams the request body via a `Subscriber<ByteBuf>` backed by `QueueInputStream` — backpressure flows through the reactive chain so the handler can read the body as it arrives without buffering the entire payload
- The blocking http4k handler runs via `ratpack.exec.Blocking.get()` so it does not block Ratpack's event loop
- Responses are streamed back using `Streams.flatYield` and chunked writes

## Gotchas

- **Streaming, not buffering**: The adapter subscribes to the Ratpack reactive body stream. The http4k `Request` body is a `QueueInputStream`; reading it before the stream completes blocks until data arrives. Handlers that need the full body before responding will work correctly, but those that buffer the body via `bodyString()` will block until the upstream Ratpack stream closes.
- **Method validation**: Unsupported HTTP methods return `501 NOT_IMPLEMENTED`.
- **RequestSource**: Extracts `remoteAddress.host` and `remoteAddress.port` from the Ratpack request.
- **Chunked response**: Response bodies are streamed back in chunks via `Streams.flatYield`; the response is not held in memory before being sent.
