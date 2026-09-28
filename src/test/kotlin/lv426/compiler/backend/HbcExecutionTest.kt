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

    @Test fun `int to real conversion executes`() {
        val main = IrFunction("main", 0, 0, listOf(
            integer(7), IrInstruction.IntToReal, IrInstruction.Return(true)
        ))
        val module = HbcReader.read(HbcBackend.compileToBytes(IrProgram(listOf(main))))
        assertEquals(7.0, BytecodeProbe(module).run("main"))
    }

    @Test fun `void call returns unit and event arguments keep source order`() {
        val main = IrFunction("main", 0, 0, listOf(
            IrInstruction.Call("empty", 0), IrInstruction.Pop,
            integer(7), integer(9), IrInstruction.Emit("pair", 2), IrInstruction.Return()
        ))
        val empty = IrFunction("empty", 0, 0, listOf(IrInstruction.Return()))
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(IrProgram(listOf(main, empty)))))
        assertEquals(Unit, probe.run("main"))
        assertEquals(listOf("pair" to listOf<Any?>(7L, 9L)), probe.events)
    }

    @Test fun `globals aggregates defaults and event handlers execute together`() {
        val int = HbcType.IntType
        val item = HbcType.Struct("Item")
        val items = HbcType.Array(item, 2)
        val structs = listOf(IrStruct("Item", listOf(
            IrField("value", int),
            IrField("samples", HbcType.ListType(int))
        )))
        val globals = listOf(
            IrGlobal("items", items),
            IrGlobal("last", int)
        )

        val mutate = IrFunction("mutate", 0, 1, listOf(
            IrInstruction.LoadGlobal("items"), integer(0), IrInstruction.LoadIndex, IrInstruction.StoreLocal(0),
            IrInstruction.LoadLocal(0), IrInstruction.LoadLocal(0), IrInstruction.LoadField("Item", "value"),
            integer(2), IrInstruction.Binary(BinaryOperation.ADD), IrInstruction.StoreField("Item", "value"),
            IrInstruction.LoadLocal(0), IrInstruction.LoadField("Item", "samples"), integer(7), IrInstruction.ListAppend,
            IrInstruction.LoadLocal(0), IrInstruction.LoadField("Item", "value"), IrInstruction.Emit("changed", 1),
            IrInstruction.Return()
        ))
        val changed = IrFunction("changedHandler", 1, 1, listOf(
            IrInstruction.LoadLocal(0), IrInstruction.StoreGlobal("last"), IrInstruction.Return()
        ))
        val getValue = IrFunction("getValue", 0, 0, listOf(
            IrInstruction.LoadGlobal("items"), integer(0), IrInstruction.LoadIndex,
            IrInstruction.LoadField("Item", "value"), IrInstruction.Return(true)
        ))
        val getSize = IrFunction("getSize", 0, 0, listOf(
            IrInstruction.LoadGlobal("items"), integer(0), IrInstruction.LoadIndex,
            IrInstruction.LoadField("Item", "samples"), IrInstruction.Length, IrInstruction.Return(true)
        ))
        val getLast = IrFunction("getLast", 0, 0, listOf(
            IrInstruction.LoadGlobal("last"), IrInstruction.Return(true)
        ))
        val program = IrProgram(
            functions = listOf(mutate, changed, getValue, getSize, getLast),
            structs = structs,
            globals = globals,
            events = listOf(IrEvent("changed", listOf(int))),
            handlers = listOf(IrHandler.Start("mutate"), IrHandler.Event("changed", "changedHandler"))
        )
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(program)))
        probe.runStartHandlers()
        assertEquals(2L, probe.run("getValue"))
        assertEquals(1L, probe.run("getSize"))
        assertEquals(2L, probe.run("getLast"))
    }

    @Test fun `direct arrays lists indexing and removal execute`() {
        val main = IrFunction("main", 0, 2, listOf(
            integer(10), integer(20), IrInstruction.NewArray(HbcType.IntType, 2), IrInstruction.StoreLocal(0),
            IrInstruction.LoadLocal(0), integer(1), integer(30), IrInstruction.StoreIndex,
            integer(4), integer(5), IrInstruction.NewList(HbcType.IntType, 2), IrInstruction.StoreLocal(1),
            IrInstruction.LoadLocal(1), integer(0), IrInstruction.ListRemove, IrInstruction.Pop,
            IrInstruction.LoadLocal(1), integer(9), IrInstruction.ListAppend,
            IrInstruction.LoadLocal(0), integer(1), IrInstruction.LoadIndex,
            IrInstruction.LoadLocal(1), IrInstruction.Length,
            IrInstruction.Binary(BinaryOperation.ADD), IrInstruction.Return(true)
        ))
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(IrProgram(listOf(main)))))
        assertEquals(32L, probe.run("main"))
    }
    @Test fun `default factories create fresh mutable values on every evaluation`() {
        val int = HbcType.IntType
        val bag = HbcType.Struct("Bag")
        val structs = listOf(IrStruct("Bag", listOf(IrField("values", HbcType.ListType(int)))))
        val function = IrFunction("make", 0, 2, listOf(
            IrInstruction.DefaultValue(bag), IrInstruction.StoreLocal(0),
            IrInstruction.DefaultValue(bag), IrInstruction.StoreLocal(1),
            IrInstruction.LoadLocal(0), IrInstruction.LoadField("Bag", "values"), integer(7), IrInstruction.ListAppend,
            IrInstruction.LoadLocal(1), IrInstruction.LoadField("Bag", "values"), IrInstruction.Length,
            IrInstruction.Return(true)
        ))
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(IrProgram(listOf(function), structs = structs))))
        assertEquals(0L, probe.run("make"))
        assertEquals(0L, probe.run("make"))
    }

    @Test fun `array default creates a fresh struct for each element`() {
        val int = HbcType.IntType
        val item = HbcType.Struct("Item")
        val array = HbcType.Array(item, 2)
        val structs = listOf(IrStruct("Item", listOf(IrField("value", int))))
        val globals = listOf(IrGlobal("items", array))
        val mutate = IrFunction("mutate", 0, 1, listOf(
            IrInstruction.LoadGlobal("items"), integer(0), IrInstruction.LoadIndex, IrInstruction.StoreLocal(0),
            IrInstruction.LoadLocal(0), integer(9), IrInstruction.StoreField("Item", "value"),
            IrInstruction.LoadGlobal("items"), integer(1), IrInstruction.LoadIndex,
            IrInstruction.LoadField("Item", "value"), IrInstruction.Return(true)
        ))
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(
            IrProgram(listOf(mutate), structs = structs, globals = globals)
        )))
        assertEquals(0L, probe.run("mutate"))
    }
}

