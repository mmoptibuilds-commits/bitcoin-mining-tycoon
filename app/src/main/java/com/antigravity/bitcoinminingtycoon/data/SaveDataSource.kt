package com.antigravity.bitcoinminingtycoon.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.CorruptionException
import com.antigravity.bitcoinminingtycoon.data.migrations.SaveMigrations
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

class GameSaveSerializer(
    private val recoveryCheckpoint: SaveRecoveryCheckpoint,
    private val freshSaveAuthorizedOnLaunch: Boolean = false
) : Serializer<GameSave> {
    override val defaultValue: GameSave = GameSave()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
        isLenient = true
    }
    override suspend fun readFrom(input: InputStream): GameSave {
        val rawBytes = readBounded(input)
        if (rawBytes.size > SaveRecoveryCheckpoint.MAX_BYTES) {
            throw recoveryCorruption(
                SaveReadiness.CorruptUncheckpointed("Save exceeds the 4 MiB recovery limit")
            )
        }
        val raw = rawBytes.toString(StandardCharsets.UTF_8)
        return when (val result = SaveMigrations.migrate(raw)) {
            is SaveMigrationResult.Ready -> {
                if (result.recoveryWarnings.isNotEmpty()) {
                    val checkpointed = recoveryCheckpoint.checkpoint(rawBytes)
                    if (!checkpointed) {
                        throw recoveryCorruption(
                            SaveReadiness.CorruptUncheckpointed(result.recoveryWarnings.joinToString("; "))
                        )
                    }
                }
                result.save.copy(
                    requiresSchemaCommit = result.migratedFromVersion != null || result.recoveryWarnings.isNotEmpty()
                )
            }
            is SaveMigrationResult.RecoveryRequired -> {
                val checkpointed = recoveryCheckpoint.checkpoint(rawBytes)
                val safeForConfirmedFreshSave = result.schemaVersion == null || result.schemaVersion <= GameSave.CURRENT_SCHEMA_VERSION
                if (checkpointed && safeForConfirmedFreshSave && freshSaveAuthorizedOnLaunch) {
                    defaultValue
                } else if (checkpointed) {
                    val readiness = if (result.schemaVersion != null && result.schemaVersion > GameSave.CURRENT_SCHEMA_VERSION) {
                        SaveReadiness.UnsupportedSchema(result.schemaVersion)
                    } else {
                        SaveReadiness.CorruptCheckpointed(result.reason)
                    }
                    throw recoveryCorruption(readiness)
                } else {
                    throw recoveryCorruption(SaveReadiness.CorruptUncheckpointed(result.reason))
                }
            }
        }
    }

    override suspend fun writeTo(t: GameSave, output: OutputStream) {
        if (t.schemaVersion != GameSave.CURRENT_SCHEMA_VERSION) {
            throw IOException("Refusing to write unsupported save schema ${t.schemaVersion}")
        }
        val raw = json.encodeToString(GameSave.serializer(), t)
        output.bufferedWriter().use { it.write(raw) }
    }

    private fun recoveryCorruption(readiness: SaveReadiness): CorruptionException =
        CorruptionException("Save requires explicit recovery", SaveRecoveryException(readiness))

    private fun readBounded(input: InputStream): ByteArray {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        var remaining = SaveRecoveryCheckpoint.MAX_BYTES + 1
        while (remaining > 0) {
            val read = input.read(buffer, 0, minOf(buffer.size, remaining))
            if (read < 0) break
            result.write(buffer, 0, read)
            remaining -= read
        }
        return result.toByteArray()
    }
}

class SaveRecoveryException(
    val readiness: SaveReadiness
) : IOException(when (readiness) {
    is SaveReadiness.CorruptCheckpointed -> readiness.reason
    is SaveReadiness.CorruptUncheckpointed -> readiness.reason
    SaveReadiness.RecoveryRestartRequired -> "Explicit save recovery requires an app restart"
    is SaveReadiness.UnsupportedSchema -> "Save schema ${readiness.schemaVersion} is not supported"
    is SaveReadiness.PersistenceFailed -> readiness.reason
    SaveReadiness.Loading -> "Save is still loading"
    SaveReadiness.Ready -> "Save recovery requested"
})

fun Throwable.findSaveRecoveryException(): SaveRecoveryException? {
    var current: Throwable? = this
    while (current != null) {
        if (current is SaveRecoveryException) return current
        current = current.cause
    }
    return null
}

interface CheckpointedRecoveryDataSource {
    suspend fun authorizeFreshSaveOnNextLaunch(): Boolean
    fun acknowledgeFreshSaveCommitted()
}

/**
 * Interface for save storage operations.
 */
interface SaveDataSource {
    val saveFlow: Flow<GameSave>
    suspend fun update(transform: suspend (GameSave) -> GameSave): GameSave
    suspend fun clear()
}

/**
 * DataStore-backed persistent storage implementation.
 */
class DataStoreSaveDataSource(
    private val dataStore: DataStore<GameSave>,
    private val recoveryCheckpoint: SaveRecoveryCheckpoint? = null
) : SaveDataSource, CheckpointedRecoveryDataSource {

    override val saveFlow: Flow<GameSave> = dataStore.data

    override suspend fun update(transform: suspend (GameSave) -> GameSave): GameSave {
        return dataStore.updateData { current ->
            transform(current)
        }
    }

    override suspend fun clear() {
        dataStore.updateData { GameSave() }
    }

    override suspend fun authorizeFreshSaveOnNextLaunch(): Boolean =
        recoveryCheckpoint?.authorizeFreshSaveOnNextLaunch() == true

    override fun acknowledgeFreshSaveCommitted() {
        recoveryCheckpoint?.acknowledgeFreshSaveCommitted()
    }
}
