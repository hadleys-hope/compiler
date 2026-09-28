package lv426.compiler.semantic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BuiltinsTest {

    @Test
    fun `builtins are installed`() {

        val table =
            SymbolTable()

        Builtins.install(table)

        val sqrt =
            table.findFunction("sqrt")

        assertNotNull(sqrt)

        assertEquals(
            RealType,
            sqrt.returnType
        )

        assertEquals(
            listOf(RealType),
            sqrt.parameters.map { it.type }
        )
    }
}