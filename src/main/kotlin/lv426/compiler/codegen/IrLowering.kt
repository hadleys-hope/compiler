package lv426.compiler.codegen

import java.util.ArrayDeque
import lv426.compiler.ast.*
import lv426.compiler.backend.*
import lv426.compiler.semantic.*

/** Converts a semantically checked HopeLang AST into the backend's stack IR. */
class IrLowering(private val semantic: SemanticResult) {
    private val program = requireNotNull(semantic.program) { "SemanticResult does not contain a program" }
    private val functions = mutableListOf<IrFunction>()
    private var generatedId = 0

    fun lower(): IrProgram {
        require(semantic.isValid) { "Cannot lower a program with semantic errors" }
        functions.clear()
        generatedId = 0

        val structs = program.declarations.filterIsInstance<StructDeclNode>().map(::lowerStruct)
        val globals = program.declarations.mapNotNull { declaration ->
            when (declaration) {
                is ConstDeclNode -> lowerGlobal(declaration.name, declaration.type, declaration.value, mutable = false)
                is GlobalVarDeclNode -> lowerGlobal(declaration.name, declaration.type, declaration.initialValue, mutable = true)
                else -> null
            }
        }
        val events = program.declarations.filterIsInstance<EventDeclNode>().map { event ->
            IrEvent(event.name, event.parameters.map { toHbcType(typeOf(it.type)) })
        }

        program.declarations.filterIsInstance<FunctionDeclNode>().forEach { function ->
            val symbol = semantic.symbols.findFunction(function.name)
                ?: error("Missing semantic function '${function.name}'")
            functions += FunctionLowerer(
                semantic = semantic,
                name = function.name,
                parameters = symbol.parameters.map { it.name to it.type },
                returnType = symbol.returnType
            ).lowerBody(function.body)
        }

        val handlers = mutableListOf<IrHandler>()
        program.declarations.forEach { declaration ->
            when (declaration) {
                is StartHandlerNode -> {
                    val name = generated("handler/start")
                    functions += FunctionLowerer(semantic, name, emptyList(), VoidType).lowerBody(declaration.body)
                    handlers += IrHandler.Start(name)
                }
                is EventHandlerNode -> {
                    val event = semantic.symbols.findEvent(declaration.eventName)
                        ?: error("Missing semantic event '${declaration.eventName}'")
                    val name = generated("handler/event/${declaration.eventName}")
                    val parameters = declaration.parameters.mapIndexed { index, parameter ->
                        parameter to event.parameters[index].type
                    }
                    functions += FunctionLowerer(semantic, name, parameters, VoidType).lowerBody(declaration.body)
                    handlers += IrHandler.Event(declaration.eventName, name)
                }
                is EveryHandlerNode -> {
                    val name = generated("handler/every")
                    functions += FunctionLowerer(semantic, name, emptyList(), VoidType).lowerBody(declaration.body)
                    handlers += IrHandler.Every(durationMillis(declaration.duration), name)
                }
                is AtHandlerNode -> {
                    val name = generated("handler/at")
                    functions += FunctionLowerer(semantic, name, emptyList(), VoidType).lowerBody(declaration.body)
                    handlers += IrHandler.At(durationMillis(declaration.duration), name)
                }
                else -> Unit
            }
        }

        if (functions.isEmpty()) {
            functions += IrFunction("@module/noop", 0, 0, listOf(IrInstruction.Return()))
        }

        return IrProgram(
            functions = functions.toList(),
            structs = structs,
            globals = globals,
            events = events,
            handlers = handlers
        )
    }

    private fun lowerStruct(node: StructDeclNode): IrStruct {
        val symbol = semantic.symbols.findStruct(node.name) ?: error("Missing semantic struct '${node.name}'")
        return IrStruct(node.name, node.fields.map { field ->
            val fieldType = symbol.findField(field.name)?.type ?: typeOf(field.type)
            val initializer = field.initialValue?.let { expression ->
                val name = generated("init/field/${node.name}/${field.name}")
                functions += FunctionLowerer(semantic, name, emptyList(), fieldType)
                    .lowerValue(expression, fieldType)
                name
            }
            IrField(field.name, toHbcType(fieldType), initializer)
        })
    }

