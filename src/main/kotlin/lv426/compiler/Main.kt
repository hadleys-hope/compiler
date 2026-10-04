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
import lv426.compiler.backend.IrPrinter
import lv426.compiler.codegen.IrLowering
import lv426.compiler.frontend.FrontendPipeline
import lv426.compiler.semantic.DiagnosticSeverity
import lv426.compiler.semantic.SemanticAnalyzer

fun main(args: Array<String>) {
    // Если запущено без аргументов (кнопка Run в IDEA) — запускаем твой AST Demo
    if (args.isEmpty()) {
        runFrontendDemo()
        return
    }

    if (args.contentEquals(arrayOf("--benchmark"))) {
        runFrontendDemo(benchmark = true)
        return
    }

    if (args.contentEquals(arrayOf("--benchmark-one-house"))) {
        runFrontendDemo(benchmark = true, oneHouse = true)
        return
    }

    if (args.contentEquals(arrayOf("--benchmark-one-house-cold"))) {
        runFrontendDemo(benchmark = true, oneHouse = true, cold = true)
        return
    }

    if (args.contentEquals(arrayOf("--help"))) {
        println("Hope Compiler & VM CLI:")
        println("  (no args)           : Run Frontend AST demo (Hadley's Hope)")
        println("  --parse <file.hope> : Parse Hope file and dump AST")
        println("  --check <file.hope> : Parse and run semantic analysis")
        println("  --dump-ir <file.hope> : Parse, check, lower and dump stack IR")
        println("  --compile <file.hope> <output.hbc> : Compile Hope source to HBC")
        println("  --benchmark        : Warm up JIT 10 times, then benchmark 100 compilations")
        println("  --benchmark-one-house : Benchmark the same program with HOUSE_COUNT = 1")
        println("  --benchmark-one-house-cold : Compile one-house program once without JIT warmup")
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
            "--check" -> {
                require(args.size == 2) { "Expected --check <file.hope>" }
                val source = Files.readString(Path.of(args[1]))
                val result = CompilerPipeline.analyze(source).semantic
                result.diagnostics.forEach { diagnostic ->
                    val location = diagnostic.location?.let { "${it.line}:${it.column}: " } ?: ""
                    val stream = if (diagnostic.severity == DiagnosticSeverity.ERROR) System.err else System.out
                    stream.println("$location${diagnostic.severity.name.lowercase()}: ${diagnostic.message}")
                }
                if (!result.isValid) exitProcess(1)
                println("Semantic analysis: OK")
            }
            "--dump-ir" -> {
                require(args.size == 2) { "Expected --dump-ir <file.hope>" }
                val source = Files.readString(Path.of(args[1]))
                println(IrPrinter.print(CompilerPipeline.lower(source).ir))
            }
            "--compile" -> {
                require(args.size == 3) { "Expected --compile <file.hope> <output.hbc>" }
                val source = Files.readString(Path.of(args[1]))
                val path = Path.of(args[2])
                val module = CompilerPipeline.write(source, path)
                println("Compiled ${args[1]} -> ${path.toAbsolutePath()} (${module.functions.size} functions)")
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

private fun runFrontendDemo(
    benchmark: Boolean = false,
    oneHouse: Boolean = false,
    cold: Boolean = false
) {
    val hadleysHopeCode = """
        program houses_domain

        const HOUSE_COUNT of int = 5000
        const HISTORY_SLOTS of int = 144

        struct HouseConfig
            comfort_c of real
            eco_c of real
            antifreeze_c of real
            limit_level5_w of real
            aeration_w of real
            tick_seconds of real
            freeze_ticks_to_frozen of int
            frozen_ticks_to_burst of int
        end

        struct House
            ctrl_t of int
            ctrl_target of real
            ctrl_heater of bool
            ctrl_valve of bool
            ctrl_appl of bool
            ext of bool

            program_name of string
            reason of string

            target of real
            heater_on of bool
            valve_open of bool
            appliances_on of bool

            base_w of real
            heater_w of real
            limit_w of real
            aeration_ok of bool

            draw_w of real
            heat_w of real

            on_ups of bool
            power_ok of bool

            t_in of real
            ua of real
            cap of real
            residents of int

            water_ok of bool
            pipes_ok of bool
            burst of bool
            frozen of int

            net_online of bool

            sector of int
            x of real
            y of real

            prev_valid of bool
            prev_power of bool
            prev_ups of bool
            prev_water of bool
            prev_net of bool
            prev_pipes of bool
            prev_burst of bool
            prev_limit of bool
            prev_heater of bool
            prev_target of real
            prev_program of string
            prev_reason of string

            hist_temperature of real[HISTORY_SLOTS]
            hist_draw of real[HISTORY_SLOTS]
        end

        var cfg of HouseConfig
        var houses of House[HOUSE_COUNT]

        var sim_minute of int = 0
        var outside_temperature of real = -45.0
        var wind of real = 8.0
        var night of bool = false
        var history_index of int = 0

        event HouseLog(
            house_id of int,
            message of string
        )

        event ControllerStateChanged(
            house_id of int,
            program_name of string,
            reason of string,
            target of real
        )

        event PipeFrozen(
            house_id of int
        )

        event PipeBurst(
            house_id of int,
            sector of int,
            x of real,
            y of real,
            cost_multiplier of real
        )

        def min_real(a of real, b of real) of real
            if a < b
                return a
            else
                return b
            end
        end

        def max_real(a of real, b of real) of real
            if a > b
                return a
            else
                return b
            end
        end

        def abs_real(value of real) of real
            if value < 0.0
                return -value
            end

            return value
        end

        def houses_decide() of void
            for i from 0 to HOUSE_COUNT - 1
                var h of House = houses[i]

                var ext of bool = sim_minute - h.ctrl_t < 15
                var target of real = cfg.comfort_c

                if h.on_ups or h.limit_w > 0.0
                    target = cfg.eco_c
                end

                if h.limit_w > 0.0 and h.limit_w <= cfg.limit_level5_w
                    target = cfg.antifreeze_c
                end

                var heater_on of bool = h.heater_on

                if h.t_in < target - 0.5
                    heater_on = true
                elif h.t_in > target + 0.5
                    heater_on = false
                end

                var valve_open of bool = not h.burst
                var appliances_on of bool = true

                if ext
                    target = h.ctrl_target
                    heater_on = h.ctrl_heater
                    valve_open = h.ctrl_valve and not h.burst
                    appliances_on = h.ctrl_appl
                end

                h.target = target
                h.heater_on = heater_on
                h.valve_open = valve_open
                h.appliances_on = appliances_on
                h.ext = ext
            end
        end

        def houses_demand() of void
            for i from 0 to HOUSE_COUNT - 1
                var h of House = houses[i]

                var base of real = h.base_w

                if night
                    base = base * 0.6
                end

                base += random_real(-50.0, 50.0)

                if base < 80.0
                    base = 80.0
                end

                if not h.appliances_on
                    base = 100.0
                end

                var heater of real = 0.0

                if h.heater_on
                    heater = h.heater_w
                end

                var aeration of real = 0.0

                if h.aeration_ok
                    aeration = cfg.aeration_w
                end

                var want of real = base + heater + aeration

                var limit of real = h.limit_w
                var lim of real = 1000000000.0

                if limit > 0.0
                    lim = limit
                end

                var essential of real = aeration + 100.0

                var heat_budget of real =
                    max_real(0.0, lim - essential)

                var heat_alloc of real =
                    min_real(heater, heat_budget)

                var rest_budget of real =
                    max_real(
                        0.0,
                        lim - essential - heat_alloc
                    )

                var rest of real =
                    min_real(
                        base - 100.0,
                        rest_budget
                    )

                var draw of real =
                    min_real(
                        essential + heat_alloc + rest,
                        want
                    )

                h.draw_w = draw
                h.heat_w = heat_alloc
            end
        end

        def houses_step() of void
            var dt of real = cfg.tick_seconds

            for i from 0 to HOUSE_COUNT - 1
                var h of House = houses[i]

                var ua_eff of real =
                    h.ua * (1.0 + 0.006 * wind)

                var q_loss of real =
                    ua_eff * (h.t_in - outside_temperature)

                var q_int of real =
                    (h.draw_w - h.heat_w) * 0.8
                    + h.residents * 80.0

                h.t_in +=
                    (h.heat_w + q_int - q_loss)
                    * dt
                    / h.cap

                var cold of bool = h.t_in < 0.0

                if cold
                    h.frozen += 1
                else
                    h.frozen = 0
                end

                if h.t_in > 3.0 and not h.burst
                    h.pipes_ok = true
                end

                if h.frozen == cfg.freeze_ticks_to_frozen
                    and h.pipes_ok

                    h.pipes_ok = false

                    emit PipeFrozen(i)
                    emit HouseLog(i, "pipes frozen")
                end

                if h.frozen ==
                    cfg.freeze_ticks_to_frozen
                    + cfg.frozen_ticks_to_burst
                    and not h.burst

                    h.burst = true

                    var cost_multiplier of real = 1.0

                    if not h.valve_open
                        cost_multiplier = 0.5
                    end

                    emit PipeBurst(
                        i,
                        h.sector,
                        h.x,
                        h.y,
                        cost_multiplier
                    )
                end
            end

            hydraulic_step()
        end

        def log_bool_transition(
            house_id of int,
            previous of bool,
            current of bool,
            true_message of string,
            false_message of string
        ) of void

            if current != previous
                if current
                    emit HouseLog(
                        house_id,
                        true_message
                    )
                else
                    emit HouseLog(
                        house_id,
                        false_message
                    )
                end
            end
        end

        def house_events() of void
            for i from 0 to HOUSE_COUNT - 1
                var h of House = houses[i]

                var current_limit of bool =
                    h.limit_w > 0.0

                if h.prev_valid
                    log_bool_transition(
                        i,
                        h.prev_power,
                        h.power_ok,
                        "power restored",
                        "power lost"
                    )

                    log_bool_transition(
                        i,
                        h.prev_ups,
                        h.on_ups,
                        "back on the grid",
                        "running on the sector UPS"
                    )

                    log_bool_transition(
                        i,
                        h.prev_water,
                        h.water_ok,
                        "water supply restored",
                        "no water"
                    )

                    log_bool_transition(
                        i,
                        h.prev_net,
                        h.net_online,
                        "network link up",
                        "network link lost"
                    )

                    log_bool_transition(
                        i,
                        h.prev_pipes,
                        h.pipes_ok,
                        "pipes thawed or repaired",
                        "pipes frozen"
                    )

                    log_bool_transition(
                        i,
                        h.prev_burst,
                        h.burst,
                        "pipes repaired",
                        "pipes burst"
                    )

                    log_bool_transition(
                        i,
                        h.prev_limit,
                        current_limit,
                        "power limit lifted",
                        "power limit imposed by the grid"
                    )

                    if h.ext
                        if h.reason != h.prev_reason
                            or abs_real(
                                h.target - h.prev_target
                            ) > 0.1

                            emit ControllerStateChanged(
                                i,
                                h.program_name,
                                h.reason,
                                h.target
                            )
                        end
                    end
                end

                h.prev_power = h.power_ok
                h.prev_ups = h.on_ups
                h.prev_water = h.water_ok
                h.prev_net = h.net_online
                h.prev_pipes = h.pipes_ok
                h.prev_burst = h.burst
                h.prev_limit = current_limit
                h.prev_heater = h.heater_on
                h.prev_target = h.target
                h.prev_program = h.program_name
                h.prev_reason = h.reason
                h.prev_valid = true
            end

            if sim_minute % 10 == 0
                for i from 0 to HOUSE_COUNT - 1
                    houses[i].hist_temperature[history_index] =
                        houses[i].t_in

                    houses[i].hist_draw[history_index] =
                        houses[i].draw_w
                end

                history_index =
                    (history_index + 1) % HISTORY_SLOTS
            end
        end

        def hydraulic_step() of void
        end
    """.trimIndent()

    if (benchmark) {
        val benchmarkSource = if (oneHouse) {
            hadleysHopeCode.replaceFirst(
                "const HOUSE_COUNT of int = 5000",
                "const HOUSE_COUNT of int = 1"
            )
        } else {
            hadleysHopeCode
        }
        runCompilationBenchmark(
            benchmarkSource,
            warmupRuns = if (cold) 0 else 10,
            measuredRuns = if (cold) 1 else 100
        )
        return
    }

    println("=== Hadley's Hope compiler demo ===")

    try {
        println("[1/4] Parsing...")
        val parsingStartedAt = System.nanoTime()
        val ast = FrontendPipeline.parse(hadleysHopeCode)
        val parsingNanos = System.nanoTime() - parsingStartedAt

        println("Program: ${ast.programName}")
        println("Top-level declarations: ${ast.declarations.size}")

        println("[2/4] Semantic analysis...")
        val semanticStartedAt = System.nanoTime()
        val semantic = SemanticAnalyzer().analyze(ast)
        val semanticNanos = System.nanoTime() - semanticStartedAt

        semantic.diagnostics.forEach { diagnostic ->
            val location = diagnostic.location?.let {
                "${it.line}:${it.column}: "
            } ?: ""

            println(
                "$location${diagnostic.severity.name.lowercase()}: ${diagnostic.message}"
            )
        }

        if (!semantic.isValid) {
            println("Semantic analysis failed")
            return
        }

        println("Semantic analysis: OK")

        println("[3/4] Lowering to IR...")
        val loweringStartedAt = System.nanoTime()
        val ir = IrLowering(semantic).lower()
        val loweringNanos = System.nanoTime() - loweringStartedAt

        println(IrPrinter.print(ir))

        println("[4/4] Compiling HBC...")
        val backendStartedAt = System.nanoTime()
        val module = HbcBackend.compile(ir)
        val backendNanos = System.nanoTime() - backendStartedAt

        val compilationNanos =
            parsingNanos +
                    semanticNanos +
                    loweringNanos +
                    backendNanos

        println("HBC compilation: OK")

        println(
            "Compilation time: %.3f ms (parse %.3f ms, semantic %.3f ms, IR %.3f ms, HBC %.3f ms)".format(
                compilationNanos / 1_000_000.0,
                parsingNanos / 1_000_000.0,
                semanticNanos / 1_000_000.0,
                loweringNanos / 1_000_000.0,
                backendNanos / 1_000_000.0
            )
        )

        println("Functions: ${module.functions.size}")
        println("Globals: ${module.globals.size}")
        println("Structs: ${module.structs.size}")
        println("Events: ${module.events.size}")
        println("Handlers: ${module.handlers.size}")

        val output =
            Path.of("build/hadleys-hope.hbc")

        Files.createDirectories(output.parent)
        Files.write(output, module.toBytes())

        println(
            "Written: ${output.toAbsolutePath()}"
        )

        println()
        println("=== HBC ===")
        println(
            HbcDisassembler.disassemble(module)
        )

    } catch (e: Exception) {
        System.err.println(
            "Compilation failed: ${e.message}"
        )
        e.printStackTrace()
    }
}

private fun runCompilationBenchmark(
    sourceCode: String,
    warmupRuns: Int = 10,
    measuredRuns: Int = 100
) {
    println("Warming up JIT: $warmupRuns compilations...")
    repeat(warmupRuns) {
        CompilerPipeline.compile(sourceCode)
    }

    println("Measuring: $measuredRuns compilations...")
    val durations = LongArray(measuredRuns)
    repeat(measuredRuns) { index ->
        val startedAt = System.nanoTime()
        CompilerPipeline.compile(sourceCode)
        durations[index] = System.nanoTime() - startedAt
    }

    val sorted = durations.sorted()
    val totalNanos = durations.sum()
    val averageNanos = totalNanos.toDouble() / measuredRuns
    val medianNanos = sorted[measuredRuns / 2]
    val p95Nanos = sorted[(measuredRuns - 1) * 95 / 100]

    println("Benchmark results:")
    println("  total:   %.3f ms".format(totalNanos / 1_000_000.0))
    println("  average: %.3f ms".format(averageNanos / 1_000_000.0))
    println("  median:  %.3f ms".format(medianNanos / 1_000_000.0))
    println("  p95:     %.3f ms".format(p95Nanos / 1_000_000.0))
    println("  min:     %.3f ms".format(sorted.first() / 1_000_000.0))
    println("  max:     %.3f ms".format(sorted.last() / 1_000_000.0))
}
