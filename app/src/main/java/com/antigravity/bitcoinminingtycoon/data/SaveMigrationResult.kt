package com.antigravity.bitcoinminingtycoon.data

sealed interface SaveMigrationResult {
    data class Ready(
        val save: GameSave,
        val migratedFromVersion: Int?,
        val recoveryWarnings: List<String> = emptyList()
    ) : SaveMigrationResult

    data class RecoveryRequired(
        val reason: String,
        val schemaVersion: Int? = null,
        val checkpointed: Boolean = false
    ) : SaveMigrationResult
}

sealed interface SaveReadiness {
    data object Loading : SaveReadiness
    data object Ready : SaveReadiness
    data object RecoveryRestartRequired : SaveReadiness
    data class CorruptCheckpointed(val reason: String) : SaveReadiness
    data class CorruptUncheckpointed(val reason: String) : SaveReadiness
    data class UnsupportedSchema(val schemaVersion: Int) : SaveReadiness
    data class PersistenceFailed(val reason: String) : SaveReadiness
}
