package lv426.compiler

import lv426.compiler.ast.ProgramNode
import lv426.compiler.parser.SmartHomeDSLLexer
import lv426.compiler.parser.SmartHomeDSLParser
import lv426.compiler.visitor.AstBuilderVisitor
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

object CompilerPipeline {

    class CompilerSyntaxException(message: String) : RuntimeException(message)

    fun parseToAst(sourceCode: String): ProgramNode {
        val charStream = CharStreams.fromString(sourceCode)
        val lexer = SmartHomeDSLLexer(charStream)

        // Прерываем работу сразу при первой лексической ошибке
        lexer.removeErrorListeners()
        lexer.addErrorListener(object : BaseErrorListener() {
            override fun syntaxError(
                recognizer: Recognizer<*, *>?,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String?,
                e: RecognitionException?
            ) {
                throw CompilerSyntaxException("Lexer error at $line:$charPositionInLine: $msg")
            }
        })

        val tokenStream = CommonTokenStream(lexer)
        val parser = SmartHomeDSLParser(tokenStream)

        // Прерываем работу сразу при первой синтаксической ошибке
        parser.removeErrorListeners()
        parser.addErrorListener(object : BaseErrorListener() {
            override fun syntaxError(
                recognizer: Recognizer<*, *>?,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String?,
                e: RecognitionException?
            ) {
                throw CompilerSyntaxException("Parser error at $line:$charPositionInLine: $msg")
            }
        })

        val parseTree = parser.program()
        val visitor = AstBuilderVisitor()
        return visitor.visit(parseTree) as ProgramNode
    }
}

fun main() {
    val sampleHopeProgram = """
        sensor   temp_in:  float;
        sensor   temp_out: float;
        sensor   power_ok: bool;
        sensor   battery:  float;
        sensor   motion:   bool;

        actuator heater: bool;
        actuator ups:    bool;
        actuator lights: bool;

        const  target:     float = 20.0;
        var    cold_ticks: int   = 0;

        on init {
            heater = on;
            log("house online, target ", target);
        }

        on change(power_ok) {
            ups = not power_ok;
            log("grid ok=", power_ok, " ups=", ups);
        }

        on change(motion) {
            lights = motion and is_night();
        }

        on time(23:00) {
            lights = off;
        }

        on tick {
            if temp_in < target - 1.0 { 
                heater = on; 
            }
            if temp_in > target + 1.0 { 
                heater = off; 
            }

            if ups and battery < 20.0 {
                heater = off;
                log("battery low, heater off to save the pump");
            }

            if temp_in < 5.0 { 
                cold_ticks = cold_ticks + 1; 
            } else { 
                cold_ticks = 0; 
            }
            
            if cold_ticks == 30 { 
                log("freeze warning, 30 minutes below 5C"); 
            }
        }
    """.trimIndent()

    try {
        println("Compiling smart house module for LV-426...")
        val ast = CompilerPipeline.parseToAst(sampleHopeProgram)
        println("AST built successfully!")
        println("Declarations count: ${ast.declarations.size}")
        println("Handlers count: ${ast.handlers.size}")
        println("\n--- Program AST Root Dump ---")
        println(ast)
    } catch (e: CompilerPipeline.CompilerSyntaxException) {
        System.err.println("Compilation failed:\n${e.message}")
    }
}