package lv426.compiler.semantic

import java.math.BigDecimal
import java.util.IdentityHashMap
import lv426.compiler.ast.*

class SemanticAnalyzer {
    private lateinit var program: SourceNode
    private lateinit var symbols: SymbolTable
    private lateinit var diagnostics: DiagnosticBuilder
    private lateinit var context: SemanticContext
    private lateinit var resolver: TypeResolver
    private val expressionTypes = IdentityHashMap<ExprNode, HopeType>()
    private val constDecls = linkedMapOf<String, ConstDeclNode>()
    private val constIntCache = linkedMapOf<String, Long>()
    private val constEvalStack = linkedSetOf<String>()

    fun analyze(program: SourceNode): SemanticResult {
        this.program = program
        symbols = SymbolTable()
        diagnostics = DiagnosticBuilder()
        context = SemanticContext(symbols, diagnostics)
        expressionTypes.clear()
        constDecls.clear()
        constIntCache.clear()
        constEvalStack.clear()

        Builtins.install(symbols)
        program.declarations.filterIsInstance<ConstDeclNode>().forEach { constDecls[it.name] = it }

        collectTypeNames()
        resolver = TypeResolver(symbols, diagnostics, ::evaluateIntConstant)
        collectStructFields()
        collectValueDeclarations()
        checkTopLevelDeclarations()

        return SemanticResult(
            symbol = symbols,
            diagnostics = diagnostics.all(),
            program = program,
            expressionTypes = expressionTypes,
            resolvedTypes = resolver.resolvedTypes(),
            constantIntValues = constIntCache.toMap()
        )
    }

    private fun collectTypeNames() {
        program.declarations.forEach { decl ->
            when (decl) {
                is StructDeclNode -> declareGlobal(StructSymbol(decl.name), decl.location)
                is EnumDeclNode -> {
                    val duplicate = decl.members.groupingBy { it }.eachCount().entries.firstOrNull { it.value > 1 }
                    if (duplicate != null) diagnostics.error("Duplicate enum member '${duplicate.key}' in '${decl.name}'", decl.location)
                    declareGlobal(EnumSymbol(decl.name, decl.members.toList()), decl.location)
                }
                else -> Unit
            }
        }
    }

    private fun collectStructFields() {
        program.declarations.filterIsInstance<StructDeclNode>().forEach { decl ->
            val symbol = symbols.findStruct(decl.name) ?: return@forEach
            for (field in decl.fields) {
                if (symbol.fields.containsKey(field.name)) {
                    diagnostics.error("Duplicate field '${field.name}' in struct '${decl.name}'", field.location)
                    continue
                }
                val type = resolver.resolve(field.type)
                if (type == VoidType) diagnostics.error("Struct field '${field.name}' cannot have type void", field.type.location)
                if (type != ErrorType && type != VoidType) symbol.fields[field.name] = StructField(field.name, type)
            }
        }
    }

    private fun collectValueDeclarations() {
        program.declarations.forEach { decl ->
            when (decl) {
                is ConstDeclNode -> {
                    val type = resolver.resolve(decl.type)
                    if (type == VoidType) diagnostics.error("Constant '${decl.name}' cannot have type void", decl.type.location)
                    declareGlobal(VariableSymbol(decl.name, type, mutable = false), decl.location)
                }
                is GlobalVarDeclNode -> {
                    val type = resolver.resolve(decl.type)
                    if (type == VoidType) diagnostics.error("Variable '${decl.name}' cannot have type void", decl.type.location)
                    declareGlobal(VariableSymbol(decl.name, type), decl.location)
                }
                is FunctionDeclNode -> {
                    val parameters = resolveParameters(decl.parameters, "function '${decl.name}'")
                    val returnType = resolver.resolve(decl.returnType)
                    declareGlobal(FunctionSymbol(decl.name, parameters, returnType), decl.location)
                }
                is EventDeclNode -> {
                    val parameters = resolveEventParameters(decl.parameters, decl.name)
                    declareGlobal(EventSymbol(decl.name, parameters), decl.location)
                }
                else -> Unit
            }
        }
    }

