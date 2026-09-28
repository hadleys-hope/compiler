package lv426.compiler.semantic

sealed interface Symbol {
    val name: String
}

data class IntrinsicSymbol(override val name: String) : Symbol

data class VariableSymbol(
    override val name: String,
    val type: HopeType,
    val mutable: Boolean = true
) : Symbol

data class FunctionParameter(val name: String, val type: HopeType)

data class FunctionSymbol(
    override val name: String,
    val parameters: List<FunctionParameter>,
    val returnType: HopeType
) : Symbol

data class StructField(val name: String, val type: HopeType)

data class StructSymbol(
    override val name: String,
    val fields: MutableMap<String, StructField> = linkedMapOf()
) : Symbol {
    fun findField(name: String): StructField? = fields[name]
}

data class EnumSymbol(
    override val name: String,
    val values: List<String>
) : Symbol

data class EventParameter(val name: String, val type: HopeType)

data class EventSymbol(
    override val name: String,
    val parameters: List<EventParameter>
) : Symbol
