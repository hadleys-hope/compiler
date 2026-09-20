// Generated from lv426/compiler/parser/HopeLang.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link HopeLangParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface HopeLangVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#source}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource(HopeLangParser.SourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#programDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgramDecl(HopeLangParser.ProgramDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#topLevelDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopLevelDecl(HopeLangParser.TopLevelDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#constDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstDecl(HopeLangParser.ConstDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#globalVarDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobalVarDecl(HopeLangParser.GlobalVarDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#structDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructDecl(HopeLangParser.StructDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#fieldDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDecl(HopeLangParser.FieldDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#enumDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumDecl(HopeLangParser.EnumDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#enumMember}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumMember(HopeLangParser.EnumMemberContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#eventDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventDecl(HopeLangParser.EventDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#startHandler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartHandler(HopeLangParser.StartHandlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#eventHandler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventHandler(HopeLangParser.EventHandlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#everyHandler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEveryHandler(HopeLangParser.EveryHandlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#atHandler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtHandler(HopeLangParser.AtHandlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#identifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierList(HopeLangParser.IdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#functionDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDecl(HopeLangParser.FunctionDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#parameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterList(HopeLangParser.ParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(HopeLangParser.ParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#typeRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeRef(HopeLangParser.TypeRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#typeAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeAtom(HopeLangParser.TypeAtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveType(HopeLangParser.PrimitiveTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#arraySize}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArraySize(HopeLangParser.ArraySizeContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(HopeLangParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#localVarDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocalVarDecl(HopeLangParser.LocalVarDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#assignmentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStatement(HopeLangParser.AssignmentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#assignmentOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOperator(HopeLangParser.AssignmentOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#lvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLvalue(HopeLangParser.LvalueContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#lvalueSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLvalueSuffix(HopeLangParser.LvalueSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(HopeLangParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(HopeLangParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(HopeLangParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#breakStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakStatement(HopeLangParser.BreakStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#continueStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueStatement(HopeLangParser.ContinueStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(HopeLangParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#emitStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmitStatement(HopeLangParser.EmitStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(HopeLangParser.ExpressionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(HopeLangParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#logicalOr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOr(HopeLangParser.LogicalOrContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#logicalAnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAnd(HopeLangParser.LogicalAndContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#equality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquality(HopeLangParser.EqualityContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(HopeLangParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#additive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditive(HopeLangParser.AdditiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#multiplicative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicative(HopeLangParser.MultiplicativeContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary(HopeLangParser.UnaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#postfix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfix(HopeLangParser.PostfixContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#postfixSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixSuffix(HopeLangParser.PostfixSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(HopeLangParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(HopeLangParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(HopeLangParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#durationLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDurationLiteral(HopeLangParser.DurationLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link HopeLangParser#timeUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeUnit(HopeLangParser.TimeUnitContext ctx);
}