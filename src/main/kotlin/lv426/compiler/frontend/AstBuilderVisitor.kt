package lv426.compiler.frontend

import HopeLangBaseVisitor
import HopeLangParser
import lv426.compiler.ast.*
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

class AstBuilderVisitor : HopeLangBaseVisitor<AstNode>() {

    private fun ParserRuleContext.loc(): SourceLocation =
        SourceLocation(start.line, start.charPositionInLine + 1)

    private fun TerminalNode.loc(): SourceLocation =
        SourceLocation(symbol.line, symbol.charPositionInLine + 1)

    override fun visitSource(ctx: HopeLangParser.SourceContext): SourceNode {
        val programName = ctx.programDecl()?.ID()?.text
        val decls = ctx.topLevelDecl().map { visit(it) as TopLevelDeclNode }
        return SourceNode(programName, decls, ctx.loc())
    }

    // --- Top-level Declarations ---

    override fun visitConstDecl(ctx: HopeLangParser.ConstDeclContext): ConstDeclNode {
        return ConstDeclNode(
            name = ctx.ID().text,
            type = visitTypeRef(ctx.typeRef()),
            value = visitExpression(ctx.expression()),
            location = ctx.loc()
        )
    }

    override fun visitGlobalVarDecl(ctx: HopeLangParser.GlobalVarDeclContext): GlobalVarDeclNode {
        return GlobalVarDeclNode(
            name = ctx.ID().text,
            type = visitTypeRef(ctx.typeRef()),
            initialValue = ctx.expression()?.let { visitExpression(it) },
            location = ctx.loc()
        )
    }

    override fun visitStructDecl(ctx: HopeLangParser.StructDeclContext): StructDeclNode {
        val fields = ctx.fieldDecl().map { fieldCtx ->
            FieldDeclNode(
                name = fieldCtx.ID().text,
                type = visitTypeRef(fieldCtx.typeRef()),
                initialValue = fieldCtx.expression()?.let { visitExpression(it) },
                location = fieldCtx.loc()
            )
        }
        return StructDeclNode(ctx.ID().text, fields, ctx.loc())
    }

    override fun visitEnumDecl(ctx: HopeLangParser.EnumDeclContext): EnumDeclNode {
        val members = ctx.enumMember().map { it.ID().text }
        return EnumDeclNode(ctx.ID().text, members, ctx.loc())
    }

    override fun visitEventDecl(ctx: HopeLangParser.EventDeclContext): EventDeclNode {
        val params = ctx.parameterList()?.parameter()?.map { visitParameter(it) } ?: emptyList()
        return EventDeclNode(ctx.ID().text, params, ctx.loc())
    }

    override fun visitFunctionDecl(ctx: HopeLangParser.FunctionDeclContext): FunctionDeclNode {
        val params = ctx.parameterList()?.parameter()?.map { visitParameter(it) } ?: emptyList()
        val body = ctx.statement().map { visitStatement(it) }
        return FunctionDeclNode(
            name = ctx.ID().text,
            parameters = params,
            returnType = visitTypeRef(ctx.typeRef()),
            body = body,
            location = ctx.loc()
        )
    }

    override fun visitParameter(ctx: HopeLangParser.ParameterContext): ParamNode =
        ParamNode(ctx.ID().text, visitTypeRef(ctx.typeRef()), ctx.loc())

    // --- Handlers ---

    override fun visitStartHandler(ctx: HopeLangParser.StartHandlerContext): StartHandlerNode =
        StartHandlerNode(ctx.statement().map { visitStatement(it) }, ctx.loc())

    override fun visitEventHandler(ctx: HopeLangParser.EventHandlerContext): EventHandlerNode {
        val params = ctx.identifierList()?.ID()?.map { it.text } ?: emptyList()
        return EventHandlerNode(ctx.ID().text, params, ctx.statement().map { visitStatement(it) }, ctx.loc())
    }

    override fun visitEveryHandler(ctx: HopeLangParser.EveryHandlerContext): EveryHandlerNode =
        EveryHandlerNode(visitDurationLiteral(ctx.durationLiteral()), ctx.statement().map { visitStatement(it) }, ctx.loc())

    override fun visitAtHandler(ctx: HopeLangParser.AtHandlerContext): AtHandlerNode =
        AtHandlerNode(visitDurationLiteral(ctx.durationLiteral()), ctx.statement().map { visitStatement(it) }, ctx.loc())

    // --- Types ---

    override fun visitTypeRef(ctx: HopeLangParser.TypeRefContext): TypeRefNode {
        val atom = ctx.typeAtom()
        val baseType: TypeRefNode = when {
            atom.primitiveType() != null -> {
                val prim = atom.primitiveType()
                val type = when {
                    prim.INT_TYPE() != null -> PrimitiveType.INT
                    prim.REAL_TYPE() != null -> PrimitiveType.REAL
                    prim.BOOL_TYPE() != null -> PrimitiveType.BOOL
                    prim.STRING_TYPE() != null -> PrimitiveType.STRING
                    prim.TIME_TYPE() != null -> PrimitiveType.TIME
                    prim.VOID_TYPE() != null -> PrimitiveType.VOID
                    else -> error("Unknown primitive type")
                }
                PrimitiveTypeNode(type, atom.loc())
            }
            atom.LIST() != null -> ListTypeNode(visitTypeRef(atom.typeRef()), atom.loc())
            atom.ID() != null -> CustomTypeNode(atom.ID().text, atom.loc())
            else -> error("Unknown type atom")
        }

        return if (ctx.LBRACK() != null) {
            val sizeCtx = ctx.arraySize()
            val loc = sizeCtx.loc()
            val sizeNode: ArraySizeNode = if (sizeCtx.INT_LITERAL() != null) {
                IntArraySizeNode(parseInt(sizeCtx.INT_LITERAL().text, loc), loc)
            } else {
                IdentArraySizeNode(sizeCtx.ID().text, loc)
            }
            ArrayTypeNode(baseType, sizeNode, ctx.loc())
        } else {
            baseType
        }
    }

    // --- Statements ---

    override fun visitStatement(ctx: HopeLangParser.StatementContext): StmtNode =
        visit(ctx.getChild(0)) as StmtNode

    override fun visitLocalVarDecl(ctx: HopeLangParser.LocalVarDeclContext): LocalVarDeclNode =
        LocalVarDeclNode(
            name = ctx.ID().text,
            type = visitTypeRef(ctx.typeRef()),
            initialValue = ctx.expression()?.let { visitExpression(it) },
            location = ctx.loc()
        )

    override fun visitAssignmentStatement(ctx: HopeLangParser.AssignmentStatementContext): AssignmentStmtNode {
        val target = visitLvalue(ctx.lvalue())
        val op = when (ctx.assignmentOperator().text) {
            "=" -> AssignOp.ASSIGN
            "+=" -> AssignOp.PLUS_ASSIGN
            "-=" -> AssignOp.MINUS_ASSIGN
            "*=" -> AssignOp.MUL_ASSIGN
            "/=" -> AssignOp.DIV_ASSIGN
            "%=" -> AssignOp.MOD_ASSIGN
            else -> error("Unknown assign operator")
        }
        val value = visitExpression(ctx.expression())
        return AssignmentStmtNode(target, op, value, ctx.loc())
    }

    override fun visitLvalue(ctx: HopeLangParser.LvalueContext): ExprNode {
        var current: ExprNode = VarExprNode(ctx.ID().text, ctx.loc())
        for (suffix in ctx.lvalueSuffix()) {
            current = if (suffix.DOT() != null) {
                FieldAccessExprNode(current, suffix.ID().text, suffix.loc())
            } else {
                IndexAccessExprNode(current, visitExpression(suffix.expression()), suffix.loc())
            }
        }
        return current
    }

    override fun visitIfStatement(ctx: HopeLangParser.IfStatementContext): IfStmtNode {
        val branches = mutableListOf<IfBranchNode>()
        var currentCond: ExprNode? = null
        var currentStmts = mutableListOf<StmtNode>()
        var elseStmts: MutableList<StmtNode>? = null
        var inElse = false

        for (child in ctx.children) {
            if (child is TerminalNode) {
                when (child.symbol.type) {
                    HopeLangParser.IF, HopeLangParser.ELIF -> {
                        if (currentCond != null) {
                            branches.add(IfBranchNode(currentCond, currentStmts, currentCond.location))
                            currentStmts = mutableListOf()
                        }
                    }
                    HopeLangParser.ELSE -> {
                        if (currentCond != null) {
                            branches.add(IfBranchNode(currentCond, currentStmts, currentCond.location))
                            currentStmts = mutableListOf()
                        }
                        inElse = true
                        elseStmts = mutableListOf()
                    }
                    HopeLangParser.END -> {
                        if (!inElse && currentCond != null) {
                            branches.add(IfBranchNode(currentCond, currentStmts, currentCond.location))
                        }
                    }
                }
            } else if (child is HopeLangParser.ExpressionContext) {
                currentCond = visitExpression(child)
            } else if (child is HopeLangParser.StatementContext) {
                val stmt = visitStatement(child)
                if (inElse) elseStmts!!.add(stmt) else currentStmts.add(stmt)
            }
        }
        return IfStmtNode(branches, elseStmts, ctx.loc())
    }

    override fun visitWhileStatement(ctx: HopeLangParser.WhileStatementContext): WhileStmtNode =
        WhileStmtNode(visitExpression(ctx.expression()), ctx.statement().map { visitStatement(it) }, ctx.loc())

    override fun visitForStatement(ctx: HopeLangParser.ForStatementContext): ForStmtNode {
        val exprs = ctx.expression()
        val from = visitExpression(exprs[0])
        val to = visitExpression(exprs[1])
        val step = if (ctx.STEP() != null) visitExpression(exprs[2]) else null
        return ForStmtNode(ctx.ID().text, from, to, step, ctx.statement().map { visitStatement(it) }, ctx.loc())
    }

    override fun visitBreakStatement(ctx: HopeLangParser.BreakStatementContext): StmtNode = BreakStmtNode(ctx.loc())
    override fun visitContinueStatement(ctx: HopeLangParser.ContinueStatementContext): StmtNode = ContinueStmtNode(ctx.loc())
    override fun visitReturnStatement(ctx: HopeLangParser.ReturnStatementContext): StmtNode =
        ReturnStmtNode(ctx.expression()?.let { visitExpression(it) }, ctx.loc())

    override fun visitEmitStatement(ctx: HopeLangParser.EmitStatementContext): StmtNode {
        val args = ctx.argumentList()?.expression()?.map { visitExpression(it) } ?: emptyList()
        return EmitStmtNode(ctx.ID().text, args, ctx.loc())
    }

    override fun visitExpressionStatement(ctx: HopeLangParser.ExpressionStatementContext): StmtNode =
        ExprStmtNode(visitExpression(ctx.expression()), ctx.loc())

    // --- Expressions (Прямой спуск без виртуальных накладных расходов) ---

    override fun visitExpression(ctx: HopeLangParser.ExpressionContext): ExprNode =
        visitLogicalOr(ctx.logicalOr())

    override fun visitLogicalOr(ctx: HopeLangParser.LogicalOrContext): ExprNode {
        val ands = ctx.logicalAnd()
        if (ands.size == 1) return visitLogicalAnd(ands[0])

        var node = visitLogicalAnd(ands[0])
        for (i in 1 until ands.size) {
            val right = visitLogicalAnd(ands[i])
            node = BinaryExprNode(node, BinaryOp.OR, right, node.location)
        }
        return node
    }

