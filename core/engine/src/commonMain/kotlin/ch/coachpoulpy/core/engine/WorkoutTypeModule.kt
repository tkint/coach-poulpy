package ch.coachpoulpy.core.engine

import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutType
import kotlin.reflect.KClass

/**
 * Everything specific to one workout type lives behind this interface, so a new
 * type is added as a new module without touching the others.
 */
interface WorkoutTypeModule {
    val type: WorkoutType

    /** Targets this type accepts in a prescription; anything else is rejected by validation. */
    val acceptedTargets: Set<KClass<out Target>>

    /** How a work phase built from this set finishes during a session. */
    fun completionRule(set: SetPrescription): CompletionRule
}

sealed interface CompletionRule {
    /** The user taps "done". */
    data object Manual : CompletionRule

    /** The phase ends on its own when its duration has elapsed. */
    data object Timer : CompletionRule
}

/** Looks up the module for a workout type. Built once at app start from the type modules. */
class WorkoutTypeRegistry(modules: List<WorkoutTypeModule>) {
    private val byType = modules.associateBy { it.type }

    operator fun get(type: WorkoutType): WorkoutTypeModule =
        byType[type] ?: error("No module registered for workout type $type")
}
