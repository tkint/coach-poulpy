package ch.coachpoulpy.export.fit

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.model.Load
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A finished session written as a FIT activity file, which Garmin Connect and Strava import for free
 * (ADR 0011). The file holds, in order of time: the timer's start, pauses and stop; a record per
 * machine reading; a lap per phase for a walk or a run, or one lap for the whole session; a set per
 * phase done or rested for strength training, with its planned reps and weight; then the session and
 * the activity.
 */
object FitActivity {

    /**
     * The file for the session [events] tell, run against [timeline] (null when its plan is no longer on
     * the phone) from [workout], whose loads the sets carry. [serialNumber] tells this export apart from
     * other devices' files, and [utcOffsetSeconds] is where the session was done. Null when it never started.
     */
    fun encode(
        events: List<SessionEvent>,
        timeline: Timeline?,
        workout: Workout?,
        sport: FitSport,
        serialNumber: Long,
        utcOffsetSeconds: Int = 0,
    ): ByteArray? {
        val finish = events.indexOfFirst { it is SessionEvent.Finished }
        val log = if (finish < 0) events else events.subList(0, finish + 1)
        val start = log.firstOrNull { it is SessionEvent.Started }?.at ?: return null
        val end = maxOf(start, log.lastOrNull { it is SessionEvent.Finished }?.at ?: log.maxOf { it.at })
        val pauses = pauses(log, start, end)
        fun timer(from: Instant, to: Instant): Duration =
            to - from - pauses.fold(Duration.ZERO) { sum, (pausedAt, resumedAt) ->
                sum + (minOf(to, resumedAt) - maxOf(from, pausedAt)).coerceAtLeast(Duration.ZERO)
            }

        // Phases in log order, each with the readings taken while it ran.
        val distance = Counter()
        val energy = Counter()
        val readings = mutableListOf<Reading>()
        val spans = mutableListOf<Span>()
        var index = 0
        var from = start
        var current = mutableListOf<Reading>()
        for (event in log) when (event) {
            is SessionEvent.PhaseCompleted, is SessionEvent.PhaseSkipped -> {
                val to = maxOf(from, event.at)
                spans += Span(timeline?.phaseAt(index), from, to, event is SessionEvent.PhaseSkipped, current)
                index++
                from = to
                current = mutableListOf()
            }
            is SessionEvent.Sample -> if (event.at >= start) {
                val metrics = event.metrics
                val reading = Reading(event, distance.add(metrics.distanceMeters), energy.add(metrics.energyKcal?.toDouble()))
                readings += reading
                current += reading
            }
            else -> Unit
        }
        if (end > from && (timeline == null || timeline.phaseAt(index) != null)) spans += Span(timeline?.phaseAt(index), from, end, false, current)

        val timed = mutableListOf<Timed>()

        timed += Timed(start, 0, timerEvent(start, TYPE_START))
        pauses.forEach { (pausedAt, resumedAt) ->
            timed += Timed(pausedAt, 0, timerEvent(pausedAt, TYPE_STOP_ALL))
            if (resumedAt < end) timed += Timed(resumedAt, 0, timerEvent(resumedAt, TYPE_START))
        }
        timed += Timed(end, 0, timerEvent(end, TYPE_STOP_ALL))

        readings.forEach { timed += Timed(it.sample.at, 1, record(it)) }
        if (readings.isEmpty()) {
            timed += Timed(start, 1, FitMessage(RECORD).time(TIMESTAMP, start))
            timed += Timed(end, 1, FitMessage(RECORD).time(TIMESTAMP, end))
        }

        if (sport == FitSport.STRENGTH_TRAINING) {
            var setIndex = 0
            spans.forEach { span ->
                val phase = span.phase
                if (phase is Phase.Work && span.skipped) return@forEach
                if (phase !is Phase.Work && phase !is Phase.Rest) return@forEach
                val set = FitMessage(SET)
                    .time(SET_TIMESTAMP, span.to)
                    .put(0, FitType.UINT32, timer(span.from, span.to).inWholeMilliseconds)
                if (phase is Phase.Work) {
                    val kg = (workout?.let { loadOf(it, phase.step, phase.setIndex) } as? Load.Kilograms)?.kg
                    set.put(3, FitType.UINT16, (phase.target as? Target.Reps)?.count?.toLong())
                        .put(4, FitType.UINT16, kg?.let { (it * 16).roundToLong() })
                        .put(5, FitType.UINT8, SET_ACTIVE)
                        .put(9, FitType.UINT16, kg?.let { UNIT_KILOGRAM })
                } else {
                    set.put(5, FitType.UINT8, SET_REST)
                }
                set.time(6, span.from).put(10, FitType.UINT16, (setIndex++).toLong())
                timed += Timed(span.to, 2, set)
            }
        }

        val perPhase = sport.sport == SPORT_RUNNING && spans.isNotEmpty()
        val laps = if (perPhase) spans else listOf(Span(null, start, end, false, readings))
        // A lap's distance and energy run from the last reading before it, so the laps add up to the session.
        var distanceBefore = 0.0
        var energyBefore = 0.0
        laps.forEachIndexed { lapIndex, span ->
            val lastDistance = span.readings.lastOrNull { it.distance != null }?.distance
            val lastEnergy = span.readings.lastOrNull { it.energy != null }?.energy
            val totals = Totals(
                timer = timer(span.from, span.to),
                distance = lastDistance?.let { it - distanceBefore },
                energy = lastEnergy?.let { it - energyBefore },
                readings = span.readings,
            )
            lastDistance?.let { distanceBefore = it }
            lastEnergy?.let { energyBefore = it }
            val lap = FitMessage(LAP)
                .time(TIMESTAMP, span.to)
                .put(MESSAGE_INDEX, FitType.UINT16, lapIndex.toLong())
                .put(0, FitType.ENUM, EVENT_LAP)
                .put(1, FitType.ENUM, TYPE_STOP)
                .time(2, span.from)
                .put(7, FitType.UINT32, (span.to - span.from).inWholeMilliseconds)
                .put(8, FitType.UINT32, totals.timer.inWholeMilliseconds)
                .put(9, FitType.UINT32, totals.distance?.let { (it * 100).roundToLong() })
                .put(11, FitType.UINT16, totals.energy?.roundToLong())
                .put(13, FitType.UINT16, totals.averageSpeed?.let { (it * 1000).roundToLong() })
                .put(15, FitType.UINT8, totals.averageHeartRate)
                .put(16, FitType.UINT8, totals.maxHeartRate)
                .put(23, FitType.ENUM, intensity(span.phase))
                .put(24, FitType.ENUM, if (lapIndex == laps.lastIndex) TRIGGER_SESSION_END else TRIGGER_MANUAL)
                .put(25, FitType.ENUM, sport.sport.toLong())
                .put(39, FitType.ENUM, sport.subSport.toLong())
            timed += Timed(span.to, 3, lap)
        }

        val writer = FitWriter()
        writer.write(
            FitMessage(FILE_ID)
                .put(0, FitType.ENUM, FILE_ACTIVITY)
                .put(1, FitType.UINT16, MANUFACTURER_DEVELOPMENT)
                .put(2, FitType.UINT16, 0)
                .put(3, FitType.UINT32Z, serialNumber and 0xFFFFFFFFL)
                .time(4, start),
        )
        // The device that made the file and the sport it holds: every activity file from a watch or a bike computer has both.
        writer.write(
            FitMessage(DEVICE_INFO)
                .time(TIMESTAMP, start)
                .put(0, FitType.UINT8, 0) // the file's creator
                .put(2, FitType.UINT16, MANUFACTURER_DEVELOPMENT)
                .put(3, FitType.UINT32Z, serialNumber and 0xFFFFFFFFL)
                .put(4, FitType.UINT16, 0),
        )
        writer.write(
            FitMessage(SPORT_MESSAGE)
                .put(0, FitType.ENUM, sport.sport.toLong())
                .put(1, FitType.ENUM, sport.subSport.toLong()),
        )
        timed.sortedWith(compareBy<Timed> { it.at }.thenBy { it.order }).forEach { writer.write(it.message) }

        val session = Totals(
            timer = timer(start, end),
            distance = readings.lastOrNull { it.distance != null }?.distance,
            energy = readings.lastOrNull { it.energy != null }?.energy,
            readings = readings,
        )
        writer.write(
            FitMessage(SESSION)
                .time(TIMESTAMP, end)
                .put(MESSAGE_INDEX, FitType.UINT16, 0)
                .put(0, FitType.ENUM, EVENT_SESSION)
                .put(1, FitType.ENUM, TYPE_STOP)
                .time(2, start)
                .put(5, FitType.ENUM, sport.sport.toLong())
                .put(6, FitType.ENUM, sport.subSport.toLong())
                .put(7, FitType.UINT32, (end - start).inWholeMilliseconds)
                .put(8, FitType.UINT32, session.timer.inWholeMilliseconds)
                .put(9, FitType.UINT32, session.distance?.let { (it * 100).roundToLong() })
                .put(11, FitType.UINT16, session.energy?.roundToLong())
                .put(14, FitType.UINT16, session.averageSpeed?.let { (it * 1000).roundToLong() })
                .put(16, FitType.UINT8, session.averageHeartRate)
                .put(17, FitType.UINT8, session.maxHeartRate)
                .put(25, FitType.UINT16, 0)
                .put(26, FitType.UINT16, laps.size.toLong())
                .put(28, FitType.ENUM, 0), // activity end
        )
        writer.write(
            FitMessage(ACTIVITY)
                .time(TIMESTAMP, end)
                .put(0, FitType.UINT32, session.timer.inWholeMilliseconds)
                .put(1, FitType.UINT16, 1)
                .put(2, FitType.ENUM, 0) // manual
                .put(3, FitType.ENUM, EVENT_ACTIVITY)
                .put(4, FitType.ENUM, TYPE_STOP)
                .put(5, FitType.UINT32, fitTime(end) + utcOffsetSeconds),
        )
        return writer.bytes()
    }

