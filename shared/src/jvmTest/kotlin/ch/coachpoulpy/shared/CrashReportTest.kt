package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrashReportTest {
    private fun report(error: Throwable) =
        CrashReport.write(error, thread = "main", appVersion = "1.4.0", system = "Android 15", device = "Pixel 8", time = "2026-11-08 01:20")

    @Test
    fun saysWhatRanAndTheErrorWithItsCause() {
        val text = report(IllegalStateException("Stream closed", java.io.IOException("Socket reset")))
        val lines = text.lines()
        assertEquals(listOf("Coach Poulpy 1.4.0", "Android 15, Pixel 8", "2026-11-08 01:20", "Thread: main", ""), lines.take(5))
        assertTrue(lines[5].startsWith("java.lang.IllegalStateException: Stream closed"))
        assertTrue(text.contains("Caused by: java.io.IOException: Socket reset"))
        assertTrue(text.contains("CrashReportTest"))
    }

    @Test
    fun aVeryDeepErrorIsCutToFitAShareSheet() {
        fun deep(n: Int): Nothing = if (n == 0) throw StackOverflowError("deep") else deep(n - 1)
        val error = runCatching { deep(5_000) }.exceptionOrNull()!!
        val text = report(RuntimeException("x".repeat(80_000), error))
        assertEquals(CrashReport.MAX_LENGTH + 2, text.length)
        assertTrue(text.endsWith("\n…"))
    }
}