    override fun visitLogicalAnd(ctx: HopeLangParser.LogicalAndContext): ExprNode {
        val eqs = ctx.equality()
        if (eqs.size == 1) return visitEquality(eqs[0])

        var node = visitEquality(eqs[0])
        for (i in 1 until eqs.size) {
            val right = visitEquality(eqs[i])
            node = BinaryExprNode(node, BinaryOp.AND, right, node.location)
        }
        return node
    }

    override fun visitEquality(ctx: HopeLangParser.EqualityContext): ExprNode {
        val comps = ctx.comparison()
        if (comps.size == 1) return visitComparison(comps[0])

        var node = visitComparison(comps[0])
        for (i in 1 until comps.size) {
            val opToken = ctx.getChild(2 * i - 1) as TerminalNode
            val op = if (opToken.symbol.type == HopeLangParser.EQ) BinaryOp.EQ else BinaryOp.NEQ
            val right = visitComparison(comps[i])
            node = BinaryExprNode(node, op, right, opToken.loc())
        }
        return node
    }

    override fun visitComparison(ctx: HopeLangParser.ComparisonContext): ExprNode {
        val adds = ctx.additive()
        if (adds.size == 1) return visitAdditive(adds[0])

        var node = visitAdditive(adds[0])
        for (i in 1 until adds.size) {
            val opToken = ctx.getChild(2 * i - 1) as TerminalNode
            val op = when (opToken.symbol.type) {
                HopeLangParser.LT -> BinaryOp.LT
                HopeLangParser.LTE -> BinaryOp.LTE
                HopeLangParser.GT -> BinaryOp.GT
                HopeLangParser.GTE -> BinaryOp.GTE
                else -> error("Unknown comparison op")
            }
            val right = visitAdditive(adds[i])
            node = BinaryExprNode(node, op, right, opToken.loc())
        }
        return node
    }

    override fun visitAdditive(ctx: HopeLangParser.AdditiveContext): ExprNode {
        val muls = ctx.multiplicative()
        if (muls.size == 1) return visitMultiplicative(muls[0])

        var node = visitMultiplicative(muls[0])
        for (i in 1 until muls.size) {
            val opToken = ctx.getChild(2 * i - 1) as TerminalNode
            val op = if (opToken.symbol.type == HopeLangParser.PLUS) BinaryOp.PLUS else BinaryOp.MINUS
            val right = visitMultiplicative(muls[i])
            node = BinaryExprNode(node, op, right, opToken.loc())
        }
        return node
    }

    override fun visitMultiplicative(ctx: HopeLangParser.MultiplicativeContext): ExprNode {
        val unaries = ctx.unary()
        if (unaries.size == 1) return visitUnary(unaries[0])

        var node = visitUnary(unaries[0])
        for (i in 1 until unaries.size) {
            val opToken = ctx.getChild(2 * i - 1) as TerminalNode
            val op = when (opToken.symbol.type) {
                HopeLangParser.MUL -> BinaryOp.MUL
                HopeLangParser.DIV -> BinaryOp.DIV
                HopeLangParser.MOD -> BinaryOp.MOD
                else -> error("Unknown multiplicative op")
            }
            val right = visitUnary(unaries[i])
            node = BinaryExprNode(node, op, right, opToken.loc())
        }
        return node
    }

    override fun visitUnary(ctx: HopeLangParser.UnaryContext): ExprNode {
        if (ctx.postfix() != null) return visitPostfix(ctx.postfix())
        val operand = visitUnary(ctx.unary())
        val op = when {
            ctx.NOT() != null -> UnaryOp.NOT
            ctx.MINUS() != null -> UnaryOp.MINUS
            ctx.PLUS() != null -> UnaryOp.PLUS
            else -> error("Unknown unary op")
        }
        return UnaryExprNode(op, operand, ctx.loc())
    }

