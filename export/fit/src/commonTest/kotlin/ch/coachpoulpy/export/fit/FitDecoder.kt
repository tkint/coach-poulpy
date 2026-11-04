package ch.coachpoulpy.export.fit

import kotlin.test.assertEquals

/** One data message read back from a FIT file: its global number and raw field values by field number. */
data class DecodedMessage(val global: Int, val fields: Map<Int, Long>)

/**
 * Reads back what [FitWriter] writes, checking the header, both CRCs and that every data message has
 * its definition, so the tests can look at the messages a session gives.
 */
fun decodeFit(bytes: ByteArray): List<DecodedMessage> {
    fun u(i: Int) = bytes[i].toInt() and 0xFF
    fun le(at: Int, size: Int): Long = (0 until size).fold(0L) { sum, i -> sum or (u(at + i).toLong() shl (8 * i)) }

    val headerSize = u(0)
    assertEquals(14, headerSize)
    assertEquals(".FIT", (8..11).map { u(it).toChar() }.joinToString(""))
    assertEquals(le(12, 2).toInt(), FitWriter.crc(bytes.take(12)), "header CRC")
    val dataSize = le(4, 4).toInt()
    assertEquals(bytes.size, headerSize + dataSize + 2, "data size")
    assertEquals(le(bytes.size - 2, 2).toInt(), FitWriter.crc(bytes.take(bytes.size - 2)), "file CRC")

    class Definition(val global: Int, val fields: List<Triple<Int, Int, Int>>)
    val definitions = mutableMapOf<Int, Definition>()
    val messages = mutableListOf<DecodedMessage>()
    var at = headerSize
    while (at < headerSize + dataSize) {
        val header = u(at++)
        assertEquals(0, header and 0x80, "normal headers only")
        val local = header and 0x0F
        if (header and 0x40 != 0) {
            assertEquals(0, u(at + 1), "little endian")
            val global = le(at + 2, 2).toInt()
            val count = u(at + 4)
            at += 5
            definitions[local] = Definition(global, List(count) { i -> Triple(u(at + 3 * i), u(at + 3 * i + 1), u(at + 3 * i + 2)) })
            at += 3 * count
        } else {
            val definition = definitions.getValue(local)
            val fields = mutableMapOf<Int, Long>()
            definition.fields.forEach { (number, size, type) ->
                val raw = le(at, size)
                fields[number] = if (type == 0x83) raw.toShort().toLong() else raw
                at += size
            }
            messages += DecodedMessage(definition.global, fields)
        }
    }
    return messages
}
