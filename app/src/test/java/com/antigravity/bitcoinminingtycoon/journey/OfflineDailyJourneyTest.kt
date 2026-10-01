package com.antigravity.bitcoinminingtycoon.journey

import com.antigravity.bitcoinminingtycoon.content.DailyRewards
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.engine.BulkMode
import com.antigravity.bitcoinminingtycoon.engine.FleetEngine
import com.antigravity.bitcoinminingtycoon.engine.OfflineEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class OfflineDailyJourneyTest {

    @Test
    fun testOfflineReturnAndDailyRewardLifecycleJourney() {
        var state = GameState()
        var currentWall = 1_000_000_000L // 1,000,000 seconds wall

        // 1. Initial play: Fund player and purchase 5 Ancient CPUs
        state = state.withUsd(BigDecimal("500.00"))
        state = FleetEngine.buyMiner(state, Miners.ALL[0].id, BulkMode.X1)
        state = FleetEngine.buyMiner(state, Miners.ALL[0].id, BulkMode.X1)
        state = FleetEngine.buyMiner(state, Miners.ALL[0].id, BulkMode.X1)
        state = FleetEngine.buyMiner(state, Miners.ALL[0].id, BulkMode.X1)
        state = FleetEngine.buyMiner(state, Miners.ALL[0].id, BulkMode.X1)
        assertEquals(5L, state.miners[Miners.ALL[0].id])

        // Player claims initial Day 1 Daily Reward
        assertTrue("Player should be eligible for Day 1 reward", DailyRewards.canClaim(state, currentWall))
        val (stateAfterDay1, day1Reward) = DailyRewards.claim(state, currentWall)
        assertNotNull(day1Reward)
        assertEquals(BigDecimal("3.00"), day1Reward?.usdReward)
        state = stateAfterDay1
        assertEquals(2, state.dailyRewardDay)

        // Attempt immediate re-claim (blocked by 20h cooldown)
        assertFalse(DailyRewards.canClaim(state, currentWall + 1000L))
        val (unalteredState, blockedReward) = DailyRewards.claim(state, currentWall + 1000L)
        assertNull(blockedReward)
        assertEquals(state, unalteredState)

        // 2. Player backgrounds app for 4 hours
        val lastSavedWall = currentWall
        val fourHoursMillis = 4L * 3600L * 1000L
        currentWall += fourHoursMillis

        // 3. Player returns after 4 hours -> calculate offline progress
        val offlineReport = OfflineEngine.calculateOfflineProgress(
            state = state,
            lastSavedWallMillis = lastSavedWall,
            currentWallMillis = currentWall
        )

        assertEquals(4.0 * 3600.0, offlineReport.durationSeconds, 0.001)
        assertTrue("Offline report must contain positive mined BTC", offlineReport.minedBtc > BigDecimal.ZERO)

        // Apply offline reward
        val btcBefore = state.btcBigDecimal
        state = OfflineEngine.applyOfflineReward(state, offlineReport)
        assertEquals(btcBefore.add(offlineReport.minedBtc), state.btcBigDecimal)
        assertEquals(state.btcBigDecimal, state.stats.lifetimeBtcBigDecimal)

        // 4. Check daily reward eligibility after 4 hours (20h cooldown not elapsed yet)
        assertFalse("4 hours is less than 20 hours cooldown", DailyRewards.canClaim(state, currentWall))

        // 5. Advance wall clock to 21 hours total (cooldown elapsed)
        currentWall += (17L * 3600L * 1000L) // 4h + 17h = 21h elapsed since Day 1 claim
        assertTrue("Daily reward should now be claimable for Day 2", DailyRewards.canClaim(state, currentWall))

        val (stateAfterDay2, day2Reward) = DailyRewards.claim(state, currentWall)
        assertNotNull(day2Reward)
        assertEquals(2, day2Reward?.dayNumber)
        assertEquals(0, DailyRewards.getForDay(2, state).btcReward.compareTo(day2Reward!!.btcReward))
        assertEquals(3, stateAfterDay2.dailyRewardDay)
        state = stateAfterDay2

        // 6. Extreme offline test: player leaves for 30 days (720 hours)
        val monthSavedWall = currentWall
        currentWall += (30L * 24L * 3600L * 1000L)

        val cappedReport = OfflineEngine.calculateOfflineProgress(
            state = state,
            lastSavedWallMillis = monthSavedWall,
            currentWallMillis = currentWall
        )

        // Clamped to 12 hours (43,200 seconds)
        assertEquals(12.0 * 3600.0, cappedReport.durationSeconds, 0.001)

        // Streak check: Day 3 reward is still waiting (cumulative non-resetting per D018)
        assertTrue(DailyRewards.canClaim(state, currentWall))
        val (stateAfterMonth, day3Reward) = DailyRewards.claim(state, currentWall)
        assertNotNull(day3Reward)
        assertEquals("Should still be Day 3 reward despite 30 days gap", 3, day3Reward?.dayNumber)
        assertEquals(0, DailyRewards.getForDay(3, stateAfterDay2).usdReward.compareTo(day3Reward!!.usdReward))
        assertEquals(4, stateAfterMonth.dailyRewardDay)
    }
}
