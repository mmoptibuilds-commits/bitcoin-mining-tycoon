package com.antigravity.bitcoinminingtycoon.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.antigravity.bitcoinminingtycoon.data.migrations.SaveMigrations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object GameSaveSerializer : Serializer<GameSave> {
    override val defaultValue: GameSave = GameSave()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
        isLenient = true
    }

    override suspend fun readFrom(input: InputStream): GameSave {
        val raw = input.bufferedReader().use { it.readText() }
        if (raw.isBlank()) return defaultValue
        return try {
            SaveMigrations.migrate(raw)
        } catch (e: Exception) {
            throw CorruptionException("Failed to deserialize GameSave payload", e)
        }
    }

    override suspend fun writeTo(t: GameSave, output: OutputStream) {
        val raw = json.encodeToString(GameSave.serializer(), t)
        output.bufferedWriter().use { it.write(raw) }
    }
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
    private val dataStore: DataStore<GameSave>
) : SaveDataSource {

    override val saveFlow: Flow<GameSave> = dataStore.data.catch { exception ->
        if (exception is CorruptionException) {
            emit(GameSave())
        } else {
            throw exception
        }
    }

    override suspend fun update(transform: suspend (GameSave) -> GameSave): GameSave {
        return dataStore.updateData { current ->
            transform(current)
        }
    }

    override suspend fun clear() {
        dataStore.updateData { GameSave() }
    }
}
