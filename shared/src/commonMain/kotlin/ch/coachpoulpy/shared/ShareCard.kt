package ch.coachpoulpy.shared

/**
 * The picture a person shares after a session: the finished screen as an image, the same one straight after the
 * session and from the history. Everything on it is decided in [FinishedScreen]; each platform only draws it, in the
 * colours of the current theme.
 */
data class ShareCard(
    val heading: String,
    val quote: String,
    val title: String,
    val startedAtMillis: Long,
    val drawing: PoulpyDrawing,
    val lines: List<FinishedStat>,
    val poulpy: PoulpyStatus,
    val appName: String,
) {
    companion object {
        fun of(screen: FinishedScreen, languageCode: String) = ShareCard(
            heading = screen.heading,
            quote = screen.quote,
            title = screen.workoutName,
            startedAtMillis = screen.startedAtMillis,
            drawing = screen.drawing,
            lines = screen.stats,
            poulpy = screen.poulpy,
            appName = DomainText.shareApp(languageCode),
        )

        /** "5.2 km" (the number in the language's decimal sign, a non-breaking space before the unit). */
        internal fun measure(measure: Measure, languageCode: String): String {
            var scale = 1L
            repeat(measure.decimals) { scale *= 10 }
            val scaled = kotlin.math.round(measure.value * scale).toLong()
            val fraction = if (measure.decimals == 0) "" else (if (languageCode == "fr") "," else ".") + (scaled % scale).toString().padStart(measure.decimals, '0')
            return "${scaled / scale}$fraction\u00a0${measure.unit}"
        }

        internal fun clock(seconds: Long): String {
            val hours = seconds / 3600
            val minutes = seconds % 3600 / 60
            val rest = seconds % 60
            return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}:${rest.toString().padStart(2, '0')}"
            else "$minutes:${rest.toString().padStart(2, '0')}"
        }
    }
}
