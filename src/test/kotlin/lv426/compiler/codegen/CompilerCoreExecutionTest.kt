package lv426.compiler.codegen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import lv426.compiler.ast.*
import lv426.compiler.backend.BytecodeProbe
import lv426.compiler.backend.HbcBackend
import lv426.compiler.backend.HbcReader
import lv426.compiler.semantic.SemanticAnalyzer

class CompilerCoreExecutionTest {
    private fun type(type: PrimitiveType) = PrimitiveTypeNode(type)
    private fun house(index: ExprNode): ExprNode = IndexAccessExprNode(VarExprNode("houses"), index)
    private fun field(target: ExprNode, name: String): ExprNode = FieldAccessExprNode(target, name)

    @Test
    fun `checked AST lowers to executable aggregates events and numeric widening`() {
        val source = SourceNode("integration", listOf(
            StructDeclNode("House", listOf(
                FieldDeclNode("temperature", type(PrimitiveType.REAL), RealLiteralNode(20.0)),
                FieldDeclNode("hits", type(PrimitiveType.INT), IntLiteralNode(0)),
                FieldDeclNode("samples", ListTypeNode(type(PrimitiveType.INT)), null)
            )),
            GlobalVarDeclNode(
                "houses",
                ArrayTypeNode(CustomTypeNode("House"), IntArraySizeNode(2)),
                null
            ),
            EventDeclNode("Alarm", listOf(ParamNode("house_id", type(PrimitiveType.INT)))),
            FunctionDeclNode(
                "update",
                listOf(ParamNode("i", type(PrimitiveType.INT))),
                type(PrimitiveType.VOID),
                listOf(
                    AssignmentStmtNode(
                        field(house(VarExprNode("i")), "temperature"),
                        AssignOp.PLUS_ASSIGN,
                        IntLiteralNode(1)
                    ),
                    AssignmentStmtNode(
                        field(house(VarExprNode("i")), "hits"),
                        AssignOp.PLUS_ASSIGN,
                        IntLiteralNode(1)
                    ),
                    ExprStmtNode(CallExprNode(
                        VarExprNode("push"),
                        listOf(field(house(VarExprNode("i")), "samples"), IntLiteralNode(7))
                    ))
                )
            ),
            FunctionDeclNode("temperature", emptyList(), type(PrimitiveType.REAL), listOf(
                ReturnStmtNode(field(house(IntLiteralNode(0)), "temperature"))
            )),
            FunctionDeclNode("hits", emptyList(), type(PrimitiveType.INT), listOf(
                ReturnStmtNode(field(house(IntLiteralNode(0)), "hits"))
            )),
            FunctionDeclNode("sample_count", emptyList(), type(PrimitiveType.INT), listOf(
                ReturnStmtNode(CallExprNode(
                    VarExprNode("size"),
                    listOf(field(house(IntLiteralNode(0)), "samples"))
                ))
            )),
            StartHandlerNode(listOf(
                ExprStmtNode(CallExprNode(VarExprNode("update"), listOf(IntLiteralNode(0)))),
                EmitStmtNode("Alarm", listOf(IntLiteralNode(0)))
            )),
            EventHandlerNode("Alarm", listOf("house_id"), listOf(
                ExprStmtNode(CallExprNode(VarExprNode("update"), listOf(VarExprNode("house_id"))))
            ))
        ))

        val semantic = SemanticAnalyzer().analyze(source)
        assertTrue(semantic.isValid, semantic.diagnostics.joinToString("\n"))
        val module = HbcReader.read(HbcBackend.compileToBytes(IrLowering(semantic).lower()))
        val vm = BytecodeProbe(module)
        vm.runStartHandlers()

        assertEquals(22.0, vm.run("temperature"))
        assertEquals(2L, vm.run("hits"))
        assertEquals(2L, vm.run("sample_count"))
    }
}
