package lv426.compiler.backend

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HbcReaderValidationTest {
    @Test fun `every truncated prefix of a complete file is rejected`() {
        val complete = rawHbc()
        assertEquals(1, HbcReader.read(complete).functions.size)
        for (size in complete.indices) {
            assertFailsWith<HbcFormatException>("Accepted prefix of length $size") {
                HbcReader.read(complete.copyOf(size))
            }
        }
    }

    @Test fun `bad magic version and trailing bytes are rejected`() {
        val valid = rawHbc()
        val invalid = listOf(
            valid.copyOf().also { it[0] = 0 },
            valid.copyOf().also { it[5] = 2 },
            valid + rawCode(0)
        )
        invalid.forEach { assertFailsWith<HbcFormatException> { HbcReader.read(it) } }
    }

    @Test fun `unknown constant tags and noncanonical booleans are rejected`() {
        val invalidConstants = listOf(rawCode(0), rawCode(6), rawCode(255), rawCode(3, 2), rawCode(3, 255))
        invalidConstants.forEach { constant ->
            assertFailsWith<HbcFormatException> {
                HbcReader.read(rawHbc(constants = listOf(rawString("main"), constant)))
            }
        }
    }

    @Test fun `malformed utf8 is rejected instead of silently replaced`() {
        val invalidStrings = listOf(
            rawCode(0xc3, 0x28), // Invalid continuation byte.
            rawCode(0xc0, 0xaf), // Overlong encoding.
            rawCode(0xed, 0xa0, 0x80), // UTF-8 encoding of a surrogate.
            rawCode(0xf4, 0x90, 0x80, 0x80), // Above Unicode's maximum code point.
            rawCode(0xe2, 0x82) // Missing final continuation byte.
        )
        invalidStrings.forEach { utf8 ->
            val constant = encodedBytes { writeByte(4); writeShort(utf8.size); write(utf8) }
            assertFailsWith<HbcFormatException> {
                HbcReader.read(rawHbc(constants = listOf(rawString("main"), constant)))
            }
        }
    }

    @Test fun `all constant types including unicode retain their values and bytes`() {
        val constants = listOf(
            HbcConstant.StringValue("main"), HbcConstant.IntValue(Long.MIN_VALUE),
            HbcConstant.RealValue(-0.0), HbcConstant.BoolValue(false), HbcConstant.BoolValue(true),
            HbcConstant.StringValue("Привет, 🌍\u0000"), HbcConstant.TimeValue(Long.MAX_VALUE)
        )
        val bytes = rawHbc(constants = listOf(
            rawString("main"), encodedBytes { writeByte(1); writeLong(Long.MIN_VALUE) },
            encodedBytes { writeByte(2); writeDouble(-0.0) }, rawCode(3, 0), rawCode(3, 1),
            rawString("Привет, 🌍\u0000"), encodedBytes { writeByte(5); writeLong(Long.MAX_VALUE) }
        ))
        val module = HbcReader.read(bytes)
        assertEquals(constants, module.constants)
        assertContentEquals(bytes, module.toBytes())
    }

    @Test fun `invalid function frames names and code lengths are rejected`() {
        val functions = listOf(
            RawHbcFunction(name = 1),
            RawHbcFunction(parameters = 1, locals = 0),
            RawHbcFunction(declaredSize = -1),
            RawHbcFunction(declaredSize = Int.MAX_VALUE),
            RawHbcFunction(code = byteArrayOf())
        )
        functions.forEach { function ->
            assertFailsWith<HbcFormatException> { HbcReader.read(rawHbc(functions = listOf(function))) }
        }
        assertFailsWith<HbcFormatException> {
            HbcReader.read(rawHbc(constants = listOf(rawCode(3, 1))))
        }
    }

    @Test fun `duplicate or blank function names are rejected`() {
        assertFailsWith<HbcFormatException> {
            HbcReader.read(rawHbc(functions = listOf(RawHbcFunction(), RawHbcFunction())))
        }
        // Duplicate names are invalid even if they use different constant-pool entries.
        assertFailsWith<HbcFormatException> {
            HbcReader.read(rawHbc(
                constants = listOf(rawString("main"), rawString("main")),
                functions = listOf(RawHbcFunction(), RawHbcFunction(name = 1))
            ))
        }
        for (name in listOf("", " \t\n")) {
            assertFailsWith<HbcFormatException> {
                HbcReader.read(rawHbc(constants = listOf(rawString(name))))
            }
        }
    }

    @Test fun `every operand-bearing instruction rejects a partial operand`() {
        val operandWidths = mapOf(
            0x01 to 2, 0x02 to 2, 0x03 to 2, 0x04 to 2, 0x05 to 2,
            0x40 to 4, 0x41 to 4, 0x50 to 4, 0x51 to 4
        )
        operandWidths.forEach { (opcode, width) ->
            for (available in 0 until width) {
                val code = rawCode(opcode) + ByteArray(available)
                assertFailsWith<HbcFormatException>("opcode $opcode with $available operand bytes") {
                    HbcReader.read(rawHbc(functions = listOf(RawHbcFunction(code))))
                }
            }
        }
    }

    @Test fun `unknown opcodes and invalid constant references are rejected`() {
        val codes = listOf(rawCode(0xff), rawCode(0x01, 0, 1, 0x61), rawCode(0x04, 0, 1, 0x61))
        codes.forEach { code ->
            assertFailsWith<HbcFormatException> {
                HbcReader.read(rawHbc(functions = listOf(RawHbcFunction(code))))
            }
        }
        val numericName = encodedBytes { writeByte(1); writeLong(10) }
        for (opcode in listOf(0x04, 0x05, 0x50, 0x51)) {
            val operands = if (opcode >= 0x50) rawCode(0, 1, 0, 0) else rawCode(0, 1)
            assertFailsWith<HbcFormatException> {
                HbcReader.read(rawHbc(
                    constants = listOf(rawString("main"), numericName),
                    functions = listOf(RawHbcFunction(rawCode(opcode) + operands + rawCode(0x60)))
                ))
            }
        }
    }

    @Test fun `unreachable malformed instructions are still rejected`() {
        for (invalid in listOf(rawCode(0xff), rawCode(0x02, 0, 1), rawCode(0x01, 0, 1))) {
            assertFailsWith<HbcFormatException> {
                HbcReader.read(rawHbc(functions = listOf(RawHbcFunction(rawCode(0x60) + invalid))))
            }
        }
    }

    @Test fun `local access cannot escape its function frame`() {
        for (opcode in listOf(0x02, 0x03)) {
            for (slot in listOf(1, 65535)) {
                val code = encodedBytes { writeByte(opcode); writeShort(slot); writeByte(0x60) }
                assertFailsWith<HbcFormatException> {
                    HbcReader.read(rawHbc(functions = listOf(RawHbcFunction(code, locals = 1))))
                }
            }
        }
    }

    @Test fun `jumps cannot target operands outside code or one past its end`() {
        // Jump at offset 0 ends at 5; RETURN is the only other instruction, at offset 5.
        for (delta in listOf(-6, -4, 1, 2, Int.MIN_VALUE, Int.MAX_VALUE)) {
            val code = encodedBytes { writeByte(0x40); writeInt(delta); writeByte(0x60) }
            assertFailsWith<HbcFormatException>("Accepted jump displacement $delta") {
                HbcReader.read(rawHbc(functions = listOf(RawHbcFunction(code))))
            }
        }
        val selfLoop = encodedBytes { writeByte(0x40); writeInt(-5) }
        assertEquals(1, HbcReader.read(rawHbc(functions = listOf(RawHbcFunction(selfLoop)))).functions.size)
    }
}
