package com.antigravity.bitcoinminingtycoon.data

import com.antigravity.bitcoinminingtycoon.data.migrations.SaveMigrations
import com.antigravity.bitcoinminingtycoon.engine.PrestigeEngine
import com.antigravity.bitcoinminingtycoon.engine.EconomyEngine
import com.antigravity.bitcoinminingtycoon.engine.PowerEngine
import com.antigravity.bitcoinminingtycoon.engine.ThermalEngine
import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.MarketTrend
import com.antigravity.bitcoinminingtycoon.util.NumberFormatPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.test.runTest
import androidx.datastore.core.CorruptionException
import java.nio.file.Files
import java.io.ByteArrayInputStream
import java.io.File
import java.math.BigDecimal
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SaveMigrationsTest {

    @Test
    fun migrate_preservesDistinctiveSchemaOneFixtureAndAddsSafeCurrentDefaults() {
        val result = SaveMigrations.migrate(fixture("mid.json"))
        assertTrue(result is SaveMigrationResult.Ready)
        val save = (result as SaveMigrationResult.Ready).save

        assertEquals(1, result.migratedFromVersion)
        assertEquals(GameSave.CURRENT_SCHEMA_VERSION, save.schemaVersion)
        assertEquals("12.340056789", save.btc)
        assertEquals("8765.43", save.usd)
        assertEquals("7654321.125", save.manualHashStrength)
        assertEquals(mapOf("ancient_cpu" to 12L, "gaming_cpu" to 4L, "gaming_gpu" to 2L, "dual_gpu_rig" to 1L), save.miners)
        assertEquals(setOf("copper_fingers", "cpu_overclock_1", "gpu_vram_tuning"), save.purchasedUpgrades)
        assertEquals(3, save.powerGridTier)
        assertEquals(2, save.coolingTier)
        assertEquals("63421.987", save.marketPrice)
        assertEquals(MarketTrend.VOLATILE, save.marketTrend)
        assertEquals(listOf("50000", "51234.56", "63421.987"), save.marketHistory)
        assertEquals(23.75, save.marketTimerSeconds, 0.0)
        assertEquals(8.5, save.eventTimerSeconds, 0.0)
        assertTrue(save.autoSellEnabled)
        assertEquals("42424.24", save.autoSellThresholdUsd)
        assertEquals("0", save.autoSellPendingBtc)
        assertEquals(2, save.activeEvents.size)
        assertEquals("bull_run", save.activeEvents[0].eventId)
        assertEquals(1726123456789L, save.activeEvents[0].expiresAtWallMillis)
        assertEquals(1.125, save.activeEvents[0].multiplier, 0.0)
        assertEquals(0.875, save.activeEvents[0].powerModifier, 0.0)
        assertEquals(1.25, save.activeEvents[0].heatModifier, 0.0)
        assertFalse(save.activeEvents[0].isWindfall)
        assertTrue(save.activeEvents[1].isWindfall)
        assertEquals(setOf("first_hash", "first_liquidation", "first_rig"), save.achievements)
        assertEquals(17L, save.satoshiPoints)
        assertEquals(setOf("efficient_silicon", "cold_start"), save.purchasedPrestigeNodes)
        assertEquals(6, save.dailyRewardDay)
        assertEquals(1726100001234L, save.lastDailyClaimWallMillis)
        assertEquals("123456.700000001234", save.stats.lifetimeBtcMined)
        assertEquals("1098765.4321", save.stats.lifetimeUsdEarned)
        assertEquals("987654321.0123", save.stats.peakHashrate)
        assertEquals(124578L, save.stats.totalManualTaps)
        assertEquals(1024L, save.stats.totalMinersPurchased)
        assertEquals(31L, save.stats.totalUpgradesPurchased)
        assertEquals("7654.321", save.stats.totalBtcSold)
        assertEquals("98765.43", save.stats.highestPriceObserved)
        assertEquals("123.45", save.stats.lowestPriceObserved)
        assertEquals(7L, save.stats.totalPrestiges)
        assertEquals(37L, save.stats.lifetimeSatoshiPointsEarned)
        assertEquals(908172L, save.stats.totalPlaytimeSeconds)
        assertEquals(19L, save.stats.totalEventsTriggered)
        assertTrue(save.onboardingCompleted)
        assertTrue(save.settings.soundEnabled)
        assertFalse(save.settings.hapticsEnabled)
        assertTrue(save.settings.reducedMotion)
        assertEquals(NumberFormatPreference.SCIENTIFIC, save.settings.numberFormat)
        assertEquals(1726123400001L, save.lastSaveWallMillis)
        assertEquals(-8765432101L, save.rngSeed)
        assertEquals(37L, save.stats.lifetimeSatoshiPointsEarned)
        assertEquals(0L, save.stats.dailyPointsEarnedSinceV2)
        assertEquals(
            minOf(save.stats.lifetimeSatoshiPointsEarned,
                PrestigeEngine.calculateTotalPointsFromLifetimeBtc(BigDecimal(save.stats.lifetimeBtcMined))),
            save.stats.prestigePointsBaselineV2
        )
        assertEquals(2, save.balanceRulesVersion)
        assertTrue(save.completedTeachingCueIds.isEmpty())
    }

    @Test
    fun legacyAndPreCoverageSchemaTwoSavesAreMarkedAsPartialSourceBreakdowns() {
        val legacy = SaveMigrations.migrate(fixture("mid.json")) as SaveMigrationResult.Ready
        val olderSchemaTwo = SaveMigrations.migrate(
            """{"schemaVersion":2,"stats":{"lifetimeBtcMined":"1","lifetimeUsdEarned":"2"}}"""
        ) as SaveMigrationResult.Ready

        assertFalse(legacy.save.stats.sourceBreakdownTrackedSinceV12)
        assertFalse(olderSchemaTwo.save.stats.sourceBreakdownTrackedSinceV12)
        assertTrue(GameSave().stats.sourceBreakdownTrackedSinceV12)
    }

    @Test
    fun schemaTwoSaveMigratesToThreeWithFractionalAutoSellQueueDefaultedAndOtherFieldsPreserved() {
        val result = SaveMigrations.migrate(
            """{"schemaVersion":2,"btc":"0.125","usd":"250.00","autoSellEnabled":true,"autoSellThresholdUsd":"64000.25","miners":{"ancient_cpu":3},"lastSaveWallMillis":1700000000000}"""
        ) as SaveMigrationResult.Ready

        assertEquals(2, result.migratedFromVersion)
        assertEquals(3, result.save.schemaVersion)
        assertEquals("0", result.save.autoSellPendingBtc)
        assertEquals("0.125", result.save.btc)
        assertEquals("250.00", result.save.usd)
        assertTrue(result.save.autoSellEnabled)
        assertEquals("64000.25", result.save.autoSellThresholdUsd)
        assertEquals(3L, result.save.miners["ancient_cpu"])
        assertEquals(1700000000000L, result.save.lastSaveWallMillis)
    }

    @Test
    fun malformedFractionalAutoSellQueueIsCheckpointWorthyAndRepaired() {
        val result = SaveMigrations.migrate(
            """{"schemaVersion":3,"btc":"0.5","autoSellPendingBtc":"-2"}"""
        ) as SaveMigrationResult.Ready

        assertEquals("0.5", result.save.btc)
        assertEquals("0", result.save.autoSellPendingBtc)
        assertTrue("Invalid autoSellPendingBtc" in result.recoveryWarnings)
    }

    @Test
    fun schemaThreePreservesValidFractionalAutoSellBitcoinAcrossMigration() {
        val result = SaveMigrations.migrate(
            """{"schemaVersion":3,"btc":"0.5","autoSellPendingBtc":"0.0000000125","autoSellEnabled":true}"""
        ) as SaveMigrationResult.Ready

        assertEquals(null, result.migratedFromVersion)
        assertEquals("0.5", result.save.btc)
        assertEquals("0.0000000125", result.save.autoSellPendingBtc)
        assertTrue(result.save.autoSellEnabled)
        assertTrue(result.recoveryWarnings.isEmpty())
    }

    @Test
    fun schemaThreeAcceptsExplicitNullPendingOfflineSummaryFromCurrentWriter() {
        val result = SaveMigrations.migrate(
            """{"schemaVersion":3,"btc":"0.5","pendingOfflineSummary":null}"""
        ) as SaveMigrationResult.Ready

        assertEquals(null, result.save.pendingOfflineSummary)
        assertTrue(result.recoveryWarnings.isEmpty())
    }

    @Test
    fun schemaThreeLegacyOfflineSummaryWithoutAverageHashrateRemainsValid() {
        val result = SaveMigrations.migrate(
            """{"schemaVersion":3,"btc":"0.5","pendingOfflineSummary":{"durationSeconds":60.0,"creditedBtc":"0.1","creditedAtWallMillis":100000}}"""
        ) as SaveMigrationResult.Ready
        val serialized = Json { encodeDefaults = true }.encodeToString(GameSave.serializer(), result.save)

        assertTrue(result.recoveryWarnings.isEmpty())
        assertTrue(serialized.contains("\"averageEffectiveHashrate\":null"))
    }

    @Test
    fun schemaThreeAcceptsMergedOfflineSummaryLongerThanOneOfflineSession() {
        val result = SaveMigrations.migrate(
            """{"schemaVersion":3,"btc":"0.5","pendingOfflineSummary":{"durationSeconds":86400.0,"creditedBtc":"0.2","creditedAtWallMillis":100000,"averageEffectiveHashrate":"1000"}}"""
        ) as SaveMigrationResult.Ready
        val serialized = Json { encodeDefaults = true }.encodeToString(GameSave.serializer(), result.save)

        assertTrue(result.recoveryWarnings.isEmpty())
        assertTrue(serialized.contains("\"durationSeconds\":86400.0"))
        assertTrue(serialized.contains("\"averageEffectiveHashrate\":\"1000\""))
    }

    @Test
    fun invalidOfflineSummaryHashrateIsReportedForCheckpointedRepair() {
        val result = SaveMigrations.migrate(
            """{"schemaVersion":3,"btc":"0.5","pendingOfflineSummary":{"durationSeconds":60.0,"creditedBtc":"0.1","creditedAtWallMillis":100000,"averageEffectiveHashrate":"NaN"}}"""
        ) as SaveMigrationResult.Ready

        assertTrue(result.recoveryWarnings.contains("Invalid pending offline summary"))
        assertEquals(null, result.save.pendingOfflineSummary)
    }

    @Test
    fun migrate_preservesUnknownContentAndDoesNotUseItForKnownAssetTotals() {
        val result = SaveMigrations.migrate(fixture("unknown-content.json")) as SaveMigrationResult.Ready

        assertEquals(5L, result.save.miners["ancient_cpu"])
        assertEquals(13L, result.save.miners["unknown_miner_v0"])
        assertTrue("unknown_upgrade_v0" in result.save.purchasedUpgrades)
    }

    @Test
    fun migrationNormalizesOnlyRecognizedLegacyManualHashDefaults() {
        val oldDefault = fixture("early.json").replace("\"manualHashStrength\":\"10\"", "\"manualHashStrength\":\"10.0\"")
        val migratedDefault = SaveMigrations.migrate(oldDefault) as SaveMigrationResult.Ready
        val migratedCustom = SaveMigrations.migrate(fixture("mid.json")) as SaveMigrationResult.Ready

        assertEquals("50000", migratedDefault.save.manualHashStrength)
        assertEquals("7654321.125", migratedCustom.save.manualHashStrength)
    }

    @Test
    fun everyArchivedSchemaOneFixtureMigratesWithoutRepairWarnings() {
        val fixtures = listOf(
            "early.json", "mid.json", "late.json", "prestiged.json", "daily-claimed.json",
            "settings-disabled.json", "large-value.json", "missing-optional.json",
            "unknown-content.json", "expired-event.json"
        )

        fixtures.forEach { name ->
            val result = SaveMigrations.migrate(fixture(name))
            assertTrue("$name should migrate: $result", result is SaveMigrationResult.Ready)
            result as SaveMigrationResult.Ready
            assertEquals("$name schema", GameSave.CURRENT_SCHEMA_VERSION, result.save.schemaVersion)
            assertEquals("$name repair warnings", emptyList<String>(), result.recoveryWarnings)
        }
    }

    @Test
    fun outOfRangeMarketPriceIsCheckpointWorthyAndReplacedBeforeWriting() {
        listOf("999.99", "1000000.01").forEach { invalidPrice ->
            val raw = """{"schemaVersion":2,"marketPrice":"$invalidPrice","marketHistory":["$invalidPrice"]}"""
            val result = SaveMigrations.migrate(raw) as SaveMigrationResult.Ready

            assertEquals(GameSave().marketPrice, result.save.marketPrice)
            assertTrue("Invalid marketPrice" in result.recoveryWarnings)
        }
    }

    @Test
    fun unknownSavedEventIdsArePreservedButExcludedFromEconomy() {
        val state = GameState(
            miners = mapOf("ancient_cpu" to 5L),
            activeEvents = listOf(
                ActiveEventState(
                    eventId = "unknown_event_v0",
                    expiresAtWallMillis = 2_000_000L,
                    multiplier = 100.0,
                    powerModifier = 100.0,
                    heatModifier = 100.0
                )
            )
        )
        val plain = state.copy(activeEvents = emptyList())

        assertEquals(0, EconomyEngine.calculateEffectiveHashrate(state).compareTo(EconomyEngine.calculateEffectiveHashrate(plain)))
        assertEquals(PowerEngine.calculateDemandKw(plain), PowerEngine.calculateDemandKw(state), 0.0)
        assertEquals(ThermalEngine.calculateEquilibriumTemp(plain), ThermalEngine.calculateEquilibriumTemp(state), 0.0)
    }

    @Test
    fun migrate_missingOptionalFieldsUsesDefaultsWithoutLosingAssets() {
        val result = SaveMigrations.migrate(fixture("missing-optional.json")) as SaveMigrationResult.Ready

        assertEquals("12.340056789", result.save.btc)
        assertEquals(23.75, result.save.marketTimerSeconds, 0.0)
        assertEquals(listOf(result.save.marketPrice), result.save.marketHistory)
        assertTrue(result.save.activeEvents.isEmpty())
        assertEquals(1726123400001L, result.save.lastSaveWallMillis)
    }

    @Test
    fun migrate_futureSchemaRequiresRecoveryInsteadOfReturningWritableDefaults() {
        val result = SaveMigrations.migrate("""{"schemaVersion":99,"btc":"500"}""")
        assertTrue(result is SaveMigrationResult.RecoveryRequired)
        assertEquals(99, (result as SaveMigrationResult.RecoveryRequired).schemaVersion)
    }

    @Test
    fun migrate_malformedExplicitSchemaVersionRequiresRecovery() {
        listOf(
            "{\"schemaVersion\":\"future\",\"btc\":\"500\"}",
            "{\"schemaVersion\":1.5,\"btc\":\"500\"}",
            "{\"schemaVersion\":{},\"btc\":\"500\"}",
            "{\"schemaVersion\":0,\"btc\":\"500\"}"
        ).forEach { payload ->
            assertTrue(
                "Malformed schema must remain read-only: $payload",
                SaveMigrations.migrate(payload) is SaveMigrationResult.RecoveryRequired
            )
        }
    }

    @Test
    fun migrate_malformedJsonRequiresRecoveryInsteadOfReturningNewGame() {
        val malformed = "{ \"schemaVersion\": 1, \"btc\": 0.000... INVALID JSON }"
        val result = SaveMigrations.migrate(malformed)
        assertTrue(result is SaveMigrationResult.RecoveryRequired)
        assertFalse(result is SaveMigrationResult.Ready)
    }

    @Test
    fun migrate_ignoresUnknownPropertiesOnSupportedSchema() {
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
        assertTrue(result is SaveMigrationResult.Ready)
        assertEquals("0.05", (result as SaveMigrationResult.Ready).save.btc)
        assertEquals("3000", result.save.usd)
    }

    @Test
    fun migrate_roundTripsSchemaTwoSaveIdempotently() {
        val first = SaveMigrations.migrate(fixture("late.json")) as SaveMigrationResult.Ready
        val encoded = kotlinx.serialization.json.Json { encodeDefaults = true }
            .encodeToString(GameSave.serializer(), first.save)
        val second = SaveMigrations.migrate(encoded) as SaveMigrationResult.Ready

        assertEquals(null, second.migratedFromVersion)
        assertEquals(first.save, second.save)
    }

    @Test
    fun serializerCheckpointsCorruptPayloadBeforeBlockingWrites() = runTest {
        val directory = Files.createTempDirectory("bmt-save-recovery").toFile()
        try {
            val payload = "{invalid-save".toByteArray()
            val untouched = payload.copyOf()
            val serializer = GameSaveSerializer(SaveRecoveryCheckpoint(directory))
            val error = try {
                serializer.readFrom(ByteArrayInputStream(payload))
                error("Expected recovery to block the save")
            } catch (failure: CorruptionException) {
                failure
            }

            assertTrue(error.findSaveRecoveryException()!!.readiness is SaveReadiness.CorruptCheckpointed)
            assertTrue(payload.contentEquals(untouched))
            assertTrue(File(directory, "save_recovery/corrupt-save.json").readBytes().contentEquals(payload))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun serializerRejectsOversizedPayloadWithoutCheckpointOrMutation() = runTest {
        val directory = Files.createTempDirectory("bmt-save-large").toFile()
        try {
            val payload = ByteArray(SaveRecoveryCheckpoint.MAX_BYTES + 1) { 'x'.code.toByte() }
            val error = try {
                GameSaveSerializer(SaveRecoveryCheckpoint(directory)).readFrom(ByteArrayInputStream(payload))
                error("Expected oversized save to block")
            } catch (failure: CorruptionException) {
                failure
            }
            assertTrue(error.findSaveRecoveryException()!!.readiness is SaveReadiness.CorruptUncheckpointed)
            assertFalse(File(directory, "save_recovery/corrupt-save.json").exists())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun serializerPreservesFutureSchemaAndBlocksCheckpointFailure() = runTest {
        val directory = Files.createTempDirectory("bmt-save-future").toFile()
        try {
            val future = """{"schemaVersion":77,"btc":"900"}""".toByteArray()
            val error = try {
                GameSaveSerializer(SaveRecoveryCheckpoint(directory)).readFrom(ByteArrayInputStream(future))
                error("Expected unsupported version to block")
            } catch (failure: CorruptionException) {
                failure
            }
            assertTrue(error.findSaveRecoveryException()!!.readiness is SaveReadiness.UnsupportedSchema)
            assertTrue(File(directory, "save_recovery/corrupt-save.json").readBytes().contentEquals(future))

            val blockedRoot = File(directory, "blocked")
            blockedRoot.mkdirs()
            File(blockedRoot, "save_recovery").writeText("prevents-directory-creation")
            val malformed = try {
                GameSaveSerializer(SaveRecoveryCheckpoint(blockedRoot)).readFrom(ByteArrayInputStream("bad".toByteArray()))
                error("Expected unavailable checkpoint storage to block")
            } catch (failure: CorruptionException) {
                failure
            }
            assertTrue(malformed.findSaveRecoveryException()!!.readiness is SaveReadiness.CorruptUncheckpointed)
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun serializerCheckpointsIndividuallyRepairedFieldsBeforeReturningMigratedSave() = runTest {
        val directory = Files.createTempDirectory("bmt-save-field-repair").toFile()
        try {
            val original = fixture("mid.json")
                .replace("\"marketTimerSeconds\":23.75", "\"marketTimerSeconds\":999999.0")
                .replace("\"63421.987\"", "\"1000000.01\"")
            val payload = original.toByteArray()
            val migrated = GameSaveSerializer(SaveRecoveryCheckpoint(directory)).readFrom(ByteArrayInputStream(payload))

            assertEquals("12.340056789", migrated.btc)
            assertEquals(mapOf("ancient_cpu" to 12L, "gaming_cpu" to 4L, "gaming_gpu" to 2L, "dual_gpu_rig" to 1L), migrated.miners)
            assertEquals(0.0, migrated.marketTimerSeconds, 0.0)
            assertEquals(GameSave().marketPrice, migrated.marketPrice)
            assertEquals(listOf("50000", "51234.56"), migrated.marketHistory)
            assertTrue(File(directory, "save_recovery/corrupt-save.json").readBytes().contentEquals(payload))
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun fixture(name: String): String =
        javaClass.getResourceAsStream("/saves/v1/$name")!!.bufferedReader().use { it.readText() }
}
