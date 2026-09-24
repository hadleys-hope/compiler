package lv426.compiler.backend

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.EOFException

/** Strict reader useful to the VM and to tests that validate generated files. */
object HbcReader {
    fun read(bytes: ByteArray): HbcModule = try {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            val magic = ByteArray(4)
            input.readFully(magic)
            if (!magic.contentEquals(HbcModule.MAGIC)) throw HbcFormatException("Not an HBC file")
            val version = input.readUnsignedShort()
            if (version != HbcModule.VERSION) {
                throw HbcFormatException("Unsupported HBC version: $version")
            }
            val constants = List(input.readUnsignedShort()) { readConstant(input) }
            val functions = List(input.readUnsignedShort()) {
                val name = input.readUnsignedShort()
                val parameters = input.readUnsignedShort()
                val locals = input.readUnsignedShort()
                val size = input.readInt()
                if (size < 0 || size > input.available()) throw HbcFormatException("Invalid function bytecode size: $size")
                val code = ByteArray(size)
                input.readFully(code)
                HbcFunction(name, parameters, locals, code)
            }
            val module = if (input.available() != 0) {
                val marker = ByteArray(4)
                input.readFully(marker)
                if (!marker.contentEquals(HbcModule.METADATA_MAGIC)) throw HbcFormatException("Unknown HBC metadata section")
                val types = List(input.readUnsignedShort()) { readType(input) }
                val structs = List(input.readUnsignedShort()) {
                    HbcStruct(input.readUnsignedShort(), List(input.readUnsignedShort()) {
                        HbcField(input.readUnsignedShort(), input.readUnsignedShort())
                    })
                }
                val globals = List(input.readUnsignedShort()) {
                    HbcGlobal(input.readUnsignedShort(), input.readUnsignedShort(), input.readUnsignedShort(),
                        when (val flag = input.readUnsignedByte()) {
                            0 -> false
                            1 -> true
                            else -> throw HbcFormatException("Invalid global mutability: $flag")
                        })
                }
                val events = List(input.readUnsignedShort()) {
                    HbcEvent(input.readUnsignedShort(), List(input.readUnsignedShort()) { input.readUnsignedShort() })
                }
                val handlers = List(input.readUnsignedShort()) {
                    val tag = input.readUnsignedByte()
                    val kind = HbcHandlerKind.entries.find { it.tag == tag }
                        ?: throw HbcFormatException("Unknown handler kind: $tag")
                    val function = input.readUnsignedShort()
                    when (kind) {
                        HbcHandlerKind.EVENT -> HbcHandler(kind, function, eventIndex = input.readUnsignedShort())
                        HbcHandlerKind.START -> HbcHandler(kind, function)
                        HbcHandlerKind.EVERY, HbcHandlerKind.AT -> HbcHandler(kind, function, milliseconds = input.readLong())
                    }
                }
                HbcModule(constants, functions, types, structs, globals, events, handlers, metadataPresent = true)
            } else HbcModule(constants, functions)
            if (input.available() != 0) throw HbcFormatException("Trailing bytes in HBC file")
            module.also(HbcVerifier::validate)
        }
    } catch (_: EOFException) {
        throw HbcFormatException("Truncated HBC file")
    }

    private fun readType(input: DataInputStream, depth: Int = 0): HbcType {
        if (depth >= 64) throw HbcFormatException("Type nesting exceeds 64 levels")
        return when (val tag = input.readUnsignedByte()) {
            1 -> HbcType.IntType
            2 -> HbcType.RealType
            3 -> HbcType.BoolType
            4 -> HbcType.StringType
            5 -> HbcType.TimeType
            6 -> {
                val bytes = ByteArray(input.readUnsignedShort())
                input.readFully(bytes)
                HbcType.Struct(HbcUtf8.decode(bytes))
            }
            7 -> {
                val size = input.readInt()
                HbcType.Array(readType(input, depth + 1), size)
            }
            8 -> HbcType.ListType(readType(input, depth + 1))
            else -> throw HbcFormatException("Unknown type tag: $tag")
        }
    }

    private fun readConstant(input: DataInputStream): HbcConstant = when (val tag = input.readUnsignedByte()) {
        1 -> HbcConstant.IntValue(input.readLong())
        2 -> HbcConstant.RealValue(input.readDouble())
        3 -> when (val value = input.readUnsignedByte()) {
            0 -> HbcConstant.BoolValue(false)
            1 -> HbcConstant.BoolValue(true)
            else -> throw HbcFormatException("Invalid boolean constant: $value (expected 0 or 1)")
        }
        4 -> {
            val bytes = ByteArray(input.readUnsignedShort())
            input.readFully(bytes)
            HbcConstant.StringValue(HbcUtf8.decode(bytes))
        }
        5 -> HbcConstant.TimeValue(input.readLong())
        else -> throw HbcFormatException("Unknown constant tag: $tag")
    }
}
