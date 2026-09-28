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
            if (module.metadataPresent) {
                fun name(index: Int) = quote((module.constants[index] as HbcConstant.StringValue).value)
                appendLine("types (${module.types.size}):")
                module.types.forEachIndexed { index, type -> appendLine("  #$index = ${formatType(type)}") }
                appendLine("structs (${module.structs.size}):")
                module.structs.forEachIndexed { index, struct ->
                    appendLine("  #$index ${name(struct.nameConstant)}:")
                    struct.fields.forEachIndexed { slot, field ->
                        appendLine("    $slot ${name(field.nameConstant)} type=#${field.typeIndex}")
                    }
                }
                appendLine("globals (${module.globals.size}):")
                module.globals.forEach {
                    appendLine("  ${name(it.nameConstant)} type=#${it.typeIndex} mutable=${it.mutable} initializer=${name(module.functions[it.initializerFunction].nameConstant)}")
                }
                appendLine("events (${module.events.size}):")
                module.events.forEachIndexed { index, event ->
                    appendLine("  #$index ${name(event.nameConstant)} parameters=${event.parameterTypes}")
                }
                appendLine("handlers (${module.handlers.size}):")
                module.handlers.forEach {
                    val detail = when (it.kind) {
                        HbcHandlerKind.EVENT -> " event=${name(module.events[it.eventIndex].nameConstant)}"
                        HbcHandlerKind.START -> ""
                        HbcHandlerKind.EVERY, HbcHandlerKind.AT -> " time=${it.milliseconds}ms"
                    }
                    appendLine("  ${it.kind}$detail function=${name(module.functions[it.functionIndex].nameConstant)}")
                }
            }
            module.functions.forEach { function ->
                val name = (module.constants[function.nameConstant] as HbcConstant.StringValue).value
                appendLine()
                appendLine("function ${quote(name)} parameters=${function.parameterCount} locals=${function.localCount} bytes=${function.code.size}:")
                HbcDecoder.decode(function.code).forEach { instruction ->
                    append("  ${instruction.offset.toString().padStart(6, '0')}  ${instruction.opcode}")
                    when (instruction.opcode) {
                        Opcode.NEW_STRUCT -> append(" struct=#${instruction.operand}")
                        Opcode.LOAD_FIELD, Opcode.STORE_FIELD -> append(" struct=#${instruction.operand} field=${instruction.argumentCount}")
                        Opcode.NEW_ARRAY -> append(" type=#${instruction.operand}")
                        Opcode.NEW_ARRAY_INIT -> {
                            val factory = (module.constants[instruction.argumentCount] as HbcConstant.StringValue).value
                            append(" type=#${instruction.operand} initializer=${quote(factory)}")
                        }
                        Opcode.NEW_LIST -> append(" type=#${instruction.operand} count=${instruction.argumentCount}")
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

    private fun formatType(type: HbcType): String = when (type) {
        HbcType.IntType -> "int"
        HbcType.RealType -> "real"
        HbcType.BoolType -> "bool"
        HbcType.StringType -> "string"
        HbcType.TimeType -> "time"
        is HbcType.Struct -> "struct ${quote(type.name)}"
        is HbcType.Array -> "${formatType(type.element)}[${type.size}]"
        is HbcType.ListType -> "list<${formatType(type.element)}>"
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
