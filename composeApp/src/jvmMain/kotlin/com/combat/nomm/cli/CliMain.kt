package com.combat.nomm.cli

import com.combat.nomm.BuildKonfig
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*

class CliMain : CliktCommand(
    name = "nomm",
    help = "Nuclear Option Mod Manager - Headless CLI",
    epilog = "Use 'nomm <command> --help' for more information about a command."
) {
    private val version by option("--version", help = "Print version and exit").flag()

    override fun run() {
        if (version) {
            println(BuildKonfig.VERSION)
            return
        }
        throw com.github.ajalt.clikt.core.PrintHelpMessage(this)
    }
}

fun runCli(args: Array<String>): Int {
    val cli = CliMain().subcommands(
        Status(),
        Doctor(),
        ConfigCommand().subcommands(
            ConfigGet(),
            ConfigSet()
        ),
        ManifestRefresh(),
        ListCommand(),
        Search(),
        Show(),
        BepInExCommand().subcommands(
            BepInExInstall()
        ),
        Install(),
        Update(),
        Enable(),
        Disable(),
        Uninstall(),
        AddFile(),
        ImportCommand(),
        ExportCommand()
    )

    return try {
        cli.main(args)
        0
    } catch (e: Exception) {
        if (e.message != null) {
            System.err.println("Error: ${e.message}")
        }
        1
    }
}
