package lv426.compiler

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess
import lv426.compiler.ast.toPrettyTree
import lv426.compiler.backend.BackendExample
import lv426.compiler.backend.HbcBackend
import lv426.compiler.backend.HbcDisassembler
import lv426.compiler.backend.HbcReader
import lv426.compiler.frontend.FrontendPipeline

fun main(args: Array<String>) {
    // Если запущено без аргументов (кнопка Run в IDEA) — запускаем твой AST Demo
    if (args.isEmpty()) {
        runFrontendDemo()
        return
    }

    if (args.contentEquals(arrayOf("--help"))) {
        println("Hope Compiler & VM CLI:")
        println("  (no args)           : Run Frontend AST demo (Hadley's Hope)")
        println("  --parse <file.hope> : Parse Hope file and dump AST")
        println("  --demo <output.hbc> : Generate sample HBC bytecode")
        println("  --dump <input.hbc>  : Disassemble HBC bytecode file")
        return
    }

    try {
        when (args[0]) {
            "--parse" -> {
                require(args.size == 2) { "Expected --parse <file.hope>" }
                val source = Files.readString(Path.of(args[1]))
                val ast = FrontendPipeline.parse(source)
                println(ast.toPrettyTree())
            }
            "--demo" -> {
                require(args.size == 2) { "Expected --demo <output.hbc>" }
                val path = Path.of(args[1])
                HbcBackend.write(BackendExample.program(), path)
                println("Created ${path.toAbsolutePath()}")
            }
            "--dump" -> {
                require(args.size == 2) { "Expected --dump <input.hbc>" }
                val path = Path.of(args[1])
                println(HbcDisassembler.disassemble(HbcReader.read(Files.readAllBytes(path))))
            }
            else -> throw IllegalArgumentException("Unknown option '${args[0]}'; use --help")
        }
    } catch (error: IllegalArgumentException) {
        System.err.println("CLI error: ${error.message}")
        exitProcess(1)
    } catch (error: IOException) {
        System.err.println("I/O error: ${error.message}")
        exitProcess(1)
    }
}

private fun runFrontendDemo() {
    val hadleysHopeCode = """
        program hadleys_hope

        const HOUSE_COUNT of int = 300
        const TICK of time = 1 min

        enum ReactorMode
            ONLINE
            RUNBACK
            SCRAM
            COOLING
            EMERGENCY
        end

        struct Environment
            temperature of real = -45.0
            wind of real = 8.0
            storm of bool = false
        end

        struct House
            temperature of real = 20.0
            heat_capacity of real = 10000000.0
            heat_loss of real = 32.0
            heater_power of real = 3000.0
            heater_on of bool = true
            power_ok of bool = true
            pipes_ok of bool = true
        end

        struct Issue
            id of int
            house_id of int
            kind of string
            cost of real
            resolved of bool = false
        end

        var env of Environment
        var houses of House[HOUSE_COUNT]
        var issues of list<Issue>
        var reactor_mode of ReactorMode = ReactorMode.ONLINE
        var tick of int = 0

        event PipeFrozen(house_id of int)
        event PowerLost(house_id of int)

        def update_house(i of int, dt of real) of void
            var ua of real = houses[i].heat_loss * (1.0 + 0.006 * env.wind)
            var q_loss of real = ua * (houses[i].temperature - env.temperature)
            var q_heat of real = 0.0

            if houses[i].heater_on and houses[i].power_ok
                q_heat = houses[i].heater_power
            end

            houses[i].temperature +=
                (q_heat - q_loss) * dt / houses[i].heat_capacity

            if houses[i].temperature < 0.0 and houses[i].pipes_ok
                emit PipeFrozen(i)
            end
        end

        def environment_step() of void
            env.temperature += random_real(-0.1, 0.1)

            if not env.storm and random_real(0.0, 1.0) < 0.0004
                env.storm = true
            end

            if env.storm
                env.wind = clamp(env.wind + random_real(-1.0, 1.0), 20.0, 34.0)
            else
                env.wind = clamp(env.wind + random_real(-0.5, 0.5), 4.0, 14.0)
            end
        end

        on start
            for i from 0 to HOUSE_COUNT - 1
                houses[i].temperature = 20.0
                houses[i].power_ok = true
                houses[i].pipes_ok = true
            end

            log("Hadley's Hope started")
        end

        on PipeFrozen(house_id)
            houses[house_id].pipes_ok = false

            var issue of Issue
            issue.id = size(issues) + 1
            issue.house_id = house_id
            issue.kind = "pipes_frozen"
            issue.cost = 1200.0
            issue.resolved = false

            push(issues, issue)
            log("pipes frozen")
        end

        on PowerLost(house_id)
            houses[house_id].power_ok = false
        end

        every 1 min
            tick += 1

            environment_step()

            for i from 0 to HOUSE_COUNT - 1
                update_house(i, 60.0)
            end

            metric("avg_temperature", houses[0].temperature)
        end

        at 3 hour
            emit PowerLost(42)
        end
    """.trimIndent()

    println("Parsing Hadley's Hope colony program...")
    try {
        val ast = FrontendPipeline.parse(hadleysHopeCode)
        println("SUCCESS! Program: ${ast.programName}")
        println("Top-level declarations parsed: ${ast.declarations.size}")
        println("\nAST Tree Dump:")
        println(ast.toPrettyTree())
    } catch (e: Exception) {
        System.err.println("Failed to compile: ${e.message}")
        e.printStackTrace()
    }
}