package com.antigravity.bitcoinminingtycoon.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.math.BigDecimal

class GameStateTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun serialization_roundTripPreservesExactValues() {
        val original = GameState(
            btc = "0.00041285",
            usd = "12450.50",
            manualHashStrength = "15",
            miners = mapOf("ancient_cpu" to 10L, "gaming_gpu" to 2L),
            purchasedUpgrades = setOf("copper_fingers", "silicon_lottery"),
            powerGridTier = 3,
            coolingTier = 2,
            marketPrice = "62500.00",
            marketTrend = MarketTrend.BULL,
            marketHistory = listOf("58000", "60000", "62500"),
            autoSellEnabled = true,
            autoSellThresholdUsd = "61000",
            satoshiPoints = 25L,
            dailyRewardDay = 3
        )

        val serialized = json.encodeToString(GameState.serializer(), original)
        assertNotNull(serialized)

        val restored = json.decodeFromString(GameState.serializer(), serialized)
        assertEquals(original, restored)
        assertEquals(BigDecimal("0.00041285"), restored.btcBigDecimal)
        assertEquals(BigDecimal("12450.50"), restored.usdBigDecimal)
    }

    @Test
    fun withBalances_updatesValuesAccurately() {
        val initial = GameState()
        val updated = initial.withBalances(BigDecimal("1.50000000"), BigDecimal("90000.00"))

        assertEquals("1.50000000", updated.btc)
        assertEquals("90000.00", updated.usd)
        assertEquals(BigDecimal("1.50000000"), updated.btcBigDecimal)
        assertEquals(BigDecimal("90000.00"), updated.usdBigDecimal)
    }
}
