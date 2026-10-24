package ch.coachpoulpy.device.ftms

import kotlin.uuid.Uuid

/** The Bluetooth Fitness Machine Service (FTMS) and the characteristics the app uses. */
object FtmsUuids {
    val service: Uuid = short(0x1826)

    /** What the machine can report and which targets it accepts. Read once on connect. */
    val feature: Uuid = short(0x2ACC)

    /** Live treadmill readings, notified about once a second. */
    val treadmillData: Uuid = short(0x2ACD)

    val supportedSpeedRange: Uuid = short(0x2AD4)
    val supportedInclinationRange: Uuid = short(0x2AD5)

    /** Commands (request control, set speed and incline, start, stop). Not used until control lands. */
    val controlPoint: Uuid = short(0x2AD9)

    /** State changes the machine reports, such as started by the user or speed changed. */
    val machineStatus: Uuid = short(0x2ADA)

    /** A 16-bit Bluetooth SIG number expanded onto the Bluetooth base UUID. */
    fun short(value: Int): Uuid = Uuid.parse("0000${value.toString(16).padStart(4, '0')}-0000-1000-8000-00805f9b34fb")
}
