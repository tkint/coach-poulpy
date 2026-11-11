---
license: Apache-2.0
module: http4k-connect-amazon-sqs
---

# http4k-connect-amazon-sqs Reference

SQS client — connect actions for Amazon Simple Queue Service.

## Client

```kotlin
val sqs = SQS.Http(
    region = Region.of("us-east-1"),
    credentialsProvider = CredentialsProvider.Environment(),
    http = JavaHttpClient()   // optional
)
```

## Queue Operations

```kotlin
val queueUrl = sqs.createQueue(QueueName.of("my-queue")).successValue().QueueUrl
sqs.listQueues().successValue()
sqs.deleteQueue(queueUrl).successValue()
sqs.getQueueAttributes(queueUrl, listOf("All")).successValue()
```

## Send Messages

```kotlin
sqs.sendMessage(
    QueueUrl = queueUrl,
    MessageBody = "Hello, world!",
    DelaySeconds = 0,
    MessageAttributes = mapOf(
        "type" to MessageAttribute("String", "order.created")
    )
).successValue()

// Batch send
sqs.sendMessageBatch(
    QueueUrl = queueUrl,
    Entries = listOf(
        SendMessageBatchEntry("id-1", "Message 1"),
        SendMessageBatchEntry("id-2", "Message 2")
    )
).successValue()
```

## Receive Messages

```kotlin
val messages = sqs.receiveMessage(
    queueUrl = queueUrl,
    maxNumberOfMessages = 10,
    waitTimeSeconds = 20,           // long polling
    visibilityTimeout = 30,
    messageSystemAttributeNames = listOf(
        MessageSystemAttributeName.SentTimestamp,
        MessageSystemAttributeName.ApproximateReceiveCount
    )
).successValue()

messages.forEach { msg ->
    println(msg.Body)

    // Extend (or, with 0, end) how long the message stays invisible to other consumers
    sqs.changeMessageVisibility(queueUrl, msg.ReceiptHandle!!, visibilityTimeout = 0).successValue()

    // Delete after processing
    sqs.deleteMessage(queueUrl, msg.ReceiptHandle!!).successValue()
}
```

## Delete Messages (Batch)

```kotlin
sqs.deleteMessageBatch(
    QueueUrl = queueUrl,
    Entries = messages.map { DeleteMessageBatchEntry(it.MessageId!!, it.ReceiptHandle!!) }
).successValue()
```

## FIFO Queues

```kotlin
// Create FIFO queue (name must end in .fifo)
sqs.createQueue(
    QueueName.of("my-queue.fifo"),
    attributes = mapOf("FifoQueue" to "true")
).successValue()

// Send with deduplication
sqs.sendMessage(
    queueUrl, "Message",
    MessageGroupId = "group-1",
    MessageDeduplicationId = "dedup-123"
).successValue()
```

## Gotchas

- Uses JSON protocol (`application/x-amz-json-1.0`) with `X-Amz-Target` header
- `receiveMessage` returns up to 10 messages per call
- Use long polling (`WaitTimeSeconds > 0`) to reduce empty responses
- `ReceiptHandle` required to delete (not `MessageId`)
- MD5 checksums are auto-computed and validated
