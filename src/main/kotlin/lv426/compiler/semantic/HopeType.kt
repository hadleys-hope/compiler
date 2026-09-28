package lv426.compiler.semantic

sealed interface HopeType {
    val displayName: String
}

data object ErrorType : HopeType { override val displayName = "<error>" }

data object IntType : HopeType { override val displayName = "int" }
data object RealType : HopeType { override val displayName = "real" }
data object BoolType : HopeType { override val displayName = "bool" }
data object StringType : HopeType { override val displayName = "string" }
data object TimeType : HopeType { override val displayName = "time" }
data object VoidType : HopeType { override val displayName = "void" }

data class StructType(val name: String) : HopeType { override val displayName = name }
data class EnumType(val name: String) : HopeType { override val displayName = name }
data class ArrayType(val elementType: HopeType, val size: Int) : HopeType {
    override val displayName = "${elementType.displayName}[$size]"
}
data class ListType(val elementType: HopeType) : HopeType {
    override val displayName = "list<${elementType.displayName}>"
}
