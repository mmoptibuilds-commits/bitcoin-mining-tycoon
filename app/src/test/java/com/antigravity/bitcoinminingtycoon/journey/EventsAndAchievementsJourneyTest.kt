package com.antigravity.bitcoinminingtycoon.journey

import com.antigravity.bitcoinminingtycoon.content.Events
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.engine.AchievementEngine
import com.antigravity.bitcoinminingtycoon.engine.BulkMode
import com.antigravity.bitcoinminingtycoon.engine.EventEngine
import com.antigravity.bitcoinminingtycoon.engine.FleetEngine
import com.antigravity.bitcoinminingtycoon.engine.GameEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class EventsAndAchievementsJourneyTest {

    @Test
    fun testEventsAndAchievementsFullCycleJourney() {
        var state = GameState()
        var currentWall = 1_000_000L

        // 1. Player starts: performs 1 manual tap
        state = GameEngine.performManualTap(state)
        assertTrue("Manual tap should yield positive BTC", state.btcBigDecimal > BigDecimal.ZERO)
        assertTrue("Genesis entropy achievement should unlock", state.achievements.contains("first_hash"))

        // 2. Fund player and purchase first hardware rig
        state = state.withUsd(BigDecimal("500.00"))
        state = FleetEngine.buyMiner(state, Miners.ALL[0].id, BulkMode.X1)
        val (stateWithMinerAchievements, _) = AchievementEngine.evaluate(state)
        state = stateWithMinerAchievements
        assertTrue("First rig achievement should unlock", state.achievements.contains("first_rig"))

        // 3. Operational tick advances event timer and triggers ambient event
        // Advance clock by 150 seconds to trigger event timer
        state = state.copy(eventTimerSeconds = 0.5)
        currentWall += 1000L
        state = GameEngine.tick(state, deltaSeconds = 1.0, wallMillis = currentWall)

        // Event should have rolled and triggered
        assertTrue("Event timer should have triggered an event", state.stats.totalEventsTriggered >= 1L)
        assertTrue("Should have at least 1 active event", state.activeEvents.isNotEmpty())
        assertTrue("Should unlock dynamic_environment achievement", state.achievements.contains("dynamic_environment"))

        // 4. Force a windfall event (Lucky Block)
        state = EventEngine.applyEvent(state, Events.LUCKY_BLOCK, currentWall)
        val activeWindfall = state.activeEvents.firstOrNull { it.isWindfall }
        assertNotNull("Active windfall event must be present", activeWindfall)
        assertEquals("lucky_block", activeWindfall?.eventId)

        // 5. Player claims Lucky Block before expiration (grants 0.00010000 BTC)
        val btcBefore = state.btcBigDecimal
        val (claimedState, reward) = EventEngine.claimWindfall(state, "lucky_block", currentWall + 5000L)
        assertTrue("Windfall should grant BTC", reward.btcGain > BigDecimal.ZERO)
        assertEquals(btcBefore.add(reward.btcGain), claimedState.btcBigDecimal)
        assertTrue("Windfall event should be cleared from activeEvents", claimedState.activeEvents.none { it.eventId == "lucky_block" })

        // Evaluate achievements following windfall gain (unlocks First Satoshi)
        val (stateAfterClaim, claimUnlocks) = AchievementEngine.evaluate(claimedState)
        assertTrue("Should unlock first_satoshi from windfall BTC", stateAfterClaim.achievements.contains("first_satoshi"))

        // 6. Idempotency: re-evaluating achievements yields no new unlocks
        val (finalState, secondUnlocks) = AchievementEngine.evaluate(stateAfterClaim)
        assertEquals(0, secondUnlocks.size)
        assertEquals(stateAfterClaim.achievements.size, finalState.achievements.size)
    }
}
