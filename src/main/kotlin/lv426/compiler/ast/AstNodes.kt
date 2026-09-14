package lv426.compiler.ast

/**
 * Базовый интерфейс для всех узлов абстрактного синтаксического дерева.
 */
sealed interface AstNode

// --- Программа ---

data class ProgramNode(
    val declarations: List<DeclarationNode>,
    val handlers: List<HandlerNode>
) : AstNode

// --- Типы данных ---

enum class DataType {
    INT, FLOAT, BOOL, STRING;

    companion object {
        fun fromString(value: String): DataType = when (value) {
            "int" -> INT
            "float" -> FLOAT
            "bool" -> BOOL
            "string" -> STRING
            else -> throw IllegalArgumentException("Unknown type: $value")
        }
    }
}

data class TypeNode(val type: DataType) : AstNode

// --- Объявления (Declarations) ---

sealed interface DeclarationNode : AstNode {
    val name: String
    val type: TypeNode
}

data class SensorDeclNode(
    override val name: String,
    override val type: TypeNode
) : DeclarationNode

data class ActuatorDeclNode(
    override val name: String,
    override val type: TypeNode
) : DeclarationNode

data class VarDeclNode(
    override val name: String,
    override val type: TypeNode,
    val initialValue: ExprNode
) : DeclarationNode

data class ConstDeclNode(
    override val name: String,
    override val type: TypeNode,
    val initialValue: ExprNode
) : DeclarationNode

// --- Обработчики событий (Handlers) ---

sealed interface HandlerNode : AstNode {
    val body: BlockNode
}

data class OnInitHandlerNode(override val body: BlockNode) : HandlerNode

data class OnTickHandlerNode(override val body: BlockNode) : HandlerNode

data class OnChangeHandlerNode(
    val sensorName: String,
    override val body: BlockNode
) : HandlerNode

data class OnTimeHandlerNode(
    val time: String,
    override val body: BlockNode
) : HandlerNode

// --- Блоки и Инструкции (Statements) ---

data class BlockNode(val statements: List<StmtNode>) : AstNode

sealed interface StmtNode : AstNode

data class AssignStmtNode(
    val target: String,
    val value: ExprNode
) : StmtNode

data class IfStmtNode(
    val condition: ExprNode,
    val thenBlock: BlockNode,
    val elseBlock: BlockNode?
) : StmtNode

data class WhileStmtNode(
    val condition: ExprNode,
    val body: BlockNode
) : StmtNode

data class CallStmtNode(val call: CallExprNode) : StmtNode

// --- Выражения (Expressions) ---

sealed interface ExprNode : AstNode

enum class BinaryOp {
    ADD, SUB, MUL, DIV, MOD,
    EQ, NEQ, LT, LE, GT, GE,
    AND, OR
}

enum class UnaryOp {
    NEG, NOT
}

data class BinaryExprNode(
    val left: ExprNode,
    val op: BinaryOp,
    val right: ExprNode
) : ExprNode

data class UnaryExprNode(
    val op: UnaryOp,
    val operand: ExprNode
) : ExprNode

data class CallExprNode(
    val functionName: String,
    val arguments: List<ExprNode>
) : ExprNode

data class VarExprNode(val name: String) : ExprNode

data class IntLiteralNode(val value: Long) : ExprNode

data class FloatLiteralNode(val value: Double) : ExprNode

data class BoolLiteralNode(val value: Boolean) : ExprNode

data class StringLiteralNode(val value: String) : ExprNode