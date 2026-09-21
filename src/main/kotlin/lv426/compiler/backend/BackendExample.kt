package lv426.compiler.backend

/** Equivalent to a function sumTo(n): int summing 1..n, with 0 for n <= 0. */
object BackendExample {
    fun program(): IrProgram {
        val loop = IrLabel("loop")
        val done = IrLabel("done")
        // Slots: 0 = parameter n, 1 = sum, 2 = i.
        return IrProgram(listOf(IrFunction("sumTo", 1, 3, listOf(
            IrInstruction.Push(HbcConstant.IntValue(0)),
            IrInstruction.StoreLocal(1),
            IrInstruction.Push(HbcConstant.IntValue(1)),
            IrInstruction.StoreLocal(2),
            IrInstruction.Label(loop),
            IrInstruction.LoadLocal(2),
            IrInstruction.LoadLocal(0),
            IrInstruction.Binary(BinaryOperation.LESS_OR_EQUAL),
            IrInstruction.JumpIfFalse(done),
            IrInstruction.LoadLocal(1),
            IrInstruction.LoadLocal(2),
            IrInstruction.Binary(BinaryOperation.ADD),
            IrInstruction.StoreLocal(1),
            IrInstruction.LoadLocal(2),
            IrInstruction.Push(HbcConstant.IntValue(1)),
            IrInstruction.Binary(BinaryOperation.ADD),
            IrInstruction.StoreLocal(2),
            IrInstruction.Jump(loop),
            IrInstruction.Label(done),
            IrInstruction.LoadLocal(1),
            IrInstruction.Return(hasValue = true)
        ))))
    }
}
