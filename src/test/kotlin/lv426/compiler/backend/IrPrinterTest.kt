package lv426.compiler.backend

import kotlin.test.Test
import kotlin.test.assertTrue

class IrPrinterTest {
    @Test fun `prints metadata handlers labels and instructions`() {
        val done = IrLabel("done")
        val program = IrProgram(
            functions = listOf(IrFunction("tick", 0, 0, listOf(
                IrInstruction.Push(HbcConstant.IntValue(1)),
                IrInstruction.Jump(done),
                IrInstruction.Label(done),
                IrInstruction.Return()
            ))),
            structs = listOf(IrStruct("State", listOf(IrField("value", HbcType.RealType)))),
            globals = listOf(IrGlobal("state", HbcType.Struct("State"))),
            events = listOf(IrEvent("changed", listOf(HbcType.IntType))),
            handlers = listOf(IrHandler.Every(1_000, "tick"))
        )

        val text = IrPrinter.print(program)
        assertTrue("struct State" in text)
        assertTrue("var state: State default" in text)
        assertTrue("event changed(int)" in text)
        assertTrue("every 1000ms -> tick" in text)
        assertTrue("PUSH int 1" in text)
        assertTrue("done:" in text)
    }
}
