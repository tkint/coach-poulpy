package ch.coachpoulpy.device.ftms

/**
 * What the Fitness Machine Status characteristic (0x2ADA) reports: mostly the user
 * pressing buttons on the treadmill. Optional in FTMS, so some machines never send it.
 */
sealed interface MachineStatus {
    data object Reset : MachineStatus
    data object StoppedByUser : MachineStatus
    data object PausedByUser : MachineStatus
    data object StoppedBySafetyKey : MachineStatus
    data object StartedOrResumedByUser : MachineStatus
    data class TargetSpeedChanged(val kmh: Double) : MachineStatus
    data class TargetInclineChanged(val percent: Double) : MachineStatus
    data object ControlPermissionLost : MachineStatus

    /** An op code this app does not use, such as resistance or power targets. */
    data class Other(val opCode: Int) : MachineStatus
}

object MachineStatusDecoder {
    fun decode(bytes: ByteArray): MachineStatus {
        val reader = ByteReader(bytes)
        return when (val opCode = reader.uint8()) {
            0x01 -> MachineStatus.Reset
            // The parameter says which: 0x01 stop, 0x02 pause. Some machines leave it out; treat that as a stop.
            0x02 -> if (bytes.size > 1 && reader.uint8() == 0x02) MachineStatus.PausedByUser else MachineStatus.StoppedByUser
            0x03 -> MachineStatus.StoppedBySafetyKey
            0x04 -> MachineStatus.StartedOrResumedByUser
            0x05 -> MachineStatus.TargetSpeedChanged(reader.uint16() / 100.0)
            0x06 -> MachineStatus.TargetInclineChanged(reader.sint16() / 10.0)
            0xFF -> MachineStatus.ControlPermissionLost
            else -> MachineStatus.Other(opCode)
        }
    }
}

enum class BeltState {
    /** No reading yet. */
    UNKNOWN,
    MOVING,

    /** The user paused on the treadmill's console. */
    PAUSED,

    /** Stopped by the user or the safety key, or not started yet. */
    STOPPED,
}

/**
 * Whether the belt is moving, from the machine's status events when it sends them and
 * from the speed otherwise. A machine without status events cannot tell a pause from a
 * stop, so a belt at 0 km/h reads as stopped.
 */
class BeltMonitor {
    var state = BeltState.UNKNOWN
        private set

    fun onStatus(status: MachineStatus) {
        state = when (status) {
            MachineStatus.PausedByUser -> BeltState.PAUSED
            MachineStatus.StoppedByUser, MachineStatus.StoppedBySafetyKey, MachineStatus.Reset -> BeltState.STOPPED
            MachineStatus.StartedOrResumedByUser -> BeltState.MOVING
            else -> state
        }
    }

    private var lastSpeed = 0.0

    fun onReading(speedKmh: Double?) {
        speedKmh ?: return
        val halted = state == BeltState.PAUSED || state == BeltState.STOPPED
        state = when {
            // After a pause or stop the belt slows down for a few seconds: only speeding up means it moves again.
            halted -> if (speedKmh > lastSpeed) BeltState.MOVING else state
            speedKmh > 0 -> BeltState.MOVING
            else -> BeltState.STOPPED
        }
        lastSpeed = speedKmh
    }
}
