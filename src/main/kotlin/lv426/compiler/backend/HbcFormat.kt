package lv426.compiler.backend

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/** Values in HBC's deduplicated constant pool. */
sealed interface HbcConstant {
    data class IntValue(val value: Long) : HbcConstant
    data class RealValue(val value: Double) : HbcConstant
    data class BoolValue(val value: Boolean) : HbcConstant
    data class StringValue(val value: String) : HbcConstant
    data class TimeValue(val milliseconds: Long) : HbcConstant
}

enum class Opcode(val code: Int) {
    PUSH_CONST(0x01), LOAD_LOCAL(0x02), STORE_LOCAL(0x03), LOAD_GLOBAL(0x04), STORE_GLOBAL(0x05),
    NEGATE(0x10), NOT(0x11), ADD(0x20), SUBTRACT(0x21), MULTIPLY(0x22), DIVIDE(0x23), MODULO(0x24),
    EQUAL(0x30), NOT_EQUAL(0x31), LESS(0x32), LESS_OR_EQUAL(0x33), GREATER(0x34), GREATER_OR_EQUAL(0x35),
    AND(0x36), OR(0x37), JUMP(0x40), JUMP_IF_FALSE(0x41), CALL(0x50), EMIT(0x51), RETURN(0x60),
    RETURN_VALUE(0x61), POP(0x62), NOP(0x00);

    companion object {
        fun fromCode(code: Int): Opcode = entries.firstOrNull { it.code == code }
            ?: throw HbcFormatException("Unknown opcode 0x${code.toString(16)}")
    }
}

data class HbcFunction(
    val nameConstant: Int,
    val parameterCount: Int,
    val localCount: Int,
    val code: ByteArray
) {
    override fun equals(other: Any?): Boolean = other is HbcFunction &&
        nameConstant == other.nameConstant && parameterCount == other.parameterCount &&
        localCount == other.localCount && code.contentEquals(other.code)

    override fun hashCode(): Int = 31 * (31 * (31 * nameConstant + parameterCount) + localCount) + code.contentHashCode()
}

data class HbcModule(val constants: List<HbcConstant>, val functions: List<HbcFunction>) {
    /** Validates even manually constructed modules before serializing them. */
    fun toBytes(): ByteArray {
        HbcVerifier.validate(this)
        return ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { out ->
                out.write(MAGIC)
                out.writeShort(VERSION)
                out.writeShort(constants.size)
                constants.forEach { it.writeTo(out) }
                out.writeShort(functions.size)
                functions.forEach {
                    out.writeShort(it.nameConstant)
                    out.writeShort(it.parameterCount)
                    out.writeShort(it.localCount)
                    out.writeInt(it.code.size)
                    out.write(it.code)
                }
            }
            bytes.toByteArray()
        }
    }

    companion object {
        // A caller may modify the returned array without changing the file format.
        val MAGIC: ByteArray get() = byteArrayOf('H'.code.toByte(), 'B'.code.toByte(), 'C'.code.toByte(), 0)
        const val VERSION: Int = 1
    }
}

class HbcFormatException(message: String) : IllegalArgumentException(message)

private fun HbcConstant.writeTo(out: DataOutputStream) = when (this) {
    is HbcConstant.IntValue -> { out.writeByte(1); out.writeLong(value) }
    is HbcConstant.RealValue -> { out.writeByte(2); out.writeDouble(value) }
    is HbcConstant.BoolValue -> { out.writeByte(3); out.writeBoolean(value) }
    is HbcConstant.StringValue -> {
        val encoded = HbcUtf8.encode(value)
        out.writeByte(4); out.writeShort(encoded.size); out.write(encoded)
    }
    is HbcConstant.TimeValue -> { out.writeByte(5); out.writeLong(milliseconds) }
}

/** Report malformed Unicode instead of silently replacing it with a different value. */
internal object HbcUtf8 {
    fun encode(value: String): ByteArray {
        if (value.length > 0xffff) throw HbcFormatException("String constant exceeds 65535 UTF-8 bytes")
        val encoded = try {
            StandardCharsets.UTF_8.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .encode(CharBuffer.wrap(value))
        } catch (_: CharacterCodingException) {
            throw HbcFormatException("String constant contains invalid Unicode")
        }
        if (encoded.remaining() > 0xffff) throw HbcFormatException("String constant exceeds 65535 UTF-8 bytes")
        return ByteArray(encoded.remaining()).also { encoded.get(it) }
    }

    fun decode(bytes: ByteArray): String = try {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes)).toString()
    } catch (_: CharacterCodingException) {
        throw HbcFormatException("Malformed UTF-8 string constant")
    }
}
