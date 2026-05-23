package com.combat.nomm.core

import com.combat.nomm.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.StandardOpenOption

object NommService {

    val isLocked: Boolean
        get() = lockFileChannel?.isOpen == true

    private var lockFileChannel: FileChannel? = null
    private var currentLock: FileLock? = null

    private fun getLockFile(): File {
        val dir = File(System.getProperty("user.home"), ".nomm")
        dir.mkdirs()
        return File(dir, "nomm.lock")
    }

    fun acquireLock(): Boolean {
        if (currentLock != null && currentLock!!.isValid) return true
        return try {
            val lockFile = getLockFile()
            lockFileChannel = FileChannel.open(
                lockFile.toPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE
            )
            currentLock = lockFileChannel!!.tryLock()
            if (currentLock == null) {
                lockFileChannel?.close()
                lockFileChannel = null
                false
            } else true
        } catch (_: OverlappingFileLockException) {
            false
        } catch (_: IOException) {
            false
        }
    }

    fun releaseLock() {
        try {
            currentLock?.release()
        } catch (_: Exception) {}
        try {
            lockFileChannel?.close()
        } catch (_: Exception) {}
        currentLock = null
        lockFileChannel = null
    }

    inline fun <T> withLock(action: () -> T): T {
        if (!acquireLock()) {
            throw IllegalStateException("Cannot acquire lock. Another NOMM instance may be running.")
        }
        try {
            return action()
        } finally {
            releaseLock()
        }
    }

    fun getStatus(): StatusResult = runBlocking {
        LocalMods.refresh()
        val config = SettingsManager.config.value
        val gameFolder = config.gamePath?.let { File(it) }
        val bepInExFolder = gameFolder?.let { File(it, "BepInEx") }
        val mods = LocalMods.mods.value
        StatusResult(
            gamePath = config.gamePath,
            gamePathExists = gameFolder?.exists() ?: false,
            gameExeFound = gameFolder?.let { File(it, "NuclearOption.exe").exists() } ?: false,
            bepInExInstalled = bepInExFolder?.exists() ?: false,
            pluginsDirExists = bepInExFolder?.let { File(it, "plugins").exists() } ?: false,
            disabledPluginsDirExists = bepInExFolder?.let { File(it, "disabledPlugins").exists() } ?: false,
            manifestUrl = config.manifestUrl,
            manifestLoaded = RepoMods.mods.value.isNotEmpty(),
            manifestModCount = RepoMods.mods.value.size,
            installedModCount = mods.size,
            enabledModCount = mods.count { it.value.enabled == true },
            disabledModCount = mods.count { it.value.enabled == false },
            version = BuildKonfig.VERSION
        )
    }

    fun doctor(): DoctorResult = runBlocking {
        LocalMods.refresh()
        val config = SettingsManager.config.value
        val gameFolder = config.gamePath?.let { File(it) }
        val bepInExFolder = gameFolder?.let { File(it, "BepInEx") }
        val issues = mutableListOf<String>()

        if (config.gamePath.isNullOrBlank()) {
            issues.add("Game path is not configured. Use 'nomm config set gamePath <path>'")
        } else if (gameFolder?.exists() != true) {
            issues.add("Game path does not exist: ${config.gamePath}")
        } else {
            if (!File(gameFolder, "NuclearOption.exe").exists()) {
                issues.add("NuclearOption.exe not found in game path")
            }
            if (bepInExFolder?.exists() != true) {
                issues.add("BepInEx not installed in game path")
            } else {
                if (!File(bepInExFolder, "plugins").exists()) {
                    issues.add("BepInEx/plugins directory missing")
                }
                if (!File(bepInExFolder, "disabledPlugins").exists()) {
                    issues.add("BepInEx/disabledPlugins directory missing")
                }
            }
        }

        val manifestReachable = runCatching {
            RepoMods.fetchManifestBlocking()
            RepoMods.mods.value.isNotEmpty()
        }.getOrNull()

        if (manifestReachable == false) {
            issues.add("Cannot fetch NOMNOM manifest. Check network or manifest URL.")
        }

        val mods = LocalMods.mods.value
        DoctorResult(
            gamePath = config.gamePath,
            gamePathExists = gameFolder?.exists() ?: false,
            executableFound = gameFolder?.let { File(it, "NuclearOption.exe").exists() } ?: false,
            bepInExExists = bepInExFolder?.exists() ?: false,
            pluginsDirExists = bepInExFolder?.let { File(it, "plugins").exists() } ?: false,
            disabledPluginsDirExists = bepInExFolder?.let { File(it, "disabledPlugins").exists() } ?: false,
            manifestUrl = config.manifestUrl,
            manifestReachable = manifestReachable,
            installedModCount = mods.size,
            enabledModCount = mods.count { it.value.enabled == true },
            disabledModCount = mods.count { it.value.enabled == false },
            issues = issues,
            version = BuildKonfig.VERSION
        )
    }

