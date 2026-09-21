package lv426.compiler.semantic

data class SourceLocation(val line: Int, val column: Int)

enum class DiagnosticSeverity {
    ERROR,
    WARNING
}

data class Diagnostic (
    val message: String,
    val location: SourceLocation? = null,
    val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR,
)

class DiagnosticBuilder {
    private val items = mutableListOf<Diagnostic>()

    fun error(message: String, location: SourceLocation? = null) {
        items += Diagnostic(message = message, location = location, severity = DiagnosticSeverity.ERROR)
    }

    fun warning(message: String, location: SourceLocation? = null) {
        items += Diagnostic(message = message, location = location, severity = DiagnosticSeverity.WARNING)
    }

    fun all(): List<Diagnostic> = items.toList()

    fun hasErrors(): Boolean = items.any{ it.severity == DiagnosticSeverity.ERROR }

}