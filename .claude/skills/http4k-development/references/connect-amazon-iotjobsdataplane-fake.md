---
license: Apache-2.0
module: http4k-connect-amazon-iotjobsdataplane-fake
---

# http4k-connect-amazon-iotjobsdataplane-fake Reference

In-memory fake AWS IoT Jobs data plane for testing.

## Setup

```kotlin
val fake = FakeIotJobsDataPlane()
val device = fake.client()
```

## Custom Configuration

```kotlin
val fake = FakeIotJobsDataPlane(
    jobs = Storage.InMemory(),
    region = Region.of("ldn-north-1"),
    clock = Clock.systemUTC()
)
```

## End-to-End Jobs Workflow

```kotlin
// one job store backs both fakes, so cloud and device see the same state
val store = Storage.InMemory<StoredJob>()

val controlPlane = FakeIot(store).client()
val devicePlane = FakeIotJobsDataPlane(store).client()

controlPlane.createJob(
    jobId = IotJobId.of("ota-1-2-3"),
    targets = listOf(ARN.of("arn:aws:iot:ldn-north-1:000000000000:thing/my-thing")),
    document = """{"operation":"firmware-update"}"""
).successValue()

val pending = devicePlane.getPendingJobExecutions(thingName).successValue()
devicePlane.startNextPendingJobExecution(thingName).successValue()
devicePlane.updateJobExecution(thingName, jobId, SUCCEEDED).successValue()
```

## Test Contracts

```kotlin
class FakeIotJobsDataPlaneTest : IotJobsDataPlaneContract, FakeAwsContract {
    override val http = FakeIotJobsDataPlane()
}
```

## Chaos Testing

```kotlin
fake.returnStatus(Status.SERVICE_UNAVAILABLE)
fake.behave()
```

## Gotchas

- Extends `ChaoticHttpHandler`
- Shares the `Storage<StoredJob>` type with `FakeIot` — pass the same instance to both to
  run a whole Jobs workflow without AWS
- The two modules use distinct `JobId`/`ThingName` types; convert by value when crossing planes
- Lens failures on `thingName` are mapped to `BAD_REQUEST`, matching AWS; the `jobId` stays a
  raw string so `$next` remains legal
