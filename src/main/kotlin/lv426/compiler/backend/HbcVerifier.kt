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
        functionsByName.forEach { (name, function) ->
            try {
                validateFunction(module, function, functionsByName)
            } catch (error: HbcFormatException) {
                fail("Function '$name': ${error.message}")
            }
        }
    }

    private fun validateFunction(module: HbcModule, function: HbcFunction, functionsByName: Map<String, HbcFunction>) {
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
        validateStack(instructions, instructionIndexes)
    }

    private fun validateStack(instructions: List<HbcInstruction>, instructionIndexes: Map<Int, Int>) {
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
                Opcode.STORE_LOCAL, Opcode.STORE_GLOBAL, Opcode.NEGATE, Opcode.NOT,
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
                Opcode.PUSH_CONST, Opcode.LOAD_LOCAL, Opcode.LOAD_GLOBAL, Opcode.CALL,
                Opcode.NEGATE, Opcode.NOT, Opcode.ADD, Opcode.SUBTRACT, Opcode.MULTIPLY,
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
                Opcode.PUSH_CONST, Opcode.LOAD_LOCAL, Opcode.STORE_LOCAL, Opcode.LOAD_GLOBAL, Opcode.STORE_GLOBAL -> 3
                Opcode.JUMP, Opcode.JUMP_IF_FALSE, Opcode.CALL, Opcode.EMIT -> 5
                else -> 1
            }
            if (size > code.size - offset) throw HbcFormatException("Truncated $opcode instruction at byte $offset")
            val isJump = opcode == Opcode.JUMP || opcode == Opcode.JUMP_IF_FALSE
            val operand = if (isJump) signedInt(code, offset + 1) else if (size > 1) unsignedShort(code, offset + 1) else 0
            val argumentCount = if (opcode == Opcode.CALL || opcode == Opcode.EMIT) unsignedShort(code, offset + 3) else 0
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
