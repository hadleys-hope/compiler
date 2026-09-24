package lv426.compiler.backend

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.StandardCopyOption

/** Assembles stack IR into a verified, portable `.hbc` module. */
object HbcBackend {
    fun compile(program: IrProgram): HbcModule = assemble(DefaultLowering(program).lower())

    private fun assemble(program: IrProgram): HbcModule {
        require(program.functions.isNotEmpty()) { "An HBC module must contain at least one function" }
        require(program.functions.size <= 0xffff) { "HBC function table exceeds 65535 entries" }
        require(program.functions.map { it.name }.toSet().size == program.functions.size) { "Function names must be unique" }
        val pool = ConstantPool()
        val types = mutableListOf<HbcType>()
        fun typeIndex(type: HbcType): Int {
            HbcVerifier.validateTypeShape(type)
            val existing = types.indexOf(type)
            if (existing >= 0) return existing
            require(types.size < 0xffff) { "HBC type table exceeds 65535 entries" }
            types += type
            return types.lastIndex
        }
        fun nameIndex(name: String) = pool.indexOf(HbcConstant.StringValue(name))
        fun functionIndex(name: String): Int = program.functions.indexOfFirst { it.name == name }.also {
            require(it >= 0) { "Unknown metadata function '$name'" }
        }
        val structs = program.structs.map { struct ->
            HbcStruct(nameIndex(struct.name), struct.fields.map { HbcField(nameIndex(it.name), typeIndex(it.type)) })
        }
        val globals = program.globals.map {
            HbcGlobal(nameIndex(it.name), typeIndex(it.type), functionIndex(requireNotNull(it.initializer)), it.mutable)
        }
        val events = program.events.map { HbcEvent(nameIndex(it.name), it.parameters.map(::typeIndex)) }
        val handlers = program.handlers.map {
            val function = functionIndex(it.function)
            when (it) {
                is IrHandler.Event -> {
                    val event = program.events.indexOfFirst { event -> event.name == it.event }
                    require(event >= 0) { "Unknown handler event '${it.event}'" }
                    HbcHandler(HbcHandlerKind.EVENT, function, eventIndex = event)
                }
                is IrHandler.Start -> HbcHandler(HbcHandlerKind.START, function)
                is IrHandler.Every -> HbcHandler(HbcHandlerKind.EVERY, function, milliseconds = it.milliseconds)
                is IrHandler.At -> HbcHandler(HbcHandlerKind.AT, function, milliseconds = it.milliseconds)
            }
        }
        val functions = program.functions.map { function ->
            validateFunction(function)
            HbcFunction(pool.indexOf(HbcConstant.StringValue(function.name)), function.parameterCount,
                function.localCount, FunctionEncoder(pool, function, program.structs, ::typeIndex).encode())
        }
        return HbcModule(pool.values, functions, types, structs, globals, events, handlers).also(HbcVerifier::validate)
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
                is IrInstruction.NewArray -> require(it.size >= 0) { "Array size cannot be negative" }
                is IrInstruction.NewArrayWithInitializer -> require(it.size >= 0) { "Array size cannot be negative" }
                is IrInstruction.NewList -> require(it.elementCount in 0..0xffff) { "Invalid list element count" }
                else -> Unit
            }
        }
    }
}

/** Expands default construction within the backend, without depending on frontend AST classes. */
private class DefaultLowering(private val program: IrProgram) {
    private val structs = program.structs.associateBy { it.name }
    private val functions = program.functions.associateBy { it.name }
    private val factories = linkedMapOf<HbcType, String>()
    private val constructionDepths = mutableMapOf<HbcType, Int>()
    private val generated = mutableListOf<IrFunction>()
    private val visiting = linkedSetOf<HbcType>()
    // Avoid capturing even unresolved external calls when allocating generated names.
    private val reserved = buildSet {
        addAll(functions.keys)
        addAll(program.handlers.map { it.function })
        addAll(program.globals.mapNotNull { it.initializer })
        addAll(program.structs.flatMap { it.fields }.mapNotNull { it.initializer })
        program.functions.flatMap { it.instructions }.forEach {
            when (it) {
                is IrInstruction.Call -> add(it.function)
                is IrInstruction.NewArrayWithInitializer -> add(it.initializer)
                else -> Unit
            }
        }
    }.toMutableSet()
    private var nextName = 0

    fun lower(): IrProgram {
        require(functions.size == program.functions.size) { "Function names must be unique" }
        require(structs.size == program.structs.size) { "Struct names must be unique" }
        program.structs.forEach { struct ->
            require(struct.fields.map { it.name }.toSet().size == struct.fields.size) { "Duplicate field in ${struct.name}" }
            struct.fields.forEach { field ->
                field.initializer?.let { name ->
                    val function = requireNotNull(functions[name]) { "Unknown initializer '$name' for ${struct.name}.${field.name}" }
                    require(function.parameterCount == 0 && function.instructions.none { it is IrInstruction.Return && !it.hasValue }) {
                        "Field initializer '$name' must take no arguments and return a value"
                    }
                }
            }
        }
        val lowered = program.functions.map { function ->
            function.copy(instructions = function.instructions.map {
                if (it is IrInstruction.DefaultValue) IrInstruction.Call(factory(it.type), 0) else it
            })
        }
        val globals = program.globals.map { global ->
            if (global.initializer == null) {
                require(global.mutable) { "Const '${global.name}' requires an explicit initializer" }
                global.copy(initializer = factory(global.type))
            } else global
        }
        return program.copy(functions = lowered + generated, globals = globals)
    }

