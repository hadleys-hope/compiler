package lv426.compiler.backend

import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HbcExecutionTest {
    private fun integer(value: Long) = IrInstruction.Push(HbcConstant.IntValue(value))

    @Test fun `globals without initializers receive typed defaults even without user functions`() {
        val program = IrProgram(emptyList(), globals = listOf(
            IrGlobal("i", HbcType.IntType), IrGlobal("r", HbcType.RealType), IrGlobal("b", HbcType.BoolType),
            IrGlobal("s", HbcType.StringType), IrGlobal("t", HbcType.TimeType)
        ))
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(program)))
        probe.start()
        assertEquals(listOf(0L, 0.0, false, "", 0L), listOf("i", "r", "b", "s", "t").map(probe::global))
        assertFailsWith<IllegalStateException> { probe.start() }
    }

    @Test fun `nested defaults are fresh and const aliases retain reference semantics`() {
        val int = HbcType.IntType
        val items = HbcType.Array(HbcType.Struct("Item"), 2)
        val program = IrProgram(listOf(
            IrFunction("next", 0, 0, listOf(
                IrInstruction.LoadGlobal("counter"), integer(1), IrInstruction.Binary(BinaryOperation.ADD),
                IrInstruction.StoreGlobal("counter"), IrInstruction.LoadGlobal("counter"), IrInstruction.Return(true)
            )),
            IrFunction("aliasInit", 0, 0, listOf(IrInstruction.LoadGlobal("left"), IrInstruction.Return(true))),
            IrFunction("mutate", 0, 0, listOf(
                IrInstruction.LoadGlobal("alias"), integer(0), IrInstruction.LoadIndex,
                IrInstruction.LoadField("Item", "samples"), integer(9), IrInstruction.ListAppend, IrInstruction.Return()
            )),
            IrFunction("length", 2, 2, listOf(
                IrInstruction.LoadLocal(0), IrInstruction.LoadLocal(1), IrInstruction.LoadIndex,
                IrInstruction.LoadField("Item", "samples"), IrInstruction.Length, IrInstruction.Return(true)
            )),
            IrFunction("ordinal", 1, 1, listOf(
                IrInstruction.LoadGlobal("left"), IrInstruction.LoadLocal(0), IrInstruction.LoadIndex,
                IrInstruction.LoadField("Item", "ordinal"), IrInstruction.Return(true)
            )),
            IrFunction("empty", 0, 0, listOf(
                IrInstruction.DefaultValue(HbcType.Array(int, 0)), IrInstruction.Length, IrInstruction.Return(true)
            ))
        ), structs = listOf(IrStruct("Item", listOf(
            IrField("ordinal", int, "next"), IrField("samples", HbcType.ListType(int))
        ))), globals = listOf(
            IrGlobal("counter", int), IrGlobal("left", items), IrGlobal("right", items),
            IrGlobal("alias", items, "aliasInit", mutable = false)
        ))
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(program)))
        probe.start()
        assertEquals(4L, probe.global("counter"))
        assertEquals(1L, probe.run("ordinal", listOf(0L)))
        assertEquals(2L, probe.run("ordinal", listOf(1L)))
        probe.run("mutate")
        assertEquals(1L, probe.run("length", listOf(probe.global("left"), 0L)))
        assertEquals(0L, probe.run("length", listOf(probe.global("left"), 1L)))
        assertEquals(0L, probe.run("length", listOf(probe.global("right"), 0L)))
        assertEquals(0L, probe.run("empty"))
    }

    @Test fun `local defaults are evaluated anew on each call`() {
        val program = IrProgram(listOf(IrFunction("fresh", 0, 1, listOf(
            IrInstruction.DefaultValue(HbcType.ListType(HbcType.IntType)), IrInstruction.StoreLocal(0),
            IrInstruction.LoadLocal(0), integer(1), IrInstruction.ListAppend,
            IrInstruction.LoadLocal(0), IrInstruction.Length, IrInstruction.Return(true)
        ))))
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(program)))
        assertEquals(1L, probe.run("fresh"))
        assertEquals(1L, probe.run("fresh"))
    }

    @Test fun `forward global access aborts initialization before start handlers`() {
        val program = IrProgram(listOf(
            IrFunction("forward", 0, 0, listOf(IrInstruction.LoadGlobal("later"), IrInstruction.Return(true))),
            IrFunction("start", 0, 0, listOf(IrInstruction.Emit("started", 0), IrInstruction.Return()))
        ), globals = listOf(IrGlobal("first", HbcType.IntType, "forward"), IrGlobal("later", HbcType.IntType)),
            events = listOf(IrEvent("started", emptyList())), handlers = listOf(IrHandler.Start("start")))
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(program)))
        assertFailsWith<NoSuchElementException> { probe.start() }
        assertEquals(emptyList(), probe.events)
    }

    @Test fun `startup queues events until all start handlers finish and runs zero at once`() {
        val program = IrProgram(listOf(
            IrFunction("init", 0, 0, listOf(integer(7), IrInstruction.Emit("record", 1), integer(0), IrInstruction.Return(true))),
            IrFunction("startA", 0, 0, listOf(integer(8), IrInstruction.Emit("record", 1), IrInstruction.Return())),
            IrFunction("startB", 0, 0, listOf(IrInstruction.LoadGlobal("count"), IrInstruction.Emit("record", 1), IrInstruction.Return())),
            IrFunction("record", 1, 1, listOf(IrInstruction.LoadGlobal("count"), integer(1),
                IrInstruction.Binary(BinaryOperation.ADD), IrInstruction.StoreGlobal("count"), IrInstruction.Return())),
            IrFunction("atZero", 0, 0, listOf(IrInstruction.LoadGlobal("count"), IrInstruction.Emit("record", 1), IrInstruction.Return())),
            IrFunction("later", 0, 0, listOf(integer(99), IrInstruction.Emit("record", 1), IrInstruction.Return()))
        ), globals = listOf(IrGlobal("count", HbcType.IntType, "init")),
            events = listOf(IrEvent("record", listOf(HbcType.IntType))), handlers = listOf(
                IrHandler.At(0, "atZero"), IrHandler.Start("startA"), IrHandler.Start("startB"),
                IrHandler.Event("record", "record"), IrHandler.Every(1000, "later"), IrHandler.At(1000, "later")
            ))
        val probe = BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(program)))
        probe.start()
        assertEquals(listOf(7L, 8L, 0L, 3L), probe.events.map { it.second.single() })
        assertEquals(4L, probe.global("count"))
        assertFailsWith<IllegalStateException> { probe.start() }
    }

    @Test fun `extended bytecode initializes globals mutates nested fields and dispatches metadata targets`() {
        val module = HbcReader.read(HbcBackend.compileToBytes(extendedProgram()))
        val probe = BytecodeProbe(module)
        probe.start()
        assertEquals(5L, probe.run("readX"))
        assertEquals(listOf("changed" to listOf<Any>(5L)), probe.events)
        assertEquals(42L, probe.run("main"))
        assertEquals(99L, probe.run("readX"))
        assertEquals(72L, probe.run("array"))
        probe.deliver("changed", listOf(30L))
        assertEquals(30L, probe.run("readX"))
        probe.invoke(module.handlers.single { it.kind == HbcHandlerKind.EVERY })
        assertEquals(31L, probe.run("readX"))
        probe.invoke(module.handlers.single { it.kind == HbcHandlerKind.AT })
        assertEquals(123L, probe.run("readX"))
    }

    @Test fun `aggregate constructors preserve order empty values and nested array references`() {
        val int = HbcType.IntType
        val function = IrFunction("nested", 0, 1, listOf(
            integer(4), integer(6), IrInstruction.NewArray(int, 2),
            IrInstruction.NewList(HbcType.Array(int, 2), 1), IrInstruction.StoreLocal(0),
            IrInstruction.LoadLocal(0), integer(0), IrInstruction.LoadIndex,
            integer(1), IrInstruction.LoadIndex,
            IrInstruction.NewArray(int, 0), IrInstruction.Length, IrInstruction.Binary(BinaryOperation.ADD),
            IrInstruction.NewList(int), IrInstruction.Length, IrInstruction.Binary(BinaryOperation.ADD),
            IrInstruction.Return(true)
        ))
        val module = HbcReader.read(HbcBackend.compileToBytes(IrProgram(listOf(function))))
        assertEquals(6L, BytecodeProbe(module).run("nested"))
    }

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

