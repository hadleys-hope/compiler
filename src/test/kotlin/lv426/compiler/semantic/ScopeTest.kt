package lv426.compiler.semantic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScopeTest {

    @Test
    fun `child resolves parent symbol`() {

        val global =
            Scope()

        global.declare(
            VariableSymbol(
                "houses",
                IntType
            )
        )

        val child =
            Scope(global)

        val symbol =
            child.resolve("houses")

        assertTrue(
            symbol is VariableSymbol
        )

        assertEquals(
            IntType,
            symbol.type
        )
    }

    @Test
    fun `symbol outside scope is invisible`() {

        val global =
            Scope()

        val child =
            Scope(global)

        child.declare(
            VariableSymbol(
                "x",
                IntType
            )
        )

        assertNull(
            global.resolve("x")
        )
    }

    @Test
    fun `duplicate declaration rejected`() {

        val scope =
            Scope()

        assertTrue(
            scope.declare(
                VariableSymbol(
                    "x",
                    IntType
                )
            )
        )

        assertEquals(
            false,
            scope.declare(
                VariableSymbol(
                    "x",
                    RealType
                )
            )
        )
    }
}