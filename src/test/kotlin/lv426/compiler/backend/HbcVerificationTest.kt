package lv426.compiler.backend

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class HbcVerificationTest {
    private fun program(vararg instructions: IrInstruction): IrProgram =
        IrProgram(listOf(IrFunction("main", 0, 1, instructions.toList())))

    private val one = IrInstruction.Push(HbcConstant.IntValue(1))

    @Test fun `all stack-consuming instructions reject insufficient operands`() {
        val consumers = listOf(
            IrInstruction.Pop,
            IrInstruction.StoreLocal(0),
            IrInstruction.StoreGlobal("result"),
            IrInstruction.Unary(UnaryOperation.NEGATE),
            IrInstruction.Unary(UnaryOperation.NOT),
            IrInstruction.Call("external", 1),
            IrInstruction.Emit("event", 1)
        )
        consumers.forEach { instruction ->
            assertFailsWith<IllegalArgumentException>(instruction.toString()) {
                HbcBackend.compile(program(instruction, IrInstruction.Return()))
            }
        }
        BinaryOperation.entries.forEach { operation ->
            assertFailsWith<IllegalArgumentException>(operation.name) {
                HbcBackend.compile(program(one, IrInstruction.Binary(operation), IrInstruction.Return(true)))
            }
        }
        val done = IrLabel("done")
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(program(IrInstruction.JumpIfFalse(done), IrInstruction.Label(done), IrInstruction.Return()))
        }
    }

    @Test fun `return has no operands and return value has exactly one`() {
        val invalidBodies = listOf(
            arrayOf(IrInstruction.Return(true)),
            arrayOf(one, IrInstruction.Return()),
            arrayOf(one, one, IrInstruction.Return(true))
        )
        invalidBodies.forEach { body ->
            assertFailsWith<IllegalArgumentException> { HbcBackend.compile(program(*body)) }
        }
        HbcBackend.compile(program(IrInstruction.Return()))
        HbcBackend.compile(program(one, IrInstruction.Return(true)))
    }

    @Test fun `reachable end of bytecode is rejected`() {
        val done = IrLabel("done")
        val invalidBodies = listOf(
            emptyArray(), arrayOf(IrInstruction.Nop), arrayOf(one),
            arrayOf(IrInstruction.Jump(done), IrInstruction.Label(done)),
            arrayOf(one, IrInstruction.JumpIfFalse(done), IrInstruction.Label(done))
        )
        invalidBodies.forEach { body ->
            assertFailsWith<IllegalArgumentException> { HbcBackend.compile(program(*body)) }
        }
    }

    @Test fun `branches must agree on stack height at their join`() {
        val join = IrLabel("join")
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(program(
                one, IrInstruction.JumpIfFalse(join), one,
                IrInstruction.Label(join), IrInstruction.Return()
            ))
        }
    }

    @Test fun `loop cannot accumulate stack entries on each iteration`() {
        val loop = IrLabel("loop")
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(program(IrInstruction.Label(loop), one, IrInstruction.Jump(loop)))
        }
        // An intentional infinite loop with a stable stack is valid bytecode.
        HbcBackend.compile(program(IrInstruction.Label(loop), IrInstruction.Jump(loop)))
    }

    @Test fun `balanced diamond flow and conditional consumption are accepted`() {
        val alternative = IrLabel("alternative")
        val join = IrLabel("join")
        val bytes = HbcBackend.compileToBytes(program(
            IrInstruction.Push(HbcConstant.BoolValue(true)), IrInstruction.JumpIfFalse(alternative),
            one, IrInstruction.Jump(join), IrInstruction.Label(alternative),
            IrInstruction.Push(HbcConstant.IntValue(2)), IrInstruction.Label(join),
            IrInstruction.Return(true)
        ))
        assertContentEquals(bytes, HbcReader.read(bytes).toBytes())
    }

    @Test fun `internal calls must match the declared number of parameters`() {
        val caller = IrFunction("main", 0, 0, listOf(IrInstruction.Call("callee", 0), IrInstruction.Return(true)))
        val callee = IrFunction("callee", 1, 1, listOf(IrInstruction.LoadLocal(0), IrInstruction.Return(true)))
        assertFailsWith<IllegalArgumentException> { HbcBackend.compile(IrProgram(listOf(caller, callee))) }
    }

    @Test fun `calls push one result including void calls and emits push nothing`() {
        val main = IrFunction("main", 0, 0, listOf(
            one, IrInstruction.Call("external", 1), IrInstruction.Pop,
            IrInstruction.Call("voidFunction", 0), IrInstruction.Pop,
            one, IrInstruction.Emit("event", 1), IrInstruction.Return()
        ))
        val voidFunction = IrFunction("voidFunction", 0, 0, listOf(IrInstruction.Return()))
        val bytes = HbcBackend.compileToBytes(IrProgram(listOf(main, voidFunction)))
        assertContentEquals(bytes, HbcReader.read(bytes).toBytes())
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(program(one, IrInstruction.Emit("event", 1), IrInstruction.Pop, IrInstruction.Return()))
        }
        assertFailsWith<IllegalArgumentException> {
            HbcBackend.compile(program(IrInstruction.Call("external", 0), IrInstruction.Return()))
        }
    }

    @Test fun `reader and writer both refuse structurally valid stack-invalid code`() {
        val codes = listOf(
            rawCode(0x62, 0x60), // POP from an empty stack.
            rawCode(0x61), // RETURN_VALUE without a value.
            rawCode(0x01, 0, 0, 0x60), // Value left behind at RETURN.
            rawCode(0x00) // Reachable fallthrough.
        )
        codes.forEach { code ->
            assertFailsWith<HbcFormatException> {
                HbcReader.read(rawHbc(functions = listOf(RawHbcFunction(code))))
            }
            assertFailsWith<IllegalArgumentException> {
                HbcModule(listOf(HbcConstant.StringValue("main")), listOf(HbcFunction(0, 0, 0, code))).toBytes()
            }
        }
    }

    @Test fun `reader validates internal call arity across the complete function table`() {
        val caller = rawCode(0x50, 0, 1, 0, 0, 0x61)
        assertFailsWith<HbcFormatException> {
            HbcReader.read(rawHbc(
                constants = listOf(rawString("main"), rawString("callee")),
                functions = listOf(
                    RawHbcFunction(caller), RawHbcFunction(rawCode(0x02, 0, 0, 0x61), name = 1, parameters = 1)
                )
            ))
        }
    }

    @Test fun `writer revalidates bytecode mutated after compilation`() {
        val module = HbcBackend.compile(program(IrInstruction.Return()))
        module.functions.single().code[0] = Opcode.POP.code.toByte()
        assertFailsWith<IllegalArgumentException> { module.toBytes() }
    }
}
