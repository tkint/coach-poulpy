package ch.coachpoulpy.device.ftms

import kotlin.math.roundToInt

/**
 * Fitness Machine Control Point commands. Not sent in v1, which only reads: they are
 * here so the encoding is tested now and control is a UI change later. A client must
 * send [requestControl] and get [ControlResult.SUCCESS] before any other command.
 */
object ControlPoint {
    const val REQUEST_CONTROL = 0x00
    const val RESET = 0x01
    const val SET_TARGET_SPEED = 0x02
    const val SET_TARGET_INCLINATION = 0x03
    const val START_OR_RESUME = 0x07
    const val STOP_OR_PAUSE = 0x08
    const val RESPONSE_CODE = 0x80

    fun requestControl() = byteArrayOf(REQUEST_CONTROL.toByte())
    fun reset() = byteArrayOf(RESET.toByte())
    fun start() = byteArrayOf(START_OR_RESUME.toByte())
    fun stop() = byteArrayOf(STOP_OR_PAUSE.toByte(), 0x01)
    fun pause() = byteArrayOf(STOP_OR_PAUSE.toByte(), 0x02)

    /** uint16 in 0.01 km/h. */
    fun setTargetSpeed(kmh: Double): ByteArray = command(SET_TARGET_SPEED, (kmh * 100).roundToInt())

    /** sint16 in 0.1 %. */
    fun setTargetIncline(percent: Double): ByteArray = command(SET_TARGET_INCLINATION, (percent * 10).roundToInt())

    private fun command(opCode: Int, value: Int) =
        byteArrayOf(opCode.toByte(), (value and 0xFF).toByte(), ((value shr 8) and 0xFF).toByte())

    /** Decodes the indication the machine answers each command with: 0x80, the request's op code, a result. */
    fun response(bytes: ByteArray): Pair<Int, ControlResult>? {
        if (bytes.size < 3 || (bytes[0].toInt() and 0xFF) != RESPONSE_CODE) return null
        val result = ControlResult.entries.firstOrNull { it.code == (bytes[2].toInt() and 0xFF) } ?: ControlResult.OPERATION_FAILED
        return (bytes[1].toInt() and 0xFF) to result
    }
}

enum class ControlResult(val code: Int) {
    SUCCESS(0x01),
    NOT_SUPPORTED(0x02),
    INVALID_PARAMETER(0x03),
    OPERATION_FAILED(0x04),
    CONTROL_NOT_PERMITTED(0x05),
}
