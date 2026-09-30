package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal

data class OfflineReport(
    val durationSeconds: Double,
    val effectiveHashrate: BigDecimal,
    val minedBtc: BigDecimal
)

object OfflineEngine {

    const val MAX_OFFLINE_SECONDS = 12.0 * 3600.0 // 12 hours maximum clamp (F15)
    const val MIN_OFFLINE_REPORT_SECONDS = 60.0 // Minimum 1 minute to trigger report

    /**
     * Calculates offline earnings defensively according to Decision D014 and Feature F15.
     * Clamps negative delta to zero and future delta to 12 hours.
     */
    fun calculateOfflineProgress(
        state: GameState,
        lastSavedWallMillis: Long,
        currentWallMillis: Long
    ): OfflineReport {
        if (lastSavedWallMillis <= 0L || currentWallMillis <= lastSavedWallMillis) {
            return OfflineReport(
                durationSeconds = 0.0,
                effectiveHashrate = BigDecimal.ZERO,
                minedBtc = BigDecimal.ZERO
            )
        }

        val rawDeltaSeconds = (currentWallMillis - lastSavedWallMillis) / 1000.0
        val clampedDeltaSeconds = minOf(rawDeltaSeconds, MAX_OFFLINE_SECONDS).coerceAtLeast(0.0)

        // Effective hashrate snapshot at background time (events expired naturally)
        val hashrate = EconomyEngine.calculateEffectiveHashrate(state)
        if (hashrate <= BigDecimal.ZERO || clampedDeltaSeconds < MIN_OFFLINE_REPORT_SECONDS) {
            return OfflineReport(
                durationSeconds = clampedDeltaSeconds,
                effectiveHashrate = hashrate,
                minedBtc = BigDecimal.ZERO
            )
        }

        val minedBtc = EconomyEngine.calculateMinedBtc(hashrate, clampedDeltaSeconds)

        return OfflineReport(
            durationSeconds = clampedDeltaSeconds,
            effectiveHashrate = hashrate,
            minedBtc = minedBtc
        )
    }

    /**
     * Applies offline earnings idempotently to GameState and updates lifetime stats.
     */
    fun applyOfflineReward(state: GameState, report: OfflineReport): GameState {
        if (report.minedBtc <= BigDecimal.ZERO) return state

        val newBtc = state.btcBigDecimal.add(report.minedBtc, GameNumber.MATH_CONTEXT)
        val newLifetimeBtc = state.stats.lifetimeBtcBigDecimal.add(report.minedBtc, GameNumber.MATH_CONTEXT)

        val updatedStats = state.stats.copy(
            lifetimeBtcMined = newLifetimeBtc.toPlainString()
        )

        val updatedState = state.copy(
            btc = newBtc.toPlainString(),
            stats = updatedStats
        )

        val (stateWithAchievements, _) = AchievementEngine.evaluate(updatedState)
        return stateWithAchievements
    }
}
