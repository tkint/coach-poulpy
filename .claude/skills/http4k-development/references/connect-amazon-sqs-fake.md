---
license: Apache-2.0
module: http4k-connect-amazon-sqs-fake
---

# http4k-connect-amazon-sqs-fake Reference

In-memory fake SQS server for testing.

## Setup

```kotlin
val fakeSqs = FakeSQS()
val client = fakeSqs.client()
```

## Custom Configuration

```kotlin
val fakeSqs = FakeSQS(
    queues = Storage.InMemory(),
    awsAccount = AwsAccount.of("123456789012"),
    region = Region.of("us-east-1"),
    deduplication = Storage.InMemory(),
    queueConfig = Storage.InMemory(),
    clock = Clock.systemUTC()
)
```

## Test Contracts

```kotlin
class FakeSQSTest : SQSContract {
    override val sqs = FakeSQS().client()
}
```

## Chaos Testing

```kotlin
fakeSqs.returnStatus(Status.SERVICE_UNAVAILABLE)
fakeSqs.behave()
```

## Gotchas

- Extends `ChaoticHttpHandler`
- `changeMessageVisibility` only checks that the queue exists — the fake doesn't track
  per-message visibility timeouts, so it has nothing else to change
- MD5 checksums validated on receive
- Queue URLs generated as `http://localhost:{port}/{account}/{name}`
- FIFO queues supported, including deduplication: a repeat `MessageDeduplicationId` within
  5 minutes returns the original `MessageId` instead of enqueuing again. Pass a controlled
  `clock` to test the window expiring
