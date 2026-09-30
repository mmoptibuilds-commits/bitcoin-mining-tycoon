package com.antigravity.bitcoinminingtycoon.journey

import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.engine.BulkMode
import com.antigravity.bitcoinminingtycoon.engine.FleetEngine
import com.antigravity.bitcoinminingtycoon.engine.PrestigeEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PrestigeJourneyTest {

    @Test
    fun testPrestigeAndSatoshiTreeEndToEndJourney() {
        var state = GameState(
            btc = "5.00000000",
            usd = "25000.00",
            stats = StatsState(
                lifetimeBtcMined = "25.00000000", // sqrt(25) = 5 SP
                lifetimeUsdEarned = "50000.00"
            )
        )

        // 1. Buy miners and upgrades
        state = FleetEngine.buyMiner(state, Miners.ALL[0].id, BulkMode.X10)
        assertTrue(state.miners.isNotEmpty())

        // 2. Preview Prestige
        val preview = PrestigeEngine.previewPrestige(state)
        assertEquals(5L, preview.earnablePoints)
        assertTrue(preview.isPrestigeAvailable)
        assertEquals(5L, preview.newSatoshiPointsTotal)

        // 3. Confirm Prestige Reset
        val resetState = PrestigeEngine.applyPrestige(state)
        state = resetState

        // Verify Hard Reset
        assertEquals("0", state.btc)
        assertEquals("0", state.usd) // No cold_start node yet
        assertEquals(emptyMap<String, Long>(), state.miners)
        assertEquals(emptySet<String>(), state.purchasedUpgrades)

        // Verify Preserved Progression
        assertEquals(5L, state.satoshiPoints)
        assertEquals(5L, state.stats.lifetimeSatoshiPointsEarned)
        assertEquals(1L, state.stats.totalPrestiges)
        assertTrue("Genesis reset achievement should unlock", state.achievements.contains("genesis_reset"))

        // 4. Enter Satoshi Tree and purchase permanent nodes
        // Buy cold_start (Tier 1, cost 1 SP)
        state = PrestigeEngine.buyPrestigeNode(state, "cold_start")
        assertEquals(4L, state.satoshiPoints)
        assertTrue(state.purchasedPrestigeNodes.contains("cold_start"))

        // Buy efficient_silicon (Tier 1, cost 1 SP)
        state = PrestigeEngine.buyPrestigeNode(state, "efficient_silicon")
        assertEquals(3L, state.satoshiPoints)
        assertTrue(state.purchasedPrestigeNodes.contains("efficient_silicon"))

        // Attempt to buy industrial_memory (cost 10 SP): fails due to insufficient SP
        val failState = PrestigeEngine.buyPrestigeNode(state, "industrial_memory")
        assertEquals(3L, failState.satoshiPoints)
        assertFalse(failState.purchasedPrestigeNodes.contains("industrial_memory"))

        // 5. Subsequent run: Mine up to 100 BTC lifetime (sqrt(100) = 10 SP total -> 10 - 5 = 5 earnable)
        state = state.copy(
            btc = "10.00000000",
            stats = state.stats.copy(lifetimeBtcMined = "100.00000000")
        )
        val secondPreview = PrestigeEngine.previewPrestige(state)
        assertEquals(5L, secondPreview.earnablePoints)

        // 6. Execute 2nd prestige
        state = PrestigeEngine.applyPrestige(state)

        // With cold_start unlocked, player starts with $500.00 USD!
        assertEquals("500.00", state.usd)
        assertEquals("0", state.btc)
        assertEquals(8L, state.satoshiPoints) // 3 remaining + 5 newly earned = 8 SP
        assertEquals(10L, state.stats.lifetimeSatoshiPointsEarned)
        assertEquals(2L, state.stats.totalPrestiges)

        // Now buy diamond_hands (requires cold_start, cost 5 SP)
        state = PrestigeEngine.buyPrestigeNode(state, "diamond_hands")
        assertEquals(3L, state.satoshiPoints) // 8 - 5 = 3 SP
        assertTrue(state.purchasedPrestigeNodes.contains("diamond_hands"))
    }
}
