package lv426.compiler.backend

import kotlin.test.Test
import kotlin.test.assertFailsWith

class HbcMetadataValidationTest {
    private fun integer(value: Long) = IrInstruction.Push(HbcConstant.IntValue(value))
    private fun valueInitializer(name: String, value: Long = 0) = IrFunction(
        name, 0, 0, listOf(integer(value), IrInstruction.Return(true))
    )

    @Test fun `store to immutable global is rejected`() {
        val init = valueInitializer("init")
        val bad = IrFunction("bad", 0, 0, listOf(
            integer(1), IrInstruction.StoreGlobal("answer"), IrInstruction.Return()
        ))
        assertFailsWith<HbcFormatException> {
            HbcBackend.compile(IrProgram(
                functions = listOf(init, bad),
                globals = listOf(IrGlobal("answer", HbcType.IntType, "init", mutable = false))
            ))
        }
    }

    @Test fun `emit argument count must match declared event`() {
        val main = IrFunction("main", 0, 0, listOf(
            IrInstruction.Emit("changed", 0), IrInstruction.Return()
        ))
        assertFailsWith<HbcFormatException> {
            HbcBackend.compile(IrProgram(
                functions = listOf(main),
                events = listOf(IrEvent("changed", listOf(HbcType.IntType)))
            ))
        }
    }

    @Test fun `event handler parameter count must match event signature`() {
        val handler = IrFunction("handler", 0, 0, listOf(IrInstruction.Return()))
        assertFailsWith<HbcFormatException> {
            HbcBackend.compile(IrProgram(
                functions = listOf(handler),
                events = listOf(IrEvent("changed", listOf(HbcType.IntType))),
                handlers = listOf(IrHandler.Event("changed", "handler"))
            ))
        }
    }

    @Test fun `timer handler metadata rejects invalid times`() {
        val tick = IrFunction("tick", 0, 0, listOf(IrInstruction.Return()))
        assertFailsWith<HbcFormatException> {
            HbcBackend.compile(IrProgram(listOf(tick), handlers = listOf(IrHandler.Every(0, "tick"))))
        }
        assertFailsWith<HbcFormatException> {
            HbcBackend.compile(IrProgram(listOf(tick), handlers = listOf(IrHandler.At(-1, "tick"))))
        }
    }

    @Test fun `unknown struct and field references are rejected`() {
        val missingStruct = IrFunction("missingStruct", 0, 0, listOf(
            IrInstruction.NewStruct("Missing"), IrInstruction.Pop, IrInstruction.Return()
        ))
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(IrProgram(listOf(missingStruct)))
        }

        val badField = IrFunction("badField", 0, 0, listOf(
            IrInstruction.DefaultValue(HbcType.Struct("State")),
            IrInstruction.LoadField("State", "missing"),
            IrInstruction.Pop,
            IrInstruction.Return()
        ))
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(IrProgram(
                functions = listOf(badField),
                structs = listOf(IrStruct("State", listOf(IrField("value", HbcType.IntType))))
            ))
        }
    }

    @Test fun `metadata rejects unresolved struct types`() {
        val init = valueInitializer("init")
        assertFailsWith<HbcFormatException> {
            HbcBackend.compile(IrProgram(
                functions = listOf(init),
                globals = listOf(IrGlobal("ghost", HbcType.Struct("Missing"), "init"))
            ))
        }
    }

    @Test fun `handler functions must return void`() {
        val badHandler = valueInitializer("handler", 1)
        assertFailsWith<HbcFormatException> {
            HbcBackend.compile(IrProgram(
                functions = listOf(badHandler),
                handlers = listOf(IrHandler.Start("handler"))
            ))
        }
    }
    @Test fun `cyclic defaults are rejected but recursion through list is allowed`() {
        val node = HbcType.Struct("Node")
        val direct = IrStruct("Node", listOf(IrField("next", node)))
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(IrProgram(
                functions = listOf(IrFunction("noop", 0, 0, listOf(IrInstruction.Return()))),
                structs = listOf(direct),
                globals = listOf(IrGlobal("root", node))
            ))
        }

        val throughList = IrStruct("Node", listOf(IrField("children", HbcType.ListType(node))))
        HbcBackend.compile(IrProgram(
            functions = listOf(IrFunction("noop", 0, 0, listOf(IrInstruction.Return()))),
            structs = listOf(throughList),
            globals = listOf(IrGlobal("root", node))
        ))
    }

}
