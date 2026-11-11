---
license: Apache-2.0
module: http4k-connect-amazon-iotjobsdataplane
---

# http4k-connect-amazon-iotjobsdataplane Reference

AWS IoT Jobs data plane client — the device-side API for claiming and reporting job executions.

## Client

```kotlin
val device = IotJobsDataPlane.Http(
    region = Region.of("us-east-1"),
    credentialsProvider = { AwsCredentials("accessKeyId", "secretKey") },
    http = JavaHttpClient()   // optional
)
```

The endpoint defaults to `https://data.jobs.iot.<region>.amazonaws.com` and can be replaced
with `overrideEndpoint`.

## Polling for Work

```kotlin
val pending = device.getPendingJobExecutions(thingName).successValue()

pending.queuedJobs.map { it.jobId }
pending.inProgressJobs.map { it.jobId }

// $next is read-only: safe to poll on every connect
val next = device.describeJobExecution(
    thingName,
    JobId.NEXT,
    includeJobDocument = true
).successValue().execution
```

## Claiming and Completing

```kotlin
val started = device.startNextPendingJobExecution(
    thingName,
    statusDetails = mapOf("step" to "downloading"),
    stepTimeoutInMinutes = 10
).successValue().execution

device.updateJobExecution(
    thingName = thingName,
    jobId = JobId.of("firmware-update-1"),
    status = SUCCEEDED,
    statusDetails = mapOf("step" to "done"),
    expectedVersion = started?.versionNumber
).successValue()
```

## Gotchas

- `JobId.NEXT` (`$next`) is a reserved id addressing the device's next pending execution.
  `describeJobExecution` with it does **not** transition anything; `startNextPendingJobExecution`
  does
- A device may only set `IN_PROGRESS`, `SUCCEEDED`, `FAILED` and `REJECTED` — the other
  `JobExecutionStatus` values are set by the control plane
- `JobId` allows alphanumerics, `-`, `_` (max 64 chars) or the literal `$next`
- Requests are signed with the `iot-jobs-data` signing name, which is why the endpoint cannot
  be derived from the companion
- This module's `JobId`/`ThingName` are distinct types from the `http4k-connect-amazon-iot`
  ones — convert by value when crossing planes
- Pass `expectedVersion` for optimistic concurrency when several devices could update
