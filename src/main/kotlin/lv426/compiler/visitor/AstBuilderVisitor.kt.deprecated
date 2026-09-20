package lv426.compiler.visitor

import lv426.compiler.CompilerPipeline
import lv426.compiler.ast.*
import lv426.compiler.parser.SmartHomeDSLBaseVisitor
import lv426.compiler.parser.SmartHomeDSLParser

/**
 * Visitor, преобразующий конкретное дерево разбора (CST) от ANTLR4
 * в строго типизированное абстрактное синтаксическое дерево (AST).
 */
class AstBuilderVisitor : SmartHomeDSLBaseVisitor<AstNode>() {

    override fun visitProgram(ctx: SmartHomeDSLParser.ProgramContext): ProgramNode {
        val declarations = ctx.declaration().map { visit(it) as DeclarationNode }
        val handlers = ctx.handler().map { visit(it) as HandlerNode }
        return ProgramNode(declarations, handlers)
    }

    // --- Declarations ---

    override fun visitSensorDecl(ctx: SmartHomeDSLParser.SensorDeclContext): SensorDeclNode {
        val name = ctx.name.text
        val type = visit(ctx.type()) as TypeNode
        return SensorDeclNode(name, type)
    }

    override fun visitActuatorDecl(ctx: SmartHomeDSLParser.ActuatorDeclContext): ActuatorDeclNode {
        val name = ctx.name.text
        val type = visit(ctx.type()) as TypeNode
        return ActuatorDeclNode(name, type)
    }

    override fun visitVarDecl(ctx: SmartHomeDSLParser.VarDeclContext): VarDeclNode {
        val name = ctx.name.text
        val type = visit(ctx.type()) as TypeNode
        val initExpr = visit(ctx.expr()) as ExprNode
        return VarDeclNode(name, type, initExpr)
    }

    override fun visitConstDecl(ctx: SmartHomeDSLParser.ConstDeclContext): ConstDeclNode {
        val name = ctx.name.text
        val type = visit(ctx.type()) as TypeNode
        val initExpr = visit(ctx.expr()) as ExprNode
        return ConstDeclNode(name, type, initExpr)
    }

    // --- Types ---

    override fun visitIntType(ctx: SmartHomeDSLParser.IntTypeContext): TypeNode =
        TypeNode(DataType.INT)

    override fun visitFloatType(ctx: SmartHomeDSLParser.FloatTypeContext): TypeNode =
        TypeNode(DataType.FLOAT)

    override fun visitBoolType(ctx: SmartHomeDSLParser.BoolTypeContext): TypeNode =
        TypeNode(DataType.BOOL)

    override fun visitStringType(ctx: SmartHomeDSLParser.StringTypeContext): TypeNode =
        TypeNode(DataType.STRING)

    // --- Handlers ---

    override fun visitOnInitHandler(ctx: SmartHomeDSLParser.OnInitHandlerContext): OnInitHandlerNode =
        OnInitHandlerNode(visitBlock(ctx.block()))

    override fun visitOnTickHandler(ctx: SmartHomeDSLParser.OnTickHandlerContext): OnTickHandlerNode =
        OnTickHandlerNode(visitBlock(ctx.block()))

    override fun visitOnChangeHandler(ctx: SmartHomeDSLParser.OnChangeHandlerContext): OnChangeHandlerNode =
        OnChangeHandlerNode(ctx.name.text, visitBlock(ctx.block()))

    override fun visitOnTimeHandler(ctx: SmartHomeDSLParser.OnTimeHandlerContext): OnTimeHandlerNode =
        OnTimeHandlerNode(ctx.time.text, visitBlock(ctx.block()))

    // --- Block & Statements ---

    override fun visitBlock(ctx: SmartHomeDSLParser.BlockContext): BlockNode {
        val statements = ctx.statement().map { visitStatement(it) }
        return BlockNode(statements)
    }

    override fun visitStatement(ctx: SmartHomeDSLParser.StatementContext): StmtNode {
        return when {
            ctx.assignStmt() != null -> visitAssignStmt(ctx.assignStmt())
            ctx.ifStmt() != null -> visitIfStmt(ctx.ifStmt())
            ctx.whileStmt() != null -> visitWhileStmt(ctx.whileStmt())
            ctx.callStmt() != null -> visitCallStmt(ctx.callStmt())
            else -> error("Unknown statement encountered at line ${ctx.start.line}")
        }
    }

    override fun visitAssignStmt(ctx: SmartHomeDSLParser.AssignStmtContext): AssignStmtNode {
        val target = ctx.name.text
        val value = visit(ctx.expr()) as ExprNode
        return AssignStmtNode(target, value)
    }

    override fun visitIfStmt(ctx: SmartHomeDSLParser.IfStmtContext): IfStmtNode {
        val condition = visit(ctx.condition) as ExprNode
        val thenBlock = visitBlock(ctx.thenBlock)
        val elseBlock = when {
            ctx.elseBlock != null -> visitBlock(ctx.elseBlock)
            ctx.elseIf != null -> BlockNode(listOf(visitIfStmt(ctx.elseIf)))
            else -> null
        }
        return IfStmtNode(condition, thenBlock, elseBlock)
    }

    override fun visitWhileStmt(ctx: SmartHomeDSLParser.WhileStmtContext): WhileStmtNode {
        val condition = visit(ctx.condition) as ExprNode
        val body = visitBlock(ctx.block())
        return WhileStmtNode(condition, body)
    }

    override fun visitCallStmt(ctx: SmartHomeDSLParser.CallStmtContext): CallStmtNode {
        val call = visitCall(ctx.call())
        return CallStmtNode(call)
    }

