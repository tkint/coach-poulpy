package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.BodyFigure
import ch.coachpoulpy.core.model.Experience
import ch.coachpoulpy.core.model.Pain
import ch.coachpoulpy.core.model.PainDuration
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
    val bodyFigure: BodyFigure?,
    /** Where it hurts, as body map keys in reading order; empty without pain or rehab. */
    val painAreas: List<String>,
    val painDuration: PainDuration?,
    /** The day pain or rehab ends, or null when there is none or it lasts until cleared. */
    val painUntil: PlanDate?,
    /** Pain or rehab is set and its time is not up. */
    val painActive: Boolean,
)

/** Settings › Your profile: every field optional, kept on the phone, and an empty or skipped field simply stays empty. */
class ProfileModel(
    private val memory: KeyValueStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
    private val now: () -> Instant = { Clock.System.now() },
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
            bodyFigure = read<BodyFigure>(BODY_FIGURE),
        ),
    )
    val profile: StateFlow<Profile> = _profile.asStateFlow()

    private val _pain = MutableStateFlow(readPain())

    /** What was set for pain or rehab, even when its time is up; [painActive] says whether it still counts. */
    val pain: StateFlow<Pain?> = _pain.asStateFlow()

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

    fun setBodyFigure(figure: BodyFigure?) = change(BODY_FIGURE, figure?.name) { it.copy(bodyFigure = figure) }

    /**
     * Pain or rehab from now on: Poulpy stays on the blue side for [duration]. [areas] that are not areas of the body map
     * are dropped, and with none left this clears it, because it would not say where it hurts. Setting it again starts
     * the time over, which is how someone who still hurts keeps the blue side.
     */
    fun setPain(areas: Collection<String>, duration: PainDuration) {
        val kept = BodyMap.valid(areas)
        if (kept.isEmpty()) return clearPain()
        val since = now()
        memory.putString(PAIN_AREAS, kept.joinToString(","))
        memory.putString(PAIN_SINCE, since.toString())
        memory.putString(PAIN_DURATION, duration.name)
        _pain.value = Pain(kept.toSet(), since, duration)
        publish()
    }

    fun clearPain() {
        memory.putString(PAIN_AREAS, null)
        memory.putString(PAIN_SINCE, null)
        memory.putString(PAIN_DURATION, null)
        _pain.value = null
        publish()
    }

    /** [setPain] for Swift, which passes a list. */
    fun setPainTo(areas: List<String>, duration: PainDuration) = setPain(areas, duration)

    /** Pain or rehab is set and its time is not up, so Poulpy stays on the blue side. */
    fun painActive(): Boolean = _pain.value?.isActive(now()) == true

    /** "Left knee, Left hip": the areas in the person's language. */
    fun painSummary(languageCode: String): String =
        BodyMap.valid(_pain.value?.areas.orEmpty()).joinToString(", ") { BodyMap.label(it, languageCode) }

    /** [profile] in plain values, for the iOS screens. */
    private val refresh = MutableStateFlow(0)
    val ui: StateFlow<ProfileUi> = combine(_profile, _pain, refresh) { profile, _, _ -> profile.toUi() }
        .stateIn(scope, SharingStarted.Eagerly, _profile.value.toUi())

    /** Lets [ui] be read again, for a pain window whose time has run out since it was last read. */
    private fun publish() { refresh.update { it + 1 } }

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
        bodyFigure = bodyFigure,
        painAreas = BodyMap.valid(_pain.value?.areas.orEmpty()),
        painDuration = _pain.value?.duration,
        painUntil = _pain.value?.until?.let { PlanDate.of(it.toLocalDateTime(TimeZone.currentSystemDefault()).date) },
        painActive = painActive(),
    )

    private fun readPain(): Pain? {
        val areas = BodyMap.valid(memory.getString(PAIN_AREAS)?.split(',').orEmpty())
        val since = memory.getString(PAIN_SINCE)?.let { runCatching { Instant.parse(it) }.getOrNull() }
        val duration = read<PainDuration>(PAIN_DURATION)
        return if (areas.isEmpty() || since == null || duration == null) null else Pain(areas.toSet(), since, duration)
    }

    companion object {
        private const val NAME = "profile.name"
        private const val BIRTHDATE = "profile.birthdate"
        private const val PRONOUNS = "profile.pronouns"
        private const val CUSTOM_PRONOUNS = "profile.customPronouns"
        private const val SEX = "profile.sexAssignedAtBirth"
        private const val GOALS = "profile.goals"
        private const val EXPERIENCE = "profile.experience"
        private const val RESTING_HEART_RATE = "profile.restingHeartRate"
        private const val BODY_FIGURE = "profile.bodyFigure"
        private const val PAIN_AREAS = "profile.pain.areas"
        private const val PAIN_SINCE = "profile.pain.since"
        private const val PAIN_DURATION = "profile.pain.duration"
    }
}
