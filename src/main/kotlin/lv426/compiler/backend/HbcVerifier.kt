package lv426.compiler.backend

import java.util.ArrayDeque

/**
 * Shared safety checks for emitted, loaded and manually constructed modules.
 * This verifies bytecode structure and operand-stack heights, not runtime value types.
 * Parameters occupy local slots; each function starts with an empty operand stack.
 */
object HbcVerifier {
    fun validate(module: HbcModule) {
        if (module.constants.size > 0xffff) fail("HBC constant pool exceeds 65535 entries")
        if (module.functions.isEmpty() || module.functions.size > 0xffff) {
            fail("HBC module must contain between 1 and 65535 functions")
        }
        module.constants.forEach { if (it is HbcConstant.StringValue) HbcUtf8.encode(it.value) }
        val functionsByName = linkedMapOf<String, HbcFunction>()
        module.functions.forEach { function ->
            val name = nameAt(module, function.nameConstant, "Function name")
            if (functionsByName.put(name, function) != null) fail("Duplicate function name '$name'")
            if (function.parameterCount !in 0..0xffff || function.localCount !in function.parameterCount..0xffff) {
                fail("Function '$name': invalid frame (parameters=${function.parameterCount}, locals=${function.localCount})")
            }
        }
        validateMetadata(module)
        val globalsByName = module.globals.associateBy { nameAt(module, it.nameConstant, "Global") }
        val eventsByName = module.events.associateBy { nameAt(module, it.nameConstant, "Event") }
        functionsByName.forEach { (name, function) ->
            try {
                validateFunction(module, function, functionsByName, globalsByName, eventsByName)
            } catch (error: HbcFormatException) {
                fail("Function '$name': ${error.message}")
            }
        }
    }

    internal fun validateTypeShape(type: HbcType, depth: Int = 0) {
        if (depth >= 64) fail("Type nesting exceeds 64 levels")
        when (type) {
            is HbcType.Struct -> {
                if (type.name.isBlank()) fail("Struct type name cannot be blank")
                HbcUtf8.encode(type.name)
            }
            is HbcType.Array -> {
                if (type.size < 0) fail("Array size cannot be negative")
                validateTypeShape(type.element, depth + 1)
            }
            is HbcType.ListType -> validateTypeShape(type.element, depth + 1)
            else -> Unit
        }
    }

    private fun validateMetadata(module: HbcModule) {
        val tables = listOf(module.types, module.structs, module.globals, module.events, module.handlers)
        if (tables.any { it.size > 0xffff }) fail("HBC metadata table exceeds 65535 entries")
        if (!module.metadataPresent && tables.any { it.isNotEmpty() }) fail("Metadata tables require a metadata section")

        fun unique(names: List<Int>, context: String) {
            val values = names.map { nameAt(module, it, context) }
            if (values.toSet().size != values.size) fail("Duplicate $context name")
        }

        fun type(index: Int) {
            if (index !in module.types.indices) fail("Type reference $index out of range")
        }

        unique(module.structs.map { it.nameConstant }, "struct")
        unique(module.globals.map { it.nameConstant }, "global")
        unique(module.events.map { it.nameConstant }, "event")

        val structNames = module.structs.map { nameAt(module, it.nameConstant, "Struct") }.toSet()
        fun resolveType(type: HbcType) {
            when (type) {
                is HbcType.Struct -> if (type.name !in structNames) fail("Unknown struct type '${type.name}'")
                is HbcType.Array -> resolveType(type.element)
                is HbcType.ListType -> resolveType(type.element)
                else -> Unit
            }
        }

        module.types.forEach {
            validateTypeShape(it)
            resolveType(it)
        }

        module.structs.forEach { struct ->
            if (struct.fields.size > 0xffff) fail("Struct field table exceeds 65535 entries")
            unique(struct.fields.map { it.nameConstant }, "field")
            struct.fields.forEach { type(it.typeIndex) }
        }

        fun function(index: Int, parameters: Int, value: Boolean, role: String) {
            val function = module.functions.getOrNull(index) ?: fail("$role function reference out of range")
            if (function.parameterCount != parameters) fail("$role requires $parameters parameters")
            HbcDecoder.decode(function.code).forEach {
                if (it.opcode == (if (value) Opcode.RETURN else Opcode.RETURN_VALUE)) {
                    fail("$role has an incompatible return instruction")
                }
            }
        }

        module.globals.forEach {
            type(it.typeIndex)
            function(it.initializerFunction, 0, true, "Global initializer")
        }

        module.events.forEach {
            if (it.parameterTypes.size > 0xffff) fail("Event signature exceeds 65535 parameters")
            it.parameterTypes.forEach(::type)
        }

        module.handlers.forEach {
            if (it.kind != HbcHandlerKind.EVENT && it.eventIndex != 0) fail("Unexpected handler event index")
            if (it.kind != HbcHandlerKind.EVERY && it.kind != HbcHandlerKind.AT && it.milliseconds != 0L) {
                fail("Unexpected handler time")
            }
            val parameters = when (it.kind) {
                HbcHandlerKind.EVENT -> (module.events.getOrNull(it.eventIndex)
                    ?: fail("Handler event reference out of range")).parameterTypes.size
                HbcHandlerKind.START -> 0
                HbcHandlerKind.EVERY -> {
                    if (it.milliseconds <= 0) fail("Every interval must be positive")
                    0
                }
                HbcHandlerKind.AT -> {
                    if (it.milliseconds < 0) fail("At time cannot be negative")
                    0
                }
            }
            function(it.functionIndex, parameters, false, "${it.kind} handler")
        }
    }

