package lv426.compiler.semantic

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SemanticResultTest {
    @Test
    fun `result without errors is valid`() {
        val result = SemanticResult(
            symbol = SymbolTable(),
            diagnostics = listOf(
                Diagnostic("warning", severity = DiagnosticSeverity.WARNING)
            )
        )

        assertTrue(result.isValid)
    }

    @Test
    fun `result with an error is invalid`() {
        val result = SemanticResult(
            symbol = SymbolTable(),
            diagnostics = listOf(Diagnostic("error"))
        )

        assertFalse(result.isValid)
    }
}
