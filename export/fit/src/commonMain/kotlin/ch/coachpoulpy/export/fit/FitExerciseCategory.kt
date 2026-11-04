package ch.coachpoulpy.export.fit

import ch.coachpoulpy.core.model.ExerciseSlug

/**
 * The FIT exercise category of a catalog exercise, which Garmin Connect and Strava name a set after. FIT has
 * about fifty categories and no way to name an exercise itself, so each slug takes the nearest one. A slug
 * with none (the stretches) is left out and shows as unknown.
 */
internal object FitExerciseCategory {
    fun of(slug: ExerciseSlug): Long? = byName[slug.value]

    private const val BENCH_PRESS = 0L
    private const val CALF_RAISE = 1L
    private const val CARDIO = 2L
    private const val CARRY = 3L
    private const val CORE = 5L
    private const val CRUNCH = 6L
    private const val CURL = 7L
    private const val DEADLIFT = 8L
    private const val HIP_RAISE = 10L
    private const val HIP_STABILITY = 11L
    private const val HIP_SWING = 12L
    private const val LUNGE = 17L
    private const val PLANK = 19L
    private const val PLYO = 20L
    private const val PULL_UP = 21L
    private const val PUSH_UP = 22L
    private const val ROW = 23L
    private const val SHOULDER_PRESS = 24L
    private const val SHOULDER_STABILITY = 25L
    private const val SQUAT = 28L
    private const val TOTAL_BODY = 29L
    private const val TRICEPS_EXTENSION = 30L
    private const val WARM_UP = 31L
    private const val RUN = 32L

    private val byName: Map<String, Long> = buildMap {
        listOf("goblet-squat", "bodyweight-squat", "chair-squat", "wall-sit").forEach { put(it, SQUAT) }
        put("calf-raise", CALF_RAISE)
        listOf("glute-bridge", "donkey-kick").forEach { put(it, HIP_RAISE) }
        listOf("romanian-deadlift", "good-morning").forEach { put(it, DEADLIFT) }
        put("kettlebell-swing", HIP_SWING)
        listOf("reverse-lunge", "walking-lunge", "step-up").forEach { put(it, LUNGE) }
        listOf("push-up", "knee-push-up", "incline-push-up", "diamond-push-up", "one-arm-push-up").forEach { put(it, PUSH_UP) }
        put("dumbbell-bench-press", BENCH_PRESS)
        put("overhead-press", SHOULDER_PRESS)
        put("bench-dip", TRICEPS_EXTENSION)
        listOf("dumbbell-row", "band-row").forEach { put(it, ROW) }
        put("band-pull-apart", SHOULDER_STABILITY)
        put("pull-up", PULL_UP)
        put("biceps-curl", CURL)
        put("farmer-carry", CARRY)
        listOf("plank", "side-plank").forEach { put(it, PLANK) }
        listOf("dead-bug", "bird-dog", "bird-dog-knee-tuck", "bird-dog-crunch", "hollow-hold", "russian-twist").forEach { put(it, CORE) }
        put("crunch", CRUNCH)
        put("skater-jump", PLYO)
        put("burpee", TOTAL_BODY)
        listOf("jumping-jack", "high-knees", "mountain-climber", "jump-rope").forEach { put(it, CARDIO) }
        listOf("walk-with-support", "gentle-walk", "walk", "brisk-walk").forEach { put(it, CARDIO) }
        listOf("easy-run", "run").forEach { put(it, RUN) }
        listOf("cat-cow", "arm-circles", "hip-circles", "worlds-greatest-stretch").forEach { put(it, WARM_UP) }
        put("clamshell", HIP_STABILITY)
    }
}