    private fun validateFunction(
        module: HbcModule,
        function: HbcFunction,
        functionsByName: Map<String, HbcFunction>,
        globalsByName: Map<String, HbcGlobal>,
        eventsByName: Map<String, HbcEvent>
    ) {
        val instructions = HbcDecoder.decode(function.code)
        if (instructions.isEmpty()) fail("empty bytecode")
        val instructionIndexes = instructions.withIndex().associate { it.value.offset to it.index }
        instructions.forEach { instruction ->
            val context = "${instruction.opcode} at byte ${instruction.offset}"
            when (instruction.opcode) {
                Opcode.PUSH_CONST -> if (instruction.operand !in module.constants.indices) fail("$context: constant reference out of range")
                Opcode.LOAD_LOCAL, Opcode.STORE_LOCAL -> if (instruction.operand !in 0 until function.localCount) {
                    fail("$context: local slot ${instruction.operand} out of range")
                }
                Opcode.LOAD_GLOBAL, Opcode.STORE_GLOBAL, Opcode.EMIT, Opcode.CALL -> {
                    val name = nameAt(module, instruction.operand, context)
                    if (instruction.opcode == Opcode.CALL) {
                        val callee = functionsByName[name]
                        if (callee != null && instruction.argumentCount != callee.parameterCount) {
                            fail("$context: '$name' expects ${callee.parameterCount} arguments, got ${instruction.argumentCount}")
                        }
                    }
                    if (module.metadataPresent) {
                        when (instruction.opcode) {
                            Opcode.LOAD_GLOBAL, Opcode.STORE_GLOBAL -> {
                                val global = globalsByName[name] ?: fail("$context: undeclared global '$name'")
                                if (instruction.opcode == Opcode.STORE_GLOBAL && !global.mutable) {
                                    fail("$context: cannot store to immutable global '$name'")
                                }
                            }
                            Opcode.EMIT -> {
                                val event = eventsByName[name] ?: fail("$context: undeclared event '$name'")
                                if (instruction.argumentCount != event.parameterTypes.size) {
                                    fail("$context: event argument count mismatch")
                                }
                            }
                            else -> Unit
                        }
                    }
                }
                Opcode.NEW_STRUCT, Opcode.LOAD_FIELD, Opcode.STORE_FIELD -> {
                    val struct = module.structs.getOrNull(instruction.operand)
                        ?: fail("$context: struct reference out of range")
                    if (instruction.opcode != Opcode.NEW_STRUCT && instruction.argumentCount !in struct.fields.indices) {
                        fail("$context: field slot out of range")
                    }
                }
                Opcode.NEW_ARRAY, Opcode.NEW_ARRAY_INIT -> {
                    if (module.types.getOrNull(instruction.operand) !is HbcType.Array) {
                        fail("$context: expected array type")
                    }
                    if (instruction.opcode == Opcode.NEW_ARRAY_INIT) {
                        val name = nameAt(module, instruction.argumentCount, context)
                        val factory = functionsByName[name] ?: fail("$context: unknown array initializer '$name'")
                        if (factory.parameterCount != 0 || HbcDecoder.decode(factory.code).any { it.opcode == Opcode.RETURN }) {
                            fail("$context: array initializer '$name' must take no arguments and return a value")
                        }
                    }
                }
                Opcode.NEW_LIST -> {
                    if (module.types.getOrNull(instruction.operand) !is HbcType.ListType) {
                        fail("$context: expected list type")
                    }
                }
                Opcode.JUMP, Opcode.JUMP_IF_FALSE -> {
                    val target = instruction.jumpTarget
                    if (target < 0 || target >= function.code.size.toLong() || target.toInt() !in instructionIndexes) {
                        fail("$context: jump target $target is not an instruction boundary")
                    }
                }
                else -> Unit
            }
        }
        validateStack(module, instructions, instructionIndexes)
    }

