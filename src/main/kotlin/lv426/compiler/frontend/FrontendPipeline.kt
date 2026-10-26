package lv426.compiler.frontend

import HopeLangLexer
import HopeLangParser
import lv426.compiler.ast.SourceNode
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.misc.ParseCancellationException

object FrontendPipeline {

    class HopeSyntaxException(message: String) : IllegalArgumentException(message)

    fun parse(sourceCode: String): SourceNode {
        val t0 = System.nanoTime()

        val charStream = CharStreams.fromString(sourceCode)
        val lexer = HopeLangLexer(charStream)
        val t1 = System.nanoTime()

        val tokenStream = CommonTokenStream(lexer)
        val parser = HopeLangParser(tokenStream)
        val t2 = System.nanoTime()

        parser.removeErrorListeners()
        // 1. Попытка сверхбыстрого SLL-парсинга
        parser.interpreter.predictionMode = PredictionMode.SLL
        parser.errorHandler = BailErrorStrategy()

        val cst = try {
            parser.source()
        } catch (e: ParseCancellationException) {
            // Если была грамматическая неоднозначность — мягко откатываемся на LL
            tokenStream.seek(0)
            parser.reset()
            parser.errorHandler = DefaultErrorStrategy()
            parser.interpreter.predictionMode = PredictionMode.LL

            parser.addErrorListener(object : BaseErrorListener() {
                override fun syntaxError(
                    r: Recognizer<*, *>?, off: Any?, line: Int, col: Int, msg: String?, ex: RecognitionException?
                ) {
                    throw HopeSyntaxException("Parser error at $line:${col + 1}: $msg")
                }
            })
            parser.source()
        }

        val t3 = System.nanoTime()

        val ast = AstBuilderVisitor().visitSource(cst)
        val t4 = System.nanoTime()

        println("  [Детализация холодного парсинга]:")
        println("    - Инициализация Lexer:   %.2f мс".format((t1 - t0) / 1_000_000.0))
        println("    - Инициализация Parser:  %.2f мс (распаковка графа ATN)".format((t2 - t1) / 1_000_000.0))
        println("    - Разбор синтаксиса:     %.2f мс".format((t3 - t2) / 1_000_000.0))
        println("    - Построение AST-дерева: %.2f мс".format((t4 - t3) / 1_000_000.0))

        return ast
    }
}