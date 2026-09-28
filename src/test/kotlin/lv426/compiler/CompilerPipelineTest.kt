package lv426.compiler

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import lv426.compiler.backend.HbcDisassembler
import lv426.compiler.backend.HbcReader

class CompilerPipelineTest {
    @Test
    fun `source compiles through frontend semantic lowering and backend`() {
        val source = """
            program integration

            const HOUSE_COUNT of int = 2

            struct House
                temperature of real = 20.0
            end

            var houses of House[HOUSE_COUNT]

            event Alarm(house_id of int)

            def warm(i of int) of void
                houses[i].temperature += 1
            end

            on start
                warm(0)
                emit Alarm(1)
            end

            every 1 min
                warm(1)
            end
        """.trimIndent()

        val bytes = CompilerPipeline.compileToBytes(source)
        val module = HbcReader.read(bytes)
        assertContentEquals(bytes, module.toBytes())
        assertEquals(1, module.structs.size)
        assertEquals(2, module.handlers.size)
        assertTrue("INT_TO_REAL" in HbcDisassembler.disassemble(module))
    }
}
