package com.combat.nomm.cli

import com.combat.nomm.*
import com.combat.nomm.core.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { prettyPrint = true }

object CliCommands {
    fun status(): CliEnvelope<StatusResult> = CliEnvelope(ok = true, "status", data = NommService.getStatus())

    fun doctor(): CliEnvelope<DoctorResult> = CliEnvelope(ok = true, "doctor", data = NommService.doctor())

    fun configGet(): CliEnvelope<Map<String, String>> {
        val config = NommService.getConfig()
        return CliEnvelope(ok = true, "config-get", data = mapOf(
            "theme" to config.theme.name,
            "gamePath" to (config.gamePath ?: ""),
            "paletteStyle" to config.paletteStyle.name,
            "contrast" to config.contrast.name,
            "fakeManifest" to config.fakeManifest.toString(),
            "manifestUrl" to config.manifestUrl,
            "manifestVersionUrl" to config.manifestVersionUrl,
            "ignoreManifestVersion" to config.ignoreManifestVersion.toString(),
            "ignoreHashMismatch" to config.ignoreHashMismatch.toString(),
            "hueValue" to config.hueValue.toString(),
            "placement" to config.placement.name
        ))
    }

    fun configSet(args: Array<String>): CliEnvelope<ConfigResult> {
        if (args.size < 2) {
            return CliEnvelope(ok = false, "config-set", error = CliError(code = "INVALID_ARGS", message = "Usage: config-set <key> <value>"))
        }
        return CliEnvelope(ok = true, "config-set", data = NommService.setConfigValue(args[0], args[1]))
    }

    fun manifestRefresh(): CliEnvelope<ManifestRefreshResult> = CliEnvelope(ok = true, "manifest-refresh", data = NommService.refreshManifest())

    fun list(): CliEnvelope<List<ModDto>> = CliEnvelope(ok = true, "list", data = NommService.listInstalled())

    fun search(args: Array<String>): CliEnvelope<List<RepoModDto>> {
        if (args.isEmpty()) {
            return CliEnvelope(ok = false, "search", error = CliError(code = "INVALID_ARGS", message = "Usage: search <query>"))
        }
        return CliEnvelope(ok = true, "search", data = NommService.search(args.joinToString(" ")))
    }

    fun show(args: Array<String>): CliEnvelope<RepoModDto?> {
        if (args.isEmpty()) {
            return CliEnvelope(ok = false, "show", error = CliError(code = "INVALID_ARGS", message = "Usage: show <mod-id>"))
        }
        return CliEnvelope(ok = true, "show", data = NommService.show(args[0]))
    }

    fun bepinexInstall(): CliEnvelope<InstallResult> = CliEnvelope(ok = true, "bepinex-install", data = NommService.installBepInEx())

    fun modInstall(args: Array<String>): CliEnvelope<InstallResult> {
        if (args.isEmpty()) {
            return CliEnvelope(ok = false, "mod-install", error = CliError(code = "INVALID_ARGS", message = "Usage: mod-install <mod-id>"))
        }
        return CliEnvelope(ok = true, "mod-install", data = NommService.installMod(args[0]))
    }

    fun update(args: Array<String>): CliEnvelope<InstallResult> {
        if (args.isEmpty()) {
            return CliEnvelope(ok = false, "update", error = CliError(code = "INVALID_ARGS", message = "Usage: update <mod-id> or update --all"))
        }
        val result = if (args[0] == "--all") {
            val batch = NommService.updateAll()
            InstallResult(
                modId = "all",
                requestedVersion = null,
                installedVersion = null,
                enabled = true,
                dependenciesInstalled = batch.succeeded,
                wasAlreadyInstalled = false
            )
        } else {
            NommService.updateMod(args[0])
        }
        return CliEnvelope(ok = true, "update", data = result)
    }

    fun enable(args: Array<String>): CliEnvelope<ModStateResult> {
        if (args.isEmpty()) {
            return CliEnvelope(ok = false, "enable", error = CliError(code = "INVALID_ARGS", message = "Usage: enable <mod-id>"))
        }
        return CliEnvelope(ok = true, "enable", data = NommService.enableMod(args[0]))
    }

    fun disable(args: Array<String>): CliEnvelope<ModStateResult> {
        if (args.isEmpty()) {
            return CliEnvelope(ok = false, "disable", error = CliError(code = "INVALID_ARGS", message = "Usage: disable <mod-id>"))
        }
        return CliEnvelope(ok = true, "disable", data = NommService.disableMod(args[0]))
    }

    fun uninstall(args: Array<String>): CliEnvelope<ModStateResult> {
        if (args.isEmpty()) {
            return CliEnvelope(ok = false, "uninstall", error = CliError(code = "INVALID_ARGS", message = "Usage: uninstall <mod-id>"))
        }
        return CliEnvelope(ok = true, "uninstall", data = NommService.uninstallMod(args[0]))
    }

    fun addFile(args: Array<String>): CliEnvelope<AddFileResult> {
        if (args.isEmpty()) {
            return CliEnvelope(ok = false, "add-file", error = CliError(code = "INVALID_ARGS", message = "Usage: add-file <path>"))
        }
        return CliEnvelope(ok = true, "add-file", data = NommService.addFile(args[0]))
    }

   fun import(args: Array<String>): CliEnvelope<BatchResult> {
        if (args.isEmpty()) {
            return CliEnvelope(ok = false, "import", error = CliError(code = "INVALID_ARGS", message = "Usage: import <path>"))
        }
        val result = NommService.importModpack(args[0])
        return CliEnvelope(ok = true, "import", data = result)
    }

    fun export(args: Array<String>): CliEnvelope<Boolean> {
        if (args.isEmpty()) {
            return CliEnvelope(ok = false, "export", error = CliError(code = "INVALID_ARGS", message = "Usage: export <path>"))
        }
        NommService.exportModpack(args[0])
        return CliEnvelope(ok = true, "export", data = true)
    }
}
