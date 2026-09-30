package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

/**
 * Deterministic Game Engine coordinator executing sequential state updates per frame tick.
 */
object GameEngine {

    fun tick(
        state: GameState,
        deltaSeconds: Double,
        wallMillis: Long,
        rng: Random = Random(state.rngSeed)
    ): GameState {
        if (deltaSeconds <= 0.0) return state

        // 1. Tick events (expire outdated, roll new events when timer elapses)
        val eventTickedState = EventEngine.tick(state, deltaSeconds, wallMillis, rng)

        // 2. Advance simulated market price & trend
        val marketTickedState = MarketEngine.tick(eventTickedState, deltaSeconds, rng)

        // 3. Passive mining production
        val effectiveHashrate = EconomyEngine.calculateEffectiveHashrate(marketTickedState)
        val minedBtc = EconomyEngine.calculateMinedBtc(effectiveHashrate, deltaSeconds)

        // 4. Auto-sell automation execution (Decision D017)
        val (unSoldBtc, autoSellUsdGain) = MarketEngine.evaluateAutoSell(marketTickedState, minedBtc)
        val updatedBtc = marketTickedState.btcBigDecimal.add(unSoldBtc, GameNumber.MATH_CONTEXT)
        val updatedUsd = marketTickedState.usdBigDecimal.add(autoSellUsdGain, GameNumber.MATH_CONTEXT)

        // 5. Update stats
        val lifetimeBtc = marketTickedState.stats.lifetimeBtcBigDecimal.add(minedBtc, GameNumber.MATH_CONTEXT)
        val peakHash = maxOf(marketTickedState.stats.peakHashrateBigDecimal, effectiveHashrate)
        val newPlaytime = marketTickedState.stats.totalPlaytimeSeconds + deltaSeconds.toLong().coerceAtLeast(0L)
        val totalBtcSold = if (autoSellUsdGain > BigDecimal.ZERO) {
            marketTickedState.stats.totalBtcSoldBigDecimal.add(minedBtc, GameNumber.MATH_CONTEXT)
        } else {
            marketTickedState.stats.totalBtcSoldBigDecimal
        }
        val lifetimeUsdEarned = marketTickedState.stats.lifetimeUsdBigDecimal.add(autoSellUsdGain, GameNumber.MATH_CONTEXT)

        val updatedStats = marketTickedState.stats.copy(
            lifetimeBtcMined = lifetimeBtc.toPlainString(),
            peakHashrate = peakHash.toPlainString(),
            totalPlaytimeSeconds = newPlaytime,
            totalBtcSold = totalBtcSold.toPlainString(),
            lifetimeUsdEarned = lifetimeUsdEarned.toPlainString()
        )

        val updatedState = marketTickedState.copy(
            btc = updatedBtc.toPlainString(),
            usd = updatedUsd.setScale(2, RoundingMode.HALF_UP).toPlainString(),
            stats = updatedStats
        )

        // 6. Evaluate achievements
        val (stateWithAchievements, _) = AchievementEngine.evaluate(updatedState)
        return stateWithAchievements
    }

    /**
     * Executes manual player MINE tap.
     */
    fun performManualTap(state: GameState): GameState {
        val tapOutput = EconomyEngine.calculateManualTapOutput(state)
        val newBtc = state.btcBigDecimal.add(tapOutput, GameNumber.MATH_CONTEXT)
        val lifetimeBtc = state.stats.lifetimeBtcBigDecimal.add(tapOutput, GameNumber.MATH_CONTEXT)

        val updatedStats = state.stats.copy(
            lifetimeBtcMined = lifetimeBtc.toPlainString(),
            totalManualTaps = state.stats.totalManualTaps + 1L
        )

        val stateAfterTap = state.copy(
            btc = newBtc.toPlainString(),
            stats = updatedStats
        )

        val (stateWithAchievements, _) = AchievementEngine.evaluate(stateAfterTap)
        return stateWithAchievements
    }
}