/** Bounded reference interpreter for backend tests; production execution belongs to the C/C++ VM. */
internal class BytecodeProbe(private val module: HbcModule) {
    private class StructValue(val structIndex: Int, val fields: MutableList<Any?>)
    private class SequenceValue(val list: Boolean, val values: MutableList<Any?>)

    private var remainingSteps = 100_000
    private val globals = linkedMapOf<String, Any?>()
    private var dispatchedEvents = 0
    val events = mutableListOf<Pair<String, List<Any?>>>()

    init {
        module.globals.forEach { global ->
            globals[string(global.nameConstant)] = runFunction(global.initializerFunction, emptyList())
        }
    }

    fun run(name: String, arguments: List<Any?> = emptyList()): Any? {
        val index = module.functions.indexOfFirst { string(it.nameConstant) == name }
        check(index >= 0) { "Unknown function '$name'" }
        return runFunction(index, arguments)
    }

    fun runStartHandlers() {
        module.handlers.filter { it.kind == HbcHandlerKind.START }.forEach {
            runFunction(it.functionIndex, emptyList())
        }
        drainEvents()
    }

    private fun drainEvents() {
        while (dispatchedEvents < events.size) {
            val (name, arguments) = events[dispatchedEvents++]
            val eventIndex = module.events.indexOfFirst { string(it.nameConstant) == name }
            if (eventIndex < 0) continue
            module.handlers.filter { it.kind == HbcHandlerKind.EVENT && it.eventIndex == eventIndex }.forEach {
                runFunction(it.functionIndex, arguments)
            }
        }
    }

