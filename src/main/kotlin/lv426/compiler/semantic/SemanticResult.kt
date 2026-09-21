package lv426.compiler.semantic

data class SemanticResult (
    val symbol: SymbolTable,
    val diagnostics: List<Diagnostic>
) {
    val isValid: Boolean get() = diagnostics.none{
        it.severity == DiagnosticSeverity.ERROR
    }
}