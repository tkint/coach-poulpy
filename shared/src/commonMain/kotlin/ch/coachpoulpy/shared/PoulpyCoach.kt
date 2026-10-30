package ch.coachpoulpy.shared

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

/**
 * Which drawing shows. The first six are plain mood portraits; the rest are the brand book's
 * poses. Each platform maps them to its generated drawing (`poulpy_mood_happy`, `PoulpyPoseShy`...).
 */
enum class PoulpyDrawing(val assetName: String) {
    MOOD_PROUD("mood_proud"),
    MOOD_HAPPY("mood_happy"),
    MOOD_RECOVERING("mood_recovering"),
    MOOD_ANNOYED("mood_annoyed"),
    MOOD_GRUMPY("mood_grumpy"),
    MOOD_FUMING("mood_fuming"),
    COCKY("pose_cocky"),
    STRAINING("pose_straining"),
    CHAMPION("pose_champion"),
    STANDING_TALL("pose_standing_tall"),
    EXHAUSTED("pose_exhausted"),
    SIDE_GLANCE("pose_side_glance"),
    SLEEPING("pose_sleeping"),
    PATCHED_UP("pose_patched_up"),
    SHY("pose_shy"),
    THINKING("pose_thinking"),
    POUTING("pose_pouting"),
    FUMING("pose_fuming"),
}

/** What the Home header shows: Poulpy, what he says, and his eight tentacles. */
data class PoulpyStatus(
    val mood: Mood,
    val drawing: PoulpyDrawing,
    val title: String,
    val line: String,
    val tentaclesLit: Int,
    val fullOctopuses: Int,
    val tentaclesCaption: String,
) {
    /** True on the blue side; false once a session slipped (ember side). */
    val blueSide: Boolean get() = mood in BLUE_SIDE

    /** Poulpy's six mood shades, the same names as the design tokens. */
    enum class Mood { PROUD, HAPPY, RECOVERING, ANNOYED, GRUMPY, FUMING }

    companion object {
        const val TENTACLES = 8
        private val BLUE_SIDE = setOf(Mood.PROUD, Mood.HAPPY, Mood.RECOVERING)
    }
}

/**
 * Poulpy's rules, from the brand book. There are no plans yet, so nothing can be missed:
 * Poulpy stays on the blue side and only reacts to when you last trained.
 *
 * Each finished session lights one tentacle and lit tentacles never go dark. The eighth one
 * makes a full octopus (Champion), and the ring stays full until the next session starts a
 * new one, so a quiet month never takes anything away.
 */
class PoulpyCoach(private val languageCode: String, private val timeZone: TimeZone = TimeZone.currentSystemDefault()) {
    /** [finishedAt] is when each finished session ended, in any order. */
    fun status(finishedAt: List<Instant>, now: Instant): PoulpyStatus {
        val sessions = finishedAt.sorted()
        val total = sessions.size
        val full = total / PoulpyStatus.TENTACLES
        val lit = if (total > 0 && total % PoulpyStatus.TENTACLES == 0) PoulpyStatus.TENTACLES else total % PoulpyStatus.TENTACLES
        val caption = if (full == 0) DomainText.poulpyCaption(languageCode, lit) else DomainText.poulpyCaptionFull(languageCode, caption = DomainText.poulpyCaption(languageCode, lit), full = full)
        val status = { mood: PoulpyStatus.Mood, drawing: PoulpyDrawing, title: String, line: String ->
            PoulpyStatus(mood, drawing, title, line, lit, full, caption)
        }

        if (total == 0) {
            return status(
                PoulpyStatus.Mood.HAPPY, PoulpyDrawing.MOOD_HAPPY,
                DomainText.poulpyFirstTitle(languageCode),
                DomainText.poulpyFirstLine(languageCode),
            )
        }

        val today = day(now)
        val last = day(sessions.last())
        val daysSince = last.daysUntil(today)
        val gapBeforeLast = sessions.getOrNull(total - 2)?.let { day(it).daysUntil(last) }
        val left = PoulpyStatus.TENTACLES - lit

        return when {
            daysSince == 0 && lit == PoulpyStatus.TENTACLES -> status(
                PoulpyStatus.Mood.PROUD, PoulpyDrawing.CHAMPION,
                DomainText.poulpyChampionTitle(languageCode),
                DomainText.poulpyChampionLine(languageCode),
            )
            daysSince == 0 && gapBeforeLast != null && gapBeforeLast >= LONG_BREAK_DAYS -> status(
                PoulpyStatus.Mood.HAPPY, PoulpyDrawing.SHY,
                DomainText.poulpyBackTitle(languageCode),
                DomainText.poulpyBackLine(languageCode),
            )
            daysSince == 0 -> status(
                PoulpyStatus.Mood.HAPPY, PoulpyDrawing.MOOD_HAPPY,
                DomainText.poulpyLitTitle(languageCode),
                DomainText.poulpyLitLine(languageCode, left),
            )
            daysSince == 1 -> status(
                PoulpyStatus.Mood.RECOVERING, PoulpyDrawing.MOOD_RECOVERING,
                DomainText.poulpyRestTitle(languageCode),
                DomainText.poulpyRestLine(languageCode),
            )
            daysSince < LONG_BREAK_DAYS -> status(
                PoulpyStatus.Mood.HAPPY, PoulpyDrawing.MOOD_HAPPY,
                DomainText.poulpyHappyTitle(languageCode),
                if (lit == PoulpyStatus.TENTACLES) DomainText.poulpyHappyFullLine(languageCode) else DomainText.poulpyHappyLine(languageCode),
            )
            else -> status(
                PoulpyStatus.Mood.RECOVERING, PoulpyDrawing.SLEEPING,
                DomainText.poulpyNappingTitle(languageCode),
                DomainText.poulpyNappingLine(languageCode),
            )
        }
    }

    private fun day(instant: Instant): LocalDate = instant.toLocalDateTime(timeZone).date

    private companion object {
        /** A week without training: Poulpy naps, then greets you shyly when you come back. */
        const val LONG_BREAK_DAYS = 7
    }
}
