package com.combat.nomm.cli

import com.combat.nomm.core.ModDto
import com.combat.nomm.core.NommService
import com.combat.nomm.core.RepoModDto
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*


abstract class NommCommand(
    name: String,
    help: String = "",
    epilog: String = ""
) : CliktCommand(name = name, help = help, epilog = epilog) {

    val json by option("--json", help = "Output as JSON").flag()

    override fun run() {
        try {
            runWithJson()
        } catch (e: CliException) {
            if (json) {
                printError(commandName, e.message ?: "Command failed", "CLI_ERROR")
            }
            throw e
        }
    }

    abstract fun runWithJson()
}

class Status : NommCommand("status", help = "Show overall status of the NOMM setup") {
    override fun runWithJson() {
        val status = NommService.getStatus()
        if (json) {
            printJson("status", true, status)
        } else {
            println("NOMM v${status.version}")
            println()
            println("Game Path:       ${status.gamePath ?: "NOT SET"}")
            println("  Exists:        ${if (status.gamePathExists) "YES" else "NO"}")
            println("  Executable:    ${if (status.gameExeFound) "FOUND" else "NOT FOUND"}")
            println("BepInEx:         ${if (status.bepInExInstalled) "INSTALLED" else "NOT INSTALLED"}")
            println("  Plugins:       ${if (status.pluginsDirExists) "EXISTS" else "MISSING"}")
            println("  Disabled:      ${if (status.disabledPluginsDirExists) "EXISTS" else "MISSING"}")
            println("Manifest URL:    ${status.manifestUrl}")
            println("  Loaded:        ${if (status.manifestLoaded) "YES (${status.manifestModCount} mods)" else "NO"}")
            println("Installed Mods:  ${status.installedModCount}")
            println("  Enabled:       ${status.enabledModCount}")
            println("  Disabled:      ${status.disabledModCount}")
        }
    }
}

class Doctor : NommCommand("doctor", help = "Run diagnostics on the NOMM installation") {
    override fun runWithJson() {
        val result = NommService.doctor()
        if (json) {
            printJson("doctor", true, result)
        } else {
            println("NOMM Doctor v${result.version}")
            println()
            println("Game Path:       ${result.gamePath ?: "NOT SET"}")
            println("  Exists:        ${if (result.gamePathExists) "YES" else "NO"}")
            println("  Executable:    ${if (result.executableFound) "FOUND" else "NOT FOUND"}")
            println("BepInEx:         ${if (result.bepInExExists) "EXISTS" else "MISSING"}")
            println("  Plugins:       ${if (result.pluginsDirExists) "EXISTS" else "MISSING"}")
            println("  Disabled:      ${if (result.disabledPluginsDirExists) "EXISTS" else "MISSING"}")
            println("Manifest URL:    ${result.manifestUrl}")
            println("  Reachable:     ${if (result.manifestReachable == true) "YES" else if (result.manifestReachable == false) "NO" else "UNKNOWN"}")
            println("Installed Mods:  ${result.installedModCount}")
            println("  Enabled:       ${result.enabledModCount}")
            println("  Disabled:      ${result.disabledModCount}")
            if (result.issues.isNotEmpty()) {
                println()
                println("Issues:")
                result.issues.forEach { println("  ! $it") }
            }
        }
    }
}

class ConfigCommand : NommCommand("config", help = "Get or set configuration values") {
    override fun runWithJson() {
        printError("config", "Use subcommand: 'config get' or 'config set <key> <value>'", "INVALID_ARGS")
        throw PrintHelpMessage(this)
    }
}

class ConfigGet : NommCommand("get", help = "Get all configuration values") {
    override fun runWithJson() {
        val config = NommService.getConfig()
        if (json) {
            printJson("config_get", true, config)
        } else {
            println("Configuration:")
            println("  gamePath:              ${config.gamePath ?: "(not set)"}")
            println("  manifestUrl:           ${config.manifestUrl}")
            println("  manifestVersionUrl:    ${config.manifestVersionUrl}")
            println("  ignoreHashMismatch:    ${config.ignoreHashMismatch}")
            println("  ignoreManifestVersion: ${config.ignoreManifestVersion}")
            println("  fakeManifest:          ${config.fakeManifest}")
            println("  theme:                 ${config.theme}")
        }
    }
}

