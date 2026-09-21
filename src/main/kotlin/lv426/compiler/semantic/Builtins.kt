package lv426.compiler.semantic

object Builtins {
    fun install(symbols: SymbolTable){
        symbols.declare(
            FunctionSymbol(
                name = "random_int",
                parameters = listOf(
                    FunctionParameter("min", IntType),
                    FunctionParameter("max", IntType)
                ), returnType = IntType
            )
        )
        symbols.declare(
            FunctionSymbol(
                name = "random_real",
                parameters = listOf(
                    FunctionParameter("min", RealType),
                    FunctionParameter("max", RealType)
                ),
                returnType = RealType
            )
        )

        symbols.declare(
            FunctionSymbol(
                name = "sqrt",
                parameters = listOf(
                    FunctionParameter("value", RealType)
                ),
                returnType = RealType
            )
        )

        symbols.declare(
            FunctionSymbol(
                name = "sin",
                parameters = listOf(
                    FunctionParameter("value", RealType)
                ),
                returnType = RealType
            )
        )

        symbols.declare(
            FunctionSymbol(
                name = "cos",
                parameters = listOf(
                    FunctionParameter("value", RealType)
                ),
                returnType = RealType
            )
        )

        symbols.declare(
            FunctionSymbol(
                name = "log",
                parameters = listOf(
                    FunctionParameter("message", StringType)
                ),
                returnType = VoidType
            )
        )
    }
}