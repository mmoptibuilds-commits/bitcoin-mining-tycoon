package com.antigravity.bitcoinminingtycoon.journey

import com.antigravity.bitcoinminingtycoon.engine.GameEngine
import com.antigravity.bitcoinminingtycoon.engine.MarketEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.MarketTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import kotlin.random.Random

/**
 * Automated Journey verification for journeys/02-market-sell.md:
 * - Start with test BTC available
 * - Inspect fictional BTC price and trend
 * - Sell 10%, verify BTC decreases and USD increases
 * - Sell 50% and MAX
 * - Verify controls never sell more than owned, price remains bounded, feedback is clear
 */
class MarketSellJourneyTest {

    @Test
    fun executeMarketSellJourney_endToEnd() {
        // Step 1: Initial state with BTC available
        var state = GameState(
            btc = "1.00000000",
            usd = "500.00",
            marketPrice = "60000.00",
            marketTrend = MarketTrend.BULL,
            marketHistory = listOf("58000.00", "59000.00", "60000.00")
        )

        assertTrue("Price must be positive and within bounds", state.marketPriceBigDecimal >= MarketEngine.MIN_PRICE)
        assertEquals(BigDecimal("1.00000000"), state.btcBigDecimal)
        assertEquals(BigDecimal("500.00"), state.usdBigDecimal)

        // Step 2: Sell 10%
        state = MarketEngine.sellBtc(state, 10)
        assertEquals(BigDecimal("0.90000000"), state.btcBigDecimal)
        assertEquals(BigDecimal("6500.00"), state.usdBigDecimal) // 500 + (0.1 * 60,000)
        assertEquals(BigDecimal("0.10000000"), state.stats.totalBtcSoldBigDecimal)
        assertEquals(BigDecimal("6000.00"), state.stats.lifetimeUsdBigDecimal)

        // Step 3: Sell 50%
        state = MarketEngine.sellBtc(state, 50)
        assertEquals(BigDecimal("0.45000000"), state.btcBigDecimal)
        assertEquals(BigDecimal("33500.00"), state.usdBigDecimal) // 6,500 + (0.45 * 60,000 = 27,000)
        assertEquals(BigDecimal("0.55000000"), state.stats.totalBtcSoldBigDecimal)

        // Step 4: Sell MAX (100%)
        state = MarketEngine.sellBtc(state, 100)
        assertEquals(BigDecimal("0.00000000"), state.btcBigDecimal)
        assertEquals(BigDecimal("60500.00"), state.usdBigDecimal) // 33,500 + (0.45 * 60,000 = 27,000)
        assertEquals(BigDecimal("1.00000000"), state.stats.totalBtcSoldBigDecimal)

        // Step 5: Boundary & rapid tap verification — selling when balance is 0
        val stateAfterZeroSell = MarketEngine.sellBtc(state, 100)
        assertEquals(BigDecimal("0.00000000"), stateAfterZeroSell.btcBigDecimal)
        assertEquals(BigDecimal("60500.00"), stateAfterZeroSell.usdBigDecimal)
        assertEquals(state, stateAfterZeroSell)

        // Step 6: Simulate market walk and verify bounds & history size
        val rng = Random(42)
        for (tick in 1..40) {
            state = MarketEngine.tick(state, MarketEngine.TICK_INTERVAL_SECONDS, rng)
            val price = state.marketPriceBigDecimal
            assertTrue("Price $price must stay >= $1,000", price >= MarketEngine.MIN_PRICE)
            assertTrue("Price $price must stay <= $1,000,000", price <= MarketEngine.MAX_PRICE)
        }
        assertEquals(30, state.marketHistory.size)
        assertEquals(state.marketPrice, state.marketHistory.last())
    }
}
