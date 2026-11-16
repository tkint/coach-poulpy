package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.shared.FormatSwitch.FOLLOW_NEW_FORMAT
import ch.coachpoulpy.shared.FormatSwitch.KEEP_ORDER
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Switching a workout's format (#243): the conversion rules, in both directions, with both choices. */
class FormatSwitchTest {
    private var next = 0
    private fun key() = "k${next++}"

    private fun exercise(slug: String, sets: Int = 1, amount: Int = 10, rest: Int = 60, perSide: Boolean = false) =
        DraftEntry(key(), ExerciseSlug(slug), MeasurementMode.REPS, perSide, sets, amount, null, restBetweenSets = rest)

    private fun run(slug: String = "run", minutes: Int = 5) =
        DraftEntry(key(), ExerciseSlug(slug), MeasurementMode.TIME, false, 1, minutes * 60, null, walkOrRun = true)

    private fun draft(format: WorkoutFormat, vararg parts: DraftPart) = WorkoutDraft(null, 1, "Test", format, parts.toList())
    private fun work(vararg steps: DraftStep) = DraftPart(key(), PartKind.WORK, steps.toList())
    private fun part(kind: PartKind, vararg steps: DraftStep) = DraftPart(key(), kind, steps.toList())

    /** A readable summary of the steps: "squat x3", "[3: bench, row, rest 90]", "rest 60". */
    private fun DraftStep.show(): String = when (this) {
        is DraftEntry -> if (walkOrRun || sets == 1) exercise.value else "${exercise.value} x$sets/$restBetweenSets"
        is DraftRest -> "rest $seconds"
        is DraftRepeat -> "[$times: ${steps.joinToString(", ") { it.show() }}]"
    }

    private fun WorkoutDraft.shown() = parts.map { p -> p.steps.joinToString(" | ") { it.show() } }

    private fun WorkoutDraft.assertValid() {
        val workout = toWorkout { "auto" }
        assertEquals(emptySet(), workout.problems())
    }

    private fun WorkoutDraft.to(format: WorkoutFormat, switch: FormatSwitch) = switchedTo(format, switch).also { it.assertValid() }

    private val benchAndRow get() = draft(
        WorkoutFormat.SETS,
        work(exercise("bench", sets = 3, amount = 8, rest = 90), exercise("row", sets = 3, amount = 10, rest = 60)),
    )

    @Test
    fun `sets to circuit following the format makes one repeat of rounds`() {
        val result = benchAndRow.to(WorkoutFormat.CIRCUIT, FOLLOW_NEW_FORMAT)
        assertEquals(WorkoutFormat.CIRCUIT, result.format)
        assertEquals(listOf("[3: bench, row, rest 90]"), result.shown())
        val repeat = result.parts.single().steps.single() as DraftRepeat
        assertEquals(listOf(8, 10), repeat.steps.filterIsInstance<DraftEntry>().map { it.amount })
    }

    @Test
    fun `sets to circuit keeping the order makes a repeat for each exercise`() {
        val result = benchAndRow.to(WorkoutFormat.CIRCUIT, KEEP_ORDER)
        assertEquals(listOf("[3: bench, rest 90] | [3: row, rest 60]"), result.shown())
    }

    @Test
    fun `keeping the order leaves out the rest of an exercise with none`() {
        val result = draft(WorkoutFormat.SETS, work(exercise("plank", sets = 2, rest = 0), exercise("curl")))
            .to(WorkoutFormat.CIRCUIT, KEEP_ORDER)
        assertEquals(listOf("[2: plank] | curl"), result.shown())
    }

    @Test
    fun `rounds are the largest set count and the longest rest closes the round`() {
        val result = draft(WorkoutFormat.SETS, work(exercise("a", sets = 2, rest = 30), exercise("b", sets = 4, rest = 120), exercise("c")))
            .to(WorkoutFormat.CIRCUIT, FOLLOW_NEW_FORMAT)
        assertEquals(listOf("[4: a, b, c, rest 120]"), result.shown())
    }

