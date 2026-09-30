package com.antigravity.bitcoinminingtycoon.data.migrations

import com.antigravity.bitcoinminingtycoon.data.GameSave
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Versioned schema migration manager.
 * Invariant 6: Every persistent schema change increments a save version and has a migration or safe default.
 */
object SaveMigrations {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun migrate(rawJson: String): GameSave {
        if (rawJson.isBlank()) {
            return GameSave()
        }

        return try {
            val rootElement = json.parseToJsonElement(rawJson)
            if (rootElement !is JsonObject) {
                return GameSave()
            }

            val version = rootElement["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 1

            when (version) {
                1 -> json.decodeFromString(GameSave.serializer(), rawJson)
                // Future migration hooks:
                // 2 -> migrateV1ToV2(rootElement)
                else -> {
                    // Safe recovery fallback for unknown versions
                    json.decodeFromString(GameSave.serializer(), rawJson)
                }
            }
        } catch (_: Exception) {
            // Malformed payload recovers safely to default state
            GameSave()
        }
    }
}
