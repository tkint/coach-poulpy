---
license: Apache-2.0
module: http4k-connect-amazon-iotdataplane-fake
---

# http4k-connect-amazon-iotdataplane-fake Reference

In-memory fake AWS IoT data plane for testing.

## Setup

```kotlin
val fakeIotDataPlane = FakeIotDataPlane()
val client = fakeIotDataPlane.client()
```

## Custom Configuration

```kotlin
val fakeIotDataPlane = FakeIotDataPlane(
    messages = Storage.InMemory(),
    shadows = Storage.InMemory(),
    retainedMessages = Storage.InMemory(),
    region = Region.of("ldn-north-1"),
    endpoint = Uri.of("https://http4k-ats.iot.ldn-north-1.amazonaws.com"),
    clock = Clock.systemUTC()
)
```

## Test Contracts

```kotlin
class FakeIotDataPlaneTest : IotDataPlaneContract, FakeAwsContract {
    override val http = FakeIotDataPlane()
}
```

## Chaos Testing

```kotlin
fakeIotDataPlane.returnStatus(Status.SERVICE_UNAVAILABLE)
fakeIotDataPlane.behave()
```

## Gotchas

- Extends `ChaoticHttpHandler`
- Published messages are accumulated per topic in the `messages` storage, so a test can
  assert what a handler published
- Nothing is ever connected to the fake, so `deleteConnection` always answers `NOT_FOUND`
- Shadow merge semantics match the real service: `null` deletes a key and `version` increments
- The fake's default endpoint is region-derived, unlike the real account-specific endpoint