    private fun lowerGlobal(
        name: String,
        typeNode: TypeRefNode,
        initializer: ExprNode?,
        mutable: Boolean
    ): IrGlobal {
        val type = typeOf(typeNode)
        val initializerName = initializer?.let { expression ->
            val functionName = generated("init/global/$name")
            functions += FunctionLowerer(semantic, functionName, emptyList(), type)
                .lowerValue(expression, type)
            functionName
        }
        return IrGlobal(name, toHbcType(type), initializerName, mutable)
    }

    private fun generated(prefix: String): String = "@$prefix/${generatedId++}"

    private fun typeOf(node: TypeRefNode): HopeType = semantic.typeOf(node)
        ?: error("Missing resolved type at ${node.location}")

    private fun durationMillis(node: DurationLiteralNode): Long = HbcTime.milliseconds(node.value, when (node.unit) {
        TimeUnit.MS -> HbcTimeUnit.MS
        TimeUnit.SEC -> HbcTimeUnit.SEC
        TimeUnit.MIN -> HbcTimeUnit.MIN
        TimeUnit.HOUR -> HbcTimeUnit.HOUR
        TimeUnit.DAY -> HbcTimeUnit.DAY
    })
}

private data class LocalBinding(val slot: Int, val type: HopeType)
private data class LoopTarget(val breakLabel: IrLabel, val continueLabel: IrLabel)

private class FunctionLowerer(
    private val semantic: SemanticResult,
    private val name: String,
    private val parameters: List<Pair<String, HopeType>>,
    private val returnType: HopeType
) {
    private val instructions = mutableListOf<IrInstruction>()
    private val scopes = ArrayDeque<MutableMap<String, LocalBinding>>()
    private val loops = ArrayDeque<LoopTarget>()
    private var nextSlot = 0
    private var nextLabel = 0

    init {
        enterScope()
        parameters.forEach { (name, type) -> declareLocal(name, type) }
    }

    fun lowerBody(body: List<StmtNode>): IrFunction {
        body.forEach(::lowerStatement)
        if (returnType == VoidType) instructions += IrInstruction.Return()
        return IrFunction(name, parameters.size, nextSlot, instructions.toList())
    }

    fun lowerValue(expression: ExprNode, expected: HopeType): IrFunction {
        compileExpressionAs(expression, expected)
        instructions += IrInstruction.Return(true)
        return IrFunction(name, parameters.size, nextSlot, instructions.toList())
    }

    private fun lowerStatement(statement: StmtNode) {
        when (statement) {
            is LocalVarDeclNode -> lowerLocal(statement)
            is AssignmentStmtNode -> lowerAssignment(statement)
            is IfStmtNode -> lowerIf(statement)
            is WhileStmtNode -> lowerWhile(statement)
            is ForStmtNode -> lowerFor(statement)
            is BreakStmtNode -> instructions += IrInstruction.Jump(loops.peekFirst().breakLabel)
            is ContinueStmtNode -> instructions += IrInstruction.Jump(loops.peekFirst().continueLabel)
            is ReturnStmtNode -> {
                if (statement.value == null) instructions += IrInstruction.Return()
                else {
                    compileExpressionAs(statement.value, returnType)
                    instructions += IrInstruction.Return(true)
                }
            }
            is EmitStmtNode -> {
                val event = semantic.symbols.findEvent(statement.eventName)
                    ?: error("Unknown event '${statement.eventName}' after semantic analysis")
                statement.arguments.forEachIndexed { index, argument ->
                    compileExpressionAs(argument, event.parameters[index].type)
                }
                instructions += IrInstruction.Emit(statement.eventName, statement.arguments.size)
            }
            is ExprStmtNode -> if (compileExpression(statement.expr)) instructions += IrInstruction.Pop
        }
    }

    private fun lowerLocal(node: LocalVarDeclNode) {
        val type = semantic.typeOf(node.type) ?: error("Missing local type at ${node.location}")
        val slot = allocateSlot()
        if (node.initialValue != null) compileExpressionAs(node.initialValue, type)
        else instructions += IrInstruction.DefaultValue(toHbcType(type))
        instructions += IrInstruction.StoreLocal(slot)
        bindLocal(node.name, LocalBinding(slot, type))
    }

    private fun lowerAssignment(node: AssignmentStmtNode) {
        val targetType = expressionType(node.target)
        if (node.op == AssignOp.ASSIGN) {
            lowerSimpleAssignment(node.target, node.value, targetType)
            return
        }
        val operation = when (node.op) {
            AssignOp.PLUS_ASSIGN -> BinaryOperation.ADD
            AssignOp.MINUS_ASSIGN -> BinaryOperation.SUBTRACT
            AssignOp.MUL_ASSIGN -> BinaryOperation.MULTIPLY
            AssignOp.DIV_ASSIGN -> BinaryOperation.DIVIDE
            AssignOp.MOD_ASSIGN -> BinaryOperation.MODULO
            AssignOp.ASSIGN -> error("unreachable")
        }
        when (val target = node.target) {
            is VarExprNode -> {
                loadVariable(target.name)
                compileExpressionAs(node.value, targetType)
                instructions += IrInstruction.Binary(operation)
                storeVariable(target.name)
            }
            is FieldAccessExprNode -> {
                val receiverType = expressionType(target.target) as StructType
                val receiverSlot = allocateSlot()
                check(compileExpression(target.target))
                instructions += IrInstruction.StoreLocal(receiverSlot)
                instructions += IrInstruction.LoadLocal(receiverSlot)
                instructions += IrInstruction.LoadLocal(receiverSlot)
                instructions += IrInstruction.LoadField(receiverType.name, target.fieldName)
                compileExpressionAs(node.value, targetType)
                instructions += IrInstruction.Binary(operation)
                instructions += IrInstruction.StoreField(receiverType.name, target.fieldName)
            }
            is IndexAccessExprNode -> {
                val containerSlot = allocateSlot()
                val indexSlot = allocateSlot()
                check(compileExpression(target.target))
                instructions += IrInstruction.StoreLocal(containerSlot)
                compileExpressionAs(target.index, IntType)
                instructions += IrInstruction.StoreLocal(indexSlot)
                instructions += IrInstruction.LoadLocal(containerSlot)
                instructions += IrInstruction.LoadLocal(indexSlot)
                instructions += IrInstruction.LoadLocal(containerSlot)
                instructions += IrInstruction.LoadLocal(indexSlot)
                instructions += IrInstruction.LoadIndex
                compileExpressionAs(node.value, targetType)
                instructions += IrInstruction.Binary(operation)
                instructions += IrInstruction.StoreIndex
            }
            else -> error("Unsupported assignment target ${target::class.simpleName}")
        }
    }

    private fun lowerSimpleAssignment(target: ExprNode, value: ExprNode, targetType: HopeType) {
        when (target) {
            is VarExprNode -> {
                compileExpressionAs(value, targetType)
                storeVariable(target.name)
            }
            is FieldAccessExprNode -> {
                val receiverType = expressionType(target.target) as StructType
                check(compileExpression(target.target))
                compileExpressionAs(value, targetType)
                instructions += IrInstruction.StoreField(receiverType.name, target.fieldName)
            }
            is IndexAccessExprNode -> {
                check(compileExpression(target.target))
                compileExpressionAs(target.index, IntType)
                compileExpressionAs(value, targetType)
                instructions += IrInstruction.StoreIndex
            }
            else -> error("Unsupported assignment target ${target::class.simpleName}")
        }
    }

    private fun lowerIf(node: IfStmtNode) {
        val end = label("if_end")
        node.branches.forEachIndexed { index, branch ->
            val next = label("if_next_$index")
            compileExpressionAs(branch.condition, BoolType)
            instructions += IrInstruction.JumpIfFalse(next)
            enterScope()
            branch.body.forEach(::lowerStatement)
            leaveScope()
            instructions += IrInstruction.Jump(end)
            instructions += IrInstruction.Label(next)
        }
        node.elseBody?.let { body ->
            enterScope()
            body.forEach(::lowerStatement)
            leaveScope()
        }
        instructions += IrInstruction.Label(end)
        instructions += IrInstruction.Nop
    }

    private fun lowerWhile(node: WhileStmtNode) {
        val condition = label("while_condition")
        val end = label("while_end")
        instructions += IrInstruction.Label(condition)
        compileExpressionAs(node.condition, BoolType)
        instructions += IrInstruction.JumpIfFalse(end)
        loops.addFirst(LoopTarget(end, condition))
        enterScope()
        node.body.forEach(::lowerStatement)
        leaveScope()
        loops.removeFirst()
        instructions += IrInstruction.Jump(condition)
        instructions += IrInstruction.Label(end)
    }

    private fun lowerFor(node: ForStmtNode) {
        val startSlot = allocateSlot()
        val endSlot = allocateSlot()
        val stepSlot = allocateSlot()
        compileExpressionAs(node.from, IntType)
        instructions += IrInstruction.StoreLocal(startSlot)
        compileExpressionAs(node.to, IntType)
        instructions += IrInstruction.StoreLocal(endSlot)
        if (node.step != null) compileExpressionAs(node.step, IntType)
        else instructions += IrInstruction.Push(HbcConstant.IntValue(1))
        instructions += IrInstruction.StoreLocal(stepSlot)

        enterScope()
        val variableSlot = declareLocal(node.variable, IntType)
        instructions += IrInstruction.LoadLocal(startSlot)
        instructions += IrInstruction.StoreLocal(variableSlot)

        val nonZero = label("for_non_zero")
        val condition = label("for_condition")
        val negative = label("for_negative")
        val conditionDone = label("for_condition_done")
        val continueLabel = label("for_continue")
        val end = label("for_end")

        // A dynamic step cannot be rejected statically. Treat zero as an empty loop
        // instead of generating an infinite loop.
        instructions += IrInstruction.LoadLocal(stepSlot)
        instructions += IrInstruction.Push(HbcConstant.IntValue(0))
        instructions += IrInstruction.Binary(BinaryOperation.EQUAL)
        instructions += IrInstruction.JumpIfFalse(nonZero)
        instructions += IrInstruction.Jump(end)
        instructions += IrInstruction.Label(nonZero)
        instructions += IrInstruction.Label(condition)
        instructions += IrInstruction.LoadLocal(stepSlot)
        instructions += IrInstruction.Push(HbcConstant.IntValue(0))
        instructions += IrInstruction.Binary(BinaryOperation.GREATER_OR_EQUAL)
        instructions += IrInstruction.JumpIfFalse(negative)
        instructions += IrInstruction.LoadLocal(variableSlot)
        instructions += IrInstruction.LoadLocal(endSlot)
        instructions += IrInstruction.Binary(BinaryOperation.LESS_OR_EQUAL)
        instructions += IrInstruction.Jump(conditionDone)
        instructions += IrInstruction.Label(negative)
        instructions += IrInstruction.LoadLocal(variableSlot)
        instructions += IrInstruction.LoadLocal(endSlot)
        instructions += IrInstruction.Binary(BinaryOperation.GREATER_OR_EQUAL)
        instructions += IrInstruction.Label(conditionDone)
        instructions += IrInstruction.JumpIfFalse(end)

        loops.addFirst(LoopTarget(end, continueLabel))
        node.body.forEach(::lowerStatement)
        loops.removeFirst()
        instructions += IrInstruction.Label(continueLabel)
        instructions += IrInstruction.LoadLocal(variableSlot)
        instructions += IrInstruction.LoadLocal(stepSlot)
        instructions += IrInstruction.Binary(BinaryOperation.ADD)
        instructions += IrInstruction.StoreLocal(variableSlot)
        instructions += IrInstruction.Jump(condition)
        instructions += IrInstruction.Label(end)
        leaveScope()
    }

    /** Returns true when an operand-stack value was produced. */
    private fun compileExpression(node: ExprNode): Boolean = when (node) {
        is IntLiteralNode -> push(HbcConstant.IntValue(node.value))
        is RealLiteralNode -> push(HbcConstant.RealValue(node.value))
        is StringLiteralNode -> push(HbcConstant.StringValue(node.value))
        is BoolLiteralNode -> push(HbcConstant.BoolValue(node.value))
        is DurationLiteralNode -> push(HbcConstant.TimeValue(durationMillis(node)))
        is VarExprNode -> {
            loadVariable(node.name)
            true
        }
        is UnaryExprNode -> {
            check(compileExpression(node.operand))
            when (node.op) {
                UnaryOp.PLUS -> Unit
                UnaryOp.MINUS -> instructions += IrInstruction.Unary(UnaryOperation.NEGATE)
                UnaryOp.NOT -> instructions += IrInstruction.Unary(UnaryOperation.NOT)
            }
            true
        }
        is BinaryExprNode -> compileBinary(node)
        is FieldAccessExprNode -> compileFieldAccess(node)
        is IndexAccessExprNode -> {
            check(compileExpression(node.target))
            compileExpressionAs(node.index, IntType)
            instructions += IrInstruction.LoadIndex
            true
        }
        is CallExprNode -> compileCall(node)
    }

    private fun compileExpressionAs(node: ExprNode, expected: HopeType) {
        check(compileExpression(node)) { "Expression at ${node.location} does not produce a value" }
        val actual = expressionType(node)
        if (expected == RealType && actual == IntType) instructions += IrInstruction.IntToReal
    }

    private fun compileBinary(node: BinaryExprNode): Boolean {
        if (node.op == BinaryOp.AND) {
            val falseLabel = label("and_false")
            val end = label("and_end")
            compileExpressionAs(node.left, BoolType)
            instructions += IrInstruction.JumpIfFalse(falseLabel)
            compileExpressionAs(node.right, BoolType)
            instructions += IrInstruction.Jump(end)
            instructions += IrInstruction.Label(falseLabel)
            instructions += IrInstruction.Push(HbcConstant.BoolValue(false))
            instructions += IrInstruction.Label(end)
            return true
        }
        if (node.op == BinaryOp.OR) {
            val right = label("or_right")
            val end = label("or_end")
            compileExpressionAs(node.left, BoolType)
            instructions += IrInstruction.JumpIfFalse(right)
            instructions += IrInstruction.Push(HbcConstant.BoolValue(true))
            instructions += IrInstruction.Jump(end)
            instructions += IrInstruction.Label(right)
            compileExpressionAs(node.right, BoolType)
            instructions += IrInstruction.Label(end)
            return true
        }

        val leftType = expressionType(node.left)
        val rightType = expressionType(node.right)
        val common = if (TypeRules.isNumeric(leftType) && TypeRules.isNumeric(rightType) &&
            (leftType == RealType || rightType == RealType)) RealType else null
        if (common != null) {
            compileExpressionAs(node.left, common)
            compileExpressionAs(node.right, common)
        } else {
            check(compileExpression(node.left))
            check(compileExpression(node.right))
        }
        instructions += IrInstruction.Binary(when (node.op) {
            BinaryOp.PLUS -> BinaryOperation.ADD
            BinaryOp.MINUS -> BinaryOperation.SUBTRACT
            BinaryOp.MUL -> BinaryOperation.MULTIPLY
            BinaryOp.DIV -> BinaryOperation.DIVIDE
            BinaryOp.MOD -> BinaryOperation.MODULO
            BinaryOp.EQ -> BinaryOperation.EQUAL
            BinaryOp.NEQ -> BinaryOperation.NOT_EQUAL
            BinaryOp.LT -> BinaryOperation.LESS
            BinaryOp.LTE -> BinaryOperation.LESS_OR_EQUAL
            BinaryOp.GT -> BinaryOperation.GREATER
            BinaryOp.GTE -> BinaryOperation.GREATER_OR_EQUAL
            BinaryOp.AND, BinaryOp.OR -> error("short-circuit handled above")
        })
        return true
    }

    private fun compileFieldAccess(node: FieldAccessExprNode): Boolean {
        val targetName = (node.target as? VarExprNode)?.name
        val enum = targetName?.let { semantic.symbols.findEnum(it) }
        if (enum != null) {
            val ordinal = enum.values.indexOf(node.fieldName)
            check(ordinal >= 0)
            instructions += IrInstruction.Push(HbcConstant.IntValue(ordinal.toLong()))
            return true
        }
        val targetType = expressionType(node.target) as StructType
        check(compileExpression(node.target))
        instructions += IrInstruction.LoadField(targetType.name, node.fieldName)
        return true
    }

    private fun compileCall(node: CallExprNode): Boolean {
        val function = node.function as VarExprNode
        when (function.name) {
            "size" -> {
                check(compileExpression(node.arguments.single()))
                instructions += IrInstruction.Length
                return true
            }
            "push" -> {
                val listType = expressionType(node.arguments[0]) as ListType
                check(compileExpression(node.arguments[0]))
                compileExpressionAs(node.arguments[1], listType.elementType)
                instructions += IrInstruction.ListAppend
                return false
            }
            "remove_at" -> {
                check(compileExpression(node.arguments[0]))
                compileExpressionAs(node.arguments[1], IntType)
                instructions += IrInstruction.ListRemove
                return true
            }
        }
        val symbol = semantic.symbols.findFunction(function.name)
            ?: error("Unknown function '${function.name}' after semantic analysis")
        node.arguments.forEachIndexed { index, argument ->
            compileExpressionAs(argument, symbol.parameters[index].type)
        }
        instructions += IrInstruction.Call(function.name, node.arguments.size)
        return true
    }

    private fun loadVariable(name: String) {
        val local = lookupLocal(name)
        if (local != null) instructions += IrInstruction.LoadLocal(local.slot)
        else instructions += IrInstruction.LoadGlobal(name)
    }

    private fun storeVariable(name: String) {
        val local = lookupLocal(name)
        if (local != null) instructions += IrInstruction.StoreLocal(local.slot)
        else instructions += IrInstruction.StoreGlobal(name)
    }

    private fun expressionType(node: ExprNode): HopeType = semantic.typeOf(node)
        ?: error("Missing expression type at ${node.location}")

    private fun enterScope() { scopes.addFirst(linkedMapOf()) }
    private fun leaveScope() { scopes.removeFirst() }

    private fun bindLocal(name: String, binding: LocalBinding) {
        scopes.peekFirst()[name] = binding
    }

    private fun declareLocal(name: String, type: HopeType): Int {
        val slot = allocateSlot()
        bindLocal(name, LocalBinding(slot, type))
        return slot
    }

    private fun allocateSlot(): Int = nextSlot++

    private fun lookupLocal(name: String): LocalBinding? {
        for (scope in scopes) scope[name]?.let { return it }
        return null
    }

    private fun label(prefix: String): IrLabel = IrLabel("${name.replace('/', '_')}_${prefix}_${nextLabel++}")

    private fun push(constant: HbcConstant): Boolean {
        instructions += IrInstruction.Push(constant)
        return true
    }
}

internal fun toHbcType(type: HopeType): HbcType = when (type) {
    IntType -> HbcType.IntType
    RealType -> HbcType.RealType
    BoolType -> HbcType.BoolType
    StringType -> HbcType.StringType
    TimeType -> HbcType.TimeType
    is StructType -> HbcType.Struct(type.name)
    is EnumType -> HbcType.IntType
    is ArrayType -> HbcType.Array(toHbcType(type.elementType), type.size)
    is ListType -> HbcType.ListType(toHbcType(type.elementType))
    VoidType -> error("void is not a runtime storage type")
    ErrorType -> error("semantic error type reached code generation")
}

private fun durationMillis(node: DurationLiteralNode): Long = HbcTime.milliseconds(node.value, when (node.unit) {
    TimeUnit.MS -> HbcTimeUnit.MS
    TimeUnit.SEC -> HbcTimeUnit.SEC
    TimeUnit.MIN -> HbcTimeUnit.MIN
    TimeUnit.HOUR -> HbcTimeUnit.HOUR
    TimeUnit.DAY -> HbcTimeUnit.DAY
})
