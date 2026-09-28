package lv426.compiler.codegen

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import lv426.compiler.ast.*
import lv426.compiler.backend.*
import lv426.compiler.semantic.SemanticAnalyzer

class IrLoweringTest {
    private fun type(type: PrimitiveType) = PrimitiveTypeNode(type)

    @Test
    fun `lowers aggregate metadata handlers and numeric widening`() {
        val house = StructDeclNode("House", listOf(
            FieldDeclNode("temperature", type(PrimitiveType.REAL), RealLiteralNode(20.0))
        ))
        val houses = GlobalVarDeclNode(
            "houses",
            ArrayTypeNode(CustomTypeNode("House"), IntArraySizeNode(2)),
            null
        )
        val target = FieldAccessExprNode(IndexAccessExprNode(VarExprNode("houses"), VarExprNode("i")), "temperature")
        val update = FunctionDeclNode("update", listOf(ParamNode("i", type(PrimitiveType.INT))), type(PrimitiveType.VOID), listOf(
            AssignmentStmtNode(target, AssignOp.PLUS_ASSIGN, IntLiteralNode(1))
        ))
        val event = EventDeclNode("Alarm", listOf(ParamNode("i", type(PrimitiveType.INT))))
        val start = StartHandlerNode(listOf(
            ExprStmtNode(CallExprNode(VarExprNode("update"), listOf(IntLiteralNode(0)))),
            EmitStmtNode("Alarm", listOf(IntLiteralNode(1)))
        ))
        val source = SourceNode("test", listOf(house, houses, event, update, start))
        val semantic = SemanticAnalyzer().analyze(source)
        assertTrue(semantic.isValid, semantic.diagnostics.joinToString("\n"))

        val ir = IrLowering(semantic).lower()
        val module = HbcBackend.compile(ir)
        assertEquals(1, module.structs.size)
        assertEquals(1, module.globals.size)
        assertEquals(1, module.events.size)
        assertEquals(1, module.handlers.size)
        val text = HbcDisassembler.disassemble(module)
        assertTrue("INT_TO_REAL" in text)
        assertTrue("NEW_ARRAY_INIT" in text)
    }

    @Test
    fun `metadata bytecode round trips`() {
        val source = SourceNode("timers", listOf(
            EveryHandlerNode(DurationLiteralNode(1.0, TimeUnit.MIN), emptyList()),
            AtHandlerNode(DurationLiteralNode(3.0, TimeUnit.HOUR), emptyList())
        ))
        val semantic = SemanticAnalyzer().analyze(source)
        assertTrue(semantic.isValid, semantic.diagnostics.joinToString("\n"))
        val bytes = HbcBackend.compileToBytes(IrLowering(semantic).lower())
        val restored = HbcReader.read(bytes)
        assertContentEquals(bytes, restored.toBytes())
        assertEquals(60_000L, restored.handlers[0].milliseconds)
        assertEquals(10_800_000L, restored.handlers[1].milliseconds)
    }
    @Test
    fun `lowers for loop with break and continue`() {
        val loop = ForStmtNode(
            "i", IntLiteralNode(0), IntLiteralNode(10), IntLiteralNode(1), listOf(
                IfStmtNode(
                    listOf(IfBranchNode(
                        BinaryExprNode(VarExprNode("i"), BinaryOp.EQ, IntLiteralNode(3)),
                        listOf(ContinueStmtNode())
                    )), null
                ),
                IfStmtNode(
                    listOf(IfBranchNode(
                        BinaryExprNode(VarExprNode("i"), BinaryOp.EQ, IntLiteralNode(8)),
                        listOf(BreakStmtNode())
                    )), null
                )
            )
        )
        val source = SourceNode("loops", listOf(
            FunctionDeclNode("run", emptyList(), type(PrimitiveType.VOID), listOf(loop))
        ))
        val semantic = SemanticAnalyzer().analyze(source)
        assertTrue(semantic.isValid, semantic.diagnostics.joinToString("\n"))

        val module = HbcBackend.compile(IrLowering(semantic).lower())
        val text = HbcDisassembler.disassemble(module)
        assertTrue("JUMP_IF_FALSE" in text)
        assertTrue("JUMP" in text)
    }

    @Test
    fun `dynamic zero for step lowers to safe exit guard`() {
        val loop = ForStmtNode(
            "i", IntLiteralNode(0), IntLiteralNode(10), VarExprNode("step"), emptyList()
        )
        val source = SourceNode("loops", listOf(
            FunctionDeclNode(
                "run",
                listOf(ParamNode("step", type(PrimitiveType.INT))),
                type(PrimitiveType.VOID),
                listOf(loop)
            )
        ))
        val semantic = SemanticAnalyzer().analyze(source)
        assertTrue(semantic.isValid, semantic.diagnostics.joinToString("\n"))

        val ir = IrLowering(semantic).lower()
        val function = ir.functions.single { it.name == "run" }
        val equal = function.instructions.indexOfFirst {
            it == IrInstruction.Binary(BinaryOperation.EQUAL)
        }
        assertTrue(equal >= 0)
        assertTrue(function.instructions.drop(equal).any { it is IrInstruction.Jump })
        HbcBackend.compile(ir)
    }

}
