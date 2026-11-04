package ch.coachpoulpy.export.fit

import kotlin.time.Instant

/** The FIT base types this encoder writes, with their id and size in bytes. */
internal enum class FitType(val id: Int, val size: Int, val max: Long) {
    ENUM(0x00, 1, 0xFE),
    UINT8(0x02, 1, 0xFE),
    SINT16(0x83, 2, 0x7FFE),
    UINT16(0x84, 2, 0xFFFE),
    UINT32(0x86, 4, 0xFFFFFFFE),
    UINT32Z(0x8C, 4, 0xFFFFFFFF),
}

/** One field of a message: its number in the FIT profile, its type and its raw value (already scaled). */
internal data class FitField(val number: Int, val type: FitType, val value: Long)

/** One message: its global number in the FIT profile and the fields it has a value for. */
internal class FitMessage(val global: Int) {
    val fields = mutableListOf<FitField>()

    /** Adds the field when [value] is known; a value out of the type's range is kept just inside it. */
    fun put(number: Int, type: FitType, value: Long?): FitMessage {
        if (value != null) {
            val min = if (type == FitType.SINT16) -0x7FFFL else if (type == FitType.UINT32Z) 1L else 0L
            fields += FitField(number, type, value.coerceIn(min, type.max))
        }
        return this
    }

    fun time(number: Int, at: Instant?): FitMessage = put(number, FitType.UINT32, at?.let(::fitTime))
}

/** Seconds since the FIT epoch, 1989-12-31 00:00 UTC. */
internal fun fitTime(at: Instant): Long = at.epochSeconds - FIT_EPOCH_UNIX_SECONDS

private const val FIT_EPOCH_UNIX_SECONDS = 631_065_600L

/**
 * Writes a FIT file: the 14-byte header, each message after the definition it needs, and the CRC
 * at the end. A definition is written again only when a message's fields change.
 */
internal class FitWriter {
    private val body = ArrayList<Byte>()
    private val locals = arrayOfNulls<List<Any>>(LOCAL_TYPES)
    private var nextLocal = 0

    fun write(message: FitMessage) {
        val signature = listOf<Any>(message.global) + message.fields.map { it.number to it.type }
        var local = locals.indexOf(signature)
        if (local < 0) {
            local = nextLocal
            nextLocal = (nextLocal + 1) % LOCAL_TYPES
            locals[local] = signature
            body += (0x40 or local).toByte()
            body += 0 // reserved
            body += 0 // little endian
            put(message.global.toLong(), 2)
            body += message.fields.size.toByte()
            message.fields.forEach {
                body += it.number.toByte()
                body += it.type.size.toByte()
                body += it.type.id.toByte()
            }
        }
        body += local.toByte()
        message.fields.forEach { put(it.value, it.type.size) }
    }

    fun bytes(): ByteArray {
        val header = ArrayList<Byte>(HEADER_SIZE)
        header += HEADER_SIZE.toByte()
        header += PROTOCOL_VERSION.toByte()
        header.putLittleEndian(PROFILE_VERSION.toLong(), 2)
        header.putLittleEndian(body.size.toLong(), 4)
        ".FIT".forEach { header += it.code.toByte() }
        header.putLittleEndian(crc(header).toLong(), 2)
        val file = header + body
        return (file + crc(file).let { listOf(it.toByte(), (it shr 8).toByte()) }).toByteArray()
    }

    private fun put(value: Long, size: Int) = body.putLittleEndian(value, size)

    private fun MutableList<Byte>.putLittleEndian(value: Long, size: Int) {
        for (i in 0 until size) this += (value shr (8 * i)).toByte()
    }

    companion object {
        private const val LOCAL_TYPES = 16
        private const val HEADER_SIZE = 14
        private const val PROTOCOL_VERSION = 0x20
        private const val PROFILE_VERSION = 2132

        private val CRC_TABLE = intArrayOf(
            0x0000, 0xCC01, 0xD801, 0x1400, 0xF001, 0x3C00, 0x2800, 0xE401,
            0xA001, 0x6C00, 0x7800, 0xB401, 0x5000, 0x9C01, 0x8801, 0x4400,
        )

        /** The CRC-16 the FIT protocol puts after the header and at the end of the file. */
        fun crc(bytes: List<Byte>): Int {
            var crc = 0
            for (byte in bytes) {
                val b = byte.toInt() and 0xFF
                var tmp = CRC_TABLE[crc and 0xF]
                crc = (crc shr 4) and 0x0FFF
                crc = crc xor tmp xor CRC_TABLE[b and 0xF]
                tmp = CRC_TABLE[crc and 0xF]
                crc = (crc shr 4) and 0x0FFF
                crc = crc xor tmp xor CRC_TABLE[(b shr 4) and 0xF]
            }
            return crc
        }
    }
}
