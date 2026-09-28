package lv426.compiler.codegen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import lv426.compiler.ast.*
import lv426.compiler.backend.BytecodeProbe
import lv426.compiler.backend.HbcBackend
import lv426.compiler.backend.HbcReader
import lv426.compiler.semantic.SemanticAnalyzer

class LanguageExecutionCoverageTest {
    private fun type(type: PrimitiveType) = PrimitiveTypeNode(type)
    private fun call(name: String, vararg args: ExprNode) = CallExprNode(VarExprNode(name), args.toList())

    private fun compile(source: SourceNode): BytecodeProbe {
        val semantic = SemanticAnalyzer().analyze(source)
        assertTrue(semantic.isValid, semantic.diagnostics.joinToString("\n"))
        return BytecodeProbe(HbcReader.read(HbcBackend.compileToBytes(IrLowering(semantic).lower())))
    }

    @Test
    fun `while compound assignment and integer arithmetic execute`() {
        val source = SourceNode("while", listOf(
            FunctionDeclNode("sum", emptyList(), type(PrimitiveType.INT), listOf(
                LocalVarDeclNode("n", type(PrimitiveType.INT), IntLiteralNode(3)),
                LocalVarDeclNode("result", type(PrimitiveType.INT), IntLiteralNode(0)),
                WhileStmtNode(
                    BinaryExprNode(VarExprNode("n"), BinaryOp.GT, IntLiteralNode(0)),
                    listOf(
                        AssignmentStmtNode(VarExprNode("result"), AssignOp.PLUS_ASSIGN, VarExprNode("n")),
                        AssignmentStmtNode(VarExprNode("n"), AssignOp.MINUS_ASSIGN, IntLiteralNode(1))
                    )
                ),
                ReturnStmtNode(VarExprNode("result"))
            ))
        ))
        assertEquals(6L, compile(source).run("sum"))
    }

    @Test
    fun `for supports continue break negative step and dynamic zero safely`() {
        val positive = FunctionDeclNode(
            "positive",
            listOf(ParamNode("step", type(PrimitiveType.INT))),
            type(PrimitiveType.INT),
            listOf(
                LocalVarDeclNode("sum", type(PrimitiveType.INT), IntLiteralNode(0)),
                ForStmtNode("i", IntLiteralNode(0), IntLiteralNode(5), VarExprNode("step"), listOf(
                    IfStmtNode(listOf(IfBranchNode(
                        BinaryExprNode(VarExprNode("i"), BinaryOp.EQ, IntLiteralNode(2)),
                        listOf(ContinueStmtNode())
                    )), null),
                    IfStmtNode(listOf(IfBranchNode(
                        BinaryExprNode(VarExprNode("i"), BinaryOp.EQ, IntLiteralNode(4)),
                        listOf(BreakStmtNode())
                    )), null),
                    AssignmentStmtNode(VarExprNode("sum"), AssignOp.PLUS_ASSIGN, VarExprNode("i"))
                )),
                ReturnStmtNode(VarExprNode("sum"))
            )
        )
        val negative = FunctionDeclNode(
            "negative", emptyList(), type(PrimitiveType.INT), listOf(
                LocalVarDeclNode("sum", type(PrimitiveType.INT), IntLiteralNode(0)),
                ForStmtNode(
                    "i", IntLiteralNode(3), IntLiteralNode(0), UnaryExprNode(UnaryOp.MINUS, IntLiteralNode(1)),
                    listOf(AssignmentStmtNode(VarExprNode("sum"), AssignOp.PLUS_ASSIGN, VarExprNode("i")))
                ),
                ReturnStmtNode(VarExprNode("sum"))
            )
        )
        val vm = compile(SourceNode("for", listOf(positive, negative)))
        assertEquals(4L, vm.run("positive", listOf(1L)))
        assertEquals(0L, vm.run("positive", listOf(0L)))
        assertEquals(6L, vm.run("negative"))
    }

    @Test
    fun `logical operators short circuit side effects`() {
        val source = SourceNode("logical", listOf(
            GlobalVarDeclNode("count", type(PrimitiveType.INT), IntLiteralNode(0)),
            FunctionDeclNode("bump", emptyList(), type(PrimitiveType.BOOL), listOf(
                AssignmentStmtNode(VarExprNode("count"), AssignOp.PLUS_ASSIGN, IntLiteralNode(1)),
                ReturnStmtNode(BoolLiteralNode(true))
            )),
            FunctionDeclNode("run", emptyList(), type(PrimitiveType.INT), listOf(
                IfStmtNode(listOf(IfBranchNode(
                    BinaryExprNode(BoolLiteralNode(false), BinaryOp.AND, call("bump")),
                    emptyList()
                )), null),
                IfStmtNode(listOf(IfBranchNode(
                    BinaryExprNode(BoolLiteralNode(true), BinaryOp.OR, call("bump")),
                    emptyList()
                )), null),
                ReturnStmtNode(VarExprNode("count"))
            ))
        ))
        assertEquals(0L, compile(source).run("run"))
    }

    @Test
    fun `enum values compare and list remove returns removed value`() {
        val source = SourceNode("values", listOf(
            EnumDeclNode("Mode", listOf("OFF", "ON")),
            GlobalVarDeclNode("mode", CustomTypeNode("Mode"), FieldAccessExprNode(VarExprNode("Mode"), "ON")),
            GlobalVarDeclNode("values", ListTypeNode(type(PrimitiveType.INT)), null),
            FunctionDeclNode("run", emptyList(), type(PrimitiveType.INT), listOf(
                ExprStmtNode(call("push", VarExprNode("values"), IntLiteralNode(10))),
                ExprStmtNode(call("push", VarExprNode("values"), IntLiteralNode(20))),
                IfStmtNode(listOf(IfBranchNode(
                    BinaryExprNode(
                        VarExprNode("mode"), BinaryOp.EQ,
                        FieldAccessExprNode(VarExprNode("Mode"), "ON")
                    ),
                    listOf(ReturnStmtNode(call("remove_at", VarExprNode("values"), IntLiteralNode(0))))
                )), listOf(ReturnStmtNode(IntLiteralNode(-1))))
            ))
        ))
        assertEquals(10L, compile(source).run("run"))
    }

    @Test
    fun `mixed numeric arithmetic widens ints before real operations`() {
        val source = SourceNode("numeric", listOf(
            FunctionDeclNode("value", emptyList(), type(PrimitiveType.REAL), listOf(
                ReturnStmtNode(BinaryExprNode(
                    BinaryExprNode(IntLiteralNode(3), BinaryOp.MUL, RealLiteralNode(2.5)),
                    BinaryOp.PLUS,
                    IntLiteralNode(1)
                ))
            ))
        ))
        assertEquals(8.5, compile(source).run("value"))
    }
}
