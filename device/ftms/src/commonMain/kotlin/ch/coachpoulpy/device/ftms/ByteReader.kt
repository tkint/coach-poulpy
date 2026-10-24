package ch.coachpoulpy.device.ftms

/** Reads the little-endian fields FTMS characteristics are made of, front to back. */
internal class ByteReader(private val bytes: ByteArray) {
    private var position = 0

    fun uint8(): Int = unsigned(1).toInt()
    fun uint16(): Int = unsigned(2).toInt()
    fun uint24(): Int = unsigned(3).toInt()
    fun uint32(): Long = unsigned(4)
    fun sint16(): Int = uint16().let { if (it >= 0x8000) it - 0x10000 else it }

    private fun unsigned(size: Int): Long {
        if (position + size > bytes.size) {
            throw FtmsFormatException("A $size-byte field at byte $position runs past the ${bytes.size} bytes received")
        }
        var value = 0L
        for (i in 0 until size) value = value or ((bytes[position + i].toLong() and 0xFF) shl (8 * i))
        position += size
        return value
    }
}

/** The machine sent bytes that do not match the layout its flags announce. */
class FtmsFormatException(message: String) : IllegalArgumentException(message)
