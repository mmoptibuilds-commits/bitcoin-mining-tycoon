package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.MarketTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import kotlin.random.Random

class MarketEngineTest {

    @Test
    fun priceBounds_enforceHardFloorAndCeilingUnderStress() {
        var state = GameState(
            marketPrice = "1000.00",
            marketTrend = MarketTrend.CRASH,
            rngSeed = 42L
        )

        val rng = Random(42)

        // 1. Simulate 5,000 ticks from bottom floor
        for (i in 1..5000) {
            state = MarketEngine.tick(state, MarketEngine.TICK_INTERVAL_SECONDS, rng)
            val price = state.marketPriceBigDecimal
            assertTrue("Price $price below minimum floor", price >= MarketEngine.MIN_PRICE)
            assertTrue("Price $price above maximum ceiling", price <= MarketEngine.MAX_PRICE)
        }

        // 2. Test explicit top ceiling boundary
        var topState = GameState(
            marketPrice = "1000000.00",
            marketTrend = MarketTrend.PUMP,
            rngSeed = 999L
        )
        for (i in 1..100) {
            topState = MarketEngine.tick(topState, MarketEngine.TICK_INTERVAL_SECONDS, Random(999 + i))
            val price = topState.marketPriceBigDecimal
            assertTrue("Price $price exceeded ceiling", price <= MarketEngine.MAX_PRICE)
            assertTrue("Price $price fell below floor", price >= MarketEngine.MIN_PRICE)
        }
    }

    @Test
    fun trendTransitions_areDeterministicWithSameSeed() {
        val initial = GameState(
            marketPrice = "50000.00",
            marketTrend = MarketTrend.NEUTRAL,
            rngSeed = 1337L
        )

        val stateA = MarketEngine.tick(initial, 3.0, Random(1337L))
        val stateB = MarketEngine.tick(initial, 3.0, Random(1337L))

        assertEquals(stateA.marketPrice, stateB.marketPrice)
        assertEquals(stateA.marketTrend, stateB.marketTrend)
        assertEquals(stateA.marketHistory, stateB.marketHistory)
    }

    @Test
    fun marketHistory_maintains30SamplesRolling() {
        var state = GameState(
            marketPrice = "50000.00",
            marketHistory = listOf("50000.00")
        )

        val rng = Random(123)
        // Advance 50 intervals
        for (i in 1..50) {
            state = MarketEngine.tick(state, MarketEngine.TICK_INTERVAL_SECONDS, rng)
        }

        assertEquals(30, state.marketHistory.size)
        assertEquals(state.marketPrice, state.marketHistory.last())
    }

    @Test
    fun sellBtc_exactAmountsAndZeroNegativeBalances() {
        val state = GameState(
            btc = "1.00000000",
            usd = "0.00",
            marketPrice = "50000.00"
        )

        // Sell 10%
        val after10 = MarketEngine.sellBtc(state, 10)
        assertEquals(BigDecimal("0.90000000"), after10.btcBigDecimal)
        assertEquals(BigDecimal("5000.00"), after10.usdBigDecimal)
        assertEquals(BigDecimal("0.10000000"), after10.stats.totalBtcSoldBigDecimal)
        assertEquals(BigDecimal("5000.00"), after10.stats.lifetimeUsdBigDecimal)

        // Sell 50% of remaining 0.9 BTC
        val after50 = MarketEngine.sellBtc(after10, 50)
        assertEquals(BigDecimal("0.45000000"), after50.btcBigDecimal)
        assertEquals(BigDecimal("27500.00"), after50.usdBigDecimal) // 5,000 + 22,500
        assertEquals(BigDecimal("0.55000000"), after50.stats.totalBtcSoldBigDecimal)

        // Sell MAX (100%)
        val afterMax = MarketEngine.sellBtc(after50, 100)
        assertEquals(BigDecimal("0.00000000"), afterMax.btcBigDecimal)
        assertEquals(BigDecimal("50000.00"), afterMax.usdBigDecimal)
        assertEquals(BigDecimal("1.00000000"), afterMax.stats.totalBtcSoldBigDecimal)

        // Sell again when balance is 0: must be no-op, no negative values
        val afterEmpty = MarketEngine.sellBtc(afterMax, 100)
        assertEquals(BigDecimal("0.00000000"), afterEmpty.btcBigDecimal)
        assertEquals(BigDecimal("50000.00"), afterEmpty.usdBigDecimal)
    }

    @Test
    fun sellBtc_invalidPercentagesHandledSafely() {
        val state = GameState(btc = "0.50000000", usd = "100.00")
        val stateNeg = MarketEngine.sellBtc(state, -10)
        assertEquals(state, stateNeg)

        val stateZero = MarketEngine.sellBtc(state, 0)
        assertEquals(state, stateZero)
    }

    @Test
    fun autoSell_evaluatesThresholdCorrectly() {
        val stateUnder = GameState(
            marketPrice = "55000.00",
            autoSellEnabled = true,
            autoSellThresholdUsd = "60000.00"
        )
        val mined = BigDecimal("0.00100000")
        val (unSoldUnder, usdGainUnder) = MarketEngine.evaluateAutoSell(stateUnder, mined)
        assertEquals(mined, unSoldUnder)
        assertEquals(BigDecimal.ZERO, usdGainUnder)

        val stateOver = GameState(
            marketPrice = "62500.00",
            autoSellEnabled = true,
            autoSellThresholdUsd = "60000.00"
        )
        val (unSoldOver, usdGainOver) = MarketEngine.evaluateAutoSell(stateOver, mined)
        assertEquals(BigDecimal.ZERO, unSoldOver)
        assertEquals(BigDecimal("62.50"), usdGainOver) // 0.001 * 62500
    }

    @Test
    fun calculateProceeds_accurateEstimatedValues() {
        val btc = BigDecimal("2.50000000")
        val price = BigDecimal("60000.00")

        val p10 = MarketEngine.calculateProceeds(btc, price, 10)
        assertEquals(BigDecimal("15000.00"), p10)

        val p50 = MarketEngine.calculateProceeds(btc, price, 50)
        assertEquals(BigDecimal("75000.00"), p50)

        val pMax = MarketEngine.calculateProceeds(btc, price, 100)
        assertEquals(BigDecimal("150000.00"), pMax)

        val pZero = MarketEngine.calculateProceeds(BigDecimal.ZERO, price, 100)
        assertEquals(BigDecimal.ZERO, pZero)
    }

    @Test
    fun stateAwareSalePreviewMatchesActualTransactionIncludingMarketUpgrades() {
        val state = GameState(
            btc = "2.50000001",
            marketPrice = "60000.00",
            purchasedUpgrades = setOf("market_ticker_display", "limit_order_bot")
        )

        listOf(10, 50, 100).forEach { percent ->
            val expectedCash = MarketEngine.previewProceeds(state, percent)
            val afterSale = MarketEngine.sellBtc(state, percent)
            assertEquals(expectedCash, afterSale.usdBigDecimal)
        }
    }
}