    private fun runFunction(functionIndex: Int, arguments: List<Any?>): Any? {
        val function = module.functions[functionIndex]
        check(arguments.size == function.parameterCount)
        val locals = arrayOfNulls<Any?>(function.localCount)
        arguments.forEachIndexed { index, argument -> locals[index] = argument }
        val stack = mutableListOf<Any?>()
        val code = ByteBuffer.wrap(function.code)
        fun pop(): Any? = stack.removeAt(stack.lastIndex)
        fun u16(): Int = code.short.toInt() and 0xffff
        fun callArguments(count: Int): List<Any?> = List(count) { pop() }.reversed()

        while (true) {
            check(remainingSteps-- > 0) { "Bytecode exceeded the test execution budget" }
            when (val opcode = code.get().toInt() and 0xff) {
                0x00 -> Unit
                0x01 -> stack += constant(u16())
                0x02 -> stack += locals[u16()]
                0x03 -> locals[u16()] = pop()
                0x04 -> stack += globals[string(u16())]
                0x05 -> globals[string(u16())] = pop()
                0x10 -> {
                    val value = pop()
                    stack += when (value) {
                        is Long -> -value
                        is Double -> -value
                        else -> error("NEGATE expected a number")
                    }
                }
                0x11 -> stack += !(pop() as Boolean)
                0x12 -> stack += (pop() as Long).toDouble()
                in 0x20..0x24 -> {
                    val right = pop()
                    val left = pop()
                    stack += arithmetic(opcode, left, right)
                }
                in 0x30..0x37 -> {
                    val right = pop()
                    val left = pop()
                    stack += comparison(opcode, left, right)
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
                    val index = module.functions.indexOfFirst { string(it.nameConstant) == target }
                    check(index >= 0) { "Reference VM has no host function '$target'" }
                    stack += runFunction(index, args)
                }
                0x51 -> events += string(u16()) to callArguments(u16())
                0x60 -> {
                    check(stack.isEmpty())
                    return Unit
                }
                0x61 -> {
                    check(stack.size == 1)
                    return pop()
                }
                0x62 -> pop()
                0x70 -> {
                    val structIndex = u16()
                    val values = callArguments(module.structs[structIndex].fields.size).toMutableList()
                    stack += StructValue(structIndex, values)
                }
                0x71 -> {
                    val structIndex = u16()
                    val fieldIndex = u16()
                    val receiver = pop() as StructValue
                    check(receiver.structIndex == structIndex)
                    stack += receiver.fields[fieldIndex]
                }
                0x72 -> {
                    val structIndex = u16()
                    val fieldIndex = u16()
                    val value = pop()
                    val receiver = pop() as StructValue
                    check(receiver.structIndex == structIndex)
                    receiver.fields[fieldIndex] = value
                }
                0x73 -> {
                    val type = module.types[u16()] as HbcType.Array
                    stack += SequenceValue(false, callArguments(type.size).toMutableList())
                }
                0x74 -> {
                    val type = module.types[u16()]
                    check(type is HbcType.ListType)
                    stack += SequenceValue(true, callArguments(u16()).toMutableList())
                }
                0x75 -> {
                    val index = (pop() as Long).toInt()
                    val sequence = pop() as SequenceValue
                    stack += sequence.values[index]
                }
                0x76 -> {
                    val value = pop()
                    val index = (pop() as Long).toInt()
                    val sequence = pop() as SequenceValue
                    sequence.values[index] = value
                }
                0x77 -> stack += (pop() as SequenceValue).values.size.toLong()
                0x78 -> {
                    val value = pop()
                    val sequence = pop() as SequenceValue
                    check(sequence.list)
                    sequence.values += value
                }
                0x79 -> {
                    val index = (pop() as Long).toInt()
                    val sequence = pop() as SequenceValue
                    check(sequence.list)
                    stack += sequence.values.removeAt(index)
                }
                0x7a -> {
                    val type = module.types[u16()] as HbcType.Array
                    val initializer = string(u16())
                    val values = MutableList<Any?>(type.size) { run(initializer) }
                    stack += SequenceValue(false, values)
                }
                else -> error("Test interpreter does not implement opcode 0x${opcode.toString(16)}")
            }
        }
    }

    private fun arithmetic(opcode: Int, left: Any?, right: Any?): Any =
        if (left is Long && right is Long) {
            when (opcode) {
                0x20 -> left + right
                0x21 -> left - right
                0x22 -> left * right
                0x23 -> left / right
                0x24 -> left % right
                else -> error("Unknown arithmetic opcode")
            }
        } else {
            val l = (left as Number).toDouble()
            val r = (right as Number).toDouble()
            when (opcode) {
                0x20 -> l + r
                0x21 -> l - r
                0x22 -> l * r
                0x23 -> l / r
                0x24 -> l % r
                else -> error("Unknown arithmetic opcode")
            }
        }

    private fun comparison(opcode: Int, left: Any?, right: Any?): Any = when (opcode) {
        0x30 -> left == right
        0x31 -> left != right
        0x32 -> (left as Number).toDouble() < (right as Number).toDouble()
        0x33 -> (left as Number).toDouble() <= (right as Number).toDouble()
        0x34 -> (left as Number).toDouble() > (right as Number).toDouble()
        0x35 -> (left as Number).toDouble() >= (right as Number).toDouble()
        0x36 -> (left as Boolean) && (right as Boolean)
        0x37 -> (left as Boolean) || (right as Boolean)
        else -> error("Unknown comparison opcode")
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
