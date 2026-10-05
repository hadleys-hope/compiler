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
        // house I/O, provided by the runtime that hosts the program (vm: src/host/HouseIo.hpp)
        fixed("sense", listOf(StringType), RealType) // a sensor reading; booleans are 0.0 / 1.0, unknown is 0.0
        fixed("act", listOf(StringType, RealType), VoidType) // set an actuator for this house
        fixed("act_text", listOf(StringType, StringType), VoidType) // a text actuator: program name, reason
        fixed("house_id", emptyList(), IntType)
        symbols.declare(IntrinsicSymbol("size"))
        symbols.declare(IntrinsicSymbol("push"))
        symbols.declare(IntrinsicSymbol("remove_at"))
    }
}