    @Test
    fun `one set each makes one round with no closing rest`() {
        val result = draft(WorkoutFormat.SETS, work(exercise("a"), exercise("b"))).to(WorkoutFormat.CIRCUIT, FOLLOW_NEW_FORMAT)
        assertEquals(listOf("[1: a, b]"), result.shown())
    }

    @Test
    fun `a rest already in the run stays where it is`() {
        val result = draft(WorkoutFormat.SETS, work(exercise("a", sets = 3, rest = 45), DraftRest("r", 20), exercise("b", sets = 3, rest = 45)))
            .to(WorkoutFormat.CIRCUIT, FOLLOW_NEW_FORMAT)
        assertEquals(listOf("[3: a, rest 20, b, rest 45]"), result.shown())
    }

    @Test
    fun `existing supersets stay and the runs around them become repeats`() {
        val superset = DraftRepeat("s", 3, listOf(exercise("x"), exercise("y")))
        val result = draft(WorkoutFormat.SETS, work(exercise("a", sets = 2), superset, exercise("b", sets = 3)))
            .to(WorkoutFormat.CIRCUIT, FOLLOW_NEW_FORMAT)
        assertEquals(listOf("[2: a, rest 60] | [3: x, y] | [3: b, rest 60]"), result.shown())
    }

    @Test
    fun `an exercise with sets inside an existing repeat drops to one set`() {
        val superset = DraftRepeat("s", 3, listOf(exercise("x", sets = 4), exercise("y")))
        for (switch in FormatSwitch.entries) {
            val result = draft(WorkoutFormat.SETS, work(superset)).to(WorkoutFormat.CIRCUIT, switch)
            assertEquals(listOf("[3: x, y]"), result.shown())
        }
    }

    @Test
    fun `per side exercises keep their sides in both directions`() {
        val toCircuit = draft(WorkoutFormat.SETS, work(exercise("lunge", sets = 3, perSide = true))).to(WorkoutFormat.CIRCUIT, FOLLOW_NEW_FORMAT)
        val lunge = (toCircuit.parts.single().steps.single() as DraftRepeat).steps.filterIsInstance<DraftEntry>().single()
        assertTrue(lunge.perSide)
        assertEquals(1, lunge.sets)
        val back = toCircuit.to(WorkoutFormat.SETS, FOLLOW_NEW_FORMAT)
        val again = back.parts.single().steps.single() as DraftEntry
        assertTrue(again.perSide)
        assertEquals(3, again.sets)
    }

    @Test
    fun `a walk or a run keeps one set going to a circuit`() {
        for (switch in FormatSwitch.entries) {
            val result = draft(WorkoutFormat.SETS, work(run(), exercise("a", sets = 3))).to(WorkoutFormat.CIRCUIT, switch)
            val entries = result.leaves.filterIsInstance<DraftEntry>()
            assertTrue(entries.all { it.sets == 1 })
        }
        assertEquals(listOf("[3: run, a, rest 60]"), draft(WorkoutFormat.SETS, work(run(), exercise("a", sets = 3))).to(WorkoutFormat.CIRCUIT, FOLLOW_NEW_FORMAT).shown())
    }

    @Test
    fun `warm up and cool down keep the order in a switch that follows the format`() {
        val sets = draft(
            WorkoutFormat.SETS,
            part(PartKind.WARM_UP, exercise("jog", sets = 2, rest = 20)),
            work(exercise("a", sets = 3)),
            part(PartKind.COOL_DOWN, exercise("stretch", sets = 2, rest = 10)),
        )
        val circuit = sets.to(WorkoutFormat.CIRCUIT, FOLLOW_NEW_FORMAT)
        assertEquals(listOf("[2: jog, rest 20]", "[3: a, rest 60]", "[2: stretch, rest 10]"), circuit.shown())
        val back = draft(
            WorkoutFormat.CIRCUIT,
            part(PartKind.WARM_UP, DraftRepeat("w", 2, listOf(exercise("jog")))),
            work(DraftRepeat("m", 3, listOf(exercise("a")))),
        ).to(WorkoutFormat.SETS, FOLLOW_NEW_FORMAT)
        assertEquals(listOf("[2: jog]", "a x3/60"), back.shown())
    }

