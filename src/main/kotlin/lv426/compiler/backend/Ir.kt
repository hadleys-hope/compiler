package lv426.compiler.backend

/** Stack IR accepted by the backend after frontend name/type checking and lowering. */
data class IrProgram(
    val functions: List<IrFunction>,
    val structs: List<IrStruct> = emptyList(),
    val globals: List<IrGlobal> = emptyList(),
    val events: List<IrEvent> = emptyList(),
    val handlers: List<IrHandler> = emptyList()
)

data class IrField(val name: String, val type: HbcType, val initializer: String? = null)
data class IrStruct(val name: String, val fields: List<IrField>)
data class IrGlobal(val name: String, val type: HbcType, val initializer: String? = null, val mutable: Boolean = true)
data class IrEvent(val name: String, val parameters: List<HbcType>)

sealed interface IrHandler {
    val function: String
    data class Event(val event: String, override val function: String) : IrHandler
    data class Start(override val function: String) : IrHandler
    data class Every(val milliseconds: Long, override val function: String) : IrHandler
    data class At(val milliseconds: Long, override val function: String) : IrHandler
}

data class IrFunction(
    val name: String,
    /** Parameters occupy local slots 0 until parameterCount. */
    val parameterCount: Int,
    /** Total frame slots, INCLUDING parameters. The frontend allocates these slots. */
    val localCount: Int,
    val instructions: List<IrInstruction>
)

/** Labels are symbolic until [HbcBackend.compile] resolves them to byte offsets. */
data class IrLabel(val name: String)

sealed interface IrInstruction {
    data class Label(val label: IrLabel) : IrInstruction
    data class Push(val constant: HbcConstant) : IrInstruction
    data class DefaultValue(val type: HbcType) : IrInstruction
    data class LoadLocal(val slot: Int) : IrInstruction
    data class StoreLocal(val slot: Int) : IrInstruction
    data class LoadGlobal(val name: String) : IrInstruction
    data class StoreGlobal(val name: String) : IrInstruction
    data class NewStruct(val struct: String) : IrInstruction
    data class LoadField(val struct: String, val field: String) : IrInstruction
    data class StoreField(val struct: String, val field: String) : IrInstruction
    data class NewArray(val elementType: HbcType, val size: Int) : IrInstruction
    data class NewArrayWithInitializer(val elementType: HbcType, val size: Int, val initializer: String) : IrInstruction
    data class NewList(val elementType: HbcType, val elementCount: Int = 0) : IrInstruction
    data object LoadIndex : IrInstruction
    data object StoreIndex : IrInstruction
    data object Length : IrInstruction
    data object ListAppend : IrInstruction
    data object ListRemove : IrInstruction
    data object IntToReal : IrInstruction
    data class Unary(val operation: UnaryOperation) : IrInstruction
    data class Binary(val operation: BinaryOperation) : IrInstruction
    data class Jump(val target: IrLabel) : IrInstruction
    /** Consumes the Boolean condition on BOTH paths. */
    data class JumpIfFalse(val target: IrLabel) : IrInstruction
    /** Arguments are pushed left to right; consumes them and pushes one value (unit for void). */
    data class Call(val function: String, val argumentCount: Int) : IrInstruction
    /** Consumes arguments pushed left to right; leaves no result. Dispatch belongs to the host. */
    data class Emit(val event: String, val argumentCount: Int) : IrInstruction
    data class Return(val hasValue: Boolean = false) : IrInstruction
    data object Pop : IrInstruction
    data object Nop : IrInstruction
}

enum class UnaryOperation { NEGATE, NOT }

/** AND/OR are eager; the frontend lowers short-circuit logic using conditional jumps. */
enum class BinaryOperation {
    ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO,
    EQUAL, NOT_EQUAL, LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL,
    AND, OR
}
