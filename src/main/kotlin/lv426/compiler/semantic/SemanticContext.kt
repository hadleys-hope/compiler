package lv426.compiler.semantic

class SemanticContext(
    val symbols: SymbolTable,
    val diagnosticBuilder: DiagnosticBuilder
) {
    var currentScope: Scope = symbols.globalScope
    var currentFunction: FunctionSymbol? = null
    var loopDepth: Int = 0

    fun enterScope() {
        currentScope = Scope(currentScope)
    }

    fun leaveScope() {
        currentScope = currentScope.parent ?: error("Cannot leave global scope")
    }

    fun enterLoop() { loopDepth++ }

    fun leaveLoop() {
        check(loopDepth > 0) { "Cannot leave loop when not inside one" }
        loopDepth--
    }

    fun isInsideLoop(): Boolean = loopDepth > 0
}
