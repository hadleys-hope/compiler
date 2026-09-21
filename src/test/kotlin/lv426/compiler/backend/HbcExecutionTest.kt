package lv426.compiler.backend

import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class HbcExecutionTest {
    private fun integer(value: Long) = IrInstruction.Push(HbcConstant.IntValue(value))

    @Test fun `documented example returns zero for nonpositive input and sums positive input`() {
        val module = HbcReader.read(HbcBackend.compileToBytes(BackendExample.program()))
        for ((input, expected) in listOf(-3L to 0L, 0L to 0L, 5L to 15L)) {
            assertEquals(expected, BytecodeProbe(module).run("sumTo", listOf(input)))
        }
    }

    @Test fun `compiled loop executes forward and backward branches and returns its sum`() {
        val loop = IrLabel("loop")
        val done = IrLabel("done")
        val main = IrFunction("main", 0, 0, listOf(
            integer(5), IrInstruction.Call("sum", 1), IrInstruction.Return(true)
        ))
        val sum = IrFunction("sum", 1, 2, listOf(
            integer(0), IrInstruction.StoreLocal(1),
            IrInstruction.Label(loop), IrInstruction.LoadLocal(0), integer(0),
            IrInstruction.Binary(BinaryOperation.GREATER), IrInstruction.JumpIfFalse(done),
            IrInstruction.LoadLocal(1), IrInstruction.LoadLocal(0), IrInstruction.Binary(BinaryOperation.ADD),
            IrInstruction.StoreLocal(1), IrInstruction.LoadLocal(0), integer(1),
            IrInstruction.Binary(BinaryOperation.SUBTRACT), IrInstruction.StoreLocal(0),
            IrInstruction.Jump(loop), IrInstruction.Label(done), IrInstruction.LoadLocal(1), IrInstruction.Return(true)
        ))
        val module = HbcReader.read(HbcBackend.compileToBytes(IrProgram(listOf(main, sum))))
        assertEquals(15L, BytecodeProbe(module).run("main"))
    }

    @Test fun `recursive calls preserve caller operands and argument order`() {
        val recurse = IrLabel("recurse")
        val main = IrFunction("main", 0, 0, listOf(
            integer(6), IrInstruction.Call("factorial", 1),
            integer(20), integer(3), IrInstruction.Call("difference", 2),
            IrInstruction.Binary(BinaryOperation.ADD), IrInstruction.Return(true)
        ))
        val factorial = IrFunction("factorial", 1, 1, listOf(
            IrInstruction.LoadLocal(0), integer(1), IrInstruction.Binary(BinaryOperation.LESS_OR_EQUAL),
            IrInstruction.JumpIfFalse(recurse), integer(1), IrInstruction.Return(true),
            IrInstruction.Label(recurse), IrInstruction.LoadLocal(0), IrInstruction.LoadLocal(0), integer(1),
            IrInstruction.Binary(BinaryOperation.SUBTRACT), IrInstruction.Call("factorial", 1),
            IrInstruction.Binary(BinaryOperation.MULTIPLY), IrInstruction.Return(true)
        ))
        val difference = IrFunction("difference", 2, 2, listOf(
            IrInstruction.LoadLocal(0), IrInstruction.LoadLocal(1),
            IrInstruction.Binary(BinaryOperation.SUBTRACT), IrInstruction.Return(true)
        ))
        val bytes = HbcBackend.compileToBytes(IrProgram(listOf(main, factorial, difference)))
        assertEquals(737L, BytecodeProbe(HbcReader.read(bytes)).run("main"))
    }

    @Test fun `void call returns unit and event arguments keep source order`() {
        val main = IrFunction("main", 0, 0, listOf(
            IrInstruction.Call("empty", 0), IrInstruction.Pop,
            integer(7), integer(9), IrInstruction.Emit("pair", 2), IrInstruction.Return()
        ))
        val empty = IrFunction("empty", 0, 0, listOf(IrInstruction.Return()))
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(IrProgram(listOf(main, empty)))))
        assertEquals(Unit, probe.run("main"))
        assertEquals(listOf("pair" to listOf<Any>(7L, 9L)), probe.events)
    }
}

/** Minimal, bounded test interpreter for the v1 ABI, deliberately independent of the encoder. */
private class BytecodeProbe(private val module: HbcModule) {
    private var remainingSteps = 10_000
    val events = mutableListOf<Pair<String, List<Any>>>()

    fun run(name: String, arguments: List<Any> = emptyList()): Any {
        val function = module.functions.single { string(it.nameConstant) == name }
        check(arguments.size == function.parameterCount)
        val locals = arrayOfNulls<Any>(function.localCount)
        arguments.forEachIndexed { index, argument -> locals[index] = argument }
        val stack = mutableListOf<Any>()
        val code = ByteBuffer.wrap(function.code)
        fun pop(): Any = stack.removeAt(stack.lastIndex)
        fun u16(): Int = code.short.toInt() and 0xffff
        fun callArguments(count: Int): List<Any> = List(count) { pop() }.reversed()

        while (true) {
            check(remainingSteps-- > 0) { "Bytecode exceeded the test execution budget" }
            when (val opcode = code.get().toInt() and 0xff) {
                0x00 -> Unit
                0x01 -> stack += constant(u16())
                0x02 -> stack += checkNotNull(locals[u16()])
                0x03 -> locals[u16()] = pop()
                0x20, 0x21, 0x22, 0x33, 0x34 -> {
                    val right = pop() as Long
                    val left = pop() as Long
                    stack += when (opcode) {
                        0x20 -> left + right
                        0x21 -> left - right
                        0x22 -> left * right
                        0x33 -> left <= right
                        else -> left > right
                    }
                }
                0x40 -> {
                    val displacement = code.int
                    code.position(code.position() + displacement)
                }
                0x41 -> {
                    val displacement = code.int
                    if (!(pop() as Boolean)) code.position(code.position() + displacement)
                }
                0x50 -> {
                    val target = string(u16())
                    val args = callArguments(u16())
                    stack += run(target, args)
                }
                0x51 -> {
                    val event = string(u16())
                    events += event to callArguments(u16())
                }
                0x60 -> { check(stack.isEmpty()); return Unit }
                0x61 -> { check(stack.size == 1); return pop() }
                0x62 -> pop()
                else -> error("Test interpreter does not implement opcode $opcode")
            }
        }
    }

    private fun string(index: Int): String = (module.constants[index] as HbcConstant.StringValue).value

    private fun constant(index: Int): Any = when (val value = module.constants[index]) {
        is HbcConstant.IntValue -> value.value
        is HbcConstant.RealValue -> value.value
        is HbcConstant.BoolValue -> value.value
        is HbcConstant.StringValue -> value.value
        is HbcConstant.TimeValue -> value.milliseconds
    }
}