    private fun validateStack(
        module: HbcModule,
        instructions: List<HbcInstruction>,
        instructionIndexes: Map<Int, Int>
    ) {
        val heights = IntArray(instructions.size) { -1 }
        val pending = ArrayDeque<Int>()
        heights[0] = 0
        pending.addLast(0)

        fun visit(index: Int, height: Int, from: HbcInstruction) {
            if (index == instructions.size) fail("${from.opcode} at byte ${from.offset}: reachable fallthrough past end of function")
            val previous = heights[index]
            if (previous == -1) {
                heights[index] = height
                pending.addLast(index)
            } else if (previous != height) {
                fail("Inconsistent stack height at byte ${instructions[index].offset}: $previous versus $height")
            }
        }

        while (pending.isNotEmpty()) {
            val index = pending.removeFirst()
            val instruction = instructions[index]
            val height = heights[index]
            val opcode = instruction.opcode
            val required = when (opcode) {
                Opcode.NEW_STRUCT -> module.structs[instruction.operand].fields.size
                Opcode.NEW_ARRAY -> (module.types[instruction.operand] as HbcType.Array).size
                Opcode.NEW_LIST -> instruction.argumentCount
                Opcode.LOAD_FIELD, Opcode.LENGTH -> 1
                Opcode.STORE_FIELD, Opcode.LOAD_INDEX, Opcode.LIST_APPEND, Opcode.LIST_REMOVE -> 2
                Opcode.STORE_INDEX -> 3
                Opcode.STORE_LOCAL, Opcode.STORE_GLOBAL, Opcode.NEGATE, Opcode.NOT, Opcode.INT_TO_REAL,
                Opcode.JUMP_IF_FALSE, Opcode.RETURN_VALUE, Opcode.POP -> 1
                Opcode.ADD, Opcode.SUBTRACT, Opcode.MULTIPLY, Opcode.DIVIDE, Opcode.MODULO,
                Opcode.EQUAL, Opcode.NOT_EQUAL, Opcode.LESS, Opcode.LESS_OR_EQUAL,
                Opcode.GREATER, Opcode.GREATER_OR_EQUAL, Opcode.AND, Opcode.OR -> 2
                Opcode.CALL, Opcode.EMIT -> instruction.argumentCount
                else -> 0
            }
            if (height < required) fail("$opcode at byte ${instruction.offset}: stack underflow (needs $required, has $height)")
            if (opcode == Opcode.RETURN || opcode == Opcode.RETURN_VALUE) {
                if (height != required) fail("$opcode at byte ${instruction.offset}: expected exactly $required stack values, got $height")
                continue
            }
            val produced = when (opcode) {
                Opcode.NEW_STRUCT, Opcode.NEW_ARRAY, Opcode.NEW_ARRAY_INIT, Opcode.NEW_LIST,
                Opcode.LOAD_FIELD, Opcode.LOAD_INDEX, Opcode.LENGTH, Opcode.LIST_REMOVE -> 1
                Opcode.PUSH_CONST, Opcode.LOAD_LOCAL, Opcode.LOAD_GLOBAL, Opcode.CALL,
                Opcode.NEGATE, Opcode.NOT, Opcode.INT_TO_REAL, Opcode.ADD, Opcode.SUBTRACT, Opcode.MULTIPLY,
                Opcode.DIVIDE, Opcode.MODULO, Opcode.EQUAL, Opcode.NOT_EQUAL, Opcode.LESS,
                Opcode.LESS_OR_EQUAL, Opcode.GREATER, Opcode.GREATER_OR_EQUAL, Opcode.AND, Opcode.OR -> 1
                else -> 0
            }
            val nextHeight = height - required + produced
            if (opcode == Opcode.JUMP || opcode == Opcode.JUMP_IF_FALSE) {
                visit(instructionIndexes.getValue(instruction.jumpTarget.toInt()), nextHeight, instruction)
            }
            if (opcode != Opcode.JUMP) visit(index + 1, nextHeight, instruction)
        }
    }