class ConfigSet : NommCommand("set", help = "Set a configuration value. Usage: config set <key> <value>") {
    val pair by argument("key value", help = "Key and value to set").min(2).max(2)

    override fun runWithJson() {
        val key = pair[0]
        val value = pair[1]
        try {
            val result = NommService.setConfigValue(key, value)
            if (json) {
                printJson("config_set", true, result)
            } else {
                println("Set ${result.key} = ${result.value}")
            }
        } catch (e: IllegalArgumentException) {
            if (json) {
                printError("config_set", e.message ?: "Unknown config key", "INVALID_CONFIG")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.INVALID_CONFIG)
        }
    }
}

class ManifestRefresh : NommCommand("refresh", help = "Refresh the NOMNOM mod manifest") {
    override fun runWithJson() {
        try {
            val result = NommService.refreshManifest()
            if (json) {
                printJson("manifest_refresh", true, result)
            } else {
                println("Manifest refreshed: v${result.version}, ${result.modCount} mods${if (result.cached) " (cached)" else ""}")
            }
        } catch (e: Exception) {
            if (json) {
                printError("manifest_refresh", e.message ?: "Failed to fetch manifest", "MANIFEST_FETCH_FAILED")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.MANIFEST_FETCH_FAILED)
        }
    }
}

class ListCommand : NommCommand("list", help = "List installed mods") {
    val installed by option("--installed", help = "List only installed mods").flag()

    override fun runWithJson() {
        val mods = NommService.listInstalled()
        if (!installed) {
            if (json) {
                printJson("list", true, mods)
            } else {
                if (mods.isEmpty()) {
                    println("No mods installed.")
                } else {
                    println("Installed mods:")
                    mods.forEach { m ->
                        println("  ${m.id} v${m.version ?: "?"} [${if (m.enabled == true) "ENABLED" else "DISABLED"}]${if (m.hasUpdate) " (update available)" else ""}")
                    }
                }
            }
        } else {
            if (json) {
                printJson("list", true, mods.filter { it.enabled == true })
            } else {
                val enabled = mods.filter { it.enabled == true }
                if (enabled.isEmpty()) {
                    println("No enabled mods.")
                } else {
                    println("Enabled mods:")
                    enabled.forEach { m ->
                        println("  ${m.id} v${m.version ?: "?"}")
                    }
                }
            }
        }
    }
}

class Search : NommCommand("search", help = "Search mods in the manifest") {
    val query by argument("query", help = "Search query").required()

    override fun runWithJson() {
        if (NommService.getStatus().manifestModCount == 0) {
            try {
                NommService.refreshManifest()
            } catch (_: Exception) {}
        }
        val results = NommService.search(query)
        if (json) {
            printJson("search", true, results)
        } else {
            if (results.isEmpty()) {
                println("No results for '$query'.")
            } else {
                println("Results for '$query':")
                results.forEach { m ->
                    println("  ${m.id} - ${m.displayName} v${m.latestVersion ?: "?"}${if (m.installed) " [INSTALLED]" else ""}")
                }
            }
        }
    }
}

class Show : NommCommand("show", help = "Show detailed information about a mod") {
    val modId by argument("mod-id", help = "Mod ID to show").required()

