package lv426.compiler.backend

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/** An independent writer for deliberately malformed files; bypasses production validation. */
internal data class RawHbcFunction(
    val code: ByteArray = rawCode(0x60),
    val name: Int = 0,
    val parameters: Int = 0,
    val locals: Int = parameters,
    val declaredSize: Int = code.size
)

internal fun rawCode(vararg bytes: Int): ByteArray = ByteArray(bytes.size) { bytes[it].toByte() }

internal fun encodedBytes(write: DataOutputStream.() -> Unit): ByteArray =
    ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { it.write() }
        bytes.toByteArray()
    }

internal fun rawString(value: String): ByteArray = encodedBytes {
    val utf8 = value.toByteArray(Charsets.UTF_8)
    writeByte(4)
    writeShort(utf8.size)
    write(utf8)
}

internal fun rawHbc(
    constants: List<ByteArray> = listOf(rawString("main")),
    functions: List<RawHbcFunction> = listOf(RawHbcFunction()),
    extension: ByteArray = byteArrayOf()
): ByteArray = encodedBytes {
    write(rawCode(0x48, 0x42, 0x43, 0))
    writeShort(1)
    writeShort(constants.size)
    constants.forEach { write(it) }
    writeShort(functions.size)
    functions.forEach {
        writeShort(it.name)
        writeShort(it.parameters)
        writeShort(it.locals)
        writeInt(it.declaredSize)
        write(it.code)
    }
    write(extension)
}

/** Exercises all extended metadata and nested aggregate operations through the public IR. */
internal fun extendedProgram(): IrProgram {
    fun integer(value: Long) = IrInstruction.Push(HbcConstant.IntValue(value))
    val int = HbcType.IntType
    val functions = listOf(
        IrFunction("initBase", 0, 0, listOf(integer(5), IrInstruction.Return(true))),
        IrFunction("initState", 0, 0, listOf(
            IrInstruction.LoadGlobal("base"), integer(10), integer(20), IrInstruction.NewList(int, 2),
            IrInstruction.NewStruct("State"), IrInstruction.Return(true)
        )),
        IrFunction("main", 0, 1, listOf(
            IrInstruction.LoadGlobal("state"), IrInstruction.LoadField("State", "samples"), IrInstruction.StoreLocal(0),
            IrInstruction.LoadLocal(0), integer(1), integer(42), IrInstruction.StoreIndex,
            IrInstruction.LoadLocal(0), integer(7), IrInstruction.ListAppend,
            IrInstruction.LoadLocal(0), integer(0), IrInstruction.ListRemove, IrInstruction.Pop,
            IrInstruction.LoadGlobal("state"), integer(99), IrInstruction.StoreField("State", "x"),
            IrInstruction.LoadLocal(0), integer(0), IrInstruction.LoadIndex, IrInstruction.Return(true)
        )),
        IrFunction("start", 0, 0, listOf(
            IrInstruction.LoadGlobal("base"), IrInstruction.Emit("changed", 1), IrInstruction.Return()
        )),
        IrFunction("changedHandler", 1, 1, listOf(
            IrInstruction.LoadGlobal("state"), IrInstruction.LoadLocal(0),
            IrInstruction.StoreField("State", "x"), IrInstruction.Return()
        )),
        IrFunction("tick", 0, 0, listOf(
            IrInstruction.LoadGlobal("state"), IrInstruction.LoadGlobal("state"),
            IrInstruction.LoadField("State", "x"), integer(1), IrInstruction.Binary(BinaryOperation.ADD),
            IrInstruction.StoreField("State", "x"), IrInstruction.Return()
        )),
        IrFunction("deadline", 0, 0, listOf(
            integer(123), IrInstruction.NewList(int), IrInstruction.NewStruct("State"),
            IrInstruction.StoreGlobal("state"), IrInstruction.Return()
        )),
        IrFunction("readX", 0, 0, listOf(
            IrInstruction.LoadGlobal("state"), IrInstruction.LoadField("State", "x"), IrInstruction.Return(true)
        )),
        IrFunction("array", 0, 1, listOf(
            integer(8), integer(9), IrInstruction.NewArray(int, 2), IrInstruction.StoreLocal(0),
            IrInstruction.LoadLocal(0), integer(0), integer(70), IrInstruction.StoreIndex,
            IrInstruction.LoadLocal(0), integer(0), IrInstruction.LoadIndex,
            IrInstruction.LoadLocal(0), IrInstruction.Length,
            IrInstruction.Binary(BinaryOperation.ADD), IrInstruction.Return(true)
        ))
    )
    return IrProgram(functions,
        structs = listOf(IrStruct("State", listOf(IrField("x", int), IrField("samples", HbcType.ListType(int))))),
        globals = listOf(IrGlobal("base", int, "initBase", mutable = false), IrGlobal("state", HbcType.Struct("State"), "initState")),
        events = listOf(IrEvent("changed", listOf(int))),
        handlers = listOf(IrHandler.Start("start"), IrHandler.Event("changed", "changedHandler"),
            IrHandler.Every(1000, "tick"), IrHandler.At(5000, "deadline"))
    )
}
