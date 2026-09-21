package lv426.compiler.semantic

class SymbolTable {
    val globalScope = Scope()

    fun declare(symbol: Symbol) : Boolean = globalScope.declare(symbol)
    fun resolve(name: String): Symbol? = globalScope.resolve(name)
    fun findStruct(name: String) : StructSymbol? = globalScope.resolve(name) as? StructSymbol
    fun findEnum(name: String) : EnumSymbol? = globalScope.resolve(name) as? EnumSymbol
    fun findFunction(name: String) : FunctionSymbol? = globalScope.resolve(name) as? FunctionSymbol
    fun fundEvent(name: String) : EventSymbol? = globalScope.resolve(name) as? EventSymbol
}