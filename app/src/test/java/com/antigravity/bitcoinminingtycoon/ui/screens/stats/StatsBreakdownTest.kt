package com.antigravity.bitcoinminingtycoon.ui.screens.stats

import com.antigravity.bitcoinminingtycoon.content.Achievements
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.PowerEnergySample
import com.antigravity.bitcoinminingtycoon.model.StatsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class StatsBreakdownTest {

    @Test
    fun breakdownSeparatesCurrentLifetimeAndTrackedMiningSources() {
        val state = GameState(
            btc = "0.25",
            usd = "12.50",
            miners = mapOf("ancient_cpu" to 2L, "gaming_gpu" to 1L),
            purchasedUpgrades = setOf("copper_fingers"),
            stats = StatsState(
                lifetimeBtcMined = "5",
                lifetimeUsdEarned = "1000",
                manualBtc = "1",
                foregroundPassiveBtc = "2",
                offlineBtc = "0.5",
                dailyRewardBtc = "0.1",
                windfallBtc = "0.2",
                achievementRewardBtc = "0.05",
                sourceBreakdownTrackedSinceV12 = false,
                totalPlaytimeSeconds = 3661,
                playtimeFractionalSeconds = "0.25",
                peakTemperatureC = 82.5,
                powerEnergyHistory = listOf(PowerEnergySample(300, "1.5", "5", "0.125"))
            )
        )

        val rows = StatsBreakdown.sections(state, BigDecimal("1250"))
            .flatMap { it.rows }
            .associate { it.label to it.value }

        assertEquals("0.25000000 BTC", rows["Current Bitcoin"])
        assertEquals("$ 12.50", rows["Cash on hand"])
        assertEquals("5.00000000 BTC", rows["Lifetime Bitcoin mined"])
        assertEquals("1.00000000 BTC", rows["Manual mining"])
        assertEquals("2.00000000 BTC", rows["Foreground machines"])
        assertEquals("0.50000000 BTC", rows["Offline production"])
        assertEquals("0.10000000 BTC", rows["Daily rewards"])
        assertEquals("0.20000000 BTC", rows["Event windfalls"])
        assertEquals("0.05000000 BTC", rows["Achievement rewards"])
        assertEquals("1h 1m 1.25s", rows["Active play time"])
        assertTrue(rows.getValue("Mining source coverage").contains("Partial"))
        assertTrue(rows.getValue("Energy in retained samples").contains("0.125"))
    }

    @Test
    fun everyRetainedAchievementHasAnHonestProgressValue() {
        val progress = StatsBreakdown.achievementProgress(
            achievementId = "manual_century",
            state = GameState(stats = StatsState(totalManualTaps = 40L))
        )

        val row = requireNotNull(progress)
        val numberFormat = GameState().settings.numberFormat
        assertEquals(BigDecimal("40"), row.current)
        assertEquals(BigDecimal("100"), row.target)
        assertEquals("taps", row.unit)
        assertTrue(row.displayValue(numberFormat).contains("40"))
        Achievements.ALL.forEach { achievement ->
            assertNotNull("${achievement.id} needs a progress row", StatsBreakdown.achievementProgress(achievement.id, GameState()))
        }
    }
}
