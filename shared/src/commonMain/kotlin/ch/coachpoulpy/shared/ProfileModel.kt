package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.Experience
import ch.coachpoulpy.core.model.TrainingGoal
import ch.coachpoulpy.core.model.Profile
import ch.coachpoulpy.core.model.Pronouns
import ch.coachpoulpy.core.model.SexAssignedAtBirth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** [Profile] as plain values for Swift: no Kotlin dates, and text where a number may be missing. */
data class ProfileUi(
    /** Empty when there is none. */
    val displayName: String,
    val birthdate: PlanDate?,
    val age: Int?,
    val pronouns: Pronouns?,
    /** Empty unless the pronouns are custom. */
    val customPronouns: String,
    val sexAssignedAtBirth: SexAssignedAtBirth?,
    /** In the order of the list of goals. */
    val goals: List<TrainingGoal>,
    val experience: Experience?,
    /** Digits, or empty when there is none. */
    val restingHeartRate: String,
)

/** Settings › Your profile: every field optional, kept on the phone, and an empty or skipped field simply stays empty. */
class ProfileModel(
    private val memory: KeyValueStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
) {
    private val _profile = MutableStateFlow(
        Profile(
            displayName = memory.getString(NAME),
            birthdate = memory.getString(BIRTHDATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            pronouns = read<Pronouns>(PRONOUNS),
            customPronouns = memory.getString(CUSTOM_PRONOUNS),
            sexAssignedAtBirth = read<SexAssignedAtBirth>(SEX),
            goals = memory.getString(GOALS)?.split(',')?.mapNotNull { saved -> TrainingGoal.entries.firstOrNull { it.name == saved } }?.toSet().orEmpty(),
            experience = read<Experience>(EXPERIENCE),
            restingHeartRate = memory.getString(RESTING_HEART_RATE)?.toIntOrNull(),
        ),
    )
    val profile: StateFlow<Profile> = _profile.asStateFlow()

    /** A blank name is no name. */
    fun setDisplayName(name: String?) = change(NAME, name?.trim()?.ifEmpty { null }) { it.copy(displayName = name?.trim()?.ifEmpty { null }) }

    fun setBirthdate(date: LocalDate?) = change(BIRTHDATE, date?.toString()) { it.copy(birthdate = date) }

    /** Leaving [Pronouns.CUSTOM] forgets the typed text; [custom] is only kept with it. */
    fun setPronouns(pronouns: Pronouns?, custom: String? = null) {
        val text = if (pronouns == Pronouns.CUSTOM) custom?.trim()?.ifEmpty { null } else null
        memory.putString(PRONOUNS, pronouns?.name)
        memory.putString(CUSTOM_PRONOUNS, text)
        _profile.update { it.copy(pronouns = pronouns, customPronouns = text) }
    }

    fun setSexAssignedAtBirth(sex: SexAssignedAtBirth?) = change(SEX, sex?.name) { it.copy(sexAssignedAtBirth = sex) }

    /** Several goals at once, kept in the order of the list so the saved text never depends on the order they were ticked. */
    fun setGoals(goals: Set<TrainingGoal>) {
        val kept = TrainingGoal.entries.filter { it in goals }
        change(GOALS, kept.joinToString(",").ifEmpty { null }) { it.copy(goals = kept.toSet()) }
    }

    /** [setGoals] for Swift, which passes a list. */
    fun setGoalsTo(goals: List<TrainingGoal>) = setGoals(goals.toSet())

    fun setExperience(experience: Experience?) = change(EXPERIENCE, experience?.name) { it.copy(experience = experience) }

    /** Beats per minute; anything outside 30 to 120 is not a resting heart rate and clears the field. */
    fun setRestingHeartRate(bpm: Int?) {
        val valid = bpm?.takeIf { it in 30..120 }
        change(RESTING_HEART_RATE, valid?.toString()) { it.copy(restingHeartRate = valid) }
    }

    /** [profile] in plain values, for the iOS screens. */
    val ui: StateFlow<ProfileUi> = _profile.map { it.toUi() }.stateIn(scope, SharingStarted.Eagerly, _profile.value.toUi())

    /** [setBirthdate] for Swift, which has no Kotlin date to pass; an impossible date clears the field. */
    fun setBirthdateTo(date: PlanDate) = setBirthdate(runCatching { date.date }.getOrNull())

    fun clearBirthdate() = setBirthdate(null)

    /** Raw text from a field: its digits are the beats per minute, and anything else clears it. */
    fun setRestingHeartRateText(text: String) = setRestingHeartRate(text.filter { it.isDigit() }.toIntOrNull())

    /** The age in whole years today, from the birthdate; never stored. */
    fun age(of: Profile): Int? = of.age(today())

    fun watch(onChange: (ProfileUi) -> Unit): Cancellable = ui.watchIn(scope, onChange)

    private fun change(key: String, saved: String?, edit: (Profile) -> Profile) {
        memory.putString(key, saved)
        _profile.update(edit)
    }

    private inline fun <reified T : Enum<T>> read(key: String): T? =
        memory.getString(key)?.let { saved -> enumValues<T>().firstOrNull { it.name == saved } }

    private fun Profile.toUi() = ProfileUi(
        displayName = displayName.orEmpty(),
        birthdate = birthdate?.let { PlanDate.of(it) },
        age = age(today()),
        pronouns = pronouns,
        customPronouns = customPronouns.orEmpty(),
        sexAssignedAtBirth = sexAssignedAtBirth,
        goals = TrainingGoal.entries.filter { it in goals },
        experience = experience,
        restingHeartRate = restingHeartRate?.toString().orEmpty(),
    )

    companion object {
        private const val NAME = "profile.name"
        private const val BIRTHDATE = "profile.birthdate"
        private const val PRONOUNS = "profile.pronouns"
        private const val CUSTOM_PRONOUNS = "profile.customPronouns"
        private const val SEX = "profile.sexAssignedAtBirth"
        private const val GOALS = "profile.goals"
        private const val EXPERIENCE = "profile.experience"
        private const val RESTING_HEART_RATE = "profile.restingHeartRate"
    }
}
