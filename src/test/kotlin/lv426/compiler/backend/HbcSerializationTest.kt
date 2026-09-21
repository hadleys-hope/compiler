package lv426.compiler.backend

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HbcSerializationTest {
    private fun moduleWithString(value: String): HbcModule = HbcModule(
        listOf(HbcConstant.StringValue("main"), HbcConstant.StringValue(value)),
        listOf(HbcFunction(0, 0, 0, rawCode(0x60)))
    )

    @Test fun `unpaired utf16 surrogates cannot silently change during serialization`() {
        for (value in listOf("\uD800", "\uDC00", "text\uD800end")) {
            assertFailsWith<HbcFormatException> { moduleWithString(value).toBytes() }
        }
    }

    @Test fun `string length limit counts utf8 bytes and includes 65535`() {
        for (value in listOf("a".repeat(65535), "я".repeat(32767) + "a")) {
            val restored = HbcReader.read(moduleWithString(value).toBytes())
            assertEquals(HbcConstant.StringValue(value), restored.constants[1])
        }
        for (value in listOf("a".repeat(65536), "я".repeat(32768))) {
            assertFailsWith<HbcFormatException> { moduleWithString(value).toBytes() }
        }
    }

    @Test fun `mutating exposed magic bytes cannot change the format header`() {
        HbcModule.MAGIC.fill(0)
        val bytes = moduleWithString("value").toBytes()
        assertContentEquals(rawCode(0x48, 0x42, 0x43, 0), bytes.copyOfRange(0, 4))
        assertEquals(1, HbcReader.read(bytes).functions.size)
    }
}
