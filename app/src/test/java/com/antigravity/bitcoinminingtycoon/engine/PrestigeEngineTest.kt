package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.content.PrestigeNodes
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PrestigeEngineTest {

    @Test
    fun testSublinearPrestigePointsFormula() {
        assertEquals(0L, PrestigeEngine.calculateTotalPointsFromLifetimeBtc(BigDecimal("0")))
        assertEquals(0L, PrestigeEngine.calculateTotalPointsFromLifetimeBtc(BigDecimal("0.99999999")))
        assertEquals(1L, PrestigeEngine.calculateTotalPointsFromLifetimeBtc(BigDecimal("1.00000000")))
        assertEquals(2L, PrestigeEngine.calculateTotalPointsFromLifetimeBtc(BigDecimal("4.00000000")))
        assertEquals(10L, PrestigeEngine.calculateTotalPointsFromLifetimeBtc(BigDecimal("100.00000000")))
        assertEquals(100L, PrestigeEngine.calculateTotalPointsFromLifetimeBtc(BigDecimal("10000.00000000")))
    }

    @Test
    fun integerSquareRootIsExactAndSaturatesForHugeDecimalValues() {
        val max = BigDecimal(Long.MAX_VALUE.toString())
        assertEquals(Long.MAX_VALUE, PrestigeEngine.calculateTotalPointsFromLifetimeBtc(max.multiply(max)))
        assertEquals(
            Long.MAX_VALUE,
            PrestigeEngine.calculateTotalPointsFromLifetimeBtc(BigDecimal("1" + "0".repeat(200)))
        )
        assertEquals(3037000499L, PrestigeEngine.calculateTotalPointsFromLifetimeBtc(BigDecimal("9223372030926249001")))
    }

    @Test
    fun dailyLegacyPointsDoNotBlockNewPrestigeProgressAfterMigration() {
        val state = GameState(
            stats = StatsState(
                lifetimeBtcMined = "1002001",
                lifetimeSatoshiPointsEarned = 1000,
                prestigePointsBaselineV2 = 1000,
                prestigePointsEarnedSinceV2 = 0,
                dailyPointsEarnedSinceV2 = 1000
            )
        )

        val preview = PrestigeEngine.previewPrestige(state)
        assertEquals(1L, preview.earnablePoints)
        val awarded = PrestigeEngine.applyPrestige(state)
        assertEquals(1L, awarded.stats.prestigePointsEarnedSinceV2)
        assertEquals(1001L, awarded.stats.lifetimeSatoshiPointsEarned)
        assertEquals(1000L, awarded.stats.dailyPointsEarnedSinceV2)
    }

    @Test
    fun testPreviewAndApplyEquivalence() {
        val state = GameState(
            stats = StatsState(
                lifetimeBtcMined = "25.00000000" // sqrt(25) = 5 SP
            )
        )

        val preview = PrestigeEngine.previewPrestige(state)
        assertEquals(5L, preview.earnablePoints)
        assertTrue(preview.isPrestigeAvailable)

        val resetState = PrestigeEngine.applyPrestige(state)
        assertEquals(5L, resetState.satoshiPoints)
        assertEquals(5L, resetState.stats.lifetimeSatoshiPointsEarned)
        assertEquals(1L, resetState.stats.totalPrestiges)

        // Subsequent preview shows 0 earnable points
        val secondPreview = PrestigeEngine.previewPrestige(resetState)
        assertEquals(0L, secondPreview.earnablePoints)
        assertFalse(secondPreview.isPrestigeAvailable)
    }

    @Test
    fun testZeroPointsPrestigeIsNoOp() {
        val state = GameState(
            btc = "0.50000000",
            usd = "1000.00",
            miners = mapOf(Miners.ALL[0].id to 10L),
            stats = StatsState(lifetimeBtcMined = "0.50000000") // 0 SP
        )

        val result = PrestigeEngine.applyPrestige(state)
        assertEquals(state, result)
    }

    @Test
    fun testPrestigeHardResetPreservesDocumentedPermanentData() {
        val state = GameState(
            btc = "10.00000000",
            usd = "50000.00",
            miners = mapOf(Miners.ALL[0].id to 50L),
            purchasedUpgrades = setOf("ancient_firmware", "overclock_1"),
            powerGridTier = 3,
            coolingTier = 2,
            satoshiPoints = 2L,
            purchasedPrestigeNodes = setOf("cold_start", "industrial_memory"),
            achievements = setOf("first_hash", "first_rig"),
            dailyRewardDay = 4,
            stats = StatsState(
                lifetimeBtcMined = "100.00000000", // sqrt(100) = 10 SP total -> 10 - 2 = 8 earnable
                lifetimeSatoshiPointsEarned = 2L,
                prestigePointsBaselineV2 = 2L,
                totalPrestiges = 1L
            )
        )

        val reset = PrestigeEngine.applyPrestige(state)

        // Hard reset fields
        assertEquals("0", reset.btc)
        assertEquals(emptyMap<String, Long>(), reset.miners)
        assertEquals(emptySet<String>(), reset.purchasedUpgrades)
        assertEquals(1, reset.coolingTier)

        // Preserved & modified by nodes
        assertEquals("500.00", reset.usd) // cold_start node gives $500 starting cash
        assertEquals(2, reset.powerGridTier) // industrial_memory node gives Tier 2 grid
        assertEquals(10L, reset.satoshiPoints) // 2 previous + 8 earned = 10 SP
        assertEquals(10L, reset.stats.lifetimeSatoshiPointsEarned)
        assertEquals(2L, reset.stats.totalPrestiges)
        assertEquals(setOf("cold_start", "industrial_memory"), reset.purchasedPrestigeNodes)
        assertEquals(4, reset.dailyRewardDay)
        assertTrue(reset.achievements.contains("first_hash"))
    }

    @Test
    fun prestigeCountsQueuedAutoSellBitcoinAsResetAndClearsItsQueue() {
        val state = GameState(
            btc = "1.25",
            autoSellPendingBtc = "0.0000000125",
            stats = StatsState(lifetimeBtcMined = "4")
        )

        val preview = PrestigeEngine.previewPrestige(state)
        val reset = PrestigeEngine.applyPrestige(state)

        assertEquals("1.2500000125", preview.currentBtcToLose)
        assertEquals("0", reset.btc)
        assertEquals("0", reset.autoSellPendingBtc)
    }

    @Test
    fun testPrestigeNodePurchasingRules() {
        val state0 = GameState(satoshiPoints = 1L)

        // 1. Buy Tier 1 node: efficient_silicon (cost 1 SP)
        val state1 = PrestigeEngine.buyPrestigeNode(state0, "efficient_silicon")
        assertEquals(0L, state1.satoshiPoints)
        assertTrue(state1.purchasedPrestigeNodes.contains("efficient_silicon"))

        // 2. Cannot buy diamond_hands: lacks cold_start prereq and has 0 SP
        val state2 = PrestigeEngine.buyPrestigeNode(state1, "diamond_hands")
        assertEquals(state1, state2)

        // 3. Fund with 10 SP, buy cold_start, then buy diamond_hands
        val stateWithSp = state1.copy(satoshiPoints = 10L)
        val state3 = PrestigeEngine.buyPrestigeNode(stateWithSp, "cold_start")
        assertEquals(9L, state3.satoshiPoints)
        assertTrue(state3.purchasedPrestigeNodes.contains("cold_start"))

        val state4 = PrestigeEngine.buyPrestigeNode(state3, "diamond_hands")
        assertEquals(4L, state4.satoshiPoints) // 9 - 5 = 4 SP
        assertTrue(state4.purchasedPrestigeNodes.contains("diamond_hands"))

        // 4. Cannot re-buy diamond_hands
        val state5 = PrestigeEngine.buyPrestigeNode(state4, "diamond_hands")
        assertEquals(state4, state5)
    }
}
