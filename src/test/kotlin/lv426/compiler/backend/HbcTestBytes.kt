package lv426.compiler.backend

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/** An independent writer for deliberately malformed files; bypasses production validation. */
internal data class RawHbcFunction(
    val code: ByteArray = rawCode(0x60),
    val name: Int = 0,
    val parameters: Int = 0,
    val locals: Int = parameters,
    val declaredSize: Int = code.size
)

internal fun rawCode(vararg bytes: Int): ByteArray = ByteArray(bytes.size) { bytes[it].toByte() }

internal fun encodedBytes(write: DataOutputStream.() -> Unit): ByteArray =
    ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { it.write() }
        bytes.toByteArray()
    }

internal fun rawString(value: String): ByteArray = encodedBytes {
    val utf8 = value.toByteArray(Charsets.UTF_8)
    writeByte(4)
    writeShort(utf8.size)
    write(utf8)
}

internal fun rawHbc(
    constants: List<ByteArray> = listOf(rawString("main")),
    functions: List<RawHbcFunction> = listOf(RawHbcFunction())
): ByteArray = encodedBytes {
    write(rawCode(0x48, 0x42, 0x43, 0))
    writeShort(1)
    writeShort(constants.size)
    constants.forEach { write(it) }
    writeShort(functions.size)
    functions.forEach {
        writeShort(it.name)
        writeShort(it.parameters)
        writeShort(it.locals)
        writeInt(it.declaredSize)
        write(it.code)
    }
}
