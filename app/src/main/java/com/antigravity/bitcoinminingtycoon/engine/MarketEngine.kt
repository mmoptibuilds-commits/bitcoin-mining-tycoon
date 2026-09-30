package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.MarketTrend
import com.antigravity.bitcoinminingtycoon.model.StatsState
import com.antigravity.bitcoinminingtycoon.content.BalanceConfig
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

/**
 * Pure Kotlin deterministic simulation engine for the in-game fictional Bitcoin market.
 * Invariant 1: UI composables never contain market simulation or economy formulas.
 * Invariant 2: Game calculations are deterministic when supplied the same state, clock, and RNG seed.
 * Invariant 3: Monetary/game magnitude arithmetic must never emit NaN or Infinity. Bounded within [$1,000, $1,000,000].
 */
object MarketEngine {

    val MIN_PRICE: BigDecimal = BigDecimal(BalanceConfig.MARKET_MIN_USD)
    val MAX_PRICE: BigDecimal = BigDecimal(BalanceConfig.MARKET_MAX_USD)
    val INITIAL_PRICE: BigDecimal = BalanceConfig.INITIAL_MARKET_PRICE_USD

    const val HISTORY_CAPACITY = 30
    const val TICK_INTERVAL_SECONDS = BalanceConfig.MARKET_TICK_SECONDS

    /**
     * Executes market price progression and trend state transitions.
     */
    fun tick(
        state: GameState,
        deltaSeconds: Double,
        rng: Random = Random(state.rngSeed xor 0x5DEECE66DL)
    ): GameState {
        if (deltaSeconds <= 0.0) return state

        val newTimer = state.marketTimerSeconds + deltaSeconds
        if (newTimer < TICK_INTERVAL_SECONDS) {
            return state.copy(marketTimerSeconds = newTimer)
        }

        // Timer expired -> advance market step
        val remainingTimer = newTimer - TICK_INTERVAL_SECONDS

        // 1. Evaluate Markov trend transition
        val nextTrend = transitionTrend(state.marketTrend, rng)

        // 2. Compute percentage delta for current trend
        val percentChange = calculateTrendDelta(nextTrend, rng)

        // 3. Apply active event influences (if any)
        val eventAdjustedDelta = applyEventModifiers(percentChange, state)

        // 4. Calculate bounded new price
        val currentPrice = state.marketPriceBigDecimal
        val priceMultiplier = BigDecimal.ONE.add(BigDecimal.valueOf(eventAdjustedDelta))
        val rawNextPrice = currentPrice.multiply(priceMultiplier, GameNumber.MATH_CONTEXT)
        val clampedPrice = rawNextPrice
            .max(MIN_PRICE)
            .min(MAX_PRICE)
            .setScale(2, RoundingMode.HALF_UP)

        // 5. Update 30-sample rolling history
        val updatedHistory = (state.marketHistory + clampedPrice.toPlainString())
            .takeLast(HISTORY_CAPACITY)

        // 6. Update lifetime price observation stats
        val currentHighest = state.stats.highestPriceBigDecimal
        val currentLowest = state.stats.lowestPriceBigDecimal
        val newHighest = maxOf(currentHighest, clampedPrice)
        val newLowest = if (currentLowest.compareTo(BigDecimal.ZERO) == 0) clampedPrice else minOf(currentLowest, clampedPrice)

        val updatedStats = state.stats.copy(
            highestPriceObserved = newHighest.toPlainString(),
            lowestPriceObserved = newLowest.toPlainString()
        )

        val nextSeed = rng.nextLong()

        return state.copy(
            marketPrice = clampedPrice.toPlainString(),
            marketTrend = nextTrend,
            marketHistory = updatedHistory,
            marketTimerSeconds = remainingTimer,
            stats = updatedStats,
            rngSeed = nextSeed
        )
    }

    /**
     * Transitions between market regimes according to a Markov state transition matrix.
     */
    fun transitionTrend(current: MarketTrend, rng: Random): MarketTrend {
        val roll = rng.nextDouble() // [0.0, 1.0)
        val weights = BalanceConfig.MARKET_TRENDS.getValue(current).transitionWeights
        var cumulative = 0.0
        for ((next, weight) in weights) {
            cumulative += weight
            if (roll < cumulative) return next
        }
        return weights.keys.last()
    }

    /**
     * Generates a price percentage delta bounded within the regime characteristics.
     */
    fun calculateTrendDelta(trend: MarketTrend, rng: Random): Double {
        val tuning = BalanceConfig.MARKET_TRENDS.getValue(trend)
        return tuning.deltaMin + ((tuning.deltaMax - tuning.deltaMin) * rng.nextDouble())
    }

