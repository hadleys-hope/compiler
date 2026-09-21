package lv426.compiler.semantic

import lv426.compiler.semantic.TypeRules
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeRulesTest {

    @Test
    fun `int assignable to real`() {
        assertTrue(
            TypeRules.isAssignable(
                RealType,
                IntType
            )
        )
    }

    @Test
    fun `real is not assignable to int`() {
        assertFalse(
            TypeRules.isAssignable(
                IntType,
                RealType
            )
        )
    }

    @Test
    fun `int plus int returns int`() {
        assertEquals(
            IntType,
            TypeRules.arithmeticResult(
                IntType,
                IntType
            )
        )
    }

    @Test
    fun `int plus real returns real`() {
        assertEquals(
            RealType,
            TypeRules.arithmeticResult(
                IntType,
                RealType
            )
        )
    }

    @Test
    fun `bool arithmetic is invalid`() {
        assertEquals(
            null,
            TypeRules.arithmeticResult(
                BoolType,
                IntType
            )
        )
    }
}