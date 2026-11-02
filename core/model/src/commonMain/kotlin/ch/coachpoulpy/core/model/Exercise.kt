package ch.coachpoulpy.core.model

import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonArray

/**
 * A catalog entry. The slug is the stable key: workouts point at it, so an entry is
 * never deleted. One that is no longer offered is [retired], which hides it from the
 * catalog while workouts that already use it keep working. [aliases] are other names
 * people search with; they are never shown.
 *
 * [equipment] lists what the exercise needs: each need is a list of alternatives, and
 * one of them must be at hand (goblet squat: dumbbell or kettlebell; dumbbell bench
 * press: dumbbells, and a bench). No need means bodyweight. [optionalEquipment] helps
 * but isn't needed, like a mat or a treadmill for a walk.
 *
 * [focus] says what the exercise is for and [positions] how the body is placed doing it,
 * both so people can find exercises they don't know by name; an exercise can have several.
 * [jumps] marks the exercises where both feet leave the ground, which the "No jumping"
 * tag leaves out ([tags]).
 */
@Serializable
data class Exercise(
    val slug: ExerciseSlug,
    val name: LocalizedText,
    val aliases: Aliases = Aliases(),
    val movementPattern: MovementPattern,
    val muscleGroups: List<MuscleGroup>,
    @Serializable(with = EquipmentNeedsSerializer::class)
    val equipment: List<List<Equipment>> = emptyList(),
    val optionalEquipment: List<Equipment> = emptyList(),
    val measurement: MeasurementMode,
    val perSide: Boolean = false,
    val focus: List<ExerciseFocus> = emptyList(),
    val positions: List<BodyPosition> = emptyList(),
    val jumps: Boolean = false,
    val images: List<String> = emptyList(),
    val instructions: List<LocalizedText> = emptyList(),
    /** How this variation differs from the others in its group, shown only in the variations sheet. */
    val variationNote: LocalizedText? = null,
    val retired: Boolean = false,
) {
    /** Every piece of equipment named, needed or optional, once each. */
    val allEquipment: List<Equipment> get() = (equipment.flatten() + optionalEquipment).distinct()

    /** The tags the exercise carries: how it is done, worked out from its measurement, sides and [jumps]. */
    val tags: Set<ExerciseTag>
        get() = buildSet {
            if (!jumps) add(ExerciseTag.NO_JUMPING)
            if (perSide) add(ExerciseTag.EACH_SIDE)
            if (measurement == MeasurementMode.HOLD) add(ExerciseTag.HOLD)
            if (measurement == MeasurementMode.TIME) add(ExerciseTag.TIMED)
        }

    /** True when every need has one alternative in [owned]. */
    fun fits(owned: Set<Equipment>): Boolean = equipment.all { need -> need.any { it in owned } }
}

/**
 * Reads [Exercise.equipment] as needs. Catalogs before version 5 stored one flat list;
 * the phone still decodes those entries once before replacing them, so each item is
 * read as its own need, and bodyweight and mat, never needs, are dropped.
 */
internal object EquipmentNeedsSerializer : KSerializer<List<List<Equipment>>> {
    private val needs = ListSerializer(ListSerializer(Equipment.serializer()))
    override val descriptor = needs.descriptor

    override fun serialize(encoder: Encoder, value: List<List<Equipment>>) = needs.serialize(encoder, value)

    override fun deserialize(decoder: Decoder): List<List<Equipment>> {
        val json = decoder as? JsonDecoder ?: return needs.deserialize(decoder)
        return json.decodeJsonElement().jsonArray.mapNotNull { need ->
            if (need is JsonArray) {
                need.map { json.json.decodeFromJsonElement(Equipment.serializer(), it) }
            } else {
                json.json.decodeFromJsonElement(Equipment.serializer(), need)
                    .takeIf { it != Equipment.BODYWEIGHT && it != Equipment.MAT }
                    ?.let(::listOf)
            }
        }
    }
}

/** Search-only names for an exercise, per language code, like "RDL" or "abdos". A language may have none. */
@Serializable
@JvmInline
value class Aliases(val byLanguage: Map<String, List<String>> = emptyMap()) {
    val all: List<String> get() = byLanguage.values.flatten()

    operator fun get(language: String): List<String> = byLanguage[language].orEmpty()
}

@Serializable
enum class MovementPattern { SQUAT, HINGE, LUNGE, PUSH, PULL, CARRY, CORE, ROTATION, LOCOMOTION, MOBILITY, STRETCH }

@Serializable
enum class MuscleGroup {
    CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, FOREARMS, CORE, GLUTES, QUADRICEPS, HAMSTRINGS, CALVES, HIPS, NECK, FULL_BODY
}

@Serializable
enum class Equipment { BODYWEIGHT, DUMBBELL, KETTLEBELL, BARBELL, BAND, MAT, BENCH, PULL_UP_BAR, TREADMILL, MACHINE }

@Serializable
enum class MeasurementMode { REPS, TIME, DISTANCE, HOLD }

/** What an exercise is for: the first filter of the exercise list for finding one by purpose. */
@Serializable
enum class ExerciseFocus { STRENGTH, CARDIO, MOBILITY, STRETCHING, WARM_UP, COOL_DOWN }

/** How the body is placed during an exercise; [FLOOR] covers lying, kneeling and all fours. */
@Serializable
enum class BodyPosition { STANDING, SEATED, FLOOR }

/** What is worth knowing before picking an exercise; never stored, see [Exercise.tags]. */
enum class ExerciseTag { NO_JUMPING, EACH_SIDE, HOLD, TIMED }
