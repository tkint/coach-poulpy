package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.WorkoutFormat

/**
 * What happens to the exercises when a workout's format changes (ADR 0013): they are done in
 * the order the new format does them, or in the order they were already done in.
 */
enum class FormatSwitch {
    /** Reorder the exercises the way the new format does them: A A A B B B ⇄ A B, A B, A B. */
    FOLLOW_NEW_FORMAT,

    /** Do the exercises in the same order as before; only the steps are rewritten to fit the format. */
    KEEP_ORDER,
}

/**
 * This draft in [format], with its steps rewritten as [switch] says. A pure function: the result
 * always passes `Workout.problems()` when the draft did. Changing to the format the draft
 * already has returns it as it is.
 *
 * Sets → Circuit, following the format: in each work part, each run of steps outside a repeat
 * becomes one repeat of as many rounds as its largest set count, each exercise keeping one set
 * and the longest rest between sets closing the round. Repeats already there stay. Keeping the
 * order: each exercise of several sets becomes a repeat of that many rounds of that exercise and
 * its rest. Everything else stays; an exercise inside a repeat already there drops to one set,
 * as a circuit has no sets.
 *
 * Circuit → Sets, following the format: each repeat of R rounds becomes its exercises in order,
 * each with R sets and the longest rest of the repeat (or the default) between them; a walk or a
 * run cannot have sets, so it stays in a repeat of R rounds of its own. Keeping the order: only
 * the format changes.
 *
 * The warm-up and the cool down always keep the order.
 */
internal fun WorkoutDraft.switchedTo(format: WorkoutFormat, switch: FormatSwitch): WorkoutDraft {
    if (this.format == format) return this
    val follow = switch == FormatSwitch.FOLLOW_NEW_FORMAT
    val parts = parts.map { part ->
        val steps = when (format) {
            WorkoutFormat.CIRCUIT -> if (follow && part.kind == PartKind.WORK) part.steps.toRounds() else part.steps.keepingOrderAsCircuit()
            WorkoutFormat.SETS -> if (follow && part.kind == PartKind.WORK) part.steps.toSets() else part.steps
        }
        part.copy(steps = steps)
    }
    return copy(format = format, parts = parts)
}

/** A circuit has no sets: one set, whatever the exercise had. */
private fun DraftStep.withOneSet(): DraftStep = if (this is DraftEntry) copy(sets = 1) else this

private fun DraftRepeat.withOneSetEach() = copy(steps = steps.map { it.withOneSet() })

private fun newKey() = Ids.newUuidV7()

internal fun DraftEntry.hasSets() = !walkOrRun && sets > 1

/** Sets → Circuit, keeping the order: an exercise of N sets becomes N rounds of that exercise and its rest. */
private fun List<DraftStep>.keepingOrderAsCircuit(): List<DraftStep> = flatMap { step ->
    when {
        step is DraftRepeat -> listOf(step.withOneSetEach())
        step is DraftEntry && step.hasSets() -> {
            val rest = if (step.restBetweenSets > 0) listOf(DraftRest(newKey(), step.restBetweenSets)) else emptyList()
            listOf(DraftRepeat(newKey(), step.sets, listOf(step.copy(sets = 1)) + rest))
        }
        else -> listOf(step.withOneSet())
    }
}

/** Sets → Circuit, following the format: each run of steps outside a repeat becomes one repeat. */
private fun List<DraftStep>.toRounds(): List<DraftStep> {
    val result = mutableListOf<DraftStep>()
    val run = mutableListOf<DraftStep>()
    fun closeRun() {
        val exercises = run.filterIsInstance<DraftEntry>()
        if (exercises.isEmpty()) {
            result += run
        } else {
            val rounds = exercises.maxOf { if (it.walkOrRun) 1 else it.sets }
            val rest = exercises.filter { it.hasSets() }.maxOfOrNull { it.restBetweenSets } ?: 0
            val closing = if (rounds > 1 && rest > 0) listOf(DraftRest(newKey(), rest)) else emptyList()
            result += DraftRepeat(newKey(), rounds, run.map { it.withOneSet() } + closing)
        }
        run.clear()
    }
    for (step in this) {
        if (step is DraftRepeat) {
            closeRun()
            result += step.withOneSetEach()
        } else {
            run += step
        }
    }
    closeRun()
    return result
}

/** Circuit → Sets, following the format: each repeat of R rounds becomes its exercises with R sets each. */
private fun List<DraftStep>.toSets(): List<DraftStep> = flatMap { step ->
    if (step !is DraftRepeat) return@flatMap listOf(step)
    val rest = step.steps.filterIsInstance<DraftRest>().maxOfOrNull { it.seconds }
    step.steps.filterIsInstance<DraftEntry>().flatMap { entry ->
        if (entry.walkOrRun) {
            // A walk or a run has no sets: it keeps its rounds, with the rest after it.
            val after = if (step.times > 1 && rest != null) listOf(DraftRest(newKey(), rest)) else emptyList()
            if (step.times > 1) listOf(DraftRepeat(newKey(), step.times, listOf(entry) + after)) else listOf(entry)
        } else {
            listOf(entry.copy(sets = step.times, restBetweenSets = rest ?: DraftEntry.DEFAULT_REST_BETWEEN_SETS))
        }
    }
}
