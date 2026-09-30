package com.antigravity.bitcoinminingtycoon.data

import com.antigravity.bitcoinminingtycoon.data.migrations.SaveMigrations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SaveMigrationsTest {

    @Test
    fun migrate_handlesBlankInputSafely() {
        val result = SaveMigrations.migrate("")
        assertNotNull(result)
        assertEquals(GameSave.CURRENT_SCHEMA_VERSION, result.schemaVersion)
        assertEquals("0", result.btc)
    }

    @Test
    fun migrate_handlesCorruptedMalformedJsonSafely() {
        val malformed = "{ \"schemaVersion\": 1, \"btc\": 0.000... INVALID JSON }"
        val result = SaveMigrations.migrate(malformed)
        assertNotNull(result)
        assertEquals(GameSave.CURRENT_SCHEMA_VERSION, result.schemaVersion)
        assertEquals("0", result.btc)
    }

    @Test
    fun migrate_handlesUnknownFuturePropertiesSafely() {
        val jsonWithUnknownFields = """
            {
                "schemaVersion": 1,
                "btc": "0.05",
                "usd": "3000",
                "nonExistentFeatureField": "futureValue",
                "unexpectedArray": [1, 2, 3]
            }
        """.trimIndent()

        val result = SaveMigrations.migrate(jsonWithUnknownFields)
        assertEquals("0.05", result.btc)
        assertEquals("3000", result.usd)
    }
}
