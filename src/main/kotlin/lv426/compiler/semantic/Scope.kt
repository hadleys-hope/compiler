package lv426.compiler.semantic

class Scope (
    val parent: Scope? = null
) {
    private val symbols = linkedMapOf<String, Symbol>()

    fun declare (symbol: Symbol) : Boolean {
        if (symbols.containsKey(symbol.name)) {
            return false
        }

        symbols[symbol.name] = symbol
        return true
    }

    fun resolvedLocal (name: String): Symbol? = symbols[name]
    fun resolve (name: String): Symbol? = symbols[name]  ?: parent?.resolve(name)
    fun allLocalSymbols(): Collection<Symbol> = symbols.values
}