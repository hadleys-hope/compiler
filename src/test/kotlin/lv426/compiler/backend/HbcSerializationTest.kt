package lv426.compiler.backend

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HbcSerializationTest {
    @Test fun `all type tags and maximum permitted nesting round trip`() {
        var nested: HbcType = HbcType.IntType
        repeat(63) { nested = HbcType.ListType(nested) }
        val types = listOf(HbcType.IntType, HbcType.RealType, HbcType.BoolType, HbcType.StringType,
            HbcType.TimeType, HbcType.Struct("Узел"), HbcType.Array(HbcType.IntType, Int.MAX_VALUE), nested)
        val module = HbcBackend.compile(IrProgram(
            listOf(IrFunction("f", 0, 0, listOf(IrInstruction.Return()))),
            structs = listOf(IrStruct("Узел", emptyList())), events = listOf(IrEvent("e", types))
        ))
        assertEquals(types, module.types)
        assertEquals(module, HbcReader.read(module.toBytes()))
        assertFailsWith<HbcFormatException> { module.copy(types = types + HbcType.ListType(nested)).toBytes() }
    }

    @Test fun `manual modules cannot serialize inconsistent extended tables`() {
        val module = HbcBackend.compile(extendedProgram())
        val invalid = listOf(
            module.copy(metadataPresent = false),
            module.copy(structs = listOf(module.structs.single().copy(nameConstant = -1))),
            module.copy(structs = listOf(module.structs.single().copy(fields = listOf(HbcField(0, 65535))))),
            module.copy(globals = listOf(module.globals.first().copy(typeIndex = -1))),
            module.copy(globals = listOf(module.globals.first().copy(initializerFunction = 65535))),
            module.copy(events = listOf(module.events.first().copy(parameterTypes = listOf(-1)))),
            module.copy(handlers = listOf(HbcHandler(HbcHandlerKind.EVENT, 4, eventIndex = 65535))),
            module.copy(handlers = listOf(HbcHandler(HbcHandlerKind.START, -1))),
            module.copy(handlers = listOf(HbcHandler(HbcHandlerKind.START, 3, eventIndex = 1))),
            module.copy(handlers = listOf(HbcHandler(HbcHandlerKind.EVENT, 4, milliseconds = 1))),
            module.copy(types = module.types + HbcType.Array(HbcType.IntType, -1))
        )
        invalid.forEachIndexed { index, bad ->
            assertFailsWith<HbcFormatException>("Accepted malformed module $index") { bad.toBytes() }
        }
    }

    @Test fun `invalid aggregate bytecode operands are rejected even when unreachable`() {
        val module = HbcBackend.compile(extendedProgram())
        val codes = listOf(
            rawCode(0x70, 0xff, 0xff), rawCode(0x71, 0, 0, 0xff, 0xff),
            rawCode(0x72, 0, 0, 0xff, 0xff), rawCode(0x73, 0, 0), rawCode(0x74, 0, 0, 0, 0)
        )
        codes.forEach { code ->
            assertFailsWith<HbcFormatException> {
                module.copy(functions = module.functions.mapIndexed { index, function ->
                    if (index == 2) function.copy(code = rawCode(0x60) + code) else function
                }).toBytes()
            }
        }
    }

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
