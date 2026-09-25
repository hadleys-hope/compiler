package lv426.compiler.ast

// Координаты узла в исходном коде (для вывода красивых ошибок компиляции).
data class SourceLocation(
    val line: Int,
    val column: Int
) {
    override fun toString(): String = "$line:$column"

    companion object {
        val NONE = SourceLocation(0, 0)
    }
}

sealed interface AstNode {
    val location: SourceLocation
}

// Корень программы
data class SourceNode(
    val programName: String?,
    val declarations: List<TopLevelDeclNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : AstNode

// Декларации верхнего уровня
sealed interface TopLevelDeclNode : AstNode

data class ConstDeclNode(
    val name: String,
    val type: TypeRefNode,
    val value: ExprNode,
    override val location: SourceLocation = SourceLocation.NONE
) : TopLevelDeclNode

data class GlobalVarDeclNode(
    val name: String,
    val type: TypeRefNode,
    val initialValue: ExprNode?,
    override val location: SourceLocation = SourceLocation.NONE
) : TopLevelDeclNode

data class StructDeclNode(
    val name: String,
    val fields: List<FieldDeclNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : TopLevelDeclNode

data class FieldDeclNode(
    val name: String,
    val type: TypeRefNode,
    val initialValue: ExprNode?,
    override val location: SourceLocation = SourceLocation.NONE
) : AstNode

data class EnumDeclNode(
    val name: String,
    val members: List<String>,
    override val location: SourceLocation = SourceLocation.NONE
) : TopLevelDeclNode

data class EventDeclNode(
    val name: String,
    val parameters: List<ParamNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : TopLevelDeclNode

data class FunctionDeclNode(
    val name: String,
    val parameters: List<ParamNode>,
    val returnType: TypeRefNode,
    val body: List<StmtNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : TopLevelDeclNode

data class ParamNode(
    val name: String,
    val type: TypeRefNode,
    override val location: SourceLocation = SourceLocation.NONE
) : AstNode

// Обработчики событий
data class StartHandlerNode(
    val body: List<StmtNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : TopLevelDeclNode

data class EventHandlerNode(
    val eventName: String,
    val parameters: List<String>,
    val body: List<StmtNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : TopLevelDeclNode

data class EveryHandlerNode(
    val duration: DurationLiteralNode,
    val body: List<StmtNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : TopLevelDeclNode

data class AtHandlerNode(
    val duration: DurationLiteralNode,
    val body: List<StmtNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : TopLevelDeclNode

// Типы данных
sealed interface TypeRefNode : AstNode

enum class PrimitiveType { INT, REAL, BOOL, STRING, TIME, VOID }

data class PrimitiveTypeNode(
    val type: PrimitiveType,
    override val location: SourceLocation = SourceLocation.NONE
) : TypeRefNode

data class CustomTypeNode(
    val name: String,
    override val location: SourceLocation = SourceLocation.NONE
) : TypeRefNode

data class ListTypeNode(
    val elementType: TypeRefNode,
    override val location: SourceLocation = SourceLocation.NONE
) : TypeRefNode

// Структурированный размер массив
sealed interface ArraySizeNode : AstNode

data class IntArraySizeNode(
    val value: Long,
    override val location: SourceLocation = SourceLocation.NONE
) : ArraySizeNode

data class IdentArraySizeNode(
    val name: String,
    override val location: SourceLocation = SourceLocation.NONE
) : ArraySizeNode

data class ArrayTypeNode(
    val elementType: TypeRefNode,
    val size: ArraySizeNode,
    override val location: SourceLocation = SourceLocation.NONE
) : TypeRefNode

// Инструкции
sealed interface StmtNode : AstNode

data class LocalVarDeclNode(
    val name: String,
    val type: TypeRefNode,
    val initialValue: ExprNode?,
    override val location: SourceLocation = SourceLocation.NONE
) : StmtNode

enum class AssignOp { ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MUL_ASSIGN, DIV_ASSIGN, MOD_ASSIGN }

data class AssignmentStmtNode(
    val target: ExprNode,
    val op: AssignOp,
    val value: ExprNode,
    override val location: SourceLocation = SourceLocation.NONE
) : StmtNode

data class IfBranchNode(
    val condition: ExprNode,
    val body: List<StmtNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : AstNode

data class IfStmtNode(
    val branches: List<IfBranchNode>,
    val elseBody: List<StmtNode>?,
    override val location: SourceLocation = SourceLocation.NONE
) : StmtNode

data class WhileStmtNode(
    val condition: ExprNode,
    val body: List<StmtNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : StmtNode

data class ForStmtNode(
    val variable: String,
    val from: ExprNode,
    val to: ExprNode,
    val step: ExprNode?,
    val body: List<StmtNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : StmtNode

data class BreakStmtNode(override val location: SourceLocation = SourceLocation.NONE) : StmtNode
data class ContinueStmtNode(override val location: SourceLocation = SourceLocation.NONE) : StmtNode

data class ReturnStmtNode(
    val value: ExprNode?,
    override val location: SourceLocation = SourceLocation.NONE
) : StmtNode

data class EmitStmtNode(
    val eventName: String,
    val arguments: List<ExprNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : StmtNode

data class ExprStmtNode(
    val expr: ExprNode,
    override val location: SourceLocation = SourceLocation.NONE
) : StmtNode

//  Выражения
sealed interface ExprNode : AstNode

enum class BinaryOp {
    OR, AND, EQ, NEQ, LT, LTE, GT, GTE, PLUS, MINUS, MUL, DIV, MOD
}

data class BinaryExprNode(
    val left: ExprNode,
    val op: BinaryOp,
    val right: ExprNode,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode

enum class UnaryOp { NOT, MINUS, PLUS }

data class UnaryExprNode(
    val op: UnaryOp,
    val operand: ExprNode,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode

data class FieldAccessExprNode(
    val target: ExprNode,
    val fieldName: String,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode

data class IndexAccessExprNode(
    val target: ExprNode,
    val index: ExprNode,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode

data class CallExprNode(
    val function: ExprNode,
    val arguments: List<ExprNode>,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode

data class VarExprNode(
    val name: String,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode

data class IntLiteralNode(
    val value: Long,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode

data class RealLiteralNode(
    val value: Double,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode

data class StringLiteralNode(
    val value: String,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode

data class BoolLiteralNode(
    val value: Boolean,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode

enum class TimeUnit { MS, SEC, MIN, HOUR, DAY }

data class DurationLiteralNode(
    val value: Double,
    val unit: TimeUnit,
    override val location: SourceLocation = SourceLocation.NONE
) : ExprNode