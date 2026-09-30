package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.Infrastructure
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.PowerEnergySample
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

/** Deterministic, immutable gameplay transitions. */
object GameEngine {

    const val MAX_TICK_SECONDS = 43_200.0
    const val POWER_SAMPLE_INTERVAL_SECONDS = 300L
    const val POWER_HISTORY_CAPACITY = 288

    fun tick(
        state: GameState,
        deltaSeconds: Double,
        wallMillis: Long,
        rng: Random = Random(state.rngSeed)
    ): GameState {
        if (!deltaSeconds.isFinite() || deltaSeconds <= 0.0) return state
        val boundedDelta = deltaSeconds.coerceAtMost(MAX_TICK_SECONDS)

        // Apply each ambient modifier only for the part of the tick before its real expiry.
        val minedBtc = integrateForegroundProduction(state, boundedDelta, wallMillis)

        // Event and market transitions share one deterministic RNG; persist its continuation below.
        val eventTickedState = EventEngine.tick(state, boundedDelta, wallMillis, rng)
        val marketTickedState = MarketEngine.tick(eventTickedState, boundedDelta, rng)
        val (unsoldBtc, autoSellUsdGain) = MarketEngine.evaluateAutoSell(marketTickedState, minedBtc)
        val updatedBtc = marketTickedState.btcBigDecimal.add(unsoldBtc, GameNumber.MATH_CONTEXT)
        val updatedUsd = marketTickedState.usdBigDecimal.add(autoSellUsdGain, GameNumber.MATH_CONTEXT)

        val lifetimeBtc = marketTickedState.stats.lifetimeBtcBigDecimal.add(minedBtc, GameNumber.MATH_CONTEXT)
        val foregroundPassiveBtc = GameNumber.fromString(marketTickedState.stats.foregroundPassiveBtc)
            .add(minedBtc, GameNumber.MATH_CONTEXT)
        val peakHash = maxOf(
            marketTickedState.stats.peakHashrateBigDecimal,
            EconomyEngine.calculateEffectiveHashrate(marketTickedState)
        )
        val playtime = advancePlaytime(marketTickedState, boundedDelta)
        val sold = if (autoSellUsdGain > BigDecimal.ZERO) {
            marketTickedState.stats.totalBtcSoldBigDecimal.add(minedBtc, GameNumber.MATH_CONTEXT)
        } else marketTickedState.stats.totalBtcSoldBigDecimal
        val lifetimeUsdEarned = marketTickedState.stats.lifetimeUsdBigDecimal
            .add(autoSellUsdGain, GameNumber.MATH_CONTEXT)
        val (temperature, _) = EconomyEngine.calculateThermalState(marketTickedState)

        val baseStats = marketTickedState.stats.copy(
            lifetimeBtcMined = lifetimeBtc.toPlainString(),
            foregroundPassiveBtc = foregroundPassiveBtc.toPlainString(),
            peakHashrate = peakHash.toPlainString(),
            totalPlaytimeSeconds = playtime.first,
            playtimeFractionalSeconds = playtime.second.toPlainString(),
            totalBtcSold = sold.toPlainString(),
            lifetimeUsdEarned = lifetimeUsdEarned.toPlainString(),
            peakTemperatureC = maxOf(marketTickedState.stats.peakTemperatureC, temperature)
        )
        val updatedStats = appendPowerSample(baseStats, marketTickedState, boundedDelta)
        val updatedState = marketTickedState.copy(
            btc = updatedBtc.toPlainString(),
            usd = updatedUsd.setScale(2, RoundingMode.HALF_UP).toPlainString(),
            stats = updatedStats,
            rngSeed = rng.nextLong()
        )
        return AchievementEngine.evaluate(updatedState).first
    }

    private fun integrateForegroundProduction(state: GameState, deltaSeconds: Double, wallMillis: Long): BigDecimal {
        val deltaMillis = (deltaSeconds * 1000.0).toLong().coerceAtLeast(1L)
        val start = if (wallMillis >= Long.MIN_VALUE + deltaMillis) wallMillis - deltaMillis else Long.MIN_VALUE
        val end = maxOf(start, wallMillis)
        val expiryBoundaries = state.activeEvents.asSequence()
            .map { it.expiresAtWallMillis }
            .filter { it > start && it < end }
            .distinct()
            .sorted()
            .toList()
        var cursor = start
        var mined = BigDecimal.ZERO
        for (boundary in expiryBoundaries + end) {
            val durationMillis = boundary - cursor
            if (durationMillis > 0L) {
                val events = state.activeEvents.filter { it.expiresAtWallMillis > cursor }
                val segmentState = state.copy(activeEvents = events)
                val rate = EconomyEngine.calculateEffectiveHashrate(segmentState)
                mined = mined.add(
                    EconomyEngine.calculateMinedBtc(rate, durationMillis / 1000.0),
                    GameNumber.MATH_CONTEXT
                )
            }
            cursor = boundary
        }
        return mined
    }

