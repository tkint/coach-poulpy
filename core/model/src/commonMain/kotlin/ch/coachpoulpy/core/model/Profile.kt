package ch.coachpoulpy.core.model

import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.datetime.number
import kotlinx.serialization.Serializable

/** How the person wants to be addressed. A [CUSTOM] entry is repeated as written and never drives French agreement. */
enum class Pronouns { SHE_HER, HE_HIM, THEY_THEM, JUST_MY_NAME, CUSTOM, PREFER_NOT_TO_SAY }

/**
 * Asked only for estimates such as calories and heart rate zones, never shown, never about gender.
 * A null answer means a neutral average ([docs/brand.md] "Inclusive by default").
 */
enum class SexAssignedAtBirth { FEMALE, MALE, INTERSEX, PREFER_NOT_TO_SAY }

/** What the person trains for; they may pick several. */
enum class TrainingGoal { STAY_ACTIVE, BUILD_STRENGTH, RUN_FURTHER, REHAB, SOMETHING_ELSE }

/** Five steps on one scale, from the first week to years of training. */
enum class Experience { JUST_STARTING, GETTING_GOING, REGULAR, EXPERIENCED, VERY_EXPERIENCED }

/**
 * Which body map is drawn for pain and, later, for the muscles a workout uses. Only a drawing: it is not asked
 * as gender, not linked to [SexAssignedAtBirth] and never used for estimates.
 */
enum class BodyFigure { SHOULDERS, HIPS }

/** How long Poulpy stays on the blue side after pain or rehab is set. [UNTIL_CLEARED] has no end. */
enum class PainDuration(val hours: Int?) {
    HOURS_72(72),
    ONE_WEEK(7 * 24),
    TWO_WEEKS(14 * 24),
    UNTIL_CLEARED(null),
}

/**
 * Where it hurts, set on [since] for [duration]. [areas] are body map keys (`knee-left`), kept as text so the map
 * can change shape without touching what is saved. Poulpy stays on the blue side until [until].
 */
data class Pain(val areas: Set<String>, val since: Instant, val duration: PainDuration) {
    /** Null when it lasts until the person clears it. */
    val until: Instant? get() = duration.hours?.let { since + it.hours }

    fun isActive(now: Instant): Boolean = until?.let { now < it } ?: true
}

/** What a person tells the app about themselves. Everything is optional and stays on the phone. */
@Serializable
data class Profile(
    val displayName: String? = null,
    val birthdate: LocalDate? = null,
    val pronouns: Pronouns? = null,
    /** Only kept with [Pronouns.CUSTOM]. */
    val customPronouns: String? = null,
    val sexAssignedAtBirth: SexAssignedAtBirth? = null,
    val goals: Set<TrainingGoal> = emptySet(),
    val experience: Experience? = null,
    val restingHeartRate: Int? = null,
    val bodyFigure: BodyFigure? = null,
) {
    /** Computed from the birthdate on [today], never stored. Null without a birthdate or for one in the future. */
    fun age(today: LocalDate): Int? {
        val born = birthdate ?: return null
        if (born > today) return null
        var years = today.year - born.year
        if (today.month.number < born.month.number || (today.month == born.month && today.day < born.day)) years--
        return years
    }
}
