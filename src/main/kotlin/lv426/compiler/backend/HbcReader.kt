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
            if (version != HbcModule.VERSION) throw HbcFormatException("Unsupported HBC version: $version")
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
            if (input.available() != 0) throw HbcFormatException("Trailing bytes in HBC file")
            HbcModule(constants, functions).also(HbcVerifier::validate)
        }
    } catch (_: EOFException) {
        throw HbcFormatException("Truncated HBC file")
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