    private fun factory(type: HbcType): String {
        HbcVerifier.validateTypeShape(type)
        require(type !in visiting) { "Cyclic default initialization: ${visiting.joinToString(" -> ")} -> $type" }
        factories[type]?.let {
            require(visiting.size + constructionDepths.getValue(type) <= 64) { "Default construction nesting exceeds 64 levels" }
            return it
        }
        require(visiting.size < 64) { "Default construction nesting exceeds 64 levels" }
        require(program.functions.size + factories.size < 0xffff) { "HBC function table exceeds 65535 entries" }
        var name: String
        do { name = "@default/${nextName++}" } while (!reserved.add(name))
        factories[type] = name
        visiting += type
        var depth = 1
        fun childFactory(child: HbcType): String = factory(child).also {
            depth = maxOf(depth, 1 + constructionDepths.getValue(child))
        }
        val instructions = when (type) {
            HbcType.IntType -> listOf(IrInstruction.Push(HbcConstant.IntValue(0)))
            HbcType.RealType -> listOf(IrInstruction.Push(HbcConstant.RealValue(0.0)))
            HbcType.BoolType -> listOf(IrInstruction.Push(HbcConstant.BoolValue(false)))
            HbcType.StringType -> listOf(IrInstruction.Push(HbcConstant.StringValue("")))
            HbcType.TimeType -> listOf(IrInstruction.Push(HbcConstant.TimeValue(0)))
            is HbcType.ListType -> listOf(IrInstruction.NewList(type.element))
            is HbcType.Array -> if (type.size == 0) listOf(IrInstruction.NewArray(type.element, 0)) else {
                listOf(IrInstruction.NewArrayWithInitializer(type.element, type.size, childFactory(type.element)))
            }
            is HbcType.Struct -> {
                val struct = requireNotNull(structs[type.name]) { "Unknown struct '${type.name}'" }
                struct.fields.map { IrInstruction.Call(it.initializer ?: childFactory(it.type), 0) } + IrInstruction.NewStruct(type.name)
            }
        }
        visiting -= type
        constructionDepths[type] = depth
        generated += IrFunction(name, 0, 0, instructions + IrInstruction.Return(true))
        return name
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

private class FunctionEncoder(
    private val pool: ConstantPool, private val function: IrFunction,
    private val structs: List<IrStruct>, private val typeIndex: (HbcType) -> Int
) {
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
        is IrInstruction.DefaultValue -> error("DefaultValue must be lowered before encoding")
        is IrInstruction.LoadLocal -> operand(Opcode.LOAD_LOCAL, instruction.slot)
        is IrInstruction.StoreLocal -> operand(Opcode.STORE_LOCAL, instruction.slot)
        is IrInstruction.LoadGlobal -> operand(Opcode.LOAD_GLOBAL, pool.indexOf(HbcConstant.StringValue(instruction.name)))
        is IrInstruction.StoreGlobal -> operand(Opcode.STORE_GLOBAL, pool.indexOf(HbcConstant.StringValue(instruction.name)))
        is IrInstruction.NewStruct -> operand(Opcode.NEW_STRUCT, structIndex(instruction.struct))
        is IrInstruction.LoadField -> field(Opcode.LOAD_FIELD, instruction.struct, instruction.field)
        is IrInstruction.StoreField -> field(Opcode.STORE_FIELD, instruction.struct, instruction.field)
        is IrInstruction.NewArray -> operand(Opcode.NEW_ARRAY, typeIndex(HbcType.Array(instruction.elementType, instruction.size)))
        is IrInstruction.NewArrayWithInitializer -> {
            operand(Opcode.NEW_ARRAY_INIT, typeIndex(HbcType.Array(instruction.elementType, instruction.size)))
            out.writeShort(pool.indexOf(HbcConstant.StringValue(instruction.initializer)))
        }
        is IrInstruction.NewList -> {
            operand(Opcode.NEW_LIST, typeIndex(HbcType.ListType(instruction.elementType)))
            out.writeShort(instruction.elementCount)
        }
        IrInstruction.LoadIndex -> opcode(Opcode.LOAD_INDEX)
        IrInstruction.StoreIndex -> opcode(Opcode.STORE_INDEX)
        IrInstruction.Length -> opcode(Opcode.LENGTH)
        IrInstruction.ListAppend -> opcode(Opcode.LIST_APPEND)
        IrInstruction.ListRemove -> opcode(Opcode.LIST_REMOVE)
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
    private fun structIndex(name: String): Int = structs.indexOfFirst { it.name == name }.also {
        require(it in 0..0xfffe) { "Unknown or out-of-range struct '$name'" }
    }
    private fun field(opcode: Opcode, struct: String, field: String) {
        val index = structIndex(struct)
        val slot = structs[index].fields.indexOfFirst { it.name == field }
        require(slot in 0..0xfffe) { "Unknown or out-of-range field '$struct.$field'" }
        operand(opcode, index); out.writeShort(slot)
    }
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
