package lv426.compiler.frontend
import lv426.compiler.ast.*
import kotlin.test.*

class FrontendPipelineTest {
    @Test
    fun `test operator precedence - mul binds tighter than add`() {
        val code = """
            def calc() of int
                return 1 + 2 * 3
            end
        """.trimIndent()

        val ast = FrontendPipeline.parse(code)
        val fn = ast.declarations.first() as FunctionDeclNode
        val ret = fn.body.first() as ReturnStmtNode
        val expr = ret.value as BinaryExprNode

        // ожидается: 1 + (2 * 3), а не (1 + 2) * 3
        assertEquals(BinaryOp.PLUS, expr.op)
        val left = expr.left as IntLiteralNode
        assertEquals(1, left.value)

        val right = expr.right as BinaryExprNode
        assertEquals(BinaryOp.MUL, right.op)
        assertEquals(2, (right.left as IntLiteralNode).value)
        assertEquals(3, (right.right as IntLiteralNode).value)
    }

    @Test
    fun `test postfix chain - houses bracket i bracket dot temperature`() {
        val code = """
            def test() of void
                houses[i].temperature = 20.0
            end
        """.trimIndent()

        val ast = FrontendPipeline.parse(code)
        val fn = ast.declarations.first() as FunctionDeclNode
        val assign = fn.body.first() as AssignmentStmtNode

        // houses[i].temperature -> FieldAccess(IndexAccess(houses, i), temperature)
        val target = assign.target as FieldAccessExprNode
        assertEquals("temperature", target.fieldName)

        val indexAccess = target.target as IndexAccessExprNode
        val base = indexAccess.target as VarExprNode
        assertEquals("houses", base.name)
        val index = indexAccess.index as VarExprNode
        assertEquals("i", index.name)
    }

    @Test
    fun `test if elif else branching`() {
        val code = """
            def test_branch(x of int) of void
                if x > 10
                    log("high")
                elif x > 0
                    log("mid")
                else
                    log("low")
                end
            end
        """.trimIndent()

        val ast = FrontendPipeline.parse(code)
        val fn = ast.declarations.first() as FunctionDeclNode
        val ifStmt = fn.body.first() as IfStmtNode

        assertEquals(2, ifStmt.branches.size, "Expected if and elif branches")
        assertNotNull(ifStmt.elseBody, "Expected else branch")
        assertEquals(1, ifStmt.elseBody!!.size)
    }

    @Test
    fun `test handlers - start, event, every, at`() {
        val code = """
            on start
                log("init")
            end

            on Alert(code)
                log(code)
            end

            every 10 sec
                tick()
            end

            at 1 hour
                shutdown()
            end
        """.trimIndent()

        val ast = FrontendPipeline.parse(code)
        assertEquals(4, ast.declarations.size)

        assertTrue(ast.declarations[0] is StartHandlerNode)

        val eventHandler = ast.declarations[1] as EventHandlerNode
        assertEquals("Alert", eventHandler.eventName)
        assertEquals(listOf("code"), eventHandler.parameters)

        val everyHandler = ast.declarations[2] as EveryHandlerNode
        assertEquals(10.0, everyHandler.duration.value)
        assertEquals(TimeUnit.SEC, everyHandler.duration.unit)

        val atHandler = ast.declarations[3] as AtHandlerNode
        assertEquals(1.0, atHandler.duration.value)
        assertEquals(TimeUnit.HOUR, atHandler.duration.unit)
    }

    @Test
    fun `test fail-fast syntax error`() {
        val invalidCode = """
            def broken(
                // нет закрывающей скобки
            end
        """.trimIndent()

        val ex = assertFailsWith<FrontendPipeline.HopeSyntaxException> {
            FrontendPipeline.parse(invalidCode)
        }
        assertTrue(ex.message!!.contains("Parser error at"), "Message should contain line/column")
    }

    @Test
    fun `test source locations are populated`() {
        val code = """
            const FOO of int = 42
        """.trimIndent()

        val ast = FrontendPipeline.parse(code)
        val constDecl = ast.declarations.first() as ConstDeclNode
        assertEquals(1, constDecl.location.line)
        assertEquals(1, constDecl.location.column)
    }

    @Test
    fun `test string literal unescape`() {
        val code = """
            const MSG of string = "Hello\nWorld\t\"quotes\""
        """.trimIndent()

        val ast = FrontendPipeline.parse(code)
        val constDecl = ast.declarations.first() as ConstDeclNode
        val str = constDecl.value as StringLiteralNode
        assertEquals("Hello\nWorld\t\"quotes\"", str.value)
    }
    @Test
    fun `integer literal overflow is reported as frontend error`() {
        val code = "const X of int = 999999999999999999999999999999999999"
        val ex = assertFailsWith<FrontendPipeline.HopeSyntaxException> {
            FrontendPipeline.parse(code)
        }
        assertTrue("Integer literal out of range" in ex.message.orEmpty())
        assertTrue("1:" in ex.message.orEmpty())
    }

}