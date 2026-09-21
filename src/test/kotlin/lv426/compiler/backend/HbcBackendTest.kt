package lv426.compiler.backend

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.nio.file.Files

class HbcBackendTest {
    @Test fun `backend emits readable module and resolves backwards jump`() {
        val loop = IrLabel("loop")
        val program = IrProgram(listOf(IrFunction("main", 0, 1, listOf(
            IrInstruction.Label(loop), IrInstruction.Push(HbcConstant.IntValue(1)),
            IrInstruction.StoreLocal(0), IrInstruction.Jump(loop), IrInstruction.Return()
        ))))
        val bytes = HbcBackend.compile(program).toBytes()
        val restored = HbcReader.read(bytes)
        assertEquals("main", (restored.constants[restored.functions.single().nameConstant] as HbcConstant.StringValue).value)
        assertEquals(Opcode.JUMP.code.toByte(), restored.functions.single().code[6])
        assertContentEquals(byteArrayOf(-1, -1, -1, -11), restored.functions.single().code.copyOfRange(7, 11))
        assertContentEquals(bytes, restored.toBytes())
    }

    @Test fun `unknown label is rejected before emission`() {
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(IrProgram(listOf(IrFunction("main", 0, 0, listOf(IrInstruction.Jump(IrLabel("missing")))))))
        }
    }

    @Test fun `minimal module has stable documented wire format`() {
        val program = IrProgram(listOf(IrFunction("main", 0, 0, listOf(
            IrInstruction.Push(HbcConstant.IntValue(42)), IrInstruction.Return(true)
        ))))
        val golden = """
            48 42 43 00 00 01 00 02
            04 00 04 6d 61 69 6e
            01 00 00 00 00 00 00 00 2a
            00 01 00 00 00 00 00 00 00 00 00 04 01 00 01 61
        """.trim().split(Regex("\\s+")).map { it.toInt(16).toByte() }.toByteArray()
        assertContentEquals(golden, HbcBackend.compileToBytes(program))
    }

    @Test fun `constants deduplicate across functions and compilation is deterministic`() {
        val instructions = listOf(IrInstruction.Push(HbcConstant.StringValue("Привет 🌍")), IrInstruction.Return(true))
        val program = IrProgram(listOf(IrFunction("a", 0, 0, instructions), IrFunction("b", 0, 0, instructions)))
        val module = HbcBackend.compile(program)
        assertEquals(3, module.constants.size)
        assertEquals(1, module.constants.count { it == HbcConstant.StringValue("Привет 🌍") })
        assertContentEquals(module.toBytes(), HbcBackend.compileToBytes(program))
        assertContentEquals(module.toBytes(), HbcReader.read(module.toBytes()).toBytes())
    }

    @Test fun `jump displacement is 32 bit and targets the following instruction boundary`() {
        val done = IrLabel("done")
        val instructions = buildList {
            add(IrInstruction.Jump(done))
            repeat(70000) { add(IrInstruction.Nop) }
            add(IrInstruction.Label(done))
            add(IrInstruction.Return())
        }
        val code = HbcBackend.compile(IrProgram(listOf(IrFunction("large", 0, 0, instructions)))).functions.single().code
        assertContentEquals(byteArrayOf(0, 1, 0x11, 0x70), code.copyOfRange(1, 5))
        assertEquals(Opcode.RETURN.code.toByte(), code[70005])
    }

    @Test fun `duplicate labels fail even when never referenced`() {
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(IrProgram(listOf(IrFunction("bad", 0, 0, listOf(
                IrInstruction.Label(IrLabel("same")), IrInstruction.Label(IrLabel("same")), IrInstruction.Return()
            )))))
        }
    }

    @Test fun `oversized UTF8 constant fails compilation before creating any output`() {
        val program = IrProgram(listOf(IrFunction("bad", 0, 0, listOf(
            IrInstruction.Push(HbcConstant.StringValue("Я".repeat(32768))), IrInstruction.Return(true)
        ))))
        assertFailsWith<IllegalArgumentException> { HbcBackend.compile(program) }
    }

    @Test fun `invalid compilation preserves existing output and valid write can replace it`() {
        val directory = Files.createTempDirectory("hbc-write-test-")
        val output = directory.resolve("module.hbc")
        try {
            val original = byteArrayOf(10, 20, 30)
            Files.write(output, original)
            val invalid = IrProgram(listOf(IrFunction("bad", 0, 0, listOf(IrInstruction.Pop, IrInstruction.Return()))))
            assertFailsWith<IllegalArgumentException> { HbcBackend.write(invalid, output) }
            assertContentEquals(original, Files.readAllBytes(output))
            val module = HbcBackend.write(BackendExample.program(), output)
            assertContentEquals(module.toBytes(), Files.readAllBytes(output))
            Files.list(directory).use { files -> assertEquals(listOf(output), files.toList()) }
        } finally {
            Files.deleteIfExists(output)
            Files.deleteIfExists(directory)
        }
    }

    @Test fun `example can be read and disassembled`() {
        val module = HbcReader.read(HbcBackend.compileToBytes(BackendExample.program()))
        val text = HbcDisassembler.disassemble(module)
        assertTrue("sumTo" in text)
        assertTrue("JUMP_IF_FALSE" in text)
        assertTrue("RETURN_VALUE" in text)
    }
}
