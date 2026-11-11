---
license: Apache-2.0
module: http4k-format-fory
---

# http4k-format-fory Reference

Apache Fory binary serialization. Unlike the JSON formats this produces bytes, not text, and
the default content type is `application/octet-stream`.

## Direct Use

```kotlin
val bytes: ByteArray = Fory.asBytes(myObject)
val back = Fory.asA<MyObject>(bytes)
val back = Fory.asA(bytes, MyObject::class)
val back = Fory.asA<MyObject>(inputStream)
```

## Message Bodies

```kotlin
import org.http4k.format.Fory.binary

// write and set the content type in one call
val request = Request(GET, "/").binary(myObject)

// read back
val myObject: MyObject = request.binary()
```

## Lenses

```kotlin
import org.http4k.format.Fory.auto

val bodyLens = Body.auto<MyObject>().toLens()

val response = Response(OK).with(bodyLens of myObject)
val myObject = bodyLens(request)

// websockets
val wsLens = WsMessage.auto<MyObject>().toLens()
```

## Custom Configuration

```kotlin
val fory = Fory.custom {
    value(MyValue)
    text(BiDiMapping({ MyType.parse(it) }, MyType::toString))
}

val bytes = fory.asBytes(obj)
```

## Gotchas

- Content type is `OCTET_STREAM`, not JSON — override with the `contentType` argument to
  `auto`/`autoBody` when a more specific binary type is wanted
- Class registration is not required by the standard config, and cross-language mode
  (`withXlang`) is off
- `Fory` is backed by a `ThreadSafeFory` and is safe to use concurrently from many threads
- Value types need registering via `Fory.custom { value(MyValue) }` before they roundtrip
  inside maps and other containers — the same registration story as the JSON formats
- Common JDK and http4k types (`Instant`, `UUID`, `Uri`, `Status`, `ZoneId`, `Locale`, ...)
  roundtrip out of the box via the standard mappings
- Binary payloads are not human-readable — prefer a JSON format for public APIs and debugging