    /** One machine reading, with the distance and energy done since the first reading of the session. */
    private class Reading(val sample: SessionEvent.Sample, val distance: Double?, val energy: Double?)

    /** One phase as it ran: from its start to the moment it was completed or skipped. */
    private class Span(val phase: Phase?, val from: Instant, val to: Instant, val skipped: Boolean, val readings: List<Reading>)

    private class Timed(val at: Instant, val order: Int, val message: FitMessage)

    /** What a lap or the session sums up; [distance] in metres, [energy] in kcal. */
    private class Totals(val timer: Duration, val distance: Double?, val energy: Double?, readings: List<Reading>) {
        private val heartRates = readings.mapNotNull { it.sample.metrics.heartRateBpm }
        val averageHeartRate: Long? = heartRates.takeIf { it.isNotEmpty() }?.average()?.roundToLong()
        val maxHeartRate: Long? = heartRates.maxOrNull()?.toLong()

        /** Metres per second: the distance over the time when both are known, else the readings' average. */
        val averageSpeed: Double? = if (distance != null && timer > Duration.ZERO) {
            distance / (timer.inWholeMilliseconds / 1000.0)
        } else {
            readings.mapNotNull { it.sample.metrics.speedKmh }.takeIf { it.isNotEmpty() }?.average()?.div(3.6)
        }
    }

