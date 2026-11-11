---
license: Apache-2.0
module: http4k-connect-amazon-iotdataplane
---

# http4k-connect-amazon-iotdataplane Reference

AWS IoT data plane client — MQTT publishing, device shadows and retained messages.

## Client

```kotlin
// the IoT data endpoint is account-specific, so it cannot be derived from the region
val endpoint = Uri.of("https://000000000-ats.iot.us-east-1.amazonaws.com")

val iotDataPlane = IotDataPlane.Http(
    endpoint = endpoint,
    region = Region.of("us-east-1"),
    credentialsProvider = { AwsCredentials("accessKeyId", "secretKey") },
    http = JavaHttpClient()   // optional
)
```

Resolve the endpoint with `Iot.describeEndpoint("iot:Data-ATS")` from
`http4k-connect-amazon-iot`.

## Publishing

```kotlin
iotDataPlane.publish(
    topic = TopicName.of("http4k/example/topic"),
    payload = """{"message":"hello"}""".toByteArray()
).successValue()

// MQTT5 options
iotDataPlane.publish(
    topic = topic,
    payload = payload,
    qos = 1,
    retain = true,
    contentType = "application/json",
    payloadFormatIndicator = UTF8_DATA,
    userProperties = listOf("source" to "http4k")
).successValue()
```

## Device Shadows

```kotlin
iotDataPlane.updateThingShadow(
    thingName,
    """{"state":{"reported":{"level":3,"on":true}}}""".toByteArray()
).successValue()

val stored = iotDataPlane.getThingShadow(thingName).successValue().asShadowDocument()

// named shadows
iotDataPlane.updateThingShadow(thingName, state, ShadowName.of("config")).successValue()
iotDataPlane.getThingShadow(thingName, shadowName).successValue()

val names = iotDataPlane.listNamedShadowsForThing(thingName, pageSize = 2).successValue()

iotDataPlane.deleteThingShadow(thingName).successValue()
```

## Retained Messages

```kotlin
iotDataPlane.publish(topic, "retained payload".toByteArray(), qos = 1, retain = true).successValue()

val retained = iotDataPlane.getRetainedMessage(topic).successValue()
retained.payload?.decoded()

val page = iotDataPlane.listRetainedMessages(maxResults = 1, nextToken = null).successValue()
page.retainedTopics
```

## Connections

```kotlin
iotDataPlane.deleteConnection(ClientId.of("my-device")).successValue()
```

## Gotchas

- The endpoint is a required constructor argument — unlike other AWS clients it is not
  derivable from the region
- Shadow updates are a deep merge: a `null` value in `reported` deletes that key, and
  `version` increments on every accepted update
- A retained publish with an empty payload **clears** the retained message
- Topics may contain slashes and are still addressed as a single topic name
- `getRetainedMessage` and `getThingShadow` fail with `NOT_FOUND` when nothing is stored
- `listRetainedMessages`' `maxResults` is a maximum, not an exact page size — follow
  `nextToken` rather than assuming page sizes
