package lv426.compiler

import java.nio.file.Path
import lv426.compiler.ast.SourceNode
import lv426.compiler.backend.HbcBackend
import lv426.compiler.backend.HbcModule
import lv426.compiler.backend.IrProgram
import lv426.compiler.codegen.IrLowering
import lv426.compiler.frontend.FrontendPipeline
import lv426.compiler.semantic.Diagnostic
import lv426.compiler.semantic.SemanticAnalyzer
import lv426.compiler.semantic.SemanticResult

class HopeCompilationException(
    val diagnostics: List<Diagnostic>
) : IllegalArgumentException(
    diagnostics.joinToString("\n") { diagnostic ->
        val location = diagnostic.location?.let { "${it.line}:${it.column}: " } ?: ""
        "$location${diagnostic.severity.name.lowercase()}: ${diagnostic.message}"
    }
)

data class CheckedProgram(
    val ast: SourceNode,
    val semantic: SemanticResult
)

data class LoweredProgram(
    val ast: SourceNode,
    val semantic: SemanticResult,
    val ir: IrProgram
)

object CompilerPipeline {
    fun analyze(sourceCode: String): CheckedProgram {
        val ast = FrontendPipeline.parse(sourceCode)
        return CheckedProgram(ast, SemanticAnalyzer().analyze(ast))
    }

    fun lower(sourceCode: String): LoweredProgram {
        val checked = analyze(sourceCode)
        if (!checked.semantic.isValid) throw HopeCompilationException(checked.semantic.diagnostics)
        return LoweredProgram(checked.ast, checked.semantic, IrLowering(checked.semantic).lower())
    }

    fun compile(sourceCode: String): HbcModule = HbcBackend.compile(lower(sourceCode).ir)

    fun compileToBytes(sourceCode: String): ByteArray = HbcBackend.compileToBytes(lower(sourceCode).ir)

    fun write(sourceCode: String, output: Path): HbcModule = HbcBackend.write(lower(sourceCode).ir, output)
}