/** Bounded ABI probe, independent of the encoder. It is not a production VM or scheduler. */
private class BytecodeProbe(private val module: HbcModule) {
    private var remainingSteps = 10_000
    val events = mutableListOf<Pair<String, List<Any>>>()
    private val globals = mutableMapOf<String, Any>()
    private val pendingEvents = ArrayDeque<Pair<String, List<Any>>>()
    private var started = false
    private var dispatching = false
    private data class StructValue(val type: Int, val fields: MutableList<Any>)
    private data class SequenceValue(val resizable: Boolean, val values: MutableList<Any>)

    fun start() {
        check(!started)
        started = true
        module.globals.forEach { globals[string(it.nameConstant)] = run(string(module.functions[it.initializerFunction].nameConstant)) }
        module.handlers.filter { it.kind == HbcHandlerKind.START }.forEach(::invoke)
        drainEvents()
        module.handlers.filter { it.kind == HbcHandlerKind.AT && it.milliseconds == 0L }.forEach(::invoke)
        drainEvents()
    }

    fun global(name: String): Any = globals.getValue(name)

    fun invoke(handler: HbcHandler) { run(string(module.functions[handler.functionIndex].nameConstant)) }

    fun deliver(event: String, arguments: List<Any>) {
        pendingEvents.addLast(event to arguments)
        drainEvents()
    }