    override fun runWithJson() {
        val mod = NommService.show(modId)
        if (mod == null) {
            if (json) {
                printError("show", "Mod not found: $modId", "MOD_NOT_FOUND")
            } else {
                System.err.println("Error: Mod not found: $modId")
            }
            exit(ExitCode.MOD_NOT_FOUND)
        }
        if (json) {
            printJson("show", true, mod)
        } else {
            println("${mod.displayName} (${mod.id})")
            println("  Description: ${mod.description.take(200)}${if (mod.description.length > 200) "..." else ""}")
            println("  Authors:     ${mod.authors.joinToString(", ")}")
            println("  Tags:        ${mod.tags.joinToString(", ")}")
            println("  Latest:      v${mod.latestVersion ?: "?"}")
            println("  Versions:    ${mod.versions.joinToString(", ")}")
            println("  Downloads:   ${mod.downloadCount ?: "?"}")
            if (mod.dependencies.isNotEmpty()) {
                println("  Dependencies:")
                mod.dependencies.forEach { println("    - ${it.id} v${it.version ?: "?"}") }
            }
            mod.extends?.let { println("  Extends:     ${it.id} v${it.version ?: "?"}") }
            if (mod.incompatibilities.isNotEmpty()) {
                println("  Incompatible:")
                mod.incompatibilities.forEach { println("    - ${it.id} v${it.version ?: "?"}") }
            }
            println("  Installed:   ${if (mod.installed) "YES v${mod.installedVersion ?: "?"} [${if (mod.enabled == true) "ENABLED" else "DISABLED"}]" else "NO"}")
        }
    }
}

class BepInExCommand : NommCommand("bepinex", help = "Manage BepInEx installation") {
    override fun runWithJson() {
        printError("bepinex", "Use subcommand: 'bepinex install'", "INVALID_ARGS")
        throw PrintHelpMessage(this)
    }
}

class BepInExInstall : NommCommand("install", help = "Install BepInEx into the game folder") {
    override fun runWithJson() {
        try {
            val result = NommService.installBepInEx()
            if (json) {
                printJson("bepinex_install", true, result)
            } else {
                if (result.wasAlreadyInstalled) {
                    println("BepInEx is already installed.")
                } else {
                    println("BepInEx installed successfully.")
                }
            }
        } catch (e: Exception) {
            if (json) {
                printError("bepinex_install", e.message ?: "Failed to install BepInEx", "INSTALL_FAILED")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.INSTALL_FAILED)
        }
    }
}

class Install : NommCommand("install", help = "Install a mod from the manifest") {
    val modId by argument("mod-id", help = "Mod ID to install").required()
    val version by option("--version", "-v", help = "Specific version to install")
    val noEnable by option("--no-enable", help = "Do not enable after install").flag()

    override fun runWithJson() {
        try {
            val result = NommService.installMod(modId, version, !noEnable)
            if (json) {
                printJson("install", true, result)
            } else {
                if (result.wasAlreadyInstalled) {
                    println("${result.modId} v${result.installedVersion ?: "?"} is already installed.")
                } else {
                    println("${result.modId} v${result.installedVersion ?: "?"} installed. Enabled: ${result.enabled}")
                }
            }
        } catch (e: IllegalArgumentException) {
            if (json) {
                printError("install", e.message ?: "Mod not found", "MOD_NOT_FOUND")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.MOD_NOT_FOUND)
        } catch (e: Exception) {
            if (json) {
                printError("install", e.message ?: "Install failed", "INSTALL_FAILED")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.INSTALL_FAILED)
        }
    }
}

class Update : NommCommand("update", help = "Update a mod or all mods") {
    val modId by argument("mod-id", help = "Mod ID to update").optional()
    val all by option("--all", help = "Update all installed mods").flag()

    override fun runWithJson() {
        try {
            if (all) {
                val result = NommService.updateAll()
                if (json) {
                    printJson("update", true, result)
                } else {
                    println("Updated: ${result.succeeded.size} mod(s)")
                    if (result.failed.isNotEmpty()) {
                        println("Failed: ${result.failed.size} mod(s)")
                        result.failed.forEach { println("  ! ${it.id}: ${it.error}") }
                    }
                }
            } else if (modId != null) {
                val result = NommService.updateMod(modId!!)
                if (json) {
                    printJson("update", true, result)
                } else {
                    println("${result.modId} updated to v${result.installedVersion ?: "?"}")
                }
            } else {
                throw PrintHelpMessage(this)
            }
        } catch (e: Exception) {
            if (json) {
                printError("update", e.message ?: "Update failed", "INSTALL_FAILED")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.INSTALL_FAILED)
        }
    }
}