    private fun nameAt(module: HbcModule, index: Int, context: String): String {
        val name = (module.constants.getOrNull(index) as? HbcConstant.StringValue)?.value
            ?: fail("$context: name reference must point to a string constant")
        if (name.isBlank()) fail("$context: name cannot be blank")
        return name
    }

    private fun fail(message: String): Nothing = throw HbcFormatException(message)
}

/** The one instruction decoder shared by validation and diagnostic tools. */
internal object HbcDecoder {
    fun decode(code: ByteArray): List<HbcInstruction> {
        val instructions = mutableListOf<HbcInstruction>()
        var offset = 0
        while (offset < code.size) {
            val opcode = try {
                Opcode.fromCode(code[offset].toInt() and 0xff)
            } catch (error: HbcFormatException) {
                throw HbcFormatException("${error.message} at byte $offset")
            }
            val size = when (opcode) {
                Opcode.NEW_STRUCT, Opcode.NEW_ARRAY -> 3
                Opcode.LOAD_FIELD, Opcode.STORE_FIELD, Opcode.NEW_LIST, Opcode.NEW_ARRAY_INIT -> 5
                Opcode.PUSH_CONST, Opcode.LOAD_LOCAL, Opcode.STORE_LOCAL, Opcode.LOAD_GLOBAL, Opcode.STORE_GLOBAL -> 3
                Opcode.JUMP, Opcode.JUMP_IF_FALSE, Opcode.CALL, Opcode.EMIT -> 5
                else -> 1
            }
            if (size > code.size - offset) throw HbcFormatException("Truncated $opcode instruction at byte $offset")
            val isJump = opcode == Opcode.JUMP || opcode == Opcode.JUMP_IF_FALSE
            val operand = if (isJump) signedInt(code, offset + 1) else if (size > 1) unsignedShort(code, offset + 1) else 0
            val argumentCount = if (!isJump && size == 5) unsignedShort(code, offset + 3) else 0
            instructions += HbcInstruction(offset, opcode, size, operand, argumentCount)
            offset += size
        }
        return instructions
    }

    private fun unsignedShort(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xff) shl 8) or (bytes[offset + 1].toInt() and 0xff)

    private fun signedInt(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() shl 24) or ((bytes[offset + 1].toInt() and 0xff) shl 16) or
            ((bytes[offset + 2].toInt() and 0xff) shl 8) or (bytes[offset + 3].toInt() and 0xff)
}

internal data class HbcInstruction(
    val offset: Int,
    val opcode: Opcode,
    val size: Int,
    val operand: Int,
    val argumentCount: Int
) {
    val jumpTarget: Long get() = offset.toLong() + size + operand
}
