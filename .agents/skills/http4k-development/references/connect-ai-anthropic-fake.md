---
license: Apache-2.0
module: http4k-connect-ai-anthropic-fake
---

# http4k-connect-ai-anthropic-fake Reference

In-memory fake Anthropic server for testing.

## Setup

```kotlin
val fake = FakeAnthropicAI()
val client = AnthropicAI.Http(ApiKey.of("test-key"), ApiVersion._2023_06_01, fake)
```

`FakeAnthropicAI` also handles `countTokens`, `getModels`/`getModel`, message batches, and file upload/download/list/delete. Its backing stores are constructor params you can seed or inspect directly:

```kotlin
val fake = FakeAnthropicAI(
    completionGenerators = mapOf(AnthropicModels.Claude_Sonnet_5 to MessageContentGenerator.Echo),
    models = DEFAULT_ANTHROPIC_MODELS,   // Storage<ModelInfo>, pre-seeded with the Claude_* models
    batches = Storage.InMemory(),        // Storage<StoredBatch>
    files = Storage.InMemory()           // Storage<StoredFile>
)
```

Message batches complete synchronously — `createMessageBatch` returns a batch already in `ProcessingStatus.ended` with results available immediately.

## Convenience Client

```kotlin
val client = fake.client()   // creates AnthropicAI.Http with defaults
```

## Test Contracts

```kotlin
class FakeAnthropicAITest : AnthropicAIContract {
    private val fake = FakeAnthropicAI()
    override val anthropicAi = fake.client()
}
```

## Chaos Testing

```kotlin
fake.returnStatus(Status.SERVICE_UNAVAILABLE)
fake.behave()
```

## Gotchas

- Validates `x-api-key` and `anthropic-version` headers
- Extends `ChaoticHttpHandler`
- Use `WithRunningFake` for server-based integration tests