    fun getConfig(): Configuration = SettingsManager.config.value

    fun setConfigValue(key: String, value: String): ConfigResult = runBlocking {
        val current = SettingsManager.config.value
        val updated = when (key) {
            "gamePath" -> current.copy(gamePath = value)
            "manifestUrl" -> current.copy(manifestUrl = value)
            "manifestVersionUrl" -> current.copy(manifestVersionUrl = value)
            "ignoreHashMismatch" -> current.copy(ignoreHashMismatch = value.toBooleanStrictOrNull() ?: current.ignoreHashMismatch)
            "ignoreManifestVersion" -> current.copy(ignoreManifestVersion = value.toBooleanStrictOrNull() ?: current.ignoreManifestVersion)
            "fakeManifest" -> current.copy(fakeManifest = value.toBooleanStrictOrNull() ?: current.fakeManifest)
            else -> throw IllegalArgumentException("Unknown config key: $key")
        }
        SettingsManager.updateConfig(updated)
        SettingsManager.saveConfig()
        ConfigResult(key = key, value = value)
    }

    fun refreshManifest(): ManifestRefreshResult = runBlocking {
        val beforeVersion = SettingsManager.cachedManifest.value.version.toString()
        RepoMods.fetchManifestBlocking()
        val cachedManifest = SettingsManager.cachedManifest.value
        ManifestRefreshResult(
            version = cachedManifest.version.toString(),
            modCount = RepoMods.mods.value.size,
            cached = cachedManifest.manifest.isNotEmpty()
        )
    }

    fun listInstalled(): List<ModDto> {
        LocalMods.refresh()
        return LocalMods.mods.value.map { (_, meta) -> meta.toDto() }.sortedBy { it.id }
    }

    fun search(query: String): List<RepoModDto> {
        val manifest = RepoMods.mods.value
        val q = query.lowercase()
        val results = manifest.filter { mod ->
            mod.id.lowercase().contains(q) ||
            mod.displayName.lowercase().contains(q) ||
            mod.description.lowercase().contains(q) ||
            mod.authors.any { it.lowercase().contains(q) }
        }
        return results.map { it.toDto() }
    }

    fun show(id: String): RepoModDto? {
        val mod = RepoMods.mods.value.find { it.id == id }
        return mod?.toDto()
    }

    fun installBepInEx(): InstallResult = withLock {
        runBlocking {
            if (LocalMods.isBepInExInstalled.value) {
                return@runBlocking InstallResult(
                    modId = "BepInEx",
                    requestedVersion = null,
                    installedVersion = null,
                    enabled = true,
                    dependenciesInstalled = emptyList(),
                    wasAlreadyInstalled = true
                )
            }
            RepoMods.downloadBepInExBlocking()
            LocalMods.refresh()
            InstallResult(
                modId = "BepInEx",
                requestedVersion = null,
                installedVersion = "5.4.23.4",
                enabled = true,
                dependenciesInstalled = emptyList(),
                wasAlreadyInstalled = false
            )
        }
    }

    fun installMod(id: String, version: String? = null, enable: Boolean = true): InstallResult = withLock {
        runBlocking {
            val requestedVersion = version?.let { Version(*it.split(".").map { s -> s.toIntOrNull() ?: 0 }.toIntArray()) }
            val installedMod = LocalMods.mods.value[id]

            if (installedMod != null) {
                val currentVer = installedMod.artifact?.version
                val versionMatch = if (requestedVersion != null) currentVer == requestedVersion else true
                if (currentVer != null && versionMatch) {
                    return@runBlocking InstallResult(
                        modId = id,
                        requestedVersion = version,
                        installedVersion = currentVer.toString(),
                        enabled = installedMod.enabled == true,
                        dependenciesInstalled = emptyList(),
                        wasAlreadyInstalled = true
                    )
                }
            }

            val depCountBefore = LocalMods.mods.value.size
            RepoMods.installModBlocking(id, requestedVersion)
            LocalMods.refresh()
            val depCountAfter = LocalMods.mods.value.size

            val installedVersion = LocalMods.mods.value[id]?.artifact?.version?.toString()
            InstallResult(
                modId = id,
                requestedVersion = version,
                installedVersion = installedVersion,
                enabled = LocalMods.mods.value[id]?.enabled == true,
                dependenciesInstalled = emptyList(),
                wasAlreadyInstalled = false
            )
        }
    }

    fun updateMod(id: String): InstallResult = withLock {
        runBlocking {
            RepoMods.installModBlocking(id, null)
            LocalMods.refresh()
            val meta = LocalMods.mods.value[id]
            InstallResult(
                modId = id,
                requestedVersion = null,
                installedVersion = meta?.artifact?.version?.toString(),
                enabled = meta?.enabled == true,
                dependenciesInstalled = emptyList(),
                wasAlreadyInstalled = false
            )
        }
    }

