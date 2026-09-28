package lv426.compiler.backend

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.math.BigDecimal
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
    NEGATE(0x10), NOT(0x11), INT_TO_REAL(0x12), ADD(0x20), SUBTRACT(0x21), MULTIPLY(0x22), DIVIDE(0x23), MODULO(0x24),
    EQUAL(0x30), NOT_EQUAL(0x31), LESS(0x32), LESS_OR_EQUAL(0x33), GREATER(0x34), GREATER_OR_EQUAL(0x35),
    AND(0x36), OR(0x37), JUMP(0x40), JUMP_IF_FALSE(0x41), CALL(0x50), EMIT(0x51), RETURN(0x60),
    RETURN_VALUE(0x61), POP(0x62), NOP(0x00),
    NEW_STRUCT(0x70), LOAD_FIELD(0x71), STORE_FIELD(0x72), NEW_ARRAY(0x73), NEW_LIST(0x74),
    LOAD_INDEX(0x75), STORE_INDEX(0x76), LENGTH(0x77), LIST_APPEND(0x78), LIST_REMOVE(0x79),
    NEW_ARRAY_INIT(0x7a);

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

sealed interface HbcType {
    data object IntType : HbcType
    data object RealType : HbcType
    data object BoolType : HbcType
    data object StringType : HbcType
    data object TimeType : HbcType
    data class Struct(val name: String) : HbcType
    data class Array(val element: HbcType, val size: Int) : HbcType
    data class ListType(val element: HbcType) : HbcType
}

data class HbcField(val nameConstant: Int, val typeIndex: Int)
data class HbcStruct(val nameConstant: Int, val fields: List<HbcField>)
data class HbcGlobal(val nameConstant: Int, val typeIndex: Int, val initializerFunction: Int, val mutable: Boolean = true)
data class HbcEvent(val nameConstant: Int, val parameterTypes: List<Int>)
enum class HbcHandlerKind(val tag: Int) { EVENT(1), START(2), EVERY(3), AT(4) }
data class HbcHandler(
    val kind: HbcHandlerKind,
    val functionIndex: Int,
    val eventIndex: Int = 0,
    val milliseconds: Long = 0
)

data class HbcModule(
    val constants: List<HbcConstant>,
    val functions: List<HbcFunction>,
    val types: List<HbcType> = emptyList(),
    val structs: List<HbcStruct> = emptyList(),
    val globals: List<HbcGlobal> = emptyList(),
    val events: List<HbcEvent> = emptyList(),
    val handlers: List<HbcHandler> = emptyList(),
    val metadataPresent: Boolean = types.isNotEmpty() || structs.isNotEmpty() || globals.isNotEmpty() ||
        events.isNotEmpty() || handlers.isNotEmpty()
) {
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
                if (metadataPresent) {
                    out.write(METADATA_MAGIC)
                    out.writeShort(types.size)
                    types.forEach { it.writeTo(out) }
                    out.writeShort(structs.size)
                    structs.forEach { struct ->
                        out.writeShort(struct.nameConstant)
                        out.writeShort(struct.fields.size)
                        struct.fields.forEach {
                            out.writeShort(it.nameConstant)
                            out.writeShort(it.typeIndex)
                        }
                    }
                    out.writeShort(globals.size)
                    globals.forEach {
                        out.writeShort(it.nameConstant)
                        out.writeShort(it.typeIndex)
                        out.writeShort(it.initializerFunction)
                        out.writeBoolean(it.mutable)
                    }
                    out.writeShort(events.size)
                    events.forEach { event ->
                        out.writeShort(event.nameConstant)
                        out.writeShort(event.parameterTypes.size)
                        event.parameterTypes.forEach(out::writeShort)
                    }
                    out.writeShort(handlers.size)
                    handlers.forEach {
                        out.writeByte(it.kind.tag)
                        out.writeShort(it.functionIndex)
                        when (it.kind) {
                            HbcHandlerKind.EVENT -> out.writeShort(it.eventIndex)
                            HbcHandlerKind.START -> Unit
                            HbcHandlerKind.EVERY, HbcHandlerKind.AT -> out.writeLong(it.milliseconds)
                        }
                    }
                }
            }
            bytes.toByteArray()
        }
    }

    companion object {
        // A caller may modify the returned array without changing the file format.
        val MAGIC: ByteArray get() = byteArrayOf('H'.code.toByte(), 'B'.code.toByte(), 'C'.code.toByte(), 0)
        const val VERSION: Int = 1
        internal val METADATA_MAGIC: ByteArray get() = byteArrayOf('M'.code.toByte(), 'E'.code.toByte(), 'T'.code.toByte(), 'A'.code.toByte())
    }
}

class HbcFormatException(message: String) : IllegalArgumentException(message)

enum class HbcTimeUnit(val milliseconds: Long) {
    MS(1), SEC(1000), MIN(60_000), HOUR(3_600_000), DAY(86_400_000)
}

object HbcTime {
    fun milliseconds(value: String, unit: HbcTimeUnit): Long = try {
        BigDecimal(value).multiply(BigDecimal.valueOf(unit.milliseconds)).longValueExact()
    } catch (_: NumberFormatException) {
        throw HbcFormatException("Invalid duration literal '$value'")
    } catch (_: ArithmeticException) {
        throw HbcFormatException("Duration '$value' $unit must fit an exact i64 millisecond value")
    }

    fun milliseconds(value: Double, unit: HbcTimeUnit): Long {
        if (!value.isFinite()) throw HbcFormatException("Duration must be finite")
        return milliseconds(value.toString(), unit)
    }
}

private fun HbcType.writeTo(out: DataOutputStream): Unit = when (this) {
    HbcType.IntType -> out.writeByte(1)
    HbcType.RealType -> out.writeByte(2)
    HbcType.BoolType -> out.writeByte(3)
    HbcType.StringType -> out.writeByte(4)
    HbcType.TimeType -> out.writeByte(5)
    is HbcType.Struct -> {
        out.writeByte(6)
        val bytes = HbcUtf8.encode(name)
        out.writeShort(bytes.size)
        out.write(bytes)
    }
    is HbcType.Array -> {
        out.writeByte(7)
        out.writeInt(size)
        element.writeTo(out)
    }
    is HbcType.ListType -> {
        out.writeByte(8)
        element.writeTo(out)
    }
}

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
