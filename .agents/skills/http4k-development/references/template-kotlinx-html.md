---
module: http4k-template-kotlinx-html
license: Apache-2.0
---

# http4k-template-kotlinx-html Reference

Type-safe Kotlin DSL templates using the `kotlinx.html` library. Templates are defined in code, not files.

## Construction

```kotlin
val renderer: TemplateRenderer = KotlinxHtmlRenderer
```

`KotlinxHtmlRenderer` is a plain `object` — there's no `Templates` factory (no `CachingClasspath`/`HotReload` variants) since there's nothing to cache or reload; the DSL is compiled Kotlin code.

## Defining a ViewModel

Implement `HtmlViewModel` (a `ViewModel` subtype) and render using the `kotlinx.html` DSL against the given `TagConsumer`:

```kotlin
data class TodoList(val todos: List<String>) : HtmlViewModel {
    override fun TagConsumer<*>.render() {
        ul { todos.forEach { li { +it } } }
    }
}

data class Page(val name: String) : HtmlViewModel {
    override fun TagConsumer<*>.render() {
        html {
            head { title { +name } }
            body { h1 { +name } }
        }
    }
}
```

Emit a single root tag (`html { .. }`) for a full page, or any other tag (`div { .. }`, `ul { .. }`) for a fragment.

## Rendering

```kotlin
// Direct string
val html = KotlinxHtmlRenderer(Page("Todos"))

// To a Response
val response = KotlinxHtmlRenderer.renderToResponse(Page("Todos"))

// Via lens
val view = Body.viewModel(KotlinxHtmlRenderer, TEXT_HTML).toLens()
val response = view(TodoList(items), Response(OK))

// Into a WsMessage
val wsView = WsMessage.viewModel(KotlinxHtmlRenderer).toLens()
val message = wsView.create(TodoList(items))
```

## Composing Renderers

```kotlin
// Fall back to another renderer if the ViewModel isn't an HtmlViewModel
val combined = KotlinxHtmlRenderer.then(fallback)
```

## Gotchas

- **Auto-escaped text content**: Text added with `+"..."` is HTML-escaped automatically by `kotlinx.html` — `<script>alert('pwned')</script>` renders as `&lt;script&gt;alert('pwned')&lt;/script&gt;`. You don't need to escape content yourself, and there's no `unsafe { +rawHtml }` example in this module's tests — reach for `kotlinx.html`'s own unsafe-block API only if you deliberately need to bypass escaping.
- **Only `HtmlViewModel` is renderable**: Passing any other `ViewModel` throws `ViewNotFound`. Compose with `.then()` to fall back to another renderer for non-`HtmlViewModel` view models.
- **No template files, no caching variants**: Unlike other template modules, there's no classpath scanning, hot reload, or cache — the "template" is just Kotlin code, so recompiling your code is your hot reload.