    fun updateAll(): BatchResult = withLock {
        runBlocking {
            val mods = LocalMods.mods.value
            val succeeded = mutableListOf<String>()
            val failed = mutableListOf<BatchItemFailure>()

            for ((id, _) in mods) {
                try {
                    RepoMods.installModBlocking(id, null)
                    succeeded.add(id)
                } catch (e: Exception) {
                    failed.add(BatchItemFailure(id, e.message ?: "Unknown error"))
                }
            }
            LocalMods.refresh()
            BatchResult(succeeded = succeeded, failed = failed)
        }
    }

    fun enableMod(id: String): ModStateResult = withLock {
        val meta = LocalMods.mods.value[id] ?: throw IllegalArgumentException("Mod not found: $id")
        if (meta.enabled == true) {
            return ModStateResult(id = id, enabled = true, action = "already_enabled")
        }
        meta.resolveProblems()
        val success = meta.enable()
        if (!success) throw IllegalStateException("Failed to enable mod: $id")
        LocalMods.refresh()
        ModStateResult(id = id, enabled = true, action = "enabled")
    }

    fun disableMod(id: String): ModStateResult = withLock {
        val meta = LocalMods.mods.value[id] ?: throw IllegalArgumentException("Mod not found: $id")
        if (meta.enabled == false) {
            return ModStateResult(id = id, enabled = false, action = "already_disabled")
        }
        meta.disable()
        LocalMods.refresh()
        ModStateResult(id = id, enabled = false, action = "disabled")
    }

    fun uninstallMod(id: String): ModStateResult = withLock {
        val meta = LocalMods.mods.value[id] ?: throw IllegalArgumentException("Mod not found: $id")
        meta.uninstall()
        LocalMods.refresh()
        ModStateResult(id = id, enabled = null, action = "uninstalled")
    }

    fun addFile(path: String, move: Boolean = false): AddFileResult = withLock {
        val sourceFile = File(path)
        if (!sourceFile.exists()) throw IllegalArgumentException("File not found: $path")

        val pluginsDir = File(SettingsManager.bepInExFolder, "plugins")
        if (!pluginsDir.exists()) pluginsDir.mkdirs()
        val destinationFile = File(pluginsDir, sourceFile.name)

        if (move) {
            sourceFile.moveTo(destinationFile)
        } else {
            sourceFile.copyTo(destinationFile, overwrite = true)
        }

        LocalMods.refresh()
        AddFileResult(
            fileName = sourceFile.name,
            destination = destinationFile.absolutePath,
            action = if (move) "moved" else "copied"
        )
    }

    fun importModpack(filePath: String): BatchResult = withLock {
        runBlocking {
            val file = File(filePath)
            if (!file.exists()) throw IllegalArgumentException("File not found: $filePath")
            val jsonString = file.readText()
            val imported: List<PackageReference> = com.combat.nomm.json.decodeFromString(jsonString)

            val succeeded = mutableListOf<String>()
            val failed = mutableListOf<BatchItemFailure>()

            RepoMods.fetchManifestBlocking()

            for (ref in imported) {
                try {
                    if (LocalMods.mods.value[ref.id] == null) {
                        RepoMods.installModBlocking(ref.id, ref.version)
                    }
                    succeeded.add(ref.id)
                } catch (e: Exception) {
                    failed.add(BatchItemFailure(ref.id, e.message ?: "Unknown error"))
                }
            }

            val importedIds = imported.map { it.id }.toSet()
            for ((id, meta) in LocalMods.mods.value) {
                if (id in importedIds) {
                    meta.enable()
                } else {
                    meta.disable()
                }
            }

            LocalMods.refresh()
            BatchResult(succeeded = succeeded, failed = failed)
        }
    }

    fun exportModpack(filePath: String) {
        val mods = LocalMods.mods.value.filter { it.value.enabled == true }
            .map { (_, meta) -> PackageReference(meta.id, meta.artifact?.version) }
        val jsonString = com.combat.nomm.json.encodeToString(mods)
        File(filePath).writeText(jsonString)
    }
}

private fun File.moveTo(destination: File): Boolean {
    if (!this.exists()) return false
    if (this.canonicalPath == destination.canonicalPath) return true
    return runCatching {
        destination.deleteRecursively()
        destination.parentFile?.mkdirs()
        java.nio.file.Files.move(
            this.toPath(),
            destination.toPath(),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE
        )
        true
    }.getOrElse {
        runCatching {
            this.copyRecursively(destination, overwrite = true)
            this.deleteRecursively()
            true
        }.getOrDefault(false)
    }
}
