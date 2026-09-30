package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.DailyRewards
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class OfflineDailyTest {

    @Test
    fun testNegativeWallClockDeltaYieldsZero() {
        val state = GameState(miners = mapOf(Miners.ALL[0].id to 10L))
        val lastSaved = 10_000_000L
        val current = 9_000_000L // 1,000s in the past (clock rollback)

        val report = OfflineEngine.calculateOfflineProgress(state, lastSaved, current)

        assertEquals(0.0, report.durationSeconds, 0.001)
        assertEquals(BigDecimal.ZERO, report.minedBtc)
    }

    @Test
    fun testExtremeFutureClockClampedTo12Hours() {
        val state = GameState(miners = mapOf(Miners.ALL[0].id to 10L))
        val lastSaved = 1_000_000L
        val current = lastSaved + (72L * 3600L * 1000L) // 72 hours later

        val report = OfflineEngine.calculateOfflineProgress(state, lastSaved, current)

        assertEquals(12.0 * 3600.0, report.durationSeconds, 0.001)
        assertTrue(report.minedBtc > BigDecimal.ZERO)
    }

    @Test
    fun testNormalOfflineEarningsCalculation() {
        val state = GameState(miners = mapOf(Miners.ALL[0].id to 5L))
        val lastSaved = 1_000_000L
        val offlineDurationSec = 3600.0 // 1 hour
        val current = lastSaved + (offlineDurationSec * 1000L).toLong()

        val report = OfflineEngine.calculateOfflineProgress(state, lastSaved, current)

        assertEquals(3600.0, report.durationSeconds, 0.001)
        assertTrue(report.minedBtc > BigDecimal.ZERO)

        val appliedState = OfflineEngine.applyOfflineReward(state, report)
        assertEquals(report.minedBtc, appliedState.btcBigDecimal)
        assertEquals(report.minedBtc, appliedState.stats.lifetimeBtcBigDecimal)
    }

    @Test
    fun positiveOfflineIntervalsBelowSummaryThresholdStillCreditProduction() {
        val state = GameState(miners = mapOf(Miners.ALL[0].id to 5L))
        val report = OfflineEngine.calculateOfflineProgress(state, 1_000_000L, 1_059_999L)

        assertEquals(59.999, report.durationSeconds, 0.001)
        assertTrue(report.minedBtc > BigDecimal.ZERO)
        assertEquals(report.minedBtc, OfflineEngine.applyOfflineReward(state, report).btcBigDecimal)
    }

    @Test
    fun offlineProductionStopsEventMultiplierAtItsExpiryBoundary() {
        val state = GameState(
            miners = mapOf(Miners.ALL[0].id to 5L),
            activeEvents = listOf(
                com.antigravity.bitcoinminingtycoon.model.ActiveEventState(
                    eventId = "bull_run",
                    expiresAtWallMillis = 1_005_000L,
                    multiplier = 2.0
                )
            )
        )
        val report = OfflineEngine.calculateOfflineProgress(state, 1_000_000L, 1_010_000L)
        val baseRate = EconomyEngine.calculateEffectiveHashrate(state.copy(activeEvents = emptyList()))
        val expected = EconomyEngine.calculateMinedBtc(baseRate.multiply(BigDecimal("2")), 5.0)
            .add(EconomyEngine.calculateMinedBtc(baseRate, 5.0))

        assertEquals(0, expected.compareTo(report.minedBtc))
    }

    @Test
    fun testZeroHashrateYieldsZeroOfflineBtc() {
        val state = GameState() // No automated miners
        val lastSaved = 1_000_000L
        val current = lastSaved + 7200_000L // 2 hours

        val report = OfflineEngine.calculateOfflineProgress(state, lastSaved, current)

        assertEquals(BigDecimal.ZERO, report.minedBtc)
    }

    @Test
    fun testDailyRewardInitialClaim() {
        val state = GameState()
        val currentWall = 1_000_000L

        assertTrue(DailyRewards.canClaim(state, currentWall))

        val (claimedState, reward) = DailyRewards.claim(state, currentWall)
        assertNotNull(reward)
        assertEquals(1, reward?.dayNumber)
        assertEquals(BigDecimal("100.00"), reward?.usdReward)
        assertEquals(BigDecimal("100.00"), claimedState.usdBigDecimal)
        assertEquals(2, claimedState.dailyRewardDay)
        assertEquals(currentWall, claimedState.lastDailyClaimWallMillis)
    }

    @Test
    fun testDailyRewardCooldownPreventsPrematureClaim() {
        val currentWall = 1_000_000L
        val state = GameState(
            dailyRewardDay = 2,
            lastDailyClaimWallMillis = currentWall
        )

        // 10 hours later (cooldown is 20 hours)
        val tenHoursLater = currentWall + (10L * 3600L * 1000L)
        assertFalse(DailyRewards.canClaim(state, tenHoursLater))

        val (unalteredState, reward) = DailyRewards.claim(state, tenHoursLater)
        assertNull(reward)
        assertEquals(state, unalteredState)

        // 20 hours and 1 minute later
        val eligibleTime = currentWall + (20L * 3600L * 1000L) + 60_000L
        assertTrue(DailyRewards.canClaim(state, eligibleTime))

        val (claimedState, secondReward) = DailyRewards.claim(state, eligibleTime)
        assertNotNull(secondReward)
        assertEquals(2, secondReward?.dayNumber)
        assertEquals(3, claimedState.dailyRewardDay)
    }

    @Test
    fun testMissingDaysDoesNotResetStreak() {
        val currentWall = 1_000_000L
        val state = GameState(
            dailyRewardDay = 4,
            lastDailyClaimWallMillis = currentWall
        )

        // Player returns 7 days later
        val sevenDaysLater = currentWall + (7L * 24L * 3600L * 1000L)
        assertTrue(DailyRewards.canClaim(state, sevenDaysLater))

        val (claimedState, reward) = DailyRewards.claim(state, sevenDaysLater)
        assertNotNull(reward)
        assertEquals("Player should advance to Day 4 reward without streak reset", 4, reward?.dayNumber)
        assertEquals(5, claimedState.dailyRewardDay)
    }

    @Test
    fun testDay7WrapsToDay1AndGrantsSatoshiPoint() {
        val currentWall = 1_000_000L
        val state = GameState(
            dailyRewardDay = 7,
            lastDailyClaimWallMillis = 0L,
            satoshiPoints = 0L
        )

        val (claimedState, reward) = DailyRewards.claim(state, currentWall)
        assertNotNull(reward)
        assertEquals(7, reward?.dayNumber)
        assertEquals(1L, reward?.satoshiPointsReward)
        assertEquals(1L, claimedState.satoshiPoints)
        assertEquals(1L, claimedState.stats.lifetimeSatoshiPointsEarned)
        assertEquals(1L, claimedState.stats.dailyPointsEarnedSinceV2)
        assertEquals(0L, claimedState.stats.prestigePointsEarnedSinceV2)
        assertEquals("Cycle should wrap back to Day 1", 1, claimedState.dailyRewardDay)
    }
}