    /**
     * A machine's running total as done since the session's first reading. A total that goes down
     * (the machine was restarted) counts again from its new value, so the sum never goes back.
     */
    private class Counter {
        private var total = 0.0
        private var last: Double? = null

        fun add(raw: Double?): Double? {
            if (raw == null) return null
            last?.let { if (raw >= it) total += raw - it }
            last = raw
            return total
        }
    }

    private fun record(reading: Reading): FitMessage {
        val metrics = reading.sample.metrics
        return FitMessage(RECORD)
            .time(TIMESTAMP, reading.sample.at)
            .put(3, FitType.UINT8, metrics.heartRateBpm?.toLong())
            // FIT counts a run's cadence in strides, two steps each.
            .put(4, FitType.UINT8, metrics.cadenceSpm?.let { (it / 2).toLong() })
            .put(5, FitType.UINT32, reading.distance?.let { (it * 100).roundToLong() })
            .put(6, FitType.UINT16, metrics.speedKmh?.let { (it / 3.6 * 1000).roundToLong() })
            .put(7, FitType.UINT16, metrics.powerWatts?.toLong())
            .put(9, FitType.SINT16, metrics.inclinePercent?.let { (it * 100).roundToLong() })
    }

    private fun timerEvent(at: Instant, type: Long): FitMessage =
        FitMessage(EVENT)
            .time(TIMESTAMP, at)
            .put(0, FitType.ENUM, EVENT_TIMER)
            .put(1, FitType.ENUM, type)
            .put(4, FitType.UINT8, 0)

