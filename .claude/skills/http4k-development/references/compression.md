---
module: http4k-compression
license: Apache-2.0
---

# http4k-compression Reference

Adds Brotli and Zstandard `ContentEncoding`s for use with the negotiating `Compression` filters in `http4k-core` (which provides `Gzip()` and `Deflate()`).

## Usage

```kotlin
import org.http4k.core.Brotli
import org.http4k.core.ContentEncoding
import org.http4k.core.Zstd

// Server: encodings listed in server preference order; the best one the client accepts is used
val app = ServerFilters.Compression(ContentEncoding.Zstd(), ContentEncoding.Brotli(), ContentEncoding.Gzip())
    .then(routes)

// Client: compress requests with the first encoding, decompress responses in any of them
val client = ClientFilters.Compression(ContentEncoding.Brotli()).then(JavaHttpClient())

// SSE / HTTP + SSE (needs http4k-realtime-core)
ServerFilters.SseCompression(ContentEncoding.Zstd())
PolyFilters.Compression(ContentEncoding.Zstd(), ContentEncoding.Gzip())

ContentEncoding.Brotli(quality = 4)   // 0 (fastest) – 11 (smallest)
ContentEncoding.Zstd(level = 3)       // 1 (fastest) – 22 (smallest)
```

## Gotchas

- **Import the factories**: `Brotli()` and `Zstd()` are extension functions on `ContentEncoding.Companion` — import `org.http4k.core.Brotli` / `org.http4k.core.Zstd`.
- **Native libraries**: Brotli uses brotli4j (natives bundled for Linux, macOS and Windows on x64/aarch64) and Zstd uses zstd-jni; unsupported platforms fail at construction.
- **Zstd window limit**: Decoding enforces the 8MB window limit from RFC 9659 to bound decoder memory.
- **Order = server preference**: With equal client weights, the first encoding listed wins.