    private fun advancePlaytime(state: GameState, deltaSeconds: Double): Pair<Long, BigDecimal> {
        val delta = BigDecimal(deltaSeconds.toString())
        val fractional = GameNumber.fromString(state.stats.playtimeFractionalSeconds).min(BigDecimal.ONE)
        val total = fractional.add(delta, GameNumber.MATH_CONTEXT)
        val whole = total.setScale(0, RoundingMode.DOWN).toBigInteger()
        val boundedWhole = if (whole > BigDecimal.valueOf(Long.MAX_VALUE).toBigInteger()) Long.MAX_VALUE else whole.toLong()
        val seconds = saturatingAdd(state.stats.totalPlaytimeSeconds, boundedWhole)
        val remainder = total.subtract(BigDecimal(boundedWhole), GameNumber.MATH_CONTEXT).max(BigDecimal.ZERO)
        return seconds to remainder.stripTrailingZeros().let { if (it.scale() < 0) it.setScale(0) else it }
    }

    private fun appendPowerSample(stats: com.antigravity.bitcoinminingtycoon.model.StatsState, state: GameState, deltaSeconds: Double)
        : com.antigravity.bitcoinminingtycoon.model.StatsState {
        val newest = stats.powerEnergyHistory.lastOrNull()?.elapsedSeconds ?: 0L
        val totalSeconds = stats.totalPlaytimeSeconds
        if (totalSeconds < newest + POWER_SAMPLE_INTERVAL_SECONDS) return stats
        val bucketCount = ((totalSeconds - newest) / POWER_SAMPLE_INTERVAL_SECONDS).coerceAtLeast(1L)
        val sampledSeconds = minOf(bucketCount * POWER_SAMPLE_INTERVAL_SECONDS, MAX_TICK_SECONDS.toLong())
        val demand = EconomyEngine.calculatePowerDemand(state).takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
        val capacity = Infrastructure.getPowerStage(state.powerGridTier).capacityKw
        val energy = BigDecimal.valueOf(demand).multiply(BigDecimal(sampledSeconds), GameNumber.MATH_CONTEXT)
            .divide(BigDecimal("3600"), GameNumber.MATH_CONTEXT)
        val sample = PowerEnergySample(
            elapsedSeconds = totalSeconds,
            demandKw = BigDecimal.valueOf(demand).stripTrailingZeros().toPlainString(),
            capacityKw = BigDecimal.valueOf(capacity).stripTrailingZeros().toPlainString(),
            energyKwh = energy.toPlainString()
        )
        return stats.copy(powerEnergyHistory = (stats.powerEnergyHistory + sample).takeLast(POWER_HISTORY_CAPACITY))
    }

    private fun saturatingAdd(left: Long, right: Long): Long =
        if (right > 0L && left > Long.MAX_VALUE - right) Long.MAX_VALUE else (left + right).coerceAtLeast(0L)

    fun performManualTap(state: GameState): GameState {
        val tapOutput = EconomyEngine.calculateManualTapOutput(state)
        val newBtc = state.btcBigDecimal.add(tapOutput, GameNumber.MATH_CONTEXT)
        val lifetimeBtc = state.stats.lifetimeBtcBigDecimal.add(tapOutput, GameNumber.MATH_CONTEXT)
        val manualBtc = GameNumber.fromString(state.stats.manualBtc).add(tapOutput, GameNumber.MATH_CONTEXT)

        val updatedStats = state.stats.copy(
            lifetimeBtcMined = lifetimeBtc.toPlainString(),
            manualBtc = manualBtc.toPlainString(),
            totalManualTaps = saturatingAdd(state.stats.totalManualTaps, 1L)
        )
        val stateAfterTap = state.copy(btc = newBtc.toPlainString(), stats = updatedStats)
        return AchievementEngine.evaluate(stateAfterTap).first
    }
}
