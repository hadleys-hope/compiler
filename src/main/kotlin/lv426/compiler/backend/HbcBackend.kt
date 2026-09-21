package lv426.compiler.backend

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.StandardCopyOption

/** Assembles stack IR into a verified, portable `.hbc` module. */
object HbcBackend {
    fun compile(program: IrProgram): HbcModule {
        require(program.functions.isNotEmpty()) { "An HBC module must contain at least one function" }
        require(program.functions.size <= 0xffff) { "HBC function table exceeds 65535 entries" }
        require(program.functions.map { it.name }.toSet().size == program.functions.size) { "Function names must be unique" }
        val pool = ConstantPool()
        val functions = program.functions.map { function ->
            validateFunction(function)
            HbcFunction(pool.indexOf(HbcConstant.StringValue(function.name)), function.parameterCount,
                function.localCount, FunctionEncoder(pool, function).encode())
        }
        return HbcModule(pool.values, functions).also(HbcVerifier::validate)
    }

    fun write(program: IrProgram, output: Path): HbcModule {
        val module = compile(program)
        // Complete validation/serialization before touching an existing output file.
        val bytes = module.toBytes()
        val target = output.toAbsolutePath().normalize()
        Files.createDirectories(target.parent)
        val temporary = Files.createTempFile(target.parent, ".hbc-", ".tmp")
        try {
            Files.write(temporary, bytes)
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
        return module
    }

    fun compileToBytes(program: IrProgram): ByteArray = compile(program).toBytes()

    private fun validateFunction(function: IrFunction) {
        require(function.name.isNotBlank()) { "Function name cannot be blank" }
        require(function.parameterCount in 0..0xffff) { "Invalid parameter count in ${function.name}" }
        require(function.localCount in function.parameterCount..0xffff) { "Invalid local count in ${function.name}" }
        val labels = function.instructions.filterIsInstance<IrInstruction.Label>().map { it.label.name }
        require(labels.size == labels.toSet().size) { "Duplicate label in ${function.name}" }
        val defined = labels.toSet()
        function.instructions.forEach {
            when (it) {
                is IrInstruction.LoadLocal -> require(it.slot in 0 until function.localCount) { "Invalid local slot ${it.slot} in ${function.name}" }
                is IrInstruction.StoreLocal -> require(it.slot in 0 until function.localCount) { "Invalid local slot ${it.slot} in ${function.name}" }
                is IrInstruction.Jump -> require(it.target.name in defined) { "Unknown label ${it.target.name}" }
                is IrInstruction.JumpIfFalse -> require(it.target.name in defined) { "Unknown label ${it.target.name}" }
                is IrInstruction.Call -> require(it.argumentCount in 0..0xffff) { "Invalid call argument count" }
                is IrInstruction.Emit -> require(it.argumentCount in 0..0xffff) { "Invalid event argument count" }
                else -> Unit
            }
        }
    }
}

private class ConstantPool {
    private val indexes = linkedMapOf<HbcConstant, Int>()
    val values: List<HbcConstant> get() = indexes.keys.toList()
    fun indexOf(value: HbcConstant): Int = indexes.getOrPut(value) {
        require(indexes.size < 0xffff) { "HBC constant pool exceeds 65535 entries" }
        indexes.size
    }
}

private class FunctionEncoder(private val pool: ConstantPool, private val function: IrFunction) {
    private val output = ByteArrayOutputStream()
    private val out = DataOutputStream(output)
    private val labels = mutableMapOf<String, Int>()
    private data class Fixup(val position: Int, val target: String)
    private val fixups = mutableListOf<Fixup>()

    fun encode(): ByteArray {
        function.instructions.forEach { emit(it) }
        val bytes = output.toByteArray()
        fixups.forEach { fixup ->
            val target = labels.getValue(fixup.target)
            val afterOperand = fixup.position + 4
            val delta = target - afterOperand
            bytes[fixup.position] = (delta ushr 24).toByte()
            bytes[fixup.position + 1] = (delta ushr 16).toByte()
            bytes[fixup.position + 2] = (delta ushr 8).toByte()
            bytes[fixup.position + 3] = delta.toByte()
        }
        return bytes
    }

    private fun emit(instruction: IrInstruction) = when (instruction) {
        is IrInstruction.Label -> labels[instruction.label.name] = output.size()
        is IrInstruction.Push -> operand(Opcode.PUSH_CONST, pool.indexOf(instruction.constant))
        is IrInstruction.LoadLocal -> operand(Opcode.LOAD_LOCAL, instruction.slot)
        is IrInstruction.StoreLocal -> operand(Opcode.STORE_LOCAL, instruction.slot)
        is IrInstruction.LoadGlobal -> operand(Opcode.LOAD_GLOBAL, pool.indexOf(HbcConstant.StringValue(instruction.name)))
        is IrInstruction.StoreGlobal -> operand(Opcode.STORE_GLOBAL, pool.indexOf(HbcConstant.StringValue(instruction.name)))
        is IrInstruction.Unary -> opcode(if (instruction.operation == UnaryOperation.NEGATE) Opcode.NEGATE else Opcode.NOT)
        is IrInstruction.Binary -> opcode(binaryOpcode(instruction.operation))
        is IrInstruction.Jump -> jump(Opcode.JUMP, instruction.target)
        is IrInstruction.JumpIfFalse -> jump(Opcode.JUMP_IF_FALSE, instruction.target)
        is IrInstruction.Call -> { operand(Opcode.CALL, pool.indexOf(HbcConstant.StringValue(instruction.function))); out.writeShort(instruction.argumentCount) }
        is IrInstruction.Emit -> { operand(Opcode.EMIT, pool.indexOf(HbcConstant.StringValue(instruction.event))); out.writeShort(instruction.argumentCount) }
        is IrInstruction.Return -> opcode(if (instruction.hasValue) Opcode.RETURN_VALUE else Opcode.RETURN)
        IrInstruction.Pop -> opcode(Opcode.POP)
        IrInstruction.Nop -> opcode(Opcode.NOP)
    }

    private fun opcode(opcode: Opcode) = out.writeByte(opcode.code)
    private fun operand(opcode: Opcode, value: Int) { opcode(opcode); out.writeShort(value) }
    private fun jump(opcode: Opcode, label: IrLabel) { opcode(opcode); fixups += Fixup(output.size(), label.name); out.writeInt(0) }
    private fun binaryOpcode(operation: BinaryOperation): Opcode = when (operation) {
        BinaryOperation.ADD -> Opcode.ADD
        BinaryOperation.SUBTRACT -> Opcode.SUBTRACT
        BinaryOperation.MULTIPLY -> Opcode.MULTIPLY
        BinaryOperation.DIVIDE -> Opcode.DIVIDE
        BinaryOperation.MODULO -> Opcode.MODULO
        BinaryOperation.EQUAL -> Opcode.EQUAL
        BinaryOperation.NOT_EQUAL -> Opcode.NOT_EQUAL
        BinaryOperation.LESS -> Opcode.LESS
        BinaryOperation.LESS_OR_EQUAL -> Opcode.LESS_OR_EQUAL
        BinaryOperation.GREATER -> Opcode.GREATER
        BinaryOperation.GREATER_OR_EQUAL -> Opcode.GREATER_OR_EQUAL
        BinaryOperation.AND -> Opcode.AND
        BinaryOperation.OR -> Opcode.OR
    }
}
