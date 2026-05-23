package com.combat.nomm.cli

import com.combat.nomm.core.CliEnvelope
import com.combat.nomm.core.CliError
import com.combat.nomm.json
import kotlinx.serialization.encodeToString

enum class ExitCode(val code: Int) {
    SUCCESS(0),
    GENERAL_ERROR(1),
    INVALID_ARGS(2),
    INVALID_CONFIG(3),
    MANIFEST_FETCH_FAILED(4),
    MOD_NOT_FOUND(5),
    INSTALL_FAILED(6),
    FILESYSTEM_ERROR(7),
    HASH_MISMATCH(8),
    LOCK_HELD(9)
}

fun printJson(command: String, ok: Boolean, data: Any? = null, warnings: List<String> = emptyList(), error: CliError? = null) {
    val envelope = CliEnvelope(
        ok = ok,
        command = command,
        data = data,
        warnings = warnings,
        error = error
    )
    println(json.encodeToString(envelope))
}

fun printError(command: String, message: String, code: String = "GENERAL_ERROR", details: Map<String, String> = emptyMap()) {
    val envelope = CliEnvelope(
        ok = false,
        command = command,
        data = null,
        warnings = emptyList(),
        error = CliError(code = code, message = message, details = details)
    )
    System.err.println(json.encodeToString(envelope))
}

fun exit(code: ExitCode): Nothing = kotlin.system.exitProcess(code.code)