    override fun visitCall(ctx: SmartHomeDSLParser.CallContext): CallExprNode {
        val functionName = ctx.name.text
        val arguments = ctx.expr().map { visit(it) as ExprNode }
        return CallExprNode(functionName, arguments)
    }

    // --- Expressions ---

    override fun visitUnaryMinusExpr(ctx: SmartHomeDSLParser.UnaryMinusExprContext): UnaryExprNode {
        val operand = visit(ctx.expr()) as ExprNode
        return UnaryExprNode(UnaryOp.NEG, operand)
    }

    override fun visitNotExpr(ctx: SmartHomeDSLParser.NotExprContext): UnaryExprNode {
        val operand = visit(ctx.expr()) as ExprNode
        return UnaryExprNode(UnaryOp.NOT, operand)
    }

    override fun visitMulDivExpr(ctx: SmartHomeDSLParser.MulDivExprContext): BinaryExprNode {
        val left = visit(ctx.left) as ExprNode
        val right = visit(ctx.right) as ExprNode
        val op = when (ctx.op.text) {
            "*" -> BinaryOp.MUL
            "/" -> BinaryOp.DIV
            "%" -> BinaryOp.MOD
            else -> error("Unexpected operator: ${ctx.op.text}")
        }
        return BinaryExprNode(left, op, right)
    }

    override fun visitAddSubExpr(ctx: SmartHomeDSLParser.AddSubExprContext): BinaryExprNode {
        val left = visit(ctx.left) as ExprNode
        val right = visit(ctx.right) as ExprNode
        val op = when (ctx.op.text) {
            "+" -> BinaryOp.ADD
            "-" -> BinaryOp.SUB
            else -> error("Unexpected operator: ${ctx.op.text}")
        }
        return BinaryExprNode(left, op, right)
    }

    override fun visitComparisonExpr(ctx: SmartHomeDSLParser.ComparisonExprContext): BinaryExprNode {
        val left = visit(ctx.left) as ExprNode
        val right = visit(ctx.right) as ExprNode

        // Проверяем запрет чейнинга сравнений: a < b < c
        if (left is BinaryExprNode && left.op in comparisonOps ||
            right is BinaryExprNode && right.op in comparisonOps) {
            throw CompilerPipeline.CompilerSyntaxException(
                "Syntax error at line ${ctx.start.line}: comparison operators are non-associative (chaining like 'a < b < c' is forbidden)"
            )
        }

        val op = when (ctx.op.text) {
            "==" -> BinaryOp.EQ
            "!=" -> BinaryOp.NEQ
            "<"  -> BinaryOp.LT
            "<=" -> BinaryOp.LE
            ">"  -> BinaryOp.GT
            ">=" -> BinaryOp.GE
            else -> error("Unexpected operator: ${ctx.op.text}")
        }
        return BinaryExprNode(left, op, right)
    }

    private val comparisonOps = setOf(
        BinaryOp.EQ, BinaryOp.NEQ, BinaryOp.LT, BinaryOp.LE, BinaryOp.GT, BinaryOp.GE
    )

    override fun visitAndExpr(ctx: SmartHomeDSLParser.AndExprContext): BinaryExprNode {
        val left = visit(ctx.left) as ExprNode
        val right = visit(ctx.right) as ExprNode
        return BinaryExprNode(left, BinaryOp.AND, right)
    }

    override fun visitOrExpr(ctx: SmartHomeDSLParser.OrExprContext): BinaryExprNode {
        val left = visit(ctx.left) as ExprNode
        val right = visit(ctx.right) as ExprNode
        return BinaryExprNode(left, BinaryOp.OR, right)
    }

    override fun visitCallExpr(ctx: SmartHomeDSLParser.CallExprContext): CallExprNode =
        visitCall(ctx.call())

    override fun visitVarExpr(ctx: SmartHomeDSLParser.VarExprContext): VarExprNode =
        VarExprNode(ctx.name.text)

    override fun visitIntLiteralExpr(ctx: SmartHomeDSLParser.IntLiteralExprContext): IntLiteralNode =
        IntLiteralNode(ctx.INT_LITERAL().text.toLong())

    override fun visitFloatLiteralExpr(ctx: SmartHomeDSLParser.FloatLiteralExprContext): FloatLiteralNode =
        FloatLiteralNode(ctx.FLOAT_LITERAL().text.toDouble())

    override fun visitStringLiteralExpr(ctx: SmartHomeDSLParser.StringLiteralExprContext): StringLiteralNode {
        val raw = ctx.STRING_LITERAL().text
        val unquoted = unescape(raw.substring(1, raw.length - 1))
        return StringLiteralNode(unquoted)
    }

    override fun visitBoolLiteralExpr(ctx: SmartHomeDSLParser.BoolLiteralExprContext): BoolLiteralNode {
        val value = when (ctx.boolLiteral().text) {
            "true", "on"  -> true
            "false", "off" -> false
            else -> error("Unknown boolean literal: ${ctx.boolLiteral().text}")
        }
        return BoolLiteralNode(value)
    }

    override fun visitParenExpr(ctx: SmartHomeDSLParser.ParenExprContext): ExprNode =
        visit(ctx.expr()) as ExprNode

    private fun unescape(str: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < str.length) {
            val ch = str[i]
            if (ch == '\\' && i + 1 < str.length) {
                when (str[i + 1]) {
                    '"'  -> { sb.append('"'); i += 2 }
                    'n'  -> { sb.append('\n'); i += 2 }
                    '\\' -> { sb.append('\\'); i += 2 }
                    't'  -> { sb.append('\t'); i += 2 }
                    'r'  -> { sb.append('\r'); i += 2 }
                    else -> { sb.append(ch); i++ }
                }
            } else {
                sb.append(ch)
                i++
            }
        }
        return sb.toString()
    }
}