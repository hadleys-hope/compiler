package lv426.compiler.semantic

import lv426.compiler.ast.ExprNode
import lv426.compiler.ast.SourceNode
import lv426.compiler.ast.TypeRefNode

/** Result consumed by code generation after semantic analysis succeeds. */
data class SemanticResult(
    val symbol: SymbolTable,
    val diagnostics: List<Diagnostic>,
    val program: SourceNode? = null,
    val expressionTypes: Map<ExprNode, HopeType> = emptyMap(),
    val resolvedTypes: Map<TypeRefNode, HopeType> = emptyMap(),
    val constantIntValues: Map<String, Long> = emptyMap()
) {
    val symbols: SymbolTable get() = symbol
    val isValid: Boolean get() = diagnostics.none { it.severity == DiagnosticSeverity.ERROR }
    fun typeOf(expression: ExprNode): HopeType? = expressionTypes[expression]
    fun typeOf(typeRef: TypeRefNode): HopeType? = resolvedTypes[typeRef]
}