class Enable : NommCommand("enable", help = "Enable a mod") {
    val modId by argument("mod-id", help = "Mod ID to enable").required()

    override fun runWithJson() {
        try {
            val result = NommService.enableMod(modId)
            if (json) {
                printJson("enable", true, result)
            } else {
                println("${result.id} enabled.")
            }
        } catch (e: Exception) {
            if (json) {
                printError("enable", e.message ?: "Failed to enable mod", "FILESYSTEM_ERROR")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.FILESYSTEM_ERROR)
        }
    }
}

class Disable : NommCommand("disable", help = "Disable a mod") {
    val modId by argument("mod-id", help = "Mod ID to disable").required()

    override fun runWithJson() {
        try {
            val result = NommService.disableMod(modId)
            if (json) {
                printJson("disable", true, result)
            } else {
                println("${result.id} disabled.")
            }
        } catch (e: Exception) {
            if (json) {
                printError("disable", e.message ?: "Failed to disable mod", "FILESYSTEM_ERROR")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.FILESYSTEM_ERROR)
        }
    }
}

class Uninstall : NommCommand("uninstall", help = "Uninstall a mod") {
    val modId by argument("mod-id", help = "Mod ID to uninstall").required()

    override fun runWithJson() {
        try {
            val result = NommService.uninstallMod(modId)
            if (json) {
                printJson("uninstall", true, result)
            } else {
                println("${result.id} uninstalled.")
            }
        } catch (e: Exception) {
            if (json) {
                printError("uninstall", e.message ?: "Failed to uninstall mod", "FILESYSTEM_ERROR")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.FILESYSTEM_ERROR)
        }
    }
}

class AddFile : NommCommand("add-file", help = "Add a mod from a local file") {
    val path by argument("path", help = "Path to the mod file").required()
    val move by option("--move", help = "Move file instead of copying").flag()

    override fun runWithJson() {
        try {
            val result = NommService.addFile(path, move)
            if (json) {
                printJson("add_file", true, result)
            } else {
                println("${result.action}: ${result.fileName} -> plugins/")
            }
        } catch (e: Exception) {
            if (json) {
                printError("add_file", e.message ?: "Failed to add file", "FILESYSTEM_ERROR")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.FILESYSTEM_ERROR)
        }
    }
}

class ImportCommand : NommCommand("import", help = "Import mods from a modpack file") {
    val filePath by argument("path", help = "Path to the modpack .nomm.json file").required()

    override fun runWithJson() {
        try {
            val result = NommService.importModpack(filePath)
            if (json) {
                printJson("import", true, result)
            } else {
                println("Imported ${result.succeeded.size} mod(s).")
                if (result.failed.isNotEmpty()) {
                    println("Failed: ${result.failed.size}")
                    result.failed.forEach { println("  ! ${it.id}: ${it.error}") }
                }
            }
        } catch (e: Exception) {
            if (json) {
                printError("import", e.message ?: "Import failed", "FILESYSTEM_ERROR")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.FILESYSTEM_ERROR)
        }
    }
}

class ExportCommand : NommCommand("export", help = "Export enabled mods to a modpack file") {
    val filePath by argument("path", help = "Output path for the modpack .nomm.json file").required()

    override fun runWithJson() {
        try {
            NommService.exportModpack(filePath)
            if (json) {
                printJson("export", true, mapOf("path" to filePath))
            } else {
                println("Exported to $filePath")
            }
        } catch (e: Exception) {
            if (json) {
                printError("export", e.message ?: "Export failed", "FILESYSTEM_ERROR")
            } else {
                System.err.println("Error: ${e.message}")
            }
            exit(ExitCode.FILESYSTEM_ERROR)
        }
    }
}
