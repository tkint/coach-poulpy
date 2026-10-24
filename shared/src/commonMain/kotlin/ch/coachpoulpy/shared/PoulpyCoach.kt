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
    private fun t(en: String, fr: String) = if (languageCode.startsWith("fr")) fr else en

    /** [finishedAt] is when each finished session ended, in any order. */
    fun status(finishedAt: List<Instant>, now: Instant): PoulpyStatus {
        val sessions = finishedAt.sorted()
        val total = sessions.size
        val full = total / PoulpyStatus.TENTACLES
        val lit = if (total > 0 && total % PoulpyStatus.TENTACLES == 0) PoulpyStatus.TENTACLES else total % PoulpyStatus.TENTACLES
        val caption = when {
            full == 0 -> t("$lit of 8 tentacles lit", "$lit tentacules sur 8 allumés")
            full == 1 -> t("$lit of 8 tentacles lit · 1 full octopus", "$lit tentacules sur 8 allumés · 1 poulpe complet")
            else -> t("$lit of 8 tentacles lit · $full full octopuses", "$lit tentacules sur 8 allumés · $full poulpes complets")
        }
        val status = { mood: PoulpyStatus.Mood, drawing: PoulpyDrawing, title: String, line: String ->
            PoulpyStatus(mood, drawing, title, line, lit, full, caption)
        }

        if (total == 0) {
            return status(
                PoulpyStatus.Mood.HAPPY, PoulpyDrawing.MOOD_HAPPY,
                t("Hi, I'm Poulpy", "Salut, moi c'est Poulpy"),
                t("Finish a session and I light my first tentacle.", "Termine une séance et j'allume mon premier tentacule."),
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
                t("Full octopus!", "Poulpe complet !"),
                t("All eight tentacles lit. Gold for the octopus!", "Les huit tentacules sont allumés. L'or pour le poulpe !"),
            )
            daysSince == 0 && gapBeforeLast != null && gapBeforeLast >= LONG_BREAK_DAYS -> status(
                PoulpyStatus.Mood.HAPPY, PoulpyDrawing.SHY,
                t("Oh… you're back!", "Oh… te revoilà !"),
                t("I kept your tentacles lit. One more glows now.", "J'ai gardé tes tentacules allumés. Un de plus brille maintenant."),
            )
            daysSince == 0 -> status(
                PoulpyStatus.Mood.HAPPY, PoulpyDrawing.MOOD_HAPPY,
                t("Nice one", "Bien joué"),
                if (left == 1) t("One more session and I'm a full octopus.", "Encore une séance et je suis un poulpe complet.")
                else t("Another tentacle lit. $left to go for a full octopus.", "Un tentacule de plus. Encore $left pour un poulpe complet."),
            )
            daysSince == 1 -> status(
                PoulpyStatus.Mood.RECOVERING, PoulpyDrawing.MOOD_RECOVERING,
                t("Resting", "Repos"),
                t("Rest is training too. Ready when you are.", "Le repos, c'est aussi de l'entraînement. Prêt quand tu l'es."),
            )
            daysSince < LONG_BREAK_DAYS -> status(
                PoulpyStatus.Mood.HAPPY, PoulpyDrawing.MOOD_HAPPY,
                t("Poulpy is happy", "Poulpy est content"),
                if (lit == PoulpyStatus.TENTACLES) t("A full octopus! The next session starts a new one.", "Un poulpe complet ! La prochaine séance en commence un nouveau.")
                else t("Shall we light another tentacle?", "On allume un autre tentacule ?"),
            )
            else -> status(
                PoulpyStatus.Mood.RECOVERING, PoulpyDrawing.SLEEPING,
                t("Poulpy is napping", "Poulpy fait la sieste"),
                t("Your tentacles are still lit. Wake me up with a session.", "Tes tentacules sont toujours allumés. Réveille-moi avec une séance."),
            )
        }
    }

    private fun day(instant: Instant): LocalDate = instant.toLocalDateTime(timeZone).date

    private companion object {
        /** A week without training: Poulpy naps, then greets you shyly when you come back. */
        const val LONG_BREAK_DAYS = 7
    }
}