    /** Each pause after [start], from its start to the resume, or to [end] when never resumed. */
    private fun pauses(events: List<SessionEvent>, start: Instant, end: Instant): List<Pair<Instant, Instant>> {
        val spans = mutableListOf<Pair<Instant, Instant>>()
        var pausedAt: Instant? = null
        for (event in events) when (event) {
            is SessionEvent.Paused -> if (pausedAt == null) pausedAt = event.at.coerceIn(start, end)
            is SessionEvent.Resumed -> {
                pausedAt?.let { spans += it to event.at.coerceIn(it, end) }
                pausedAt = null
            }
            else -> Unit
        }
        pausedAt?.let { spans += it to end }
        return spans
    }

    /** FIT's lap intensity: warm-up, cool down, rest (a rest or a recovery step), or active. */
    private fun intensity(phase: Phase?): Long? = when (phase) {
        null -> null
        is Phase.Rest -> 1
        is Phase.Work -> when {
            phase.role == StepRole.RECOVER -> 1
            phase.part == PartKind.WARM_UP -> 2
            phase.part == PartKind.COOL_DOWN -> 3
            else -> 0
        }
    }

    /** The planned load of set [setIndex] of the step [id] in [workout]. */
    private fun loadOf(workout: Workout, id: StepId, setIndex: Int): Load? {
        fun find(steps: List<Step>): Step.ExerciseStep? = steps.firstNotNullOfOrNull { step ->
            when (step) {
                is Step.ExerciseStep -> step.takeIf { it.id == id }
                is Step.Group -> find(step.children)
                is Step.Rest -> null
            }
        }
        return find(workout.steps)?.prescription?.sets?.getOrNull(setIndex)?.load
    }

    // Message numbers and values from the FIT profile.
    private const val FILE_ID = 0
    private const val SPORT_MESSAGE = 12
    private const val SESSION = 18
    private const val LAP = 19
    private const val RECORD = 20
    private const val EVENT = 21
    private const val DEVICE_INFO = 23
    private const val ACTIVITY = 34
    private const val SET = 225

    private const val TIMESTAMP = 253
    private const val MESSAGE_INDEX = 254

    /** The set message has its timestamp where the others have their index. */
    private const val SET_TIMESTAMP = 254

    private const val FILE_ACTIVITY = 4L
    private const val MANUFACTURER_DEVELOPMENT = 255L
    private const val SPORT_RUNNING = 1

    private const val EVENT_TIMER = 0L
    private const val EVENT_SESSION = 8L
    private const val EVENT_LAP = 9L
    private const val EVENT_ACTIVITY = 26L
    private const val TYPE_START = 0L
    private const val TYPE_STOP = 1L
    private const val TYPE_STOP_ALL = 4L

    private const val TRIGGER_MANUAL = 0L
    private const val TRIGGER_SESSION_END = 7L

    private const val SET_REST = 0L
    private const val SET_ACTIVE = 1L
    private const val UNIT_KILOGRAM = 1L
}
