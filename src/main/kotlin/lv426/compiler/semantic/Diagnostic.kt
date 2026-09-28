package lv426.compiler.semantic

import lv426.compiler.ast.SourceLocation

enum class DiagnosticSeverity {
    ERROR,
    WARNING
}

data class Diagnostic(
    val message: String,
    val location: SourceLocation? = null,
    val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR
)

class DiagnosticBuilder {
    private val items = mutableListOf<Diagnostic>()

    fun error(message: String, location: SourceLocation? = null) {
        items += Diagnostic(message, location, DiagnosticSeverity.ERROR)
    }

    fun warning(message: String, location: SourceLocation? = null) {
        items += Diagnostic(message, location, DiagnosticSeverity.WARNING)
    }

    fun all(): List<Diagnostic> = items.toList()
    fun hasErrors(): Boolean = items.any { it.severity == DiagnosticSeverity.ERROR }
}
