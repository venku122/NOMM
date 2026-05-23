package com.combat.nomm.core

import com.combat.nomm.Artifact
import com.combat.nomm.Extension
import com.combat.nomm.ModMeta
import com.combat.nomm.Version
import kotlinx.serialization.Serializable

@Serializable
data class CliError(
    val code: String,
    val message: String,
    val details: Map<String, String> = emptyMap()
)

@Serializable
data class CliEnvelope<T>(
    val ok: Boolean,
    val command: String,
    val data: T? = null,
    val warnings: List<String> = emptyList(),
    val error: CliError? = null
)

@Serializable
data class StatusResult(
    val gamePath: String?,
    val gamePathExists: Boolean,
    val gameExeFound: Boolean,
    val bepInExInstalled: Boolean,
    val pluginsDirExists: Boolean,
    val disabledPluginsDirExists: Boolean,
    val manifestUrl: String,
    val manifestLoaded: Boolean,
    val manifestModCount: Int,
    val installedModCount: Int,
    val enabledModCount: Int,
    val disabledModCount: Int,
    val version: String
)

@Serializable
data class DoctorResult(
    val gamePath: String?,
    val gamePathExists: Boolean,
    val executableFound: Boolean,
    val bepInExExists: Boolean,
    val pluginsDirExists: Boolean,
    val disabledPluginsDirExists: Boolean,
    val manifestUrl: String,
    val manifestReachable: Boolean?,
    val installedModCount: Int,
    val enabledModCount: Int,
    val disabledModCount: Int,
    val issues: List<String>,
    val version: String
)

@Serializable
data class ConfigResult(
    val key: String,
    val value: String?
)

@Serializable
data class ManifestRefreshResult(
    val version: String,
    val modCount: Int,
    val cached: Boolean
)

@Serializable
data class ModDto(
    val id: String,
    val name: String,
    val version: String?,
    val enabled: Boolean?,
    val hasUpdate: Boolean,
    val isUnidentified: Boolean,
    val problems: List<String>,
    val isFromRepo: Boolean
)

@Serializable
data class RepoModDto(
    val id: String,
    val displayName: String,
    val description: String,
    val tags: List<String>,
    val authors: List<String>,
    val downloadCount: Int?,
    val latestVersion: String?,
    val versions: List<String>,
    val dependencies: List<PackageRefDto>,
    val extends: PackageRefDto?,
    val incompatibilities: List<PackageRefDto>,
    val installed: Boolean,
    val enabled: Boolean?,
    val installedVersion: String?
)

@Serializable
data class PackageRefDto(
    val id: String,
    val version: String?
)

@Serializable
data class InstallResult(
    val modId: String,
    val requestedVersion: String?,
    val installedVersion: String?,
    val enabled: Boolean,
    val dependenciesInstalled: List<String>,
    val wasAlreadyInstalled: Boolean
)

@Serializable
data class BatchResult(
    val succeeded: List<String>,
    val failed: List<BatchItemFailure>
)

@Serializable
data class BatchItemFailure(
    val id: String,
    val error: String
)

@Serializable
data class ModStateResult(
    val id: String,
    val enabled: Boolean?,
    val action: String
)

@Serializable
data class AddFileResult(
    val fileName: String,
    val destination: String,
    val action: String
)

fun Extension.toDto(): RepoModDto {
    val latest = artifacts.maxByOrNull { it.version }
    val installedMod = com.combat.nomm.LocalMods.mods.value[id]
    return RepoModDto(
        id = id,
        displayName = displayName,
        description = description,
        tags = tags,
        authors = authors,
        downloadCount = downloadCount,
        latestVersion = latest?.version?.toString(),
        versions = artifacts.map { it.version.toString() },
        dependencies = latest?.dependencies?.map { PackageRefDto(it.id, it.version?.toString()) } ?: emptyList(),
        extends = latest?.extends?.let { PackageRefDto(it.id, it.version?.toString()) },
        incompatibilities = latest?.incompatibilities?.map { PackageRefDto(it.id, it.version?.toString()) } ?: emptyList(),
        installed = installedMod != null,
        enabled = installedMod?.enabled,
        installedVersion = installedMod?.artifact?.version?.toString()
    )
}

fun ModMeta.toDto(): ModDto {
    val repoMod = com.combat.nomm.RepoMods.mods.value.find { it.id == id }
    return ModDto(
        id = id,
        name = file?.name ?: id,
        version = artifact?.version?.toString(),
        enabled = enabled,
        hasUpdate = hasUpdate,
        isUnidentified = isUnidentified,
        problems = problems,
        isFromRepo = repoMod != null
    )
}