    @Test
    fun `circuit to sets following the format gives each exercise the rounds as sets`() {
        val circuit = draft(
            WorkoutFormat.CIRCUIT,
            work(DraftRepeat("r", 4, listOf(exercise("squat", amount = 15), exercise("pushup"), DraftRest("rest", 30)))),
        )
        val result = circuit.to(WorkoutFormat.SETS, FOLLOW_NEW_FORMAT)
        assertEquals(WorkoutFormat.SETS, result.format)
        assertEquals(listOf("squat x4/30 | pushup x4/30"), result.shown())
        assertEquals(listOf(15, 10), result.leaves.filterIsInstance<DraftEntry>().map { it.amount })
    }

    @Test
    fun `circuit to sets uses the longest rest, or the default of 60 s`() {
        val two = draft(WorkoutFormat.CIRCUIT, work(DraftRepeat("r", 3, listOf(exercise("a"), DraftRest("x", 20), exercise("b"), DraftRest("y", 45)))))
        assertEquals(listOf("a x3/45 | b x3/45"), two.to(WorkoutFormat.SETS, FOLLOW_NEW_FORMAT).shown())
        val none = draft(WorkoutFormat.CIRCUIT, work(DraftRepeat("r", 3, listOf(exercise("a"), exercise("b")))))
        assertEquals(listOf("a x3/60 | b x3/60"), none.to(WorkoutFormat.SETS, FOLLOW_NEW_FORMAT).shown())
    }

    @Test
    fun `circuit to sets keeps a walk or a run in a repeat of its own with the rest after it`() {
        val circuit = draft(WorkoutFormat.CIRCUIT, work(DraftRepeat("r", 4, listOf(exercise("squat"), run(), DraftRest("x", 30), exercise("pushup")))))
        assertEquals(listOf("squat x4/30 | [4: run, rest 30] | pushup x4/30"), circuit.to(WorkoutFormat.SETS, FOLLOW_NEW_FORMAT).shown())
        val noRest = draft(WorkoutFormat.CIRCUIT, work(DraftRepeat("r", 4, listOf(run(), exercise("squat")))))
        assertEquals(listOf("[4: run] | squat x4/60"), noRest.to(WorkoutFormat.SETS, FOLLOW_NEW_FORMAT).shown())
    }

    @Test
    fun `circuit to sets keeping the order only changes the format`() {
        val circuit = draft(WorkoutFormat.CIRCUIT, work(DraftRepeat("r", 4, listOf(exercise("squat"), exercise("pushup"), DraftRest("x", 30)))))
        val result = circuit.to(WorkoutFormat.SETS, KEEP_ORDER)
        assertEquals(circuit.parts, result.parts)
        assertEquals(WorkoutFormat.SETS, result.format)
    }

    @Test
    fun `an empty workout changes only its format`() {
        val empty = draft(WorkoutFormat.SETS, work())
        for (switch in FormatSwitch.entries) {
            assertEquals(empty.parts, empty.to(WorkoutFormat.CIRCUIT, switch).parts)
        }
    }

    @Test
    fun `switching to the same format changes nothing`() {
        val same = benchAndRow
        for (switch in FormatSwitch.entries) assertEquals(same, same.switchedTo(WorkoutFormat.SETS, switch))
    }

    @Test
    fun `following the format and back keeps the exercises`() {
        val back = benchAndRow.to(WorkoutFormat.CIRCUIT, FOLLOW_NEW_FORMAT).to(WorkoutFormat.SETS, FOLLOW_NEW_FORMAT)
        assertEquals(listOf("bench x3/90 | row x3/90"), back.shown())
    }
}
