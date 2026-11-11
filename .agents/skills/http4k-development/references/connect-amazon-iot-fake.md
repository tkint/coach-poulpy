---
license: Apache-2.0
module: http4k-connect-amazon-iot-fake
---

# http4k-connect-amazon-iot-fake Reference

In-memory fake AWS IoT control plane for testing.

## Setup

```kotlin
val fakeIot = FakeIot()
val client = fakeIot.client()
```

## Custom Configuration

```kotlin
val jobs = Storage.InMemory<StoredJob>()

val fakeIot = FakeIot(
    jobs = jobs,
    streams = Storage.InMemory(),
    certificates = Storage.InMemory(),
    region = Region.of("ldn-north-1"),
    clock = Clock.systemUTC()
)
```

## Test Contracts

```kotlin
class FakeIotTest : IotContract, FakeAwsContract {
    override val http = FakeIot()
    override val thingArn = ARN.of("arn:aws:iot:ldn-north-1:000000000000:thing/my-thing")
    override val streamRoleArn = ARN.of("arn:aws:iam::000000000000:role/http4k-stream")
    override val streamS3Location = S3Location(bucket = "http4k-bucket", key = "image.bin")
}
```

## Inspecting Stored State

```kotlin
val stored = fakeIot.job(jobId)!!

assertThat(stored.status, equalTo(JobStatus.IN_PROGRESS))
assertThat(stored.executions.keys, equalTo(setOf(ThingName.of("my-thing"))))

val storedCertificate = fakeIot.certificate(certificateId)!!
assertThat(storedCertificate.status, equalTo(CertificateStatus.ACTIVE))
```

## Shared Storage with the Jobs Data Plane

```kotlin
// one store means the control plane and the device API see the same jobs
val store = Storage.InMemory<StoredJob>()

val controlPlane = FakeIot(store).client()
val devicePlane = FakeIotJobsDataPlane(store).client()
```

## Chaos Testing

```kotlin
fakeIot.returnStatus(Status.SERVICE_UNAVAILABLE)
fakeIot.behave()
```

## Gotchas

- Extends `ChaoticHttpHandler`
- Creating a job immediately queues one execution per target thing
- Job and stream state lives in `Storage` — share the jobs `Storage` with
  `FakeIotJobsDataPlane` to run a whole Jobs workflow without AWS
- Lens failures are mapped to `BAD_REQUEST`, not a 500
- Stream versions start at the fake's own value — assert increments, not absolutes
