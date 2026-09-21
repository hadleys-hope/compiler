package lv426.compiler.ast

fun AstNode.toPrettyTree(indent: String = "", isLast: Boolean = true): String {
    val sb = StringBuilder()
    val marker = if (isLast) "└── " else "├── "
    val childIndent = indent + if (isLast) "    " else "│   "

    when (this) {
        is SourceNode -> {
            sb.appendLine("Program: ${programName ?: "<unnamed>"}")
            declarations.forEachIndexed { i, decl ->
                sb.append(decl.toPrettyTree("", i == declarations.lastIndex))
            }
        }

        is ConstDeclNode -> {
            sb.appendLine("$indent$marker[Const] $name: ${formatType(type)} = ${formatExpr(value)}")
        }

        is GlobalVarDeclNode -> {
            val init = if (initialValue != null) " = ${formatExpr(initialValue)}" else ""
            sb.appendLine("$indent$marker[GlobalVar] $name: ${formatType(type)}$init")
        }

        is StructDeclNode -> {
            sb.appendLine("$indent$marker[Struct] $name")
            fields.forEachIndexed { i, f ->
                val init = if (f.initialValue != null) " = ${formatExpr(f.initialValue)}" else ""
                val fMarker = if (i == fields.lastIndex) "└── " else "├── "
                sb.appendLine("$childIndent$fMarker${f.name}: ${formatType(f.type)}$init")
            }
        }

        is EnumDeclNode -> {
            sb.appendLine("$indent$marker[Enum] $name { ${members.joinToString(", ")} }")
        }

        is EventDeclNode -> {
            val params = parameters.joinToString(", ") { "${it.name}: ${formatType(it.type)}" }
            sb.appendLine("$indent$marker[Event] $name($params)")
        }

        is FunctionDeclNode -> {
            val params = parameters.joinToString(", ") { "${it.name}: ${formatType(it.type)}" }
            sb.appendLine("$indent$marker[Function] def $name($params): ${formatType(returnType)}")
            body.forEachIndexed { i, stmt ->
                sb.append(stmt.toPrettyTree(childIndent, i == body.lastIndex))
            }
        }

        is StartHandlerNode -> {
            sb.appendLine("$indent$marker[Handler] on start")
            body.forEachIndexed { i, stmt ->
                sb.append(stmt.toPrettyTree(childIndent, i == body.lastIndex))
            }
        }

        is EventHandlerNode -> {
            sb.appendLine("$indent$marker[Handler] on $eventName(${parameters.joinToString(", ")})")
            body.forEachIndexed { i, stmt ->
                sb.append(stmt.toPrettyTree(childIndent, i == body.lastIndex))
            }
        }

        is EveryHandlerNode -> {
            sb.appendLine("$indent$marker[Handler] every ${duration.value} ${duration.unit}")
            body.forEachIndexed { i, stmt ->
                sb.append(stmt.toPrettyTree(childIndent, i == body.lastIndex))
            }
        }

        is AtHandlerNode -> {
            sb.appendLine("$indent$marker[Handler] at ${duration.value} ${duration.unit}")
            body.forEachIndexed { i, stmt ->
                sb.append(stmt.toPrettyTree(childIndent, i == body.lastIndex))
            }
        }

        // --- Инструкции (Statements) ---

        is AssignmentStmtNode -> {
            val opStr = when (op) {
                AssignOp.ASSIGN -> "="
                AssignOp.PLUS_ASSIGN -> "+="
                AssignOp.MINUS_ASSIGN -> "-="
                AssignOp.MUL_ASSIGN -> "*="
                AssignOp.DIV_ASSIGN -> "/="
                AssignOp.MOD_ASSIGN -> "%="
            }
            sb.appendLine("$indent$marker[Assign] ${formatExpr(target)} $opStr ${formatExpr(value)}")
        }

        is LocalVarDeclNode -> {
            val init = if (initialValue != null) " = ${formatExpr(initialValue)}" else ""
            sb.appendLine("$indent$marker[Var] $name: ${formatType(type)}$init")
        }

        is IfStmtNode -> {
            sb.appendLine("$indent$marker[If]")
            branches.forEachIndexed { i, b ->
                val bMarker = if (i == branches.lastIndex && elseBody == null) "└── " else "├── "
                sb.appendLine("$childIndent${bMarker}Condition: ${formatExpr(b.condition)}")
                val branchChildIndent = childIndent + if (i == branches.lastIndex && elseBody == null) "    " else "│   "
                b.body.forEachIndexed { j, s ->
                    sb.append(s.toPrettyTree(branchChildIndent, j == b.body.lastIndex))
                }
            }
            if (elseBody != null) {
                sb.appendLine("$childIndent└── Else:")
                elseBody.forEachIndexed { j, s ->
                    sb.append(s.toPrettyTree(childIndent + "    ", j == elseBody.lastIndex))
                }
            }
        }

        is WhileStmtNode -> {
            sb.appendLine("$indent$marker[While] Condition: ${formatExpr(condition)}")
            body.forEachIndexed { i, s ->
                sb.append(s.toPrettyTree(childIndent, i == body.lastIndex))
            }
        }

        is ForStmtNode -> {
            val stepStr = if (step != null) " step ${formatExpr(step)}" else ""
            sb.appendLine("$indent$marker[For] $variable from ${formatExpr(from)} to ${formatExpr(to)}$stepStr")
            body.forEachIndexed { i, s ->
                sb.append(s.toPrettyTree(childIndent, i == body.lastIndex))
            }
        }

        is EmitStmtNode -> {
            val args = arguments.joinToString(", ") { formatExpr(it) }
            sb.appendLine("$indent$marker[Emit] $eventName($args)")
        }

        is ExprStmtNode -> {
            sb.appendLine("$indent$marker[ExprStmt] ${formatExpr(expr)}")
        }

        is ReturnStmtNode -> {
            val v = if (value != null) " ${formatExpr(value)}" else ""
            sb.appendLine("$indent$marker[Return]$v")
        }

        is BreakStmtNode -> sb.appendLine("$indent$marker[Break]")
        is ContinueStmtNode -> sb.appendLine("$indent$marker[Continue]")

        else -> sb.appendLine("$indent$marker$this")
    }

    return sb.toString()
}

// Хелперы компактного форматирования типов и выражений
private fun formatType(t: TypeRefNode): String = when (t) {
    is PrimitiveTypeNode -> t.type.name.lowercase()
    is CustomTypeNode -> t.name
    is ListTypeNode -> "list<${formatType(t.elementType)}>"
    is ArrayTypeNode -> "${formatType(t.elementType)}[${t.size}]"
}

private fun formatExpr(e: ExprNode): String = when (e) {
    is VarExprNode -> e.name
    is IntLiteralNode -> e.value.toString()
    is RealLiteralNode -> e.value.toString()
    is BoolLiteralNode -> e.value.toString()
    is StringLiteralNode -> "\"${e.value}\""
    is DurationLiteralNode -> "${e.value} ${e.unit.name.lowercase()}"
    is BinaryExprNode -> "(${formatExpr(e.left)} ${e.op} ${formatExpr(e.right)})"
    is UnaryExprNode -> "${e.op}(${formatExpr(e.operand)})"
    is FieldAccessExprNode -> "${formatExpr(e.target)}.${e.fieldName}"
    is IndexAccessExprNode -> "${formatExpr(e.target)}[${formatExpr(e.index)}]"
    is CallExprNode -> "${formatExpr(e.function)}(${e.arguments.joinToString(", ") { formatExpr(it) }})"
}