package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.content.UpgradeSpecialEffect
import com.antigravity.bitcoinminingtycoon.content.UpgradeSpecialEffectType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import kotlin.random.Random

class GameEngineTest {

    @Test
    fun tenthSecondTicksAccumulatePersistedFractionalPlaytime() {
        var state = GameState(miners = mapOf("ancient_cpu" to 1L))
        repeat(10) { index ->
            state = GameEngine.tick(state, 0.1, wallMillis = 1_700_000_000_000L + index * 100L)
        }

        assertEquals(1L, state.stats.totalPlaytimeSeconds)
        assertEquals("0", state.stats.playtimeFractionalSeconds)
    }

    @Test
    fun randomConsumingTickAdvancesSeedAndSurvivesSaveReload() {
        val original = GameState(rngSeed = 42L)
        val firstTick = GameEngine.tick(original, 2.0, 1_700_000_000_000L)
        assertNotEquals(original.rngSeed, firstTick.rngSeed)

        val restored = GameSave.fromGameState(firstTick, 1_700_000_002_000L).toGameState()
        assertEquals(
            GameEngine.tick(firstTick.copy(lastSaveWallMillis = 1_700_000_002_000L), 2.0, 1_700_000_002_000L),
            GameEngine.tick(restored, 2.0, 1_700_000_002_000L)
        )
    }

    @Test
    fun foregroundMiningUsesActiveEventOnlyUntilItsExpiryInsideTick() {
        val end = 1_700_000_010_000L
        val state = GameState(
            miners = mapOf("ancient_cpu" to 5L),
            activeEvents = listOf(
                ActiveEventState("bull_run", end - 5_000L, multiplier = 2.0)
            )
        )
        val baseHashrate = EconomyEngine.calculateEffectiveHashrate(state.copy(activeEvents = emptyList()))
        val expected = EconomyEngine.calculateMinedBtc(baseHashrate.multiply(BigDecimal("2")), 5.0)
            .add(EconomyEngine.calculateMinedBtc(baseHashrate, 5.0))

        val after = GameEngine.tick(state, 10.0, end)

        assertEquals(0, expected.compareTo(after.btcBigDecimal))
        assertTrue(after.activeEvents.isEmpty())
    }

    @Test
    fun performManualTap_incrementsStatsAndBtc() {
        val initial = GameState(btc = "0", manualHashStrength = "10")
        val afterTap = GameEngine.performManualTap(initial)

        assertTrue(afterTap.btcBigDecimal > BigDecimal.ZERO)
        assertEquals(1L, afterTap.stats.totalManualTaps)
        assertEquals(afterTap.btcBigDecimal, afterTap.stats.lifetimeBtcBigDecimal)
    }

    @Test
    fun criticalTapUsesSavedDeterministicRngOnlyWhenUpgradeIsOwned() {
        val effect = UpgradeEngine.criticalTapEffect(
            GameState(purchasedUpgrades = setOf("quantum_fingerprints"))
        )
        assertTrue("Quantum Fingerprints must carry the approved rare critical tap effect", effect != null)
        assertEquals(UpgradeSpecialEffectType.CRITICAL_TAP, effect?.type)
        val critical = effect as UpgradeSpecialEffect.CriticalTap
        assertEquals(0.02, critical.chance, 0.0)
        assertEquals(2.0, critical.payoutMultiplier, 0.0)

        val criticalSeed = (0L..100_000L).first { Random(it).nextDouble() < critical.chance }
        val normalSeed = (0L..100_000L).first { Random(it).nextDouble() >= critical.chance }
        val criticalRandom = Random(criticalSeed)
        assertTrue(criticalRandom.nextDouble() < critical.chance)
        val expectedCriticalSeed = criticalRandom.nextLong()
        val normalRandom = Random(normalSeed)
        assertTrue(normalRandom.nextDouble() >= critical.chance)
        val expectedNormalSeed = normalRandom.nextLong()

        val base = GameState(manualHashStrength = "50000")
        val criticalState = base.copy(
            purchasedUpgrades = setOf("quantum_fingerprints"),
            rngSeed = criticalSeed
        )
        val normalState = criticalState.copy(rngSeed = normalSeed)
        val criticalTap = GameEngine.performManualTap(criticalState)
        val normalTap = GameEngine.performManualTap(normalState)
        val expectedBaseTap = EconomyEngine.calculateManualTapOutput(criticalState)

        assertEquals(0, expectedBaseTap.multiply(BigDecimal("2")).compareTo(criticalTap.btcBigDecimal))
        assertEquals(0, expectedBaseTap.compareTo(normalTap.btcBigDecimal))
        assertEquals(expectedCriticalSeed, criticalTap.rngSeed)
        assertEquals(expectedNormalSeed, normalTap.rngSeed)

        val noEffectTap = GameEngine.performManualTap(base.copy(rngSeed = criticalSeed))
        assertEquals(criticalSeed, noEffectTap.rngSeed)
    }

    @Test
    fun tick_producesPassiveBtcAndExpiresEvents() {
        val initial = GameState(
            btc = "0",
            miners = mapOf("ancient_cpu" to 100L), // 100 H/s
            activeEvents = listOf(
                ActiveEventState("expired_event", expiresAtWallMillis = 500L, multiplier = 2.0),
                ActiveEventState("valid_event", expiresAtWallMillis = 5000L, multiplier = 1.5)
            )
        )

        // Run tick at wall time 1000L (expired_event should be removed)
        val next = GameEngine.tick(
            state = initial,
            deltaSeconds = 1.0,
            wallMillis = 1000L
        )

        assertEquals(1, next.activeEvents.size)
        assertEquals("valid_event", next.activeEvents.first().eventId)
        assertTrue(next.btcBigDecimal > BigDecimal.ZERO)
        assertEquals(1L, next.stats.totalPlaytimeSeconds)
    }

    @Test
    fun tick_autoSellConvertsBtcToUsdWhenPriceAboveThreshold() {
        val initial = GameState(
            btc = "0",
            usd = "0",
            powerGridTier = 5,
            coolingTier = 5,
            miners = mapOf("entry_asic" to 10L), // 120,000 H/s
            marketPrice = "65000.00",
            autoSellEnabled = true,
            autoSellThresholdUsd = "60000.00"
        )

        val next = GameEngine.tick(
            state = initial,
            deltaSeconds = 1.0,
            wallMillis = 1000L
        )

        // BTC should be converted into USD directly
        assertTrue("USD should increase from auto-sell", next.usdBigDecimal > BigDecimal.ZERO)
        assertEquals(0, BigDecimal.ZERO.compareTo(next.btcBigDecimal))
    }

    @Test
    fun autoSellUsesTheSameMarketUpgradeMultiplierAsManualSales() {
        val initial = GameState(
            powerGridTier = 5,
            coolingTier = 5,
            miners = mapOf("entry_asic" to 10L),
            marketPrice = "65000.00",
            autoSellEnabled = true,
            autoSellThresholdUsd = "60000.00"
        )

        val baseline = GameEngine.tick(initial, deltaSeconds = 1.0, wallMillis = 1000L)
        val upgraded = GameEngine.tick(
            initial.copy(purchasedUpgrades = setOf("market_ticker_display")),
            deltaSeconds = 1.0,
            wallMillis = 1000L
        )

        assertTrue(upgraded.usdBigDecimal > baseline.usdBigDecimal)
        assertEquals(0, BigDecimal.ZERO.compareTo(upgraded.btcBigDecimal))
    }
}
