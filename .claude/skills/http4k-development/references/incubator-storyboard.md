---
module: http4k-incubator-storyboard
license: http4k Commercial
---

# http4k-incubator-storyboard Reference

JUnit 5 extension that captures DOM snapshots during http4k web driver tests and renders them as an interactive HTML storyboard report.

## Core Pattern

Register `Storyboard` as a JUnit 5 extension and inject `RecordingWebDriver` as a test parameter:

```kotlin
class MyUiTest {

    @JvmField
    @RegisterExtension
    val storyboard = Storyboard(myHttpHandler)

    @Test
    fun `checkout flow`(driver: RecordingWebDriver) {
        driver.get("http://localhost/cart")
        driver.capture("Cart loaded")

        driver.findElement(By.id("checkout-btn")).click()
        driver.capture("After checkout click", "button triggers form submit")

        driver.get("http://localhost/confirmation")
        driver.capture("Confirmation page")
    }
}
```

After the test, `Storyboard` writes two files to `build/reports/http4k/storyboard/`:
- `<TestClass>.<methodName>.json` — raw story data
- `<TestClass>.<methodName>.html` — interactive HTML report with tile navigation

## Construction

```kotlin
Storyboard(
    http = myHttpHandler,                              // required
    outputDir = File("build/reports/http4k/storyboard"), // default
    clock = Clock.systemDefaultZone()                  // default
)
```

## RecordingWebDriver

`RecordingWebDriver` wraps `Http4kWebDriver` and records DOM snapshots on demand:

```kotlin
val driver = RecordingWebDriver(Http4kWebDriver(handler))

driver.get("http://localhost/page")
driver.capture("Page title")                 // notes default to ""
driver.capture("Page title", "extra notes")  // with optional notes

val frames: List<StoryFrame> = driver.frames()  // immutable copy
```

Capturing also happens automatically on `click()` via `RecordingWebElement` — clicking any element found via `findElement`/`findElements` records a frame named `"click [$selector]"`.

## Data Model

```kotlin
data class StoryFrame(val title: String, val notes: String, val dom: String)  // dom is base64-encoded HTML
data class Story(val title: String, val frames: List<StoryFrame>)
```

Render to HTML manually if needed:

```kotlin
val html: String = renderHtml(story)
val html: String = renderHtml(story, existingDataJson)  // avoids re-serializing
```

## Dependencies

```kotlin
// build.gradle.kts
testImplementation("org.http4k:http4k-incubator-storyboard")
// Transitively requires: http4k-core, http4k-format-moshi, http4k-testing-webdriver, http4k-template-freemarker
```

## Gotchas

- Each test method gets its **own independent** `RecordingWebDriver` instance — frame lists do not bleed across tests
- DOM snapshots are stored **base64-encoded** in `StoryFrame.dom` — decode before asserting on content
- `frames()` returns an **immutable copy** — mutations after calling `frames()` do not affect the returned list
- The extension requires JUnit 5 (`@RegisterExtension`) — not compatible with JUnit 4
- `@JvmField` is required on the extension field when using Kotlin; without it JUnit 5 cannot discover it
- Output directory is created automatically (`mkdirs()`) — no pre-creation needed
- This API is experimental and may change between releases
