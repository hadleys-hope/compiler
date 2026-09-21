package lv426.compiler.backend

/** A deterministic, readable view of a validated HBC module. Offsets are decimal bytes. */
object HbcDisassembler {
    fun disassemble(bytes: ByteArray): String = disassemble(HbcReader.read(bytes))

    fun disassemble(module: HbcModule): String {
        HbcVerifier.validate(module)
        return buildString {
            appendLine("HBC v${HbcModule.VERSION}")
            appendLine("constants (${module.constants.size}):")
            module.constants.forEachIndexed { index, constant ->
                appendLine("  #$index = ${formatConstant(constant)}")
            }
            module.functions.forEach { function ->
                val name = (module.constants[function.nameConstant] as HbcConstant.StringValue).value
                appendLine()
                appendLine("function ${quote(name)} parameters=${function.parameterCount} locals=${function.localCount} bytes=${function.code.size}:")
                HbcDecoder.decode(function.code).forEach { instruction ->
                    append("  ${instruction.offset.toString().padStart(6, '0')}  ${instruction.opcode}")
                    when (instruction.opcode) {
                        Opcode.PUSH_CONST, Opcode.LOAD_GLOBAL, Opcode.STORE_GLOBAL -> {
                            append(" #${instruction.operand} ; ${formatConstant(module.constants[instruction.operand])}")
                        }
                        Opcode.LOAD_LOCAL, Opcode.STORE_LOCAL -> append(" ${instruction.operand}")
                        Opcode.JUMP, Opcode.JUMP_IF_FALSE -> append(" ${instruction.operand} -> ${instruction.jumpTarget}")
                        Opcode.CALL, Opcode.EMIT -> {
                            val target = (module.constants[instruction.operand] as HbcConstant.StringValue).value
                            append(" #${instruction.operand} argc=${instruction.argumentCount} ; ${quote(target)}")
                        }
                        else -> Unit
                    }
                    appendLine()
                }
            }
        }
    }

    private fun formatConstant(constant: HbcConstant): String = when (constant) {
        is HbcConstant.IntValue -> "int ${constant.value}"
        is HbcConstant.RealValue -> "real ${constant.value}"
        is HbcConstant.BoolValue -> "bool ${constant.value}"
        is HbcConstant.StringValue -> "string ${quote(constant.value)}"
        is HbcConstant.TimeValue -> "time ${constant.milliseconds}ms"
    }

    private fun quote(value: String): String = buildString {
        append('"')
        value.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (character.isISOControl()) {
                    append("\\u${character.code.toString(16).padStart(4, '0')}")
                } else append(character)
            }
        }
        append('"')
    }
}
