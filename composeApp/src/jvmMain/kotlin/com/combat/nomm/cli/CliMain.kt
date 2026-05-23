package com.combat.nomm.cli

import com.combat.nomm.core.CliEnvelope
import com.combat.nomm.core.CliError
import com.combat.nomm.core.StatusResult
import com.combat.nomm.core.RepoModDto
import com.combat.nomm.ModMeta
import com.combat.nomm.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { prettyPrint = true }

fun runCli(args: Array<String>): Int {
    if (args.isEmpty()) {
        printHelp()
        return 0
    }

    val (jsonOutput, cleanArgs) = parseJsonFlag(args)
    
    val command = cleanArgs.firstOrNull() ?: run {
        printHelp()
        return 0
    }

    return when (command) {
        "--version", "-v" -> {
            if (jsonOutput) {
                println(json.encodeToString(CliEnvelope<Any?>(ok = true, "version", data = mapOf("version" to BuildKonfig.VERSION))))
            } else {
                println("NOMM version ${BuildKonfig.VERSION}")
            }
            0
        }
        "--help", "-h" -> {
            printHelp()
            0
        }
        "status" -> runCommand({ CliCommands.status() }, jsonOutput)
        "doctor" -> runCommand({ CliCommands.doctor() }, jsonOutput)
        "config-get" -> runCommand({ CliCommands.configGet() }, jsonOutput)
        "config-set" -> runCommand({ CliCommands.configSet(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        "manifest-refresh" -> runCommand({ CliCommands.manifestRefresh() }, jsonOutput)
        "list" -> runCommand({ CliCommands.list() }, jsonOutput)
        "search" -> runCommand({ CliCommands.search(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        "show" -> runCommand({ CliCommands.show(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        "bepinex-install" -> runCommand({ CliCommands.bepinexInstall() }, jsonOutput)
        "mod-install" -> runCommand({ CliCommands.modInstall(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        "update" -> runCommand({ CliCommands.update(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        "enable" -> runCommand({ CliCommands.enable(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        "disable" -> runCommand({ CliCommands.disable(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        "uninstall" -> runCommand({ CliCommands.uninstall(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        "add-file" -> runCommand({ CliCommands.addFile(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        "import" -> runCommand({ CliCommands.import(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        "export" -> runCommand({ CliCommands.export(cleanArgs.drop(1).toTypedArray()) }, jsonOutput)
        else -> {
            if (jsonOutput) {
                println(json.encodeToString(CliEnvelope<Any?>(ok = false, "unknown", error = CliError(code = "INVALID_ARGS", message = "Unknown command: $command"))))
            } else {
                System.err.println("Unknown command: $command")
                printHelp()
            }
            2
        }
    }
}

private fun parseJsonFlag(args: Array<String>): Pair<Boolean, Array<String>> {
    val hasJson = args.contains("--json")
    val cleanArgs = args.filter { it != "--json" }.toTypedArray()
    return hasJson to cleanArgs
}

private fun runCommand(command: () -> CliEnvelope<*>, jsonOutput: Boolean): Int {
    return try {
        val result = command()
        if (jsonOutput) {
            println(json.encodeToString(result))
        } else {
            printPlainResult(result)
        }
        if (result.ok) 0 else 1
    } catch (e: Exception) {
        val result = CliEnvelope<Any?>(ok = false, "error", error = CliError(code = "ERROR", message = e.message ?: "Unknown error"))
        if (jsonOutput) {
            println(json.encodeToString(result))
        } else {
            System.err.println("Error: ${e.message}")
        }
        1
    }
}

private fun printPlainResult(result: CliEnvelope<*>) {
    if (result.ok) {
        val data = result.data
        when {
            data is StatusResult -> {
                println("Game path: ${data.gamePath ?: "Not set"}")
                println("BepInEx installed: ${data.bepInExInstalled}")
                println("Installed mods: ${data.installedModCount}")
            }
            data is Map<*, *> -> {
                data.forEach { (k, v) ->
                    println("$k: $v")
                }
            }
            data is RepoModDto -> {
                println(json.encodeToString(data))
            }
            data is ModMeta -> {
                println(json.encodeToString(data))
            }
            data is Boolean -> {
                println("Success: $data")
            }
            data is List<*> -> {
                data.forEach { item ->
                    println(json.encodeToString(item))
                }
            }
            data != null -> {
                println(data.toString())
            }
            else -> {
                println("Success")
            }
        }
    } else {
        System.err.println("Error: ${result.error?.message}")
    }
}

private fun printHelp() {
    println("""NOMM - Nuclear Option Mod Manager
Usage: nomm [OPTIONS] <COMMAND> [ARGS...]

Options:
  --json       Output results in JSON format
  --version, -v  Show version
  --help, -h   Show this help message

Commands:
  status              Show overall status summary
  doctor              Run diagnostics on the installation
  config-get          Show all configuration values
  config-set <key> <value>  Set a configuration value
  manifest-refresh    Refresh the NOMNOM mod manifest
  list                List installed mods
  search <query>      Search mods in the manifest
  show <mod-id>       Show detailed mod information
  bepinex-install     Install BepInEx into the game folder
  mod-install <mod-id>  Install a mod
  update <mod-id>     Update a specific mod
  update --all        Update all installed mods
  enable <mod-id>     Enable a mod
  disable <mod-id>    Disable a mod
  uninstall <mod-id>  Uninstall a mod
  add-file <path>     Add a local mod file
  import <path>       Import modpack from .nomm.json
  export <path>       Export enabled mods to .nomm.json
""")
}
