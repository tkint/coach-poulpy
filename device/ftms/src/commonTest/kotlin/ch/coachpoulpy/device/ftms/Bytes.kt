package ch.coachpoulpy.device.ftms

/** Builds a little-endian packet: each pair is (value, size in bytes). */
fun packet(vararg fields: Pair<Int, Int>): ByteArray = fields.flatMap { (value, size) ->
    (0 until size).map { ((value shr (8 * it)) and 0xFF).toByte() }
}.toByteArray()