    private fun applyEventModifiers(baseDelta: Double, state: GameState): Double {
        var delta = baseDelta
        for (event in state.activeEvents) {
            when (event.eventId) {
                "bull_run" -> delta += BalanceConfig.MARKET_BULL_EVENT_DELTA
                "market_crash" -> delta += BalanceConfig.MARKET_CRASH_EVENT_DELTA
            }
        }
        return delta
    }

    /**
     * Executes manual sale of BTC for simulated USD.
     * @param percentage 10 for 10%, 50 for 50%, 100 for MAX
     */
    fun sellBtc(state: GameState, percentage: Int): GameState {
        val currentBtc = state.btcBigDecimal
        if (currentBtc.compareTo(BigDecimal.ZERO) <= 0) return state

        val btcToSell = when {
            percentage >= 100 -> currentBtc
            percentage <= 0 -> return state
            else -> {
                val ratio = BigDecimal.valueOf(percentage.toLong()).divide(BigDecimal("100"), GameNumber.MATH_CONTEXT)
                currentBtc.multiply(ratio, GameNumber.MATH_CONTEXT).setScale(8, RoundingMode.DOWN)
            }
        }

        if (btcToSell.compareTo(BigDecimal.ZERO) <= 0) return state

        val effectivePrice = effectiveSalePrice(state)
        val usdGain = btcToSell.multiply(effectivePrice, GameNumber.MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP)

        val nextBtc = currentBtc.subtract(btcToSell, GameNumber.MATH_CONTEXT).max(BigDecimal.ZERO)
        val nextUsd = state.usdBigDecimal.add(usdGain, GameNumber.MATH_CONTEXT)

        val nextTotalBtcSold = state.stats.totalBtcSoldBigDecimal.add(btcToSell, GameNumber.MATH_CONTEXT)
        val nextLifetimeUsd = state.stats.lifetimeUsdBigDecimal.add(usdGain, GameNumber.MATH_CONTEXT)

        val updatedStats = state.stats.copy(
            totalBtcSold = nextTotalBtcSold.toPlainString(),
            lifetimeUsdEarned = nextLifetimeUsd.toPlainString()
        )

        return state.copy(
            btc = nextBtc.toPlainString(),
            usd = nextUsd.toPlainString(),
            stats = updatedStats
        )
    }

    /**
     * Helper to compute estimated USD proceeds for UI display before tap.
     */
    fun calculateProceeds(btcAmount: BigDecimal, price: BigDecimal, percentage: Int): BigDecimal {
        if (btcAmount.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO
        val btcToSell = when {
            percentage >= 100 -> btcAmount
            percentage <= 0 -> return BigDecimal.ZERO
            else -> {
                val ratio = BigDecimal.valueOf(percentage.toLong()).divide(BigDecimal("100"), GameNumber.MATH_CONTEXT)
                btcAmount.multiply(ratio, GameNumber.MATH_CONTEXT).setScale(8, RoundingMode.DOWN)
            }
        }
        return btcToSell.multiply(price, GameNumber.MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP)
    }

    /** Cash shown beside Sell 10/50/MAX, using the same sale price and rounding as [sellBtc]. */
    fun previewProceeds(state: GameState, percentage: Int): BigDecimal =
        calculateProceeds(state.btcBigDecimal, effectiveSalePrice(state), percentage)

    private fun effectiveSalePrice(state: GameState): BigDecimal = state.marketPriceBigDecimal
        .multiply(BigDecimal.valueOf(UpgradeEngine.calculateMarketMultiplier(state)), GameNumber.MATH_CONTEXT)

    /**
     * Evaluates auto-sell condition on newly mined BTC delta.
     */
    fun evaluateAutoSell(
        state: GameState,
        minedBtc: BigDecimal
    ): Pair<BigDecimal, BigDecimal> {
        if (!state.autoSellEnabled || minedBtc.compareTo(BigDecimal.ZERO) <= 0) {
            return Pair(minedBtc, BigDecimal.ZERO)
        }

        val currentPrice = state.marketPriceBigDecimal
        val threshold = state.autoSellThresholdBigDecimal

        return if (currentPrice >= threshold) {
            val usdGain = minedBtc.multiply(currentPrice, GameNumber.MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP)
            Pair(BigDecimal.ZERO, usdGain)
        } else {
            Pair(minedBtc, BigDecimal.ZERO)
        }
    }
}
