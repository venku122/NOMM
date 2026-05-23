package com.combat.nomm.cli

import com.combat.nomm.BuildKonfig
import com.combat.nomm.core.NommService

object CliRunner {
    fun run(args: Array<String>): Int {
        if (args.isEmpty()) {
            return 2 // Invalid arguments
        }

        val command = args[0]
        val remaining = args.drop(1).toTypedArray()

        return when (command) {
            "--version" -> {
                println(BuildKonfig.VERSION)
                0
            }
            "--help", "-h", "help" -> {
                printHelp()
                0
            }
            "status" -> Status.run(remaining)
            "doctor" -> Doctor.run(remaining)
            "config-get" -> ConfigGet.run(remaining)
            "config-set" -> ConfigSet.run(remaining)
            "manifest-refresh" -> ManifestRefresh.run(remaining)
            "list" -> ListCommand.run(remaining)
            "search" -> Search.run(remaining)
            "show" -> Show.run(remaining)
            "bepinex-install" -> BepInExInstall.run(remaining)
            "mod-install" -> Install.run(remaining)
            "update" -> Update.run(remaining)
            "enable" -> Enable.run(remaining)
            "disable" -> Disable.run(remaining)
            "uninstall" -> Uninstall.run(remaining)
            "add-file" -> AddFile.run(remaining)
            "import" -> ImportCommand.run(remaining)
            "export" -> ExportCommand.run(remaining)
            else -> {
                printError("Unknown command: $command")
                printHelp()
                2
            }
        }
    }

    private fun printHelp() {
        println("NOMM v${BuildKonfig.VERSION} - Headless CLI")
        println()
        println("Usage: nomm <command> [options]")
        println()
        println("Commands:")
        println("  status          Show overall status")
        println("  doctor          Run diagnostics")
        println("  config-get      Get configuration")
        println("  config-set      Set configuration (config-set <key> <value>)")
        println("  manifest-refresh Refresh the mod manifest")
        println("  list            List installed mods")
        println("  search          Search mods (search <query>)")
        println("  show            Show mod details (show <mod-id>)")
        println("  bepinex-install Install BepInEx")
        println("  mod-install     Install a mod (mod-install <mod-id>)")
        println("  update          Update a mod (update <mod-id> or update --all)")
        println("  enable          Enable a mod (enable <mod-id>)")
        println("  disable         Disable a mod (disable <mod-id>)")
        println("  uninstall       Uninstall a mod (uninstall <mod-id>)")
        println("  add-file        Add a local mod file (add-file <path>)")
        println("  import          Import a modpack (import <path>)")
        println("  export          Export enabled mods (export <path>)")
        println()
        println("Options:")
        println("  --version       Print version")
        println("  --help, -h      Print this help")
    }

    private fun printError(message: String) {
        System.err.println("Error: $message")
    }
}

fun runCli(args: Array<String>): Int {
    return CliRunner.run(args)
}
