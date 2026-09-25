package lv426.compiler.frontend

import HopeLangLexer
import HopeLangParser
import lv426.compiler.ast.SourceNode
import org.antlr.v4.runtime.*

object FrontendPipeline {

    class HopeSyntaxException(message: String) : RuntimeException(message)

    fun parse(sourceCode: String): SourceNode {
        val charStream = CharStreams.fromString(sourceCode)
        val lexer = HopeLangLexer(charStream)

        lexer.removeErrorListeners()
        lexer.addErrorListener(object : BaseErrorListener() {
            override fun syntaxError(
                r: Recognizer<*, *>?, off: Any?, line: Int, col: Int, msg: String?, e: RecognitionException?
            ) {
                throw HopeSyntaxException("Lexer error at $line:$col: $msg")
            }
        })

        val tokenStream = CommonTokenStream(lexer)
        val parser = HopeLangParser(tokenStream)

        parser.removeErrorListeners()
        parser.addErrorListener(object : BaseErrorListener() {
            override fun syntaxError(
                r: Recognizer<*, *>?, off: Any?, line: Int, col: Int, msg: String?, e: RecognitionException?
            ) {
                throw HopeSyntaxException("Parser error at $line:$col: $msg")
            }
        })

        val cst = parser.source()
        val visitor = AstBuilderVisitor()
        return visitor.visit(cst) as SourceNode
    }
}