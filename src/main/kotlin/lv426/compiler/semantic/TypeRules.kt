package lv426.compiler.semantic

object TypeRules {
    fun isNumeric(type: HopeType): Boolean = type == IntType || type == RealType

    fun isAssignable(target: HopeType, source: HopeType): Boolean{
        if (target == source){
            return true
        }
        if (target == RealType && source == IntType){
            return true
        }
        return false
    }

    fun arithmeticResult (left: HopeType, right: HopeType): HopeType?{
        if (!isNumeric(left) || !isNumeric(right)){
            return null
        }

        return if (left == RealType || right == RealType){
            RealType
        } else {
            IntType
        }
    }

    fun equalityAllowed(left: HopeType, right: HopeType): Boolean{
        if (left == right){
            return true
        }
        return isNumeric(left) && isNumeric(right)
    }

    fun orderedComparisonAllowed(left: HopeType, right: HopeType): Boolean = isNumeric(left) && isNumeric(right)

    fun isBoolean(type: HopeType): Boolean = type == BoolType

    fun commonNumericType(
        left: HopeType,
        right: HopeType
    ): HopeType? {

        if (!isNumeric(left) || !isNumeric(right)) {
            return null
        }

        return if (
            left == RealType ||
            right == RealType
        ) {
            RealType
        } else {
            IntType
        }
    }
}