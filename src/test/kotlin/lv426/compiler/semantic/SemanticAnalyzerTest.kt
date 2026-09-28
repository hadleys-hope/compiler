package lv426.compiler.semantic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import lv426.compiler.ast.*

class SemanticAnalyzerTest {
    private fun type(type: PrimitiveType) = PrimitiveTypeNode(type)

    @Test
    fun `resolves named array size enum and struct field`() {
        val source = SourceNode("test", listOf(
            ConstDeclNode("COUNT", type(PrimitiveType.INT), IntLiteralNode(3)),
            EnumDeclNode("Mode", listOf("ON", "OFF")),
            StructDeclNode("House", listOf(FieldDeclNode("temperature", type(PrimitiveType.REAL), null))),
            GlobalVarDeclNode("houses", ArrayTypeNode(CustomTypeNode("House"), IdentArraySizeNode("COUNT")), null),
            GlobalVarDeclNode("mode", CustomTypeNode("Mode"), FieldAccessExprNode(VarExprNode("Mode"), "ON")),
            FunctionDeclNode("read", listOf(ParamNode("i", type(PrimitiveType.INT))), type(PrimitiveType.REAL), listOf(
                ReturnStmtNode(FieldAccessExprNode(IndexAccessExprNode(VarExprNode("houses"), VarExprNode("i")), "temperature"))
            ))
        ))

        val result = SemanticAnalyzer().analyze(source)
        assertTrue(result.isValid, result.diagnostics.joinToString("\n"))
        assertEquals(3L, result.constantIntValues["COUNT"])
        assertEquals(ArrayType(StructType("House"), 3), (result.symbol.resolve("houses") as VariableSymbol).type)
    }

    @Test
    fun `rejects constant assignment break outside loop and missing return`() {
        val source = SourceNode("bad", listOf(
            ConstDeclNode("X", type(PrimitiveType.INT), IntLiteralNode(1)),
            FunctionDeclNode("bad", emptyList(), type(PrimitiveType.INT), listOf(
                AssignmentStmtNode(VarExprNode("X"), AssignOp.ASSIGN, IntLiteralNode(2)),
                BreakStmtNode()
            ))
        ))

        val result = SemanticAnalyzer().analyze(source)
        assertFalse(result.isValid)
        val text = result.diagnostics.joinToString("\n") { it.message }
        assertTrue("Cannot assign to constant 'X'" in text)
        assertTrue("break is only allowed inside a loop" in text)
        assertTrue("may finish without returning int" in text)
    }

    @Test
    fun `checks generic list intrinsics`() {
        val push = CallExprNode(VarExprNode("push"), listOf(VarExprNode("items"), IntLiteralNode(1)))
        val size = CallExprNode(VarExprNode("size"), listOf(VarExprNode("items")))
        val source = SourceNode("lists", listOf(
            GlobalVarDeclNode("items", ListTypeNode(type(PrimitiveType.INT)), null),
            FunctionDeclNode("f", emptyList(), type(PrimitiveType.INT), listOf(
                ExprStmtNode(push),
                ReturnStmtNode(size)
            ))
        ))

        val result = SemanticAnalyzer().analyze(source)
        assertTrue(result.isValid, result.diagnostics.joinToString("\n"))
        assertEquals(VoidType, result.typeOf(push))
        assertEquals(IntType, result.typeOf(size))
    }
    @Test
    fun `rejects constant zero for step`() {
        val source = SourceNode("loop", listOf(
            ConstDeclNode("STEP", type(PrimitiveType.INT), IntLiteralNode(0)),
            FunctionDeclNode("f", emptyList(), type(PrimitiveType.VOID), listOf(
                ForStmtNode("i", IntLiteralNode(0), IntLiteralNode(10), VarExprNode("STEP"), emptyList())
            ))
        ))

        val result = SemanticAnalyzer().analyze(source)
        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { it.message == "For-loop step cannot be zero" })
    }

    @Test
    fun `rejects duration that cannot be represented as whole milliseconds`() {
        val source = SourceNode("time", listOf(
            EveryHandlerNode(DurationLiteralNode(0.5, TimeUnit.MS), emptyList())
        ))

        val result = SemanticAnalyzer().analyze(source)
        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { "exact whole number of milliseconds" in it.message })
    }

    @Test
    fun `rejects non finite real literal`() {
        val source = SourceNode("real", listOf(
            GlobalVarDeclNode("x", type(PrimitiveType.REAL), RealLiteralNode(Double.POSITIVE_INFINITY))
        ))

        val result = SemanticAnalyzer().analyze(source)
        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { it.message == "Real literal must be finite" })
    }

}