    override fun visitPostfix(ctx: HopeLangParser.PostfixContext): ExprNode {
        var current = visitPrimary(ctx.primary())
        for (suffix in ctx.postfixSuffix()) {
            current = when {
                suffix.DOT() != null -> FieldAccessExprNode(current, suffix.ID().text, suffix.loc())
                suffix.LBRACK() != null -> IndexAccessExprNode(current, visitExpression(suffix.expression()), suffix.loc())
                suffix.LPAREN() != null -> {
                    val args = suffix.argumentList()?.expression()?.map { visitExpression(it) } ?: emptyList()
                    CallExprNode(current, args, suffix.loc())
                }
                else -> error("Unknown postfix suffix")
            }
        }
        return current
    }

    override fun visitPrimary(ctx: HopeLangParser.PrimaryContext): ExprNode {
        ctx.ID()?.let { return VarExprNode(it.text, it.loc()) }
        ctx.literal()?.let { return visitLiteral(it) }
        ctx.durationLiteral()?.let { return visitDurationLiteral(it) }
        ctx.expression()?.let { return visitExpression(it) }
        error("Unknown primary at ${ctx.loc()}")
    }

    override fun visitLiteral(ctx: HopeLangParser.LiteralContext): ExprNode {
        val loc = ctx.loc()
        return when {
            ctx.INT_LITERAL() != null -> IntLiteralNode(parseInt(ctx.INT_LITERAL().text, loc), loc)
            ctx.REAL_LITERAL() != null -> RealLiteralNode(ctx.REAL_LITERAL().text.toDouble(), loc)
            ctx.STRING_LITERAL() != null -> {
                val txt = ctx.STRING_LITERAL().text
                val unescaped = unescapeString(txt.substring(1, txt.length - 1))
                StringLiteralNode(unescaped, loc)
            }
            ctx.TRUE() != null -> BoolLiteralNode(true, loc)
            ctx.FALSE() != null -> BoolLiteralNode(false, loc)
            else -> error("Unknown literal")
        }
    }

    override fun visitDurationLiteral(ctx: HopeLangParser.DurationLiteralContext): DurationLiteralNode {
        val num = ctx.INT_LITERAL()?.text?.toDouble() ?: ctx.REAL_LITERAL().text.toDouble()
        val u = ctx.timeUnit()
        val unit = when {
            u.MILLISECOND_UNIT() != null -> TimeUnit.MS
            u.SECOND_UNIT() != null -> TimeUnit.SEC
            u.MINUTE_UNIT() != null -> TimeUnit.MIN
            u.HOUR_UNIT() != null -> TimeUnit.HOUR
            u.DAY_UNIT() != null -> TimeUnit.DAY
            else -> error("Unknown time unit")
        }
        return DurationLiteralNode(num, unit, ctx.loc())
    }

    private fun parseInt(text: String, location: SourceLocation): Long =
        text.toLongOrNull() ?: throw FrontendPipeline.HopeSyntaxException(
            "Integer literal out of range at ${location.line}:${location.column}: $text"
        )

    private fun unescapeString(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (val next = s[i + 1]) {
                    'b' -> { sb.append('\b'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    'n' -> { sb.append('\n'); i += 2 }
                    'f' -> { sb.append('\u000C'); i += 2 }
                    'r' -> { sb.append('\r'); i += 2 }
                    '"' -> { sb.append('"'); i += 2 }
                    '\'' -> { sb.append('\''); i += 2 }
                    '\\' -> { sb.append('\\'); i += 2 }
                    'u' -> {
                        if (i + 5 < s.length) {
                            val hex = s.substring(i + 2, i + 6)
                            sb.append(hex.toInt(16).toChar())
                            i += 6
                        } else {
                            sb.append(c)
                            i++
                        }
                    }
                    else -> { sb.append(next); i += 2 }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }
}