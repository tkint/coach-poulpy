package ch.coachpoulpy.shared

/** One line of the share card, for example "Time" and "38:24". */
data class ShareLine(val label: String, val value: String)

/**
 * The picture a person shares after a finished session: Poulpy, the workout's name and date, and the numbers of the
 * session. The lines and their order are decided here so Android and iOS print the same card; each platform only
 * draws it, in the colours of the current theme. A value that is missing leaves its line out, and the heart rate is
 * never on the card. Straight after a session the card is the finished screen as a picture (same tiles, same tentacle
 * card); a past session from the history has no tentacle card, since that moment is gone.
 */
data class ShareCard(
    val title: String,
    val startedAtMillis: Long,
    val drawing: PoulpyDrawing,
    val lines: List<ShareLine>,
    val appName: String,
    /** The tentacle this session lit, shown as on the finished screen; null for a past session. */
    val poulpy: PoulpyStatus? = null,
) {
    companion object {
        /** The card of a session that has just finished: the tiles and the tentacle card of [screen]. */
        fun of(screen: FinishedScreen, languageCode: String, fullOctopus: Boolean = false) = ShareCard(
            title = screen.workoutName,
            startedAtMillis = screen.startedAtMillis,
            drawing = if (fullOctopus) PoulpyDrawing.CHAMPION else PoulpyDrawing.MOOD_HAPPY,
            lines = screen.stats.map { ShareLine(it.label, it.value) },
            appName = DomainText.shareApp(languageCode),
            poulpy = screen.poulpy,
        )

        /** [fullOctopus] is true when this session lit Poulpy's eighth tentacle: he shows Champion instead of Happy. */
        fun of(session: SessionRow, units: UnitSystem, languageCode: String, fullOctopus: Boolean = false): ShareCard {
            val lines = buildList {
                add(ShareLine(DomainText.shareTime(languageCode), clock(session.activeSeconds)))
                val done = session.setsDone
                if (session.kind == SessionKind.WORKOUT && done != null) {
                    val planned = session.setsPlanned
                    add(ShareLine(DomainText.shareSets(languageCode), if (planned != null) DomainText.shareSetsOf(languageCode, done, planned) else "$done"))
                }
                session.distanceMeters?.let { add(ShareLine(DomainText.shareDistance(languageCode), measure(Units.distance(it, units, decimals = 1), languageCode))) }
                session.averageSpeedKmh?.let { add(ShareLine(DomainText.shareAverageSpeed(languageCode), measure(Units.speed(it, units), languageCode))) }
                session.energyKcal?.let { add(ShareLine(DomainText.shareEnergy(languageCode), DomainText.shareEnergyValue(languageCode, it))) }
            }
            return ShareCard(
                title = session.title,
                startedAtMillis = session.startedAtMillis,
                drawing = if (fullOctopus) PoulpyDrawing.CHAMPION else PoulpyDrawing.MOOD_HAPPY,
                lines = lines,
                appName = DomainText.shareApp(languageCode),
            )
        }

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
