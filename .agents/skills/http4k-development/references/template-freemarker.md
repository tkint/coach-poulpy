---
license: Apache-2.0
module: http4k-template-freemarker
---

# http4k-template-freemarker Reference

FreeMarker template engine integration.

## Construction

```kotlin
// Recommended: use safeConfiguration() for HTML auto-escaping + standard file extension support
val templates = FreemarkerTemplates(FreemarkerTemplates.safeConfiguration())

val renderer = templates.CachingClasspath("org.example.views")
val renderer = templates.HotReload("src/main/resources/org/example/views")
```

`safeConfiguration()` preconfigures:
- `HTMLOutputFormat` as the default output format (auto-escapes `${x}` in HTML templates)
- `recognizeStandardFileExtensions = true` (`.ftlh`, `.ftlx` get per-template output format)
- `TemplateClassResolver.SAFER_RESOLVER` (blocks reflective class instantiation)
- API built-in disabled (prevents `?api` access to Java internals)

```kotlin
// Without safeConfiguration — XSS-vulnerable (${x} is NOT escaped)
val templates = FreemarkerTemplates(Configuration(Configuration.getVersion()))
```

## Template Syntax

```freemarker
<#-- Template: org/example/views/MyView (no extension in template name) -->
<h1>${name}</h1>
<ul>
  <#list items as item>
    <li>${item.name} - ${item.price}</li>
  </#list>
</ul>
```

## Gotchas

- **No file extension in template name**: FreeMarker resolves templates without an appended extension.
- **Direct property access**: ViewModel properties accessed directly (e.g., `${name}`), not via `model.` prefix.
- **Configuration-first**: Takes a FreeMarker `Configuration` object directly — use `FreemarkerTemplates.safeConfiguration()` for a secure default.
- **HotReload**: Sets `templateUpdateDelayMilliseconds = 0` to disable caching.
- **XSS by default**: A plain `Configuration` does NOT HTML-escape `${x}`. Always use `safeConfiguration()` for HTML templates, or set `outputFormat = HTMLOutputFormat.INSTANCE` explicitly.
