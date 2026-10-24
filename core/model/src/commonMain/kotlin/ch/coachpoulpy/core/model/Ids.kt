package ch.coachpoulpy.core.model

import kotlin.jvm.JvmInline
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.serialization.Serializable

/**
 * Every record gets its id on the device when it is created, so guest data can
 * later be uploaded to the server unchanged. Ids are UUIDv7: time-ordered, which
 * keeps database indexes compact and lets records sort by creation time.
 */
object Ids {
    fun newUuidV7(nowMillis: Long = Clock.System.now().toEpochMilliseconds(), random: Random = Random.Default): String {
        val bytes = random.nextBytes(16)
        for (i in 0 until 6) {
            bytes[i] = (nowMillis ushr (40 - 8 * i)).toByte()
        }
        bytes[6] = ((bytes[6].toInt() and 0x0F) or 0x70).toByte() // version 7
        bytes[8] = ((bytes[8].toInt() and 0x3F) or 0x80).toByte() // RFC 4122 variant
        val hex = bytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
        return "${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-${hex.substring(16, 20)}-${hex.substring(20)}"
    }
}

@Serializable @JvmInline value class ExerciseSlug(val value: String)
@Serializable @JvmInline value class WorkoutId(val value: String)
@Serializable @JvmInline value class StepId(val value: String)
@Serializable @JvmInline value class PlanId(val value: String)
@Serializable @JvmInline value class EnrollmentId(val value: String)
@Serializable @JvmInline value class GoalId(val value: String)
@Serializable @JvmInline value class ExecutionId(val value: String)

/** Text shipped in both supported languages. */
@Serializable
data class LocalizedText(val en: String, val fr: String) {
    fun forLanguage(languageCode: String): String = if (languageCode.startsWith("fr")) fr else en
}
