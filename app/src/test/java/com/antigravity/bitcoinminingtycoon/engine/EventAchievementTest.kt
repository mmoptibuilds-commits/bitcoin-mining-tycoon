package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.Achievements
import com.antigravity.bitcoinminingtycoon.content.Events
import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import kotlin.random.Random

class EventAchievementTest {

    @Test
    fun testEventRollDeterminism() {
        val state = GameState(rngSeed = 42L)
        val rng1 = Random(42L)
        val rng2 = Random(42L)

        val (_, event1) = EventEngine.rollRandomEvent(state, 1000L, rng1)
        val (_, event2) = EventEngine.rollRandomEvent(state, 1000L, rng2)

        assertNotNull(event1)
        assertNotNull(event2)
        assertEquals(event1?.id, event2?.id)
    }

    @Test
    fun testEventExpiry() {
        val state = GameState(
            activeEvents = listOf(
                ActiveEventState("expired_1", expiresAtWallMillis = 500L),
                ActiveEventState("active_1", expiresAtWallMillis = 2000L)
            )
        )

        val ticked = EventEngine.tick(state, deltaSeconds = 1.0, wallMillis = 1000L)

        assertEquals(1, ticked.activeEvents.size)
        assertEquals("active_1", ticked.activeEvents.first().eventId)
    }

    @Test
    fun testEventDurationExtension() {
        val initialWall = 1000L
        val event = Events.BULL_RUN // 45s duration
        val state = GameState(
            activeEvents = listOf(
                ActiveEventState(event.id, expiresAtWallMillis = initialWall + 10_000L)
            )
        )

        val updated = EventEngine.applyEvent(state, event, initialWall)

        assertEquals(1, updated.activeEvents.size)
        // Previous remaining 10s + 45s = 55s -> expires at initialWall + 55_000L
        assertEquals(initialWall + 55_000L, updated.activeEvents.first().expiresAtWallMillis)
    }

    @Test
    fun testAmbientEventLimit() {
        val initialWall = 1000L
        val event1 = Events.BULL_RUN // expires at +45s
        val event2 = Events.CHEAP_ELECTRICITY // expires at +60s
        val event3 = Events.COOLING_WEATHER // expires at +60s

        var state = GameState()
        state = EventEngine.applyEvent(state, event1, initialWall)
        state = EventEngine.applyEvent(state, event2, initialWall)
        assertEquals(2, state.activeEvents.size)

        // Adding 3rd ambient event replaces the one closest to expiration (BULL_RUN)
        state = EventEngine.applyEvent(state, event3, initialWall)
        assertEquals(2, state.activeEvents.size)
        assertFalse(state.activeEvents.any { it.eventId == event1.id })
        assertTrue(state.activeEvents.any { it.eventId == event2.id })
        assertTrue(state.activeEvents.any { it.eventId == event3.id })
    }

    @Test
    fun testWindfallSingleLimit() {
        val initialWall = 1000L
        val lucky = Events.LUCKY_BLOCK
        val perfect = Events.PERFECT_BLOCK

        var state = GameState()
        state = EventEngine.applyEvent(state, lucky, initialWall)
        assertEquals(1, state.activeEvents.size)
        assertTrue(state.activeEvents.first().isWindfall)

        // Adding 2nd windfall while 1 is active is rejected
        state = EventEngine.applyEvent(state, perfect, initialWall)
        assertEquals(1, state.activeEvents.size)
        assertEquals(lucky.id, state.activeEvents.first().eventId)
    }

    @Test
    fun testClaimWindfallLuckyBlock() {
        val initialWall = 1000L
        val lucky = Events.LUCKY_BLOCK
        val state = EventEngine.applyEvent(GameState(), lucky, initialWall)

        val (claimedState, reward) = EventEngine.claimWindfall(state, lucky.id, wallMillis = initialWall + 5000L)

        assertTrue(reward.btcGain > BigDecimal.ZERO)
        assertTrue(claimedState.btcBigDecimal > BigDecimal.ZERO)
        assertEquals(0, claimedState.activeEvents.size)
        assertEquals(claimedState.btcBigDecimal, claimedState.stats.lifetimeBtcBigDecimal)
    }

    @Test
    fun testClaimWindfallPerfectBlock() {
        val initialWall = 1000L
        val perfect = Events.PERFECT_BLOCK
        val state = EventEngine.applyEvent(GameState(), perfect, initialWall)

        val (claimedState, reward) = EventEngine.claimWindfall(state, perfect.id, wallMillis = initialWall + 5000L)

        assertTrue(reward.usdGain >= BigDecimal("250.00"))
        assertTrue(claimedState.usdBigDecimal >= BigDecimal("250.00"))
        assertEquals(0, claimedState.activeEvents.size)
        assertEquals(claimedState.usdBigDecimal, claimedState.stats.lifetimeUsdBigDecimal)
    }

    @Test
    fun testClaimWindfallExpiredFails() {
        val initialWall = 1000L
        val lucky = Events.LUCKY_BLOCK
        val state = EventEngine.applyEvent(GameState(), lucky, initialWall)

        // Claiming after 25s when duration is 20s
        val (claimedState, reward) = EventEngine.claimWindfall(state, lucky.id, wallMillis = initialWall + 25_000L)

        assertEquals(BigDecimal.ZERO, reward.btcGain)
        assertEquals(BigDecimal.ZERO, claimedState.btcBigDecimal)
        assertEquals(0, claimedState.activeEvents.size)
    }

    @Test
    fun testAchievementEvaluations() {
        val state0 = GameState()
        val (state1, unlocks1) = AchievementEngine.evaluate(state0)
        assertEquals(0, unlocks1.size)

        // Tap once -> Genesis Entropy
        val tappedState = state0.copy(stats = StatsState(totalManualTaps = 1L))
        val (state2, unlocks2) = AchievementEngine.evaluate(tappedState)
        assertEquals(1, unlocks2.size)
        assertEquals("first_hash", unlocks2.first().id)
        assertTrue(state2.achievements.contains("first_hash"))

        // Re-evaluating same state yields no duplicate unlock
        val (state3, unlocks3) = AchievementEngine.evaluate(state2)
        assertEquals(0, unlocks3.size)
    }

    @Test
    fun testHardwareAndInfrastructureAchievements() {
        val state = GameState(
            miners = mapOf("entry_asic" to 1L),
            powerGridTier = 2,
            coolingTier = 2,
            stats = StatsState(totalMinersPurchased = 1L)
        )

        val (unlockedState, unlocks) = AchievementEngine.evaluate(state)
        val unlockedIds = unlocks.map { it.id }.toSet()

        assertTrue("Should unlock first_rig", unlockedIds.contains("first_rig"))
        assertTrue("Should unlock asic_vanguard", unlockedIds.contains("asic_vanguard"))
        assertTrue("Should unlock substation_expansion", unlockedIds.contains("substation_expansion"))
        assertTrue("Should unlock forced_air_cooling", unlockedIds.contains("forced_air_cooling"))
    }
}
