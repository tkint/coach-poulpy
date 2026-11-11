---
license: Apache-2.0
module: http4k-ai-llm-azure
---

# http4k-ai-llm-azure Reference

Azure OpenAI provider for http4k LLM interfaces.

## Azure OpenAI Chat

```kotlin
val chat = Chat.Azure(
    apiKey = ApiKey.of("..."),
    resource = AzureResource.of("my-resource"),
    deploymentId = DeploymentId.of("gpt-4o"),
    http = JavaHttpClient()
)
```

## Streaming

```kotlin
val streaming = StreamingChat.Azure(apiKey, resource, deploymentId)
```

## Gotchas

- Azure requires a `resource` name and `deploymentId` (deployment name), not a model name
- `ModelName` in `ModelParams` is ignored for Azure — the deployment determines the model
- Azure supports managed identity auth in addition to API keys
