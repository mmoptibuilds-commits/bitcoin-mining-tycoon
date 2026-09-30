package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class GameEngineTest {

    @Test
    fun performManualTap_incrementsStatsAndBtc() {
        val initial = GameState(btc = "0", manualHashStrength = "10")
        val afterTap = GameEngine.performManualTap(initial)

        assertTrue(afterTap.btcBigDecimal > BigDecimal.ZERO)
        assertEquals(1L, afterTap.stats.totalManualTaps)
        assertEquals(afterTap.btcBigDecimal, afterTap.stats.lifetimeBtcBigDecimal)
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
}
