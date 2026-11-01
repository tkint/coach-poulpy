package ch.coachpoulpy.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.serialization.Serializable

/** How the person wants to be addressed. A [CUSTOM] entry is repeated as written and never drives French agreement. */
enum class Pronouns { SHE_HER, HE_HIM, THEY_THEM, JUST_MY_NAME, CUSTOM, PREFER_NOT_TO_SAY }

/**
 * Asked only for estimates such as calories and heart rate zones, never shown, never about gender.
 * A null answer means a neutral average ([docs/brand.md] "Inclusive by default").
 */
enum class SexAssignedAtBirth { FEMALE, MALE, INTERSEX, PREFER_NOT_TO_SAY }

enum class MainGoal { STAY_ACTIVE, BUILD_STRENGTH, RUN_FURTHER, REHAB, SOMETHING_ELSE }

enum class Experience { JUST_STARTING, REGULAR, EXPERIENCED }

/** What a person tells the app about themselves. Everything is optional and stays on the phone. */
@Serializable
data class Profile(
    val displayName: String? = null,
    val birthdate: LocalDate? = null,
    val pronouns: Pronouns? = null,
    /** Only kept with [Pronouns.CUSTOM]. */
    val customPronouns: String? = null,
    val sexAssignedAtBirth: SexAssignedAtBirth? = null,
    val mainGoal: MainGoal? = null,
    val experience: Experience? = null,
    val restingHeartRate: Int? = null,
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
