package lv426.compiler.ast

/**
 * Базовый интерфейс для всех узлов абстрактного синтаксического дерева.
 */
sealed interface AstNode

// --- Корень программы ---
data class SourceNode(
    val programName: String?,
    val declarations: List<TopLevelDeclNode>
) : AstNode

// --- Декларации верхнего уровня ---
sealed interface TopLevelDeclNode : AstNode

data class ConstDeclNode(
    val name: String,
    val type: TypeRefNode,
    val value: ExprNode
) : TopLevelDeclNode

data class GlobalVarDeclNode(
    val name: String,
    val type: TypeRefNode,
    val initialValue: ExprNode?
) : TopLevelDeclNode

data class StructDeclNode(
    val name: String,
    val fields: List<FieldDeclNode>
) : TopLevelDeclNode

data class FieldDeclNode(
    val name: String,
    val type: TypeRefNode,
    val initialValue: ExprNode?
) : AstNode

data class EnumDeclNode(
    val name: String,
    val members: List<String>
) : TopLevelDeclNode

data class EventDeclNode(
    val name: String,
    val parameters: List<ParamNode>
) : TopLevelDeclNode

data class FunctionDeclNode(
    val name: String,
    val parameters: List<ParamNode>,
    val returnType: TypeRefNode,
    val body: List<StmtNode>
) : TopLevelDeclNode

data class ParamNode(
    val name: String,
    val type: TypeRefNode
) : AstNode

// --- Обработчики событий ---
data class StartHandlerNode(
    val body: List<StmtNode>
) : TopLevelDeclNode

data class EventHandlerNode(
    val eventName: String,
    val parameters: List<String>,
    val body: List<StmtNode>
) : TopLevelDeclNode

data class EveryHandlerNode(
    val duration: DurationLiteralNode,
    val body: List<StmtNode>
) : TopLevelDeclNode

data class AtHandlerNode(
    val duration: DurationLiteralNode,
    val body: List<StmtNode>
) : TopLevelDeclNode
// --- Типы данных ---
sealed interface TypeRefNode : AstNode

enum class PrimitiveType { INT, REAL, BOOL, STRING, TIME, VOID }

data class PrimitiveTypeNode(val type: PrimitiveType) : TypeRefNode
data class CustomTypeNode(val name: String) : TypeRefNode
data class ListTypeNode(val elementType: TypeRefNode) : TypeRefNode
data class ArrayTypeNode(val elementType: TypeRefNode, val size: String) : TypeRefNode
// --- Объявления (Declarations) ---

// --- Инструкции (Statements) ---
sealed interface StmtNode : AstNode

data class LocalVarDeclNode(
    val name: String,
    val type: TypeRefNode,
    val initialValue: ExprNode?
) : StmtNode

enum class AssignOp { ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MUL_ASSIGN, DIV_ASSIGN, MOD_ASSIGN }

data class AssignmentStmtNode(
    val target: ExprNode,
    val op: AssignOp,
    val value: ExprNode
) : StmtNode

data class IfBranchNode(
    val condition: ExprNode,
    val body: List<StmtNode>
) : AstNode

data class IfStmtNode(
    val branches: List<IfBranchNode>,
    val elseBody: List<StmtNode>?
) : StmtNode

data class WhileStmtNode(
    val condition: ExprNode,
    val body: List<StmtNode>
) : StmtNode

data class ForStmtNode(
    val variable: String,
    val from: ExprNode,
    val to: ExprNode,
    val step: ExprNode?,
    val body: List<StmtNode>
) : StmtNode

object BreakStmtNode : StmtNode
object ContinueStmtNode : StmtNode

data class ReturnStmtNode(val value: ExprNode?) : StmtNode
data class EmitStmtNode(val eventName: String, val arguments: List<ExprNode>) : StmtNode
data class ExprStmtNode(val expr: ExprNode) : StmtNode

// --- Выражения (Expressions) ---
sealed interface ExprNode : AstNode

enum class BinaryOp {
    OR, AND, EQ, NEQ, LT, LTE, GT, GTE, PLUS, MINUS, MUL, DIV, MOD
}

data class BinaryExprNode(
    val left: ExprNode,
    val op: BinaryOp,
    val right: ExprNode
) : ExprNode

enum class UnaryOp { NOT, MINUS, PLUS }

data class UnaryExprNode(
    val op: UnaryOp,
    val operand: ExprNode
) : ExprNode

// Доступ к полям, индексам и вызовы функций
data class FieldAccessExprNode(val target: ExprNode, val fieldName: String) : ExprNode
data class IndexAccessExprNode(val target: ExprNode, val index: ExprNode) : ExprNode
data class CallExprNode(val function: ExprNode, val arguments: List<ExprNode>) : ExprNode

data class VarExprNode(val name: String) : ExprNode
data class IntLiteralNode(val value: Long) : ExprNode
data class RealLiteralNode(val value: Double) : ExprNode
data class StringLiteralNode(val value: String) : ExprNode
data class BoolLiteralNode(val value: Boolean) : ExprNode

enum class TimeUnit { MS, SEC, MIN, HOUR, DAY }

data class DurationLiteralNode(
    val value: Double,
    val unit: TimeUnit
) : ExprNode