---
license: Apache-2.0
module: http4k-connect-amazon-iot
---

# http4k-connect-amazon-iot Reference

AWS IoT control plane client — Jobs and Stream operations.

## Client

```kotlin
val iot = Iot.Http(
    region = Region.of("us-east-1"),
    credentialsProvider = { AwsCredentials("accessKeyId", "secretKey") },
    http = JavaHttpClient()   // optional
)

// or from environment
val iot = Iot.Http(Environment.ENV)
```

The endpoint is derived from the region (`https://iot.<region>.amazonaws.com`) unless
`overrideEndpoint` is passed.

## Jobs

```kotlin
val created = iot.createJob(
    jobId = JobId.of("firmware-update-1"),
    targets = listOf(ARN.of("arn:aws:iot:us-east-1:000000000000:thing/my-thing")),
    document = """{"operation":"firmware-update","url":"https://example.com/firmware.bin"}""",
    description = "Firmware update to 1.2.3",
    targetSelection = SNAPSHOT,
    timeoutConfig = TimeoutConfig(inProgressTimeoutInMinutes = 5)
).successValue()

val job = iot.describeJob(jobId).successValue().job
iot.cancelJob(jobId, comment = "no longer needed").successValue()
iot.deleteJob(jobId, force = true).successValue()
```

## Job Executions

```kotlin
val execution = iot.describeJobExecution(thingName, jobId).successValue().execution

val listed = iot.listJobExecutionsForThing(
    thingName,
    status = QUEUED,
    jobId = jobId
).successValue()

listed.executionSummaries.forEach { println(it.jobExecutionSummary.status) }
```

## Streams

```kotlin
val file = StreamFile(fileId = 0, s3Location = S3Location(bucket = "my-bucket", key = "image.bin"))

val created = iot.createStream(
    streamId = StreamId.of("ota-stream"),
    files = listOf(file),
    roleArn = ARN.of("arn:aws:iam::000000000000:role/stream-reader"),
    description = "a stream"
).successValue()

val info = iot.describeStream(streamId).successValue().streamInfo

iot.updateStream(streamId, files = listOf(replacement), description = "new file").successValue()
iot.listStreams().successValue()
iot.deleteStream(streamId).successValue()
```

## Certificates

```kotlin
val certificateId = CertificateId.of("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")

val description = iot.describeCertificate(certificateId).successValue().certificateDescription

description.status          // CertificateStatus: ACTIVE / INACTIVE / REVOKED / ...
description.certificateArn
description.validity?.notAfter
```

`CertificateId` is validated as 64 hex characters (the SHA-256 of the DER-encoded certificate).

## Endpoints

```kotlin
// resolve the account-specific data endpoint to feed to IotDataPlane
val endpoint = iot.describeEndpoint("iot:Data-ATS").successValue()
```

## Gotchas

- Requests are signed with the `iot` signing name — older SDKs used `execute-api`
- `document` is required on `createJob`: `documentSource` (S3-hosted documents), rollout,
  retry, abort and scheduling configs are not supported
- Duplicate `jobId` fails with `CONFLICT` (409)
- `deleteJob` on a live job fails with `CONFLICT` — pass `force = true` to remove it
- `createStream` with an empty file list fails with `BAD_REQUEST`
- `updateStream` replaces the whole file list and bumps `streamVersion`
- Job deletion is asynchronous on the real service — the job may briefly remain visible
- Use `http4k-connect-amazon-iot-fake` for testing, sharing its job storage with
  `FakeIotJobsDataPlane` for end-to-end Jobs workflows
