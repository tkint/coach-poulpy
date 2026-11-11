---
license: Apache-2.0
module: http4k-testing-webdriver
---

# http4k-testing-webdriver Reference

Selenium WebDriver backed by an http4k `HttpHandler`. Test web UIs without starting a real server or browser — requests go directly through the handler.

## Construction

```kotlin
// Default — no page runtime behaviour
val driver = Http4kWebDriver(myApp)

// With a PageBehaviour (e.g., for datastar or custom JS simulation)
val driver = Http4kWebDriver(myApp, behaviour = myBehaviour)
```

## Navigation

```kotlin
driver.get("/page")
driver.navigate().to("/other-page")
driver.navigate().back()
driver.navigate().forward()
driver.navigate().refresh()

driver.currentUrl    // current page URL
driver.title         // page title from <title>
driver.pageSource    // raw HTML source
```

## Finding Elements

```kotlin
val element = driver.findElement(By.id("myId"))
val elements = driver.findElements(By.className("item"))
val link = driver.findElement(By.tagName("a"))
val input = driver.findElement(By.name("email"))
val css = driver.findElement(By.cssSelector(".container > div"))
val linkByText = driver.findElement(By.linkText("Click Here"))

element.text         // text content
element.click()      // follow link or submit form
element.sendKeys("input text")
element.getAttribute("href")
element.isDisplayed
element.isEnabled
```

## Cookie Management

```kotlin
driver.manage().addCookie(Cookie("name", "value"))
driver.manage().getCookies()
driver.manage().getCookieNamed("session")
driver.manage().deleteCookieNamed("session")
driver.manage().deleteAllCookies()
```

## Form Submission

```kotlin
// Submit buttons can override form action and method
// <input type="submit" formaction="/other" formmethod="post">
element.click()  // uses formaction/formmethod if present, else form attributes

// Basic auth from URL
driver.get("http://user:pass@example.com/page")
// Subsequent form submissions include Authorization header
```

## File Upload

```kotlin
// Single file
driver.findElement(By.id("file-input")).sendKeys("/path/to/file.txt")

// Multiple files — pass each path as a separate argument
driver.findElement(By.id("multi-file-input")).sendKeys("/path/to/a.txt", "/path/to/b.txt")

driver.findElement(By.tagName("button")).submit()
```

## PageBehaviour

Extension point to hook page runtime behaviour into `Http4kWebDriver`. Implement to simulate a frontend framework or intercept element interactions.

```kotlin
interface PageBehaviour {
    fun pageLoaded(document: Document) {}
    fun beforeFind() {}                                 // called before element lookups (e.g. to apply pending updates)
    fun pageSource(document: Document): String? = null  // override the reported page source
    fun before(event: PageEvent, element: Element): Boolean = false  // return true to suppress default
    fun after(event: PageEvent, element: Element) {}
    fun displayed(element: Element): Boolean? = null  // null → throws FeatureNotImplementedYet

    companion object {
        val NoOp = object : PageBehaviour {}  // default — no-op
    }
}

enum class PageEvent { Click, Submit, SendKeys, Clear }
```

`JSoupWebElement.isDisplayed()` delegates to `behaviour.displayed()`. Without a `PageBehaviour`, calling `isDisplayed` throws `FeatureNotImplementedYet`. See `http4k-testing-webdriver-datastar` for a full implementation.

## Gotchas

- **No real browser**: Uses JSoup to parse HTML. No JavaScript execution.
- **No server needed**: Requests go directly to the `HttpHandler` — fast and in-process.
- **Cookie tracking**: Cookies are tracked automatically across requests via `ClientFilters.Cookies`.
- **Redirect following**: Redirects are followed automatically via `ClientFilters.FollowRedirects`.
- **Selenium API**: Implements the standard `WebDriver` interface — works with existing Selenium test patterns.
- **NoSuchElementException includes selector**: Error messages include which `By` selector failed, making failures easier to diagnose.
- **Basic auth from URL**: If the current page URL contains userinfo (`user:pass@`), the credentials are applied as Basic Auth headers on subsequent requests.
- **formaction/formmethod**: Submit buttons with `formaction` or `formmethod` attributes override the parent form's `action` and `method`.

A behaviour can be built from the browser's own handler, so any requests it makes carry the browser's cookies and resolve relative URLs against the current page (and its `<base href>`):

```kotlin
val driver = Http4kWebDriver(myApp, Clock.systemUTC()) { browserHandler ->
    object : PageBehaviour {
        override fun pageLoaded(document: Document) { browserHandler(Request(GET, "things/done")) }
    }
}
```
