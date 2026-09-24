package lv426.compiler.backend

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.nio.file.Files

class HbcBackendTest {
    @Test fun `duration conversion is exact across units and rejects precision loss or overflow`() {
        for ((unit, expected) in listOf(HbcTimeUnit.MS to 1L, HbcTimeUnit.SEC to 1000L,
            HbcTimeUnit.MIN to 60000L, HbcTimeUnit.HOUR to 3600000L, HbcTimeUnit.DAY to 86400000L)) {
            assertEquals(expected, HbcTime.milliseconds("1", unit))
        }
        assertEquals(1500L, HbcTime.milliseconds("1.5", HbcTimeUnit.SEC))
        assertEquals(1001L, HbcTime.milliseconds(1.001, HbcTimeUnit.SEC))
        assertEquals(1L, HbcTime.milliseconds("1e-3", HbcTimeUnit.SEC))
        assertEquals(Long.MAX_VALUE, HbcTime.milliseconds(Long.MAX_VALUE.toString(), HbcTimeUnit.MS))
        assertEquals(Long.MIN_VALUE, HbcTime.milliseconds(Long.MIN_VALUE.toString(), HbcTimeUnit.MS))
        for (value in listOf("0.5", "NaN", "Infinity", "", "9223372036854775808")) {
            assertFailsWith<HbcFormatException> { HbcTime.milliseconds(value, HbcTimeUnit.MS) }
        }
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MAX_VALUE)) {
            assertFailsWith<HbcFormatException> { HbcTime.milliseconds(value, HbcTimeUnit.SEC) }
        }
        assertFailsWith<HbcFormatException> { HbcTime.milliseconds("0.0001", HbcTimeUnit.SEC) }
        assertFailsWith<HbcFormatException> { HbcTime.milliseconds(Long.MAX_VALUE.toString(), HbcTimeUnit.DAY) }
    }

    @Test fun `defaults reject construction cycles but allow recursive empty containers`() {
        val node = HbcType.Struct("Node")
        fun program(fieldType: HbcType) = IrProgram(emptyList(),
            structs = listOf(IrStruct("Node", listOf(IrField("next", fieldType)))), globals = listOf(IrGlobal("root", node)))
        for (type in listOf(node, HbcType.Array(node, 1))) {
            val error = assertFailsWith<IllegalArgumentException> { HbcBackend.compile(program(type)) }
            assertTrue("Cyclic default initialization" in error.message.orEmpty())
        }
        for (type in listOf(HbcType.ListType(node), HbcType.Array(node, 0))) {
            val module = HbcBackend.compile(program(type))
            assertEquals(module, HbcReader.read(module.toBytes()))
        }
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(IrProgram(emptyList(), structs = listOf(
                IrStruct("A", listOf(IrField("b", HbcType.Struct("B")))),
                IrStruct("B", listOf(IrField("a", HbcType.Struct("A"))))
            ), globals = listOf(IrGlobal("root", HbcType.Struct("A")))))
        }
    }

    @Test fun `initializers require defined value functions and const requires an expression`() {
        val functions = listOf(IrFunction("void", 0, 0, listOf(IrInstruction.Return())),
            IrFunction("arg", 1, 1, listOf(IrInstruction.LoadLocal(0), IrInstruction.Return(true))))
        for (name in listOf("missing", "void", "arg")) {
            assertFailsWith<IllegalArgumentException> {
                HbcBackend.compile(IrProgram(functions, structs = listOf(
                    IrStruct("S", listOf(IrField("x", HbcType.IntType, name))))))
            }
            assertFailsWith<IllegalArgumentException> {
                HbcBackend.compile(IrProgram(functions + IrFunction("array", 0, 0, listOf(
                    IrInstruction.NewArrayWithInitializer(HbcType.IntType, 2, name), IrInstruction.Return(true)))))
            }
        }
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(IrProgram(emptyList(), globals = listOf(IrGlobal("c", HbcType.IntType, mutable = false))))
        }
    }

    @Test fun `default depth limit also applies when a nested factory was already generated`() {
        fun structs(count: Int) = List(count) { index ->
            IrStruct("S$index", if (index == count - 1) emptyList() else listOf(IrField("next", HbcType.Struct("S${index + 1}"))))
        }
        val allowed = IrProgram(emptyList(), structs = structs(64), globals = listOf(IrGlobal("root", HbcType.Struct("S0"))))
        HbcReader.read(HbcBackend.compileToBytes(allowed))
        val invalid = IrProgram(listOf(IrFunction("main", 0, 0, listOf(
            IrInstruction.DefaultValue(HbcType.Struct("S1")), IrInstruction.Pop,
            IrInstruction.DefaultValue(HbcType.Struct("S0")), IrInstruction.Return(true)
        ))), structs = structs(65))
        val error = assertFailsWith<IllegalArgumentException> { HbcBackend.compile(invalid) }
        assertTrue("nesting exceeds 64" in error.message.orEmpty())
    }

    @Test fun `default array generation is compact deterministic and preserves external call names`() {
        fun array(size: Int) = IrProgram(emptyList(), globals = listOf(IrGlobal("array", HbcType.Array(HbcType.IntType, size))))
        val small = HbcBackend.compileToBytes(array(2))
        val large = HbcBackend.compileToBytes(array(1_000_000))
        assertEquals(small.size, large.size)
        assertTrue(large.size < 1024)
        assertContentEquals(large, HbcBackend.compileToBytes(array(1_000_000)))
        assertContentEquals(large, HbcReader.read(large).toBytes())
        assertTrue("NEW_ARRAY_INIT" in HbcDisassembler.disassemble(large))
        val program = IrProgram(listOf(IrFunction("main", 0, 0, listOf(
            IrInstruction.Call("@default/0", 0), IrInstruction.Pop,
            IrInstruction.DefaultValue(HbcType.IntType), IrInstruction.Return(true)
        ))))
        val module = HbcBackend.compile(program)
        val names = module.functions.map { (module.constants[it.nameConstant] as HbcConstant.StringValue).value }
        assertTrue("@default/0" !in names)
        assertTrue("@default/1" in names)
    }

    @Test fun `minimal extended module matches independent golden bytes`() {
        val program = IrProgram(listOf(IrFunction("f", 0, 0, listOf(
            IrInstruction.NewArray(HbcType.IntType, 0), IrInstruction.Pop, IrInstruction.Return()
        ))))
        val golden = """
            48 42 43 00 00 01 00 01 04 00 01 66
            00 01 00 00 00 00 00 00 00 00 00 05 73 00 00 62 60
            4d 45 54 41 00 01 07 00 00 00 00 01
            00 00 00 00 00 00 00 00
        """.trim().split(Regex("\\s+")).map { it.toInt(16).toByte() }.toByteArray()
        assertContentEquals(golden, HbcBackend.compileToBytes(program))
        assertContentEquals(golden, HbcReader.read(golden).toBytes())
    }

    @Test fun `extended tables survive round trip with deterministic bytes and readable metadata`() {
        val program = extendedProgram()
        val module = HbcBackend.compile(program)
        assertTrue(module.metadataPresent)
        assertEquals(module, HbcReader.read(module.toBytes()))
        assertContentEquals(module.toBytes(), HbcBackend.compileToBytes(program))
        assertEquals(listOf(0L, 0L, 1000L, 5000L), module.handlers.map { it.milliseconds })
        val dump = HbcDisassembler.disassemble(module.toBytes())
        for (expected in listOf("HBC v1", "list<int>", "State", "initializer=\"initBase\"", "START", "EVENT",
            "EVERY time=1000ms", "AT time=5000ms", "STORE_FIELD", "STORE_INDEX", "NEW_ARRAY", "NEW_LIST")) {
            assertTrue(expected in dump, "Missing $expected in disassembly")
        }
    }

    @Test fun `invalid IR metadata and aggregate references are rejected`() {
        val program = extendedProgram()
        val invalid = listOf(
            program.copy(structs = program.structs + program.structs),
            program.copy(structs = listOf(program.structs.single().copy(fields = listOf(IrField("x", HbcType.IntType), IrField("x", HbcType.IntType))))),
            program.copy(globals = program.globals + program.globals),
            program.copy(events = program.events + program.events),
            program.copy(globals = listOf(IrGlobal("x", HbcType.IntType, "missing"))),
            program.copy(handlers = listOf(IrHandler.Event("missing", "start"))),
            program.copy(handlers = listOf(IrHandler.Event("changed", "start"))),
            program.copy(handlers = listOf(IrHandler.Start("changedHandler"))),
            program.copy(handlers = listOf(IrHandler.Start("initBase"))),
            program.copy(handlers = listOf(IrHandler.Every(0, "tick"))),
            program.copy(handlers = listOf(IrHandler.Every(-1, "tick"))),
            program.copy(handlers = listOf(IrHandler.At(-1, "deadline"))),
            program.copy(globals = listOf(IrGlobal("x", HbcType.IntType, "start"))),
            program.copy(events = listOf(IrEvent("changed", listOf(HbcType.Struct("Missing")))))
        )
        invalid.forEachIndexed { index, bad ->
            assertFailsWith<IllegalArgumentException>("Accepted invalid metadata $index") { HbcBackend.compile(bad) }
        }
        val badInstructions = listOf(
            listOf(IrInstruction.NewStruct("Missing")),
            listOf(IrInstruction.LoadField("State", "missing")),
            listOf(IrInstruction.StoreField("State", "missing")),
            listOf(IrInstruction.NewArray(HbcType.IntType, -1)),
            listOf(IrInstruction.NewList(HbcType.IntType, 65536)),
            listOf(IrInstruction.LoadGlobal("missing"), IrInstruction.Pop),
            listOf(IrInstruction.Push(HbcConstant.IntValue(1)), IrInstruction.StoreGlobal("base")),
            listOf(IrInstruction.Emit("missing", 0)),
            listOf(IrInstruction.Emit("changed", 0))
        )
        badInstructions.forEach { instructions ->
            assertFailsWith<IllegalArgumentException> {
                HbcBackend.compile(program.copy(functions = program.functions +
                    IrFunction("invalid", 0, 0, instructions + IrInstruction.Return())))
            }
        }
    }

    @Test fun `all aggregate stack effects are checked`() {
        val program = extendedProgram()
        val instructions = listOf(
            IrInstruction.NewStruct("State"), IrInstruction.LoadField("State", "x"), IrInstruction.StoreField("State", "x"),
            IrInstruction.NewArray(HbcType.IntType, 1), IrInstruction.NewList(HbcType.IntType, 1),
            IrInstruction.LoadIndex, IrInstruction.StoreIndex, IrInstruction.Length, IrInstruction.ListAppend, IrInstruction.ListRemove
        )
        instructions.forEach { instruction ->
            assertFailsWith<HbcFormatException>("Accepted underflow for $instruction") {
                HbcBackend.compile(program.copy(functions = program.functions +
                    IrFunction("invalid", 0, 0, listOf(instruction, IrInstruction.Return()))))
            }
        }
    }

    @Test fun `zero at and multiple handlers preserve registration order`() {
        val program = extendedProgram().copy(handlers = listOf(
            IrHandler.At(0, "deadline"), IrHandler.Start("start"), IrHandler.Start("tick"),
            IrHandler.Event("changed", "changedHandler"), IrHandler.Event("changed", "changedHandler"),
            IrHandler.Every(Long.MAX_VALUE, "tick")
        ))
        val module = HbcReader.read(HbcBackend.compileToBytes(program))
        assertEquals(listOf(6, 3, 5, 4, 4, 5), module.handlers.map { it.functionIndex })
        assertEquals(Long.MAX_VALUE, module.handlers.last().milliseconds)
    }

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
