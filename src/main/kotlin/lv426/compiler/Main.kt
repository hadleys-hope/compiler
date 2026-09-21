package lv426.compiler

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess
import lv426.compiler.backend.BackendExample
import lv426.compiler.backend.HbcBackend
import lv426.compiler.backend.HbcDisassembler
import lv426.compiler.backend.HbcReader

/** Backend smoke-test CLI. Parsing Hope source is a separate frontend responsibility. */
fun main(args: Array<String>) {
    if (args.isEmpty() || args.contentEquals(arrayOf("--help"))) {
        println("HBC backend: --demo <output.hbc> | --dump <input.hbc>")
        return
    }
    try {
        require(args.size == 2) { "Expected --demo <output.hbc> or --dump <input.hbc>" }
        val path = Path.of(args[1])
        when (args[0]) {
            "--demo" -> {
                HbcBackend.write(BackendExample.program(), path)
                println("Created ${path.toAbsolutePath()}")
            }
            "--dump" -> println(HbcDisassembler.disassemble(HbcReader.read(Files.readAllBytes(path))))
            else -> throw IllegalArgumentException("Unknown option '${args[0]}'; use --help")
        }
    } catch (error: IllegalArgumentException) {
        System.err.println("HBC error: ${error.message}")
        exitProcess(1)
    } catch (error: IOException) {
        System.err.println("I/O error: ${error.message}")
        exitProcess(1)
    }
}
