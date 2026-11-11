---
license: Apache-2.0
module: http4k-connect-ai-anthropic
---

# http4k-connect-ai-anthropic Reference

Anthropic (Claude) API client — low-level connect actions for the Anthropic Messages API.

## Client

```kotlin
val anthropic = AnthropicAI.Http(
    apiKey = ApiKey.of("sk-ant-..."),
    apiVersion = ApiVersion._2023_06_01,  // required
    http = JavaHttpClient()               // optional
)
```

## Message Completion

```kotlin
val result = anthropic.messageCompletion(
    model = AnthropicModels.Claude_Sonnet_5_5,
    messages = listOf(Message.User("What is Kotlin?")),
    maxTokens = MaxTokens.of(1024),
    systemPrompt = SystemPrompt.of("You are helpful"),  // optional
    temperature = Temperature.of(0.7),                   // optional
    tools = listOf(myTool),                              // optional
    toolChoice = ToolChoice.Auto()                        // optional
)

val response = result.successValue()
println(response.content.first().text)
println(response.usage.input_tokens)
```

## Streaming

```kotlin
anthropic.messageCompletion(model, messages, maxTokens, stream = true)
    .successValue()
    .forEach { chunk -> print(chunk.delta?.text) }
```

## Message Builders

```kotlin
Message.User("Hello")
Message.System("System prompt")
Message.Assistant("Previous response")
```

## Content Types

```kotlin
// Text + image in same message — image sources are Base64, Url, or a previously uploaded File
Message.User(listOf(
    Content.Image(Source.Base64(Base64Blob.encode(bytes), MimeType.IMAGE_PNG)),
    Content.Text("What is in this image?")
))
Content.Image(Source.Url(Uri.of("https://example.com/dog.png")))
Content.Image(Source.File(fileId))

// Documents (PDFs, plain text) take the same source shapes, plus inline text blocks
Content.Document(DocumentSource.Url(Uri.of("https://example.com/report.pdf")), title = "Q4 report")
```

## Tools

```kotlin
// Custom tool — the model calls back with a tool_use block you answer via Content.ToolResult
Tool.User(ToolName.of("get_weather"), mapOf("type" to "object"), description = "Looks up the weather")

// Built-in, Anthropic-hosted tools — constructed via factory functions, not the constructor directly
Tool.bash()
Tool.webSearch(maxUses = 3, allowedDomains = listOf("example.com"))
Tool.webFetch()
Tool.codeExecution()
Tool.textEditor()
```

`Tool` is a sealed class (`Tool.User` for custom tools, `Tool.BuiltIn` for hosted ones) — there is no flat `Tool(...)` constructor.

## Tool Choice

```kotlin
ToolChoice.Auto()                      // model decides
ToolChoice.Any()                       // must use some tool
ToolChoice.Tool("get_weather")         // must use specific tool
ToolChoice.None                        // no tool use
```

## Extended Thinking

```kotlin
anthropic.messageCompletion(
    model, messages, maxTokens,
    thinking = Thinking.Enabled(budget_tokens = 4096)   // or Thinking.Adaptive(ThinkingDisplay.summarized), Thinking.Disabled
)
```

Responses carry thinking back as `Content.Thinking(thinking, signature)` or `Content.RedactedThinking(data)` blocks; streamed deltas arrive as `DeltaContent.ThinkingDelta`/`SignatureDelta`.

## Counting Tokens

```kotlin
val count = anthropic.countTokens(
    AnthropicModels.Claude_Sonnet_5_5,
    listOf(Message.User(Content.Text("how many tokens is this?")))
).successValue()

println(count.input_tokens)
```

## Models, Files & Message Batches

```kotlin
anthropic.getModels()                       // paged list of ModelInfo
anthropic.getModel(AnthropicModels.Claude_Sonnet_5_5)

val uploaded = anthropic.uploadFile(FileName.of("report.pdf"), inputStream, contentType).successValue()
anthropic.getFile(uploaded.id)
anthropic.listFiles()                       // paged
anthropic.downloadFile(uploaded.id)         // Success<InputStream>
anthropic.deleteFile(uploaded.id)

val batch = anthropic.createMessageBatch(
    listOf(BatchRequest(CustomId.of("req-1"), MessageCompletion(model, messages, maxTokens)))
).successValue()
anthropic.getMessageBatch(batch.id)
anthropic.listMessageBatches()              // paged
anthropic.getMessageBatchResults(batch.id).successValue()   // Sequence<MessageBatchResponse>
anthropic.cancelMessageBatch(batch.id)
anthropic.deleteMessageBatch(batch.id)
```

`GetModels`, `ListFiles` and `ListMessageBatches` implement `PagedAction` — use the standard http4k-connect paging support to walk all pages.

## Models

```kotlin
AnthropicModels.Claude_Opus_5_5
AnthropicModels.Claude_Sonnet_5_5
AnthropicModels.Claude_Haiku_4_5
```

## Gotchas

- Uses `x-api-key` header (not Bearer token)
- `anthropic-version` header required — pass via `ApiVersion._2023_06_01`
- System prompt is a top-level field, not a message
- `maxTokens` is **required** for all requests
- `Source` (image) and `DocumentSource` are sealed classes (`Base64`/`Url`/`File`, plus `Text`/`Blocks` for documents) — not a single data class
- `Tool` is sealed (`Tool.User` vs `Tool.BuiltIn`) — build hosted tools with the factory functions (`Tool.bash()`, `Tool.webSearch()`, etc.) rather than `Tool.BuiltIn(...)` directly
