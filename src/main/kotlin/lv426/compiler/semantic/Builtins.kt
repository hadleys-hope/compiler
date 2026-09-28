package lv426.compiler.semantic

object Builtins {
    fun install(symbols: SymbolTable) {
        fun fixed(name: String, parameters: List<HopeType>, result: HopeType) {
            symbols.declare(FunctionSymbol(name, parameters.mapIndexed { i, type -> FunctionParameter("arg$i", type) }, result))
        }

        fixed("random_int", listOf(IntType, IntType), IntType)
        fixed("random_real", listOf(RealType, RealType), RealType)
        fixed("sqrt", listOf(RealType), RealType)
        fixed("sin", listOf(RealType), RealType)
        fixed("cos", listOf(RealType), RealType)
        fixed("abs", listOf(RealType), RealType)
        fixed("minimum", listOf(RealType, RealType), RealType)
        fixed("maximum", listOf(RealType, RealType), RealType)
        fixed("clamp", listOf(RealType, RealType, RealType), RealType)
        fixed("log", listOf(StringType), VoidType)
        fixed("metric", listOf(StringType, RealType), VoidType)
        symbols.declare(IntrinsicSymbol("size"))
        symbols.declare(IntrinsicSymbol("push"))
        symbols.declare(IntrinsicSymbol("remove_at"))
    }
}