    private fun resolveParameters(nodes: List<ParamNode>, owner: String): List<FunctionParameter> {
        val seen = mutableSetOf<String>()
        return nodes.map { node ->
            if (!seen.add(node.name)) diagnostics.error("Duplicate parameter '${node.name}' in $owner", node.location)
            val type = resolver.resolve(node.type)
            if (type == VoidType) diagnostics.error("Parameter '${node.name}' cannot have type void", node.type.location)
            FunctionParameter(node.name, type)
        }
    }

    private fun resolveEventParameters(nodes: List<ParamNode>, event: String): List<EventParameter> {
        val seen = mutableSetOf<String>()
        return nodes.map { node ->
            if (!seen.add(node.name)) diagnostics.error("Duplicate parameter '${node.name}' in event '$event'", node.location)
            val type = resolver.resolve(node.type)
            if (type == VoidType) diagnostics.error("Event parameter '${node.name}' cannot have type void", node.type.location)
            EventParameter(node.name, type)
        }
    }

    private fun checkTopLevelDeclarations() {
        program.declarations.forEach { decl ->
            when (decl) {
                is ConstDeclNode -> checkInitializer(decl.name, resolver.resolve(decl.type), decl.value)
                is GlobalVarDeclNode -> decl.initialValue?.let { checkInitializer(decl.name, resolver.resolve(decl.type), it) }
                is StructDeclNode -> decl.fields.forEach { field ->
                    field.initialValue?.let { checkInitializer("${decl.name}.${field.name}", resolver.resolve(field.type), it) }
                }
                is FunctionDeclNode -> checkFunction(decl)
                is StartHandlerNode -> checkHandler(emptyList(), decl.body)
                is EventHandlerNode -> checkEventHandler(decl)
                is EveryHandlerNode -> {
                    checkExpression(decl.duration)
                    if (decl.duration.value <= 0.0) diagnostics.error("Every interval must be greater than zero", decl.duration.location)
                    checkHandler(emptyList(), decl.body)
                }
                is AtHandlerNode -> {
                    checkExpression(decl.duration)
                    if (decl.duration.value < 0.0) diagnostics.error("At time cannot be negative", decl.duration.location)
                    checkHandler(emptyList(), decl.body)
                }
                else -> Unit
            }
        }
    }

    private fun checkInitializer(name: String, target: HopeType, expression: ExprNode) {
        val source = checkExpression(expression)
        if (!TypeRules.isAssignable(target, source)) {
            diagnostics.error("Cannot initialize '$name' of type ${target.displayName} with ${source.displayName}", expression.location)
        }
    }

    private fun checkFunction(node: FunctionDeclNode) {
        val function = symbols.findFunction(node.name) ?: return
        context.enterScope()
        val previous = context.currentFunction
        context.currentFunction = function
        node.parameters.zip(function.parameters).forEach { (ast, parameter) ->
            if (!context.currentScope.declare(VariableSymbol(parameter.name, parameter.type))) {
                diagnostics.error("Duplicate parameter '${parameter.name}'", ast.location)
            }
        }
        checkStatements(node.body)
        if (function.returnType != VoidType && function.returnType != ErrorType && !blockAlwaysReturns(node.body)) {
            diagnostics.error("Function '${node.name}' may finish without returning ${function.returnType.displayName}", node.location)
        }
        context.currentFunction = previous
        context.leaveScope()
    }

    private fun checkHandler(parameters: List<Pair<String, HopeType>>, body: List<StmtNode>) {
        context.enterScope()
        val previous = context.currentFunction
        context.currentFunction = FunctionSymbol("@handler", parameters.map { FunctionParameter(it.first, it.second) }, VoidType)
        parameters.forEach { (name, type) ->
            if (!context.currentScope.declare(VariableSymbol(name, type))) {
                diagnostics.error("Duplicate handler parameter '$name'")
            }
        }
        checkStatements(body)
        context.currentFunction = previous
        context.leaveScope()
    }

    private fun checkEventHandler(node: EventHandlerNode) {
        val event = symbols.findEvent(node.eventName)
        if (event == null) {
            diagnostics.error("Unknown event '${node.eventName}'", node.location)
            checkHandler(emptyList(), node.body)
            return
        }
        if (node.parameters.size != event.parameters.size) {
            diagnostics.error("Handler for '${node.eventName}' expects ${event.parameters.size} parameters, got ${node.parameters.size}", node.location)
        }
        val params = node.parameters.mapIndexed { index, name ->
            name to (event.parameters.getOrNull(index)?.type ?: ErrorType)
        }
        if (node.parameters.toSet().size != node.parameters.size) {
            diagnostics.error("Duplicate parameter in handler '${node.eventName}'", node.location)
        }
        checkHandler(params, node.body)
    }

    private fun checkStatements(statements: List<StmtNode>) {
        statements.forEach(::checkStatement)
    }

    private fun checkStatement(statement: StmtNode) {
        when (statement) {
            is LocalVarDeclNode -> {
                val type = resolver.resolve(statement.type)
                if (type == VoidType) diagnostics.error("Local variable '${statement.name}' cannot have type void", statement.type.location)
                statement.initialValue?.let {
                    val actual = checkExpression(it)
                    if (!TypeRules.isAssignable(type, actual)) {
                        diagnostics.error("Cannot assign ${actual.displayName} to ${type.displayName}", it.location)
                    }
                }
                if (!context.currentScope.declare(VariableSymbol(statement.name, type))) {
                    diagnostics.error("Duplicate local variable '${statement.name}'", statement.location)
                }
            }
            is AssignmentStmtNode -> checkAssignment(statement)
            is IfStmtNode -> {
                statement.branches.forEach { branch ->
                    requireBool(checkExpression(branch.condition), branch.condition)
                    context.enterScope()
                    checkStatements(branch.body)
                    context.leaveScope()
                }
                statement.elseBody?.let {
                    context.enterScope()
                    checkStatements(it)
                    context.leaveScope()
                }
            }
            is WhileStmtNode -> {
                requireBool(checkExpression(statement.condition), statement.condition)
                context.enterScope()
                context.enterLoop()
                checkStatements(statement.body)
                context.leaveLoop()
                context.leaveScope()
            }
            is ForStmtNode -> {
                requireInt(checkExpression(statement.from), statement.from, "For-loop lower bound")
                requireInt(checkExpression(statement.to), statement.to, "For-loop upper bound")
                statement.step?.let {
                    requireInt(checkExpression(it), it, "For-loop step")
                    if (evaluateIntExpression(it) == 0L) diagnostics.error("For-loop step cannot be zero", it.location)
                }
                context.enterScope()
                context.currentScope.declare(VariableSymbol(statement.variable, IntType))
                context.enterLoop()
                checkStatements(statement.body)
                context.leaveLoop()
                context.leaveScope()
            }
            is BreakStmtNode -> if (!context.isInsideLoop()) diagnostics.error("break is only allowed inside a loop", statement.location)
            is ContinueStmtNode -> if (!context.isInsideLoop()) diagnostics.error("continue is only allowed inside a loop", statement.location)
            is ReturnStmtNode -> checkReturn(statement)
            is EmitStmtNode -> checkEmit(statement)
            is ExprStmtNode -> checkExpression(statement.expr)
        }
    }

    private fun checkAssignment(node: AssignmentStmtNode) {
        val target = checkExpression(node.target)
        if (node.target is VarExprNode) {
            val symbol = context.currentScope.resolve(node.target.name)
            if (symbol is VariableSymbol && !symbol.mutable) {
                diagnostics.error("Cannot assign to constant '${node.target.name}'", node.target.location)
            }
        }
        if (node.target is FieldAccessExprNode && node.target.target is VarExprNode &&
            context.currentScope.resolve(node.target.target.name) is EnumSymbol) {
            diagnostics.error("Cannot assign to enum member '${node.target.fieldName}'", node.target.location)
        }
        val value = checkExpression(node.value)
        if (node.op == AssignOp.ASSIGN) {
            if (!TypeRules.isAssignable(target, value)) {
                diagnostics.error("Cannot assign ${value.displayName} to ${target.displayName}", node.value.location)
            }
            return
        }
        val result = if (node.op == AssignOp.MOD_ASSIGN) {
            if (target == IntType && value == IntType) IntType else null
        } else TypeRules.arithmeticResult(target, value)
        if (result == null) {
            diagnostics.error("Operator '${node.op}' requires numeric operands", node.location)
        } else if (!TypeRules.isAssignable(target, result)) {
            diagnostics.error("Result ${result.displayName} cannot be assigned to ${target.displayName}", node.location)
        }
    }

    private fun checkReturn(node: ReturnStmtNode) {
        val function = context.currentFunction
        if (function == null) {
            diagnostics.error("return is not allowed here", node.location)
            node.value?.let(::checkExpression)
            return
        }
        if (function.returnType == VoidType) {
            if (node.value != null) {
                checkExpression(node.value)
                diagnostics.error("Void function cannot return a value", node.location)
            }
        } else {
            val value = node.value
            if (value == null) {
                diagnostics.error("Function must return ${function.returnType.displayName}", node.location)
            } else {
                val actual = checkExpression(value)
                if (!TypeRules.isAssignable(function.returnType, actual)) {
                    diagnostics.error("Cannot return ${actual.displayName}; expected ${function.returnType.displayName}", value.location)
                }
            }
        }
    }

    private fun checkEmit(node: EmitStmtNode) {
        val event = symbols.findEvent(node.eventName)
        if (event == null) {
            diagnostics.error("Unknown event '${node.eventName}'", node.location)
            node.arguments.forEach(::checkExpression)
            return
        }
        if (node.arguments.size != event.parameters.size) {
            diagnostics.error("Event '${node.eventName}' expects ${event.parameters.size} arguments, got ${node.arguments.size}", node.location)
        }
        node.arguments.forEachIndexed { index, argument ->
            val actual = checkExpression(argument)
            val expected = event.parameters.getOrNull(index)?.type ?: return@forEachIndexed
            if (!TypeRules.isAssignable(expected, actual)) {
                diagnostics.error("Event argument ${index + 1} expects ${expected.displayName}, got ${actual.displayName}", argument.location)
            }
        }
    }

    private fun checkExpression(expression: ExprNode): HopeType {
        expressionTypes[expression]?.let { return it }
        val type = when (expression) {
            is IntLiteralNode -> IntType
            is RealLiteralNode -> if (expression.value.isFinite()) RealType else {
                diagnostics.error("Real literal must be finite", expression.location)
                ErrorType
            }
            is StringLiteralNode -> StringType
            is BoolLiteralNode -> BoolType
            is DurationLiteralNode -> checkDurationLiteral(expression)
            is VarExprNode -> checkVariable(expression)
            is UnaryExprNode -> checkUnary(expression)
            is BinaryExprNode -> checkBinary(expression)
            is FieldAccessExprNode -> checkFieldAccess(expression)
            is IndexAccessExprNode -> checkIndexAccess(expression)
            is CallExprNode -> checkCall(expression)
        }
        expressionTypes[expression] = type
        return type
    }

    private fun checkDurationLiteral(node: DurationLiteralNode): HopeType {
        if (!node.value.isFinite()) {
            diagnostics.error("Duration must be finite", node.location)
            return ErrorType
        }
        val multiplier = when (node.unit) {
            TimeUnit.MS -> 1L
            TimeUnit.SEC -> 1_000L
            TimeUnit.MIN -> 60_000L
            TimeUnit.HOUR -> 3_600_000L
            TimeUnit.DAY -> 86_400_000L
        }
        try {
            BigDecimal.valueOf(node.value)
                .multiply(BigDecimal.valueOf(multiplier))
                .longValueExact()
        } catch (_: ArithmeticException) {
            diagnostics.error("Duration must resolve to an exact whole number of milliseconds", node.location)
            return ErrorType
        }
        return TimeType
    }

    private fun checkVariable(node: VarExprNode): HopeType {
        return when (val symbol = context.currentScope.resolve(node.name)) {
            is VariableSymbol -> symbol.type
            null -> {
                diagnostics.error("Unknown name '${node.name}'", node.location)
                ErrorType
            }
            else -> {
                diagnostics.error("'${node.name}' is not a value", node.location)
                ErrorType
            }
        }
    }

    private fun checkUnary(node: UnaryExprNode): HopeType {
        val operand = checkExpression(node.operand)
        if (operand == ErrorType) return ErrorType
        return when (node.op) {
            UnaryOp.NOT -> if (operand == BoolType) BoolType else {
                diagnostics.error("Operator 'not' requires bool, got ${operand.displayName}", node.location)
                ErrorType
            }
            UnaryOp.MINUS, UnaryOp.PLUS -> if (TypeRules.isNumeric(operand)) operand else {
                diagnostics.error("Unary '${node.op}' requires a numeric operand", node.location)
                ErrorType
            }
        }
    }

    private fun checkBinary(node: BinaryExprNode): HopeType {
        val left = checkExpression(node.left)
        val right = checkExpression(node.right)
        if (left == ErrorType || right == ErrorType) return ErrorType
        return when (node.op) {
            BinaryOp.PLUS, BinaryOp.MINUS, BinaryOp.MUL, BinaryOp.DIV ->
                TypeRules.arithmeticResult(left, right) ?: invalidBinary(node, left, right)
            BinaryOp.MOD -> if (left == IntType && right == IntType) IntType else invalidBinary(node, left, right)
            BinaryOp.EQ, BinaryOp.NEQ -> if (TypeRules.equalityAllowed(left, right)) BoolType else invalidBinary(node, left, right)
            BinaryOp.LT, BinaryOp.LTE, BinaryOp.GT, BinaryOp.GTE ->
                if (TypeRules.orderedComparisonAllowed(left, right)) BoolType else invalidBinary(node, left, right)
            BinaryOp.AND, BinaryOp.OR -> if (left == BoolType && right == BoolType) BoolType else invalidBinary(node, left, right)
        }
    }

    private fun invalidBinary(node: BinaryExprNode, left: HopeType, right: HopeType): HopeType {
        diagnostics.error("Operator '${node.op}' cannot be applied to ${left.displayName} and ${right.displayName}", node.location)
        return ErrorType
    }

    private fun checkFieldAccess(node: FieldAccessExprNode): HopeType {
        if (node.target is VarExprNode) {
            val enum = context.currentScope.resolve(node.target.name) as? EnumSymbol
            if (enum != null) {
                if (node.fieldName !in enum.values) {
                    diagnostics.error("Enum '${enum.name}' has no member '${node.fieldName}'", node.location)
                    return ErrorType
                }
                return EnumType(enum.name)
            }
        }
        return when (val target = checkExpression(node.target)) {
            is StructType -> {
                val field = symbols.findStruct(target.name)?.findField(node.fieldName)
                if (field == null) {
                    diagnostics.error("Struct '${target.name}' has no field '${node.fieldName}'", node.location)
                    ErrorType
                } else field.type
            }
            ErrorType -> ErrorType
            else -> {
                diagnostics.error("Type ${target.displayName} has no fields", node.location)
                ErrorType
            }
        }
    }

    private fun checkIndexAccess(node: IndexAccessExprNode): HopeType {
        val target = checkExpression(node.target)
        requireInt(checkExpression(node.index), node.index, "Index")
        return when (target) {
            is ArrayType -> target.elementType
            is ListType -> target.elementType
            ErrorType -> ErrorType
            else -> {
                diagnostics.error("Type ${target.displayName} is not indexable", node.location)
                ErrorType
            }
        }
    }

    private fun checkCall(node: CallExprNode): HopeType {
        val functionNode = node.function as? VarExprNode
        if (functionNode == null) {
            diagnostics.error("Only named functions can be called", node.function.location)
            node.arguments.forEach(::checkExpression)
            return ErrorType
        }
        val name = functionNode.name
        when (name) {
            "size" -> {
                if (node.arguments.size != 1) return callArityError(node, name, 1)
                return when (val type = checkExpression(node.arguments[0])) {
                    is ArrayType, is ListType -> IntType
                    ErrorType -> ErrorType
                    else -> {
                        diagnostics.error("size() expects an array or list, got ${type.displayName}", node.arguments[0].location)
                        ErrorType
                    }
                }
            }
            "push" -> {
                if (node.arguments.size != 2) return callArityError(node, name, 2)
                val list = checkExpression(node.arguments[0])
                val value = checkExpression(node.arguments[1])
                if (list is ListType) {
                    if (!TypeRules.isAssignable(list.elementType, value)) {
                        diagnostics.error("push() expects ${list.elementType.displayName}, got ${value.displayName}", node.arguments[1].location)
                    }
                } else if (list != ErrorType) {
                    diagnostics.error("push() expects list<T> as first argument", node.arguments[0].location)
                }
                return VoidType
            }
            "remove_at" -> {
                if (node.arguments.size != 2) return callArityError(node, name, 2)
                val list = checkExpression(node.arguments[0])
                requireInt(checkExpression(node.arguments[1]), node.arguments[1], "remove_at index")
                return if (list is ListType) list.elementType else {
                    if (list != ErrorType) diagnostics.error("remove_at() expects list<T> as first argument", node.arguments[0].location)
                    ErrorType
                }
            }
        }

        val symbol = context.currentScope.resolve(name)
        if (symbol !is FunctionSymbol) {
            if (symbol == null) diagnostics.error("Unknown function '$name'", functionNode.location)
            else diagnostics.error("'$name' is not a function", functionNode.location)
            node.arguments.forEach(::checkExpression)
            return ErrorType
        }
        if (node.arguments.size != symbol.parameters.size) {
            diagnostics.error("Function '$name' expects ${symbol.parameters.size} arguments, got ${node.arguments.size}", node.location)
        }
        node.arguments.forEachIndexed { index, argument ->
            val actual = checkExpression(argument)
            val expected = symbol.parameters.getOrNull(index)?.type ?: return@forEachIndexed
            if (!TypeRules.isAssignable(expected, actual)) {
                diagnostics.error("Argument ${index + 1} of '$name' expects ${expected.displayName}, got ${actual.displayName}", argument.location)
            }
        }
        return symbol.returnType
    }

    private fun callArityError(node: CallExprNode, name: String, expected: Int): HopeType {
        diagnostics.error("$name() expects $expected arguments, got ${node.arguments.size}", node.location)
        node.arguments.forEach(::checkExpression)
        return ErrorType
    }

    private fun requireBool(type: HopeType, node: AstNode) {
        if (type != BoolType && type != ErrorType) diagnostics.error("Condition must be bool, got ${type.displayName}", node.location)
    }

    private fun requireInt(type: HopeType, node: AstNode, what: String) {
        if (type != IntType && type != ErrorType) diagnostics.error("$what must be int, got ${type.displayName}", node.location)
    }

    private fun declareGlobal(symbol: Symbol, location: SourceLocation) {
        if (!symbols.declare(symbol)) diagnostics.error("Duplicate global declaration '${symbol.name}'", location)
    }

    private fun evaluateIntConstant(name: String): Long? {
        constIntCache[name]?.let { return it }
        val declaration = constDecls[name] ?: return null
        val declaredType = declaration.type as? PrimitiveTypeNode
        if (declaredType?.type != PrimitiveType.INT) return null
        if (!constEvalStack.add(name)) {
            diagnostics.error("Cyclic integer constant '$name'", declaration.location)
            return null
        }
        val value = evaluateIntExpression(declaration.value)
        constEvalStack.remove(name)
        if (value != null) constIntCache[name] = value
        return value
    }

    private fun evaluateIntExpression(expression: ExprNode): Long? {
        return when (expression) {
            is IntLiteralNode -> expression.value
            is VarExprNode -> evaluateIntConstant(expression.name)
            is UnaryExprNode -> when (expression.op) {
                UnaryOp.PLUS -> evaluateIntExpression(expression.operand)
                UnaryOp.MINUS -> evaluateIntExpression(expression.operand)?.let { runCatching { Math.negateExact(it) }.getOrNull() }
                UnaryOp.NOT -> null
            }
            is BinaryExprNode -> {
                val left = evaluateIntExpression(expression.left) ?: return null
                val right = evaluateIntExpression(expression.right) ?: return null
                runCatching {
                    when (expression.op) {
                        BinaryOp.PLUS -> Math.addExact(left, right)
                        BinaryOp.MINUS -> Math.subtractExact(left, right)
                        BinaryOp.MUL -> Math.multiplyExact(left, right)
                        BinaryOp.DIV -> if (right == 0L) throw ArithmeticException() else left / right
                        BinaryOp.MOD -> if (right == 0L) throw ArithmeticException() else left % right
                        else -> throw ArithmeticException()
                    }
                }.getOrNull()
            }
            else -> null
        }
    }

    private fun blockAlwaysReturns(statements: List<StmtNode>): Boolean {
        for (statement in statements) if (statementAlwaysReturns(statement)) return true
        return false
    }

    private fun statementAlwaysReturns(statement: StmtNode): Boolean = when (statement) {
        is ReturnStmtNode -> true
        is IfStmtNode -> statement.elseBody != null &&
            statement.branches.all { blockAlwaysReturns(it.body) } &&
            blockAlwaysReturns(statement.elseBody)
        else -> false
    }
}
