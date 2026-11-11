---
license: Apache-2.0
module: http4k-format-kotlinx-serialization
---

# http4k-format-kotlinx-serialization Reference

JSON format module backed by Kotlin's official `kotlinx.serialization` library. Provides auto-marshalling and JSON node manipulation with `JsonElement`.

## Construction

```kotlin
// Default singleton (includes withStandardMappings())
val json = KotlinxSerialization

// Custom configuration
val custom = ConfigurableKotlinxSerialization({
    ignoreUnknownKeys = true
    prettyPrint = false
    // any kotlinx.serialization JsonBuilder configuration
})
```

## Lens Integration

```kotlin
// Typed body lens
val lens = Body.auto<MyType>().toLens()
val obj: MyType = lens(request)
val response = Response(OK).with(lens of myObj)

// Raw JSON node lens
val jsonLens = Body.json().toLens()
val node: JsonElement = jsonLens(request)

// WebSocket
val wsLens = WsMessage.auto<MyType>().toLens()
```

## JSON Node Manipulation

Uses `kotlinx.serialization.json.JsonElement` as its node type.

```kotlin
// Parse and inspect
val node = KotlinxSerialization.parse("""{"name": "http4k"}""")
KotlinxSerialization.typeOf(node)         // JsonType.Object
KotlinxSerialization.fields(node)         // [("name", JsonPrimitive)]
KotlinxSerialization.textValueOf(node, "name") // "http4k"

// Construct and format
KotlinxSerialization {
    obj("name" to string("http4k"), "active" to boolean(true))
}
```

## Explicit KSerializer Overloads

When reified generics are unavailable (e.g., from Java, DI containers, or polymorphic serializers), use explicit `KSerializer<T>` overloads:

```kotlin
val serializer: KSerializer<MyType> = MyType.serializer()

// Serialize to string or InputStream
val str: String = KotlinxSerialization.asFormatString(serializer, myObj)
val stream: InputStream = KotlinxSerialization.asInputStream(serializer, myObj)

// Deserialize from string or InputStream
val obj: MyType = KotlinxSerialization.asA(jsonString, serializer)
val obj: MyType = KotlinxSerialization.asA(inputStream, serializer)

// BiDiMapping for lens/param construction
val mapping = KotlinxSerialization.asBiDiMapping(serializer)

// HTTP body lens with explicit serializer
val lens = KotlinxSerialization.autoBody<MyType>(serializer).toLens()
```

## Gotchas

- **`ignoreUnknownKeys = true` by default**: Unknown JSON fields are silently ignored in the default singleton.
- **BigInteger not supported**: `bigInteger()` mappings throw `UnsupportedOperationException`. Use `long()` or `text()` for large integers.
- **BigDecimal as string**: BigDecimal values are serialized as string content in `JsonPrimitive`, which may cause precision representation differences compared to Jackson/Gson.
- **`@Serializable` annotation required**: Unlike reflection-based libraries (Jackson, Gson), kotlinx.serialization requires `@Serializable` on data classes for auto-marshalling.
- **`parse` accepts top-level objects and arrays only**: `KotlinxSerialization.parse(...)` handles both `{...}` and `[...]` input. Any other top-level JSON value (e.g. a bare string or number) throws `SerializationException`.