    private fun drainEvents() {
        if (dispatching) return
        dispatching = true
        try {
            while (pendingEvents.isNotEmpty()) {
                val (event, arguments) = pendingEvents.removeFirst()
                module.handlers.filter { it.kind == HbcHandlerKind.EVENT && string(module.events[it.eventIndex].nameConstant) == event }
                    .forEach { run(string(module.functions[it.functionIndex].nameConstant), arguments) }
            }
        } finally { dispatching = false }
    }

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
                0x04 -> stack += globals.getValue(string(u16()))
                0x05 -> {
                    val name = string(u16())
                    check(module.globals.single { string(it.nameConstant) == name }.mutable)
                    check(name in globals)
                    globals[name] = pop()
                }
                0x70 -> {
                    val type = u16()
                    stack += StructValue(type, callArguments(module.structs[type].fields.size).toMutableList())
                }
                0x71, 0x72 -> {
                    val type = u16()
                    val slot = u16()
                    val value = if (opcode == 0x72) pop() else null
                    val receiver = pop() as StructValue
                    check(receiver.type == type)
                    if (value == null) stack += receiver.fields[slot] else receiver.fields[slot] = value
                }
                0x73, 0x74 -> {
                    val type = module.types[u16()]
                    val count = if (opcode == 0x73) (type as HbcType.Array).size else u16()
                    stack += SequenceValue(opcode == 0x74, callArguments(count).toMutableList())
                }
                0x7a -> {
                    val type = module.types[u16()] as HbcType.Array
                    val factory = string(u16())
                    stack += SequenceValue(false, MutableList(type.size) { run(factory) })
                }
                0x75, 0x76, 0x79 -> {
                    val value = if (opcode == 0x76) pop() else null
                    val index = pop() as Long
                    val receiver = pop() as SequenceValue
                    check(index >= 0 && index < receiver.values.size)
                    when (opcode) {
                        0x75 -> stack += receiver.values[index.toInt()]
                        0x76 -> receiver.values[index.toInt()] = checkNotNull(value)
                        else -> { check(receiver.resizable); stack += receiver.values.removeAt(index.toInt()) }
                    }
                }
                0x77 -> stack += (pop() as SequenceValue).values.size.toLong()
                0x78 -> {
                    val value = pop()
                    val receiver = pop() as SequenceValue
                    check(receiver.resizable)
                    receiver.values += value
                }
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
                    val emitted = event to callArguments(u16())
                    events += emitted
                    pendingEvents.addLast(emitted)
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
