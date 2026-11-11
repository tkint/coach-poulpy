---
license: Apache-2.0
module: http4k-ai-llm-gemini
---

# http4k-ai-llm-gemini Reference

Google Gemini provider for http4k LLM interfaces.

## Chat

```kotlin
val chat = Chat.Gemini(
    apiKey = ApiKey.of("AI..."),
    http = JavaHttpClient()  // optional
)

val result = chat(ChatRequest(messages, ModelParams(ModelName.of("gemini-3.8-flash"))))
```

## Streaming Chat

```kotlin
val streaming = StreamingChat.Gemini(
    apiKey = ApiKey.of("AI..."),
    http = JavaHttpClient()
)
```

## Models

Common model names (see `GeminiModels`):
- `gemini-4-argon`
- `gemini-3.8-flash`
- `gemini-3.6-flash`
- `gemini-3.5-flash-lite`
- `gemini-3.1-flash-lite`

## Gotchas

- Get an API key from Google AI Studio (aistudio.google.com)
- Gemini uses a different message format internally — the adapter handles conversion
- `System` messages are converted to Gemini's `systemInstruction` field
- `topK` is supported via `ModelParams.topK`
