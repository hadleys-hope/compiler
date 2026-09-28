package lv426.compiler.semantic

import java.util.IdentityHashMap
import lv426.compiler.ast.*

class TypeResolver(
    private val symbols: SymbolTable,
    private val diagnostics: DiagnosticBuilder,
    private val intConstants: (String) -> Long?
) {
    private val cache = IdentityHashMap<TypeRefNode, HopeType>()

    fun resolve(node: TypeRefNode): HopeType {
        cache[node]?.let { return it }
        val type = when (node) {
            is PrimitiveTypeNode -> when (node.type) {
                PrimitiveType.INT -> IntType
                PrimitiveType.REAL -> RealType
                PrimitiveType.BOOL -> BoolType
                PrimitiveType.STRING -> StringType
                PrimitiveType.TIME -> TimeType
                PrimitiveType.VOID -> VoidType
            }
            is CustomTypeNode -> when (val symbol = symbols.resolve(node.name)) {
                is StructSymbol -> StructType(symbol.name)
                is EnumSymbol -> EnumType(symbol.name)
                null -> {
                    diagnostics.error("Unknown type '${node.name}'", node.location)
                    ErrorType
                }
                else -> {
                    diagnostics.error("'${node.name}' is not a type", node.location)
                    ErrorType
                }
            }
            is ListTypeNode -> {
                val element = resolve(node.elementType)
                if (element == VoidType) {
                    diagnostics.error("List element cannot have type void", node.location)
                    ErrorType
                } else if (element == ErrorType) ErrorType else ListType(element)
            }
            is ArrayTypeNode -> {
                val element = resolve(node.elementType)
                val rawSize: Long? = when (val size = node.size) {
                    is IntArraySizeNode -> size.value
                    is IdentArraySizeNode -> intConstants(size.name).also {
                        if (it == null) diagnostics.error("Array size '${size.name}' must be an integer constant", size.location)
                    }
                }
                when {
                    element == VoidType -> {
                        diagnostics.error("Array element cannot have type void", node.location)
                        ErrorType
                    }
                    element == ErrorType || rawSize == null -> ErrorType
                    rawSize <= 0 -> {
                        diagnostics.error("Array size must be greater than zero", node.size.location)
                        ErrorType
                    }
                    rawSize > Int.MAX_VALUE -> {
                        diagnostics.error("Array size is too large: $rawSize", node.size.location)
                        ErrorType
                    }
                    else -> ArrayType(element, rawSize.toInt())
                }
            }
        }
        cache[node] = type
        return type
    }

    fun resolvedTypes(): Map<TypeRefNode, HopeType> = cache
}
