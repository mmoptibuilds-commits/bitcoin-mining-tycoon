package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.content.BalanceConfig
import com.antigravity.bitcoinminingtycoon.model.PendingOfflineSummary
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal

data class OfflineReport(
    val durationSeconds: Double,
    val effectiveHashrate: BigDecimal,
    val minedBtc: BigDecimal,
    val lastSavedWallMillis: Long = 0L,
    val creditedThroughWallMillis: Long = 0L
)

object OfflineEngine {

    const val MAX_OFFLINE_SECONDS = BalanceConfig.MAX_OFFLINE_SECONDS // F15
    const val MIN_OFFLINE_REPORT_SECONDS = 60.0 // Summary-only threshold

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
                minedBtc = BigDecimal.ZERO,
                lastSavedWallMillis = lastSavedWallMillis,
                creditedThroughWallMillis = lastSavedWallMillis
            )
        }

        val elapsedMillis = (currentWallMillis - lastSavedWallMillis).coerceAtMost((MAX_OFFLINE_SECONDS * 1000.0).toLong())
        val clampedDeltaSeconds = elapsedMillis / 1000.0
        val departureState = state.copy(activeEvents = state.activeEvents.filter { it.expiresAtWallMillis > lastSavedWallMillis })
        val hashrateAtDeparture = offlineHashrate(departureState)
        if (hashrateAtDeparture <= BigDecimal.ZERO || clampedDeltaSeconds <= 0.0) {
            return OfflineReport(
                durationSeconds = clampedDeltaSeconds,
                effectiveHashrate = hashrateAtDeparture,
                minedBtc = BigDecimal.ZERO,
                lastSavedWallMillis = lastSavedWallMillis,
                creditedThroughWallMillis = currentWallMillis
            )
        }

        val endMillis = lastSavedWallMillis + elapsedMillis
        val boundaries = (state.activeEvents.map { it.expiresAtWallMillis })
            .filter { it > lastSavedWallMillis && it < endMillis }
            .distinct()
            .sorted()
        var cursor = lastSavedWallMillis
        var minedBtc = BigDecimal.ZERO
        for (boundary in boundaries + endMillis) {
            val segmentMillis = boundary - cursor
            if (segmentMillis > 0L) {
                val active = state.activeEvents.filter { it.expiresAtWallMillis > cursor }
                val segmentState = state.copy(activeEvents = active)
                val hashrate = offlineHashrate(segmentState)
                minedBtc = minedBtc.add(
                    EconomyEngine.calculateMinedBtc(hashrate, segmentMillis / 1000.0),
                    GameNumber.MATH_CONTEXT
                )
            }
            cursor = boundary
        }

        return OfflineReport(
            durationSeconds = clampedDeltaSeconds,
            effectiveHashrate = hashrateAtDeparture,
            minedBtc = minedBtc,
            lastSavedWallMillis = lastSavedWallMillis,
            creditedThroughWallMillis = currentWallMillis
        )
    }

    private fun offlineHashrate(state: GameState) = EconomyEngine.calculateEffectiveHashrate(state)
        .multiply(
            java.math.BigDecimal.valueOf(UpgradeEngine.calculateOfflineProductionMultiplier(state)),
            GameNumber.MATH_CONTEXT
        )

    /**
     * Applies offline earnings idempotently to GameState and updates lifetime stats.
     */
    fun applyOfflineReward(state: GameState, report: OfflineReport): GameState {
        if (report.creditedThroughWallMillis <= state.lastSaveWallMillis || report.creditedThroughWallMillis <= 0L) return state

        val creditedBtc = report.minedBtc.max(BigDecimal.ZERO)
        val newBtc = state.btcBigDecimal.add(creditedBtc, GameNumber.MATH_CONTEXT)
        val newLifetimeBtc = state.stats.lifetimeBtcBigDecimal.add(creditedBtc, GameNumber.MATH_CONTEXT)
        val newOfflineBtc = GameNumber.fromString(state.stats.offlineBtc).add(creditedBtc, GameNumber.MATH_CONTEXT)

        val updatedStats = state.stats.copy(
            lifetimeBtcMined = newLifetimeBtc.toPlainString(),
            offlineBtc = newOfflineBtc.toPlainString()
        )

        val priorSummary = state.pendingOfflineSummary
        val showSummary = report.durationSeconds >= MIN_OFFLINE_REPORT_SECONDS && creditedBtc > BigDecimal.ZERO
        val summary = when {
            priorSummary != null && showSummary -> PendingOfflineSummary(
                durationSeconds = (priorSummary.durationSeconds + report.durationSeconds).coerceAtMost(MAX_OFFLINE_SECONDS),
                creditedBtc = GameNumber.fromString(priorSummary.creditedBtc).add(creditedBtc, GameNumber.MATH_CONTEXT).toPlainString(),
                creditedAtWallMillis = report.creditedThroughWallMillis
            )
            priorSummary != null -> priorSummary
            showSummary -> PendingOfflineSummary(report.durationSeconds, creditedBtc.toPlainString(), report.creditedThroughWallMillis)
            else -> null
        }

        val updatedState = state.copy(
            btc = newBtc.toPlainString(),
            stats = updatedStats,
            pendingOfflineSummary = summary,
            lastSaveWallMillis = report.creditedThroughWallMillis
        )

        val (stateWithAchievements, _) = AchievementEngine.evaluate(updatedState)
        return stateWithAchievements
    }
}
