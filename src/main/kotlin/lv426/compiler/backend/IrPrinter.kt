package lv426.compiler.backend

/** Deterministic human-readable dump of the stack IR used between lowering and HBC emission. */
object IrPrinter {
    fun print(program: IrProgram): String = buildString {
        appendLine("IR program")

        appendLine("structs (${program.structs.size}):")
        program.structs.forEach { struct ->
            appendLine("  struct ${struct.name}")
            struct.fields.forEach { field ->
                val initializer = field.initializer?.let { " init=$it" } ?: ""
                appendLine("    ${field.name}: ${formatType(field.type)}$initializer")
            }
        }

        appendLine("globals (${program.globals.size}):")
        program.globals.forEach { global ->
            val initializer = global.initializer?.let { " init=$it" } ?: " default"
            appendLine("  ${if (global.mutable) "var" else "const"} ${global.name}: ${formatType(global.type)}$initializer")
        }

        appendLine("events (${program.events.size}):")
        program.events.forEach { event ->
            appendLine("  event ${event.name}(${event.parameters.joinToString(", ") { formatType(it) }})")
        }

        appendLine("handlers (${program.handlers.size}):")
        program.handlers.forEach { handler ->
            when (handler) {
                is IrHandler.Start -> appendLine("  start -> ${handler.function}")
                is IrHandler.Event -> appendLine("  event ${handler.event} -> ${handler.function}")
                is IrHandler.Every -> appendLine("  every ${handler.milliseconds}ms -> ${handler.function}")
                is IrHandler.At -> appendLine("  at ${handler.milliseconds}ms -> ${handler.function}")
            }
        }

        appendLine("functions (${program.functions.size}):")
        program.functions.forEach { function ->
            appendLine()
            appendLine("  function ${function.name} parameters=${function.parameterCount} locals=${function.localCount}")
            function.instructions.forEachIndexed { index, instruction ->
                appendLine("    ${index.toString().padStart(4, '0')}  ${formatInstruction(instruction)}")
            }
        }
    }.trimEnd()

    private fun formatInstruction(instruction: IrInstruction): String = when (instruction) {
        is IrInstruction.Label -> "${instruction.label.name}:"
        is IrInstruction.Push -> "PUSH ${formatConstant(instruction.constant)}"
        is IrInstruction.DefaultValue -> "DEFAULT ${formatType(instruction.type)}"
        is IrInstruction.LoadLocal -> "LOAD_LOCAL ${instruction.slot}"
        is IrInstruction.StoreLocal -> "STORE_LOCAL ${instruction.slot}"
        is IrInstruction.LoadGlobal -> "LOAD_GLOBAL ${instruction.name}"
        is IrInstruction.StoreGlobal -> "STORE_GLOBAL ${instruction.name}"
        is IrInstruction.NewStruct -> "NEW_STRUCT ${instruction.struct}"
        is IrInstruction.LoadField -> "LOAD_FIELD ${instruction.struct}.${instruction.field}"
        is IrInstruction.StoreField -> "STORE_FIELD ${instruction.struct}.${instruction.field}"
        is IrInstruction.NewArray -> "NEW_ARRAY ${formatType(instruction.elementType)}[${instruction.size}]"
        is IrInstruction.NewArrayWithInitializer ->
            "NEW_ARRAY_INIT ${formatType(instruction.elementType)}[${instruction.size}] ${instruction.initializer}"
        is IrInstruction.NewList -> "NEW_LIST ${formatType(instruction.elementType)} count=${instruction.elementCount}"
        IrInstruction.LoadIndex -> "LOAD_INDEX"
        IrInstruction.StoreIndex -> "STORE_INDEX"
        IrInstruction.Length -> "LENGTH"
        IrInstruction.ListAppend -> "LIST_APPEND"
        IrInstruction.ListRemove -> "LIST_REMOVE"
        IrInstruction.IntToReal -> "INT_TO_REAL"
        is IrInstruction.Unary -> "UNARY ${instruction.operation}"
        is IrInstruction.Binary -> "BINARY ${instruction.operation}"
        is IrInstruction.Jump -> "JUMP ${instruction.target.name}"
        is IrInstruction.JumpIfFalse -> "JUMP_IF_FALSE ${instruction.target.name}"
        is IrInstruction.Call -> "CALL ${instruction.function} argc=${instruction.argumentCount}"
        is IrInstruction.Emit -> "EMIT ${instruction.event} argc=${instruction.argumentCount}"
        is IrInstruction.Return -> if (instruction.hasValue) "RETURN_VALUE" else "RETURN"
        IrInstruction.Pop -> "POP"
        IrInstruction.Nop -> "NOP"
    }

    private fun formatConstant(constant: HbcConstant): String = when (constant) {
        is HbcConstant.IntValue -> "int ${constant.value}"
        is HbcConstant.RealValue -> "real ${constant.value}"
        is HbcConstant.BoolValue -> "bool ${constant.value}"
        is HbcConstant.StringValue -> "string ${quote(constant.value)}"
        is HbcConstant.TimeValue -> "time ${constant.milliseconds}ms"
    }

    private fun formatType(type: HbcType): String = when (type) {
        HbcType.IntType -> "int"
        HbcType.RealType -> "real"
        HbcType.BoolType -> "bool"
        HbcType.StringType -> "string"
        HbcType.TimeType -> "time"
        is HbcType.Struct -> type.name
        is HbcType.Array -> "${formatType(type.element)}[${type.size}]"
        is HbcType.ListType -> "list<${formatType(type.element)}>"
    }

    private fun quote(value: String): String = buildString {
        append('"')
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(ch)
            }
        }
        append('"')
    }
}
