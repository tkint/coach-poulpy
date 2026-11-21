package ch.coachpoulpy.shared

import ch.coachpoulpy.data.local.LocalStore
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ch.coachpoulpy.core.engine.TimelineCompiler
import kotlinx.datetime.TimeZone

/** The badges Achievements can hold. More come later; the first three are the ones confirmed on #519. */
enum class Badge(val key: String) {
    FIRST_RECORD("firstRecord"),
    GOAL_MET("goalMet"),
    FIRST_WALK("firstWalk"),
}

/**
 * What the Home row and Achievements show: [earned] oldest first, and the badges still hidden. A locked badge is never
 * named, only drawn as "?".
 */
data class AchievementsUi(val earned: List<Badge> = emptyList()) {
    val locked: List<Badge> get() = Badge.entries.filter { it !in earned }

    /** The most recent badges first, as many as [count], for the motivation row on Home. */
    fun latest(count: Int): List<Badge> = earned.asReversed().take(count)
}

/** When a badge is earned. */
object Badges {
    /** Above this average speed a treadmill session is a run, not a walk. */
    const val WALK_MAX_KMH = 6.5

    /**
     * The badges the finished sessions in [rows] earn, in the order of [Badge].
     *
     * CLEANUP: First record and Goal met stay locked: a record (#527) and a weekly goal are not defined yet.
     */
    fun earnedFrom(rows: List<SessionRow>): List<Badge> = buildList {
        if (rows.any(::isWalk)) add(Badge.FIRST_WALK)
    }

    /** A treadmill session that covered some distance at a walking speed. */
    fun isWalk(row: SessionRow): Boolean {
        val speed = row.averageSpeedKmh ?: return false
        return row.kind == SessionKind.TREADMILL && (row.distanceMeters ?: 0.0) > 0 && speed > 0 && speed <= WALK_MAX_KMH
    }
}

/**
 * The badges earned so far, kept on the phone: a badge is never taken back, even when the session that earned it is
 * deleted from the history.
 */
class AchievementLedger(private val memory: KeyValueStore) {
    /** The badges earned so far plus [computed], oldest first; new ones are saved at the end. */
    fun earned(computed: List<Badge>): List<Badge> {
        val saved = parse(memory.getString(KEY))
        val all = saved + computed.filter { it !in saved }
        if (all != saved) memory.putString(KEY, all.joinToString(",") { it.key })
        return all
    }

    private fun parse(text: String?): List<Badge> =
        text.orEmpty().split(',').mapNotNull { key -> Badge.entries.firstOrNull { it.key == key } }.distinct()

    internal companion object {
        const val KEY = "achievements.earned"
    }
}

/** The Achievements page: Poulpy with his tentacles, and the badges. */
data class AchievementsState(
    val loaded: Boolean = false,
    val poulpy: PoulpyStatus? = null,
    val achievements: AchievementsUi = AchievementsUi(),
    /** Sessions still to do for a full octopus; 0 when it is full. */
    val sessionsToFull: Int = PoulpyStatus.TENTACLES,
)

class AchievementsModel internal constructor(
    store: LocalStore,
    compiler: TimelineCompiler,
    languageCode: String,
    private val ledger: AchievementLedger,
    private val painActive: () -> Boolean,
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val labels = Labels(languageCode)
    private val coach = PoulpyCoach(languageCode, zone)

    val state: StateFlow<AchievementsState> = combine(store.finishedSessions(), store.finishedSessionEnds()) { sessions, ends ->
        val now = clock.now()
        val rows = sessions.mapNotNull { session -> session.realTotals?.let { sessionRow(session, it, labels) } }
        val poulpy = coach.status(ends, now, painActive())
        AchievementsState(
            loaded = true,
            poulpy = poulpy,
            achievements = AchievementsUi(ledger.earned(Badges.earnedFrom(rows))),
            sessionsToFull = (PoulpyStatus.TENTACLES - poulpy.tentaclesLit).coerceAtLeast(0),
        )
    }.stateIn(scope, SharingStarted.Eagerly, AchievementsState())

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: AchievementsState get() = state.value

    fun watch(onChange: (AchievementsState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun close() = scope.cancel()
}
