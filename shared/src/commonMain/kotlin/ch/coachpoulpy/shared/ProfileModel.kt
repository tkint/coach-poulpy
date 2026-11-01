package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.Experience
import ch.coachpoulpy.core.model.MainGoal
import ch.coachpoulpy.core.model.Profile
import ch.coachpoulpy.core.model.Pronouns
import ch.coachpoulpy.core.model.SexAssignedAtBirth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/** Settings › Your profile: every field optional, kept on the phone, and an empty or skipped field simply stays empty. */
class ProfileModel(
    private val memory: KeyValueStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val _profile = MutableStateFlow(
        Profile(
            displayName = memory.getString(NAME),
            birthdate = memory.getString(BIRTHDATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            pronouns = read<Pronouns>(PRONOUNS),
            customPronouns = memory.getString(CUSTOM_PRONOUNS),
            sexAssignedAtBirth = read<SexAssignedAtBirth>(SEX),
            mainGoal = read<MainGoal>(GOAL),
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

    fun setMainGoal(goal: MainGoal?) = change(GOAL, goal?.name) { it.copy(mainGoal = goal) }

    fun setExperience(experience: Experience?) = change(EXPERIENCE, experience?.name) { it.copy(experience = experience) }

    /** Beats per minute; anything outside 30 to 120 is not a resting heart rate and clears the field. */
    fun setRestingHeartRate(bpm: Int?) {
        val valid = bpm?.takeIf { it in 30..120 }
        change(RESTING_HEART_RATE, valid?.toString()) { it.copy(restingHeartRate = valid) }
    }

    fun watch(onChange: (Profile) -> Unit): Cancellable = profile.watchIn(scope, onChange)

    private fun change(key: String, saved: String?, edit: (Profile) -> Profile) {
        memory.putString(key, saved)
        _profile.update(edit)
    }

    private inline fun <reified T : Enum<T>> read(key: String): T? =
        memory.getString(key)?.let { saved -> enumValues<T>().firstOrNull { it.name == saved } }

    companion object {
        private const val NAME = "profile.name"
        private const val BIRTHDATE = "profile.birthdate"
        private const val PRONOUNS = "profile.pronouns"
        private const val CUSTOM_PRONOUNS = "profile.customPronouns"
        private const val SEX = "profile.sexAssignedAtBirth"
        private const val GOAL = "profile.goal"
        private const val EXPERIENCE = "profile.experience"
        private const val RESTING_HEART_RATE = "profile.restingHeartRate"
    }
}
