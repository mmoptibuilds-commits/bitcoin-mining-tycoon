package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.PrestigeNodes
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal
import java.math.BigInteger

data class PrestigePreview(
    val earnablePoints: Long,
    val isPrestigeAvailable: Boolean,
    val currentBtcToLose: String,
    val currentUsdToLose: String,
    val minersCountToLose: Long,
    val upgradesCountToLose: Int,
    val currentSatoshiPoints: Long,
    val newSatoshiPointsTotal: Long
)

object PrestigeEngine {

    /**
     * Deterministic sublinear prestige calculation: floor(sqrt(lifetimeBtc)).
     * Single source of truth shared identically by preview and apply.
     */
    fun calculateTotalPointsFromLifetimeBtc(lifetimeBtc: BigDecimal): Long {
        if (lifetimeBtc.signum() <= 0) return 0L
        return GameNumber.saturatingLong(GameNumber.floorSquareRoot(lifetimeBtc))
    }

    /**
     * Calculates additional Satoshi Points claimable based on progress past previous prestiges.
     */
    fun calculateEarnablePoints(state: GameState): Long {
        val totalPoints = calculateTotalPointsFromLifetimeBtc(state.stats.lifetimeBtcBigDecimal)
        val alreadyAwardedInV2 = saturatingAdd(
            state.stats.prestigePointsBaselineV2,
            state.stats.prestigePointsEarnedSinceV2
        )
        return (totalPoints - alreadyAwardedInV2).coerceAtLeast(0L)
    }

    /**
     * Previews exact reset and preservation values prior to confirmation.
     */
    fun previewPrestige(state: GameState): PrestigePreview {
        val earnable = calculateEarnablePoints(state)
        val minersCount = state.miners.values.sum()
        val upgradesCount = state.purchasedUpgrades.size

        return PrestigePreview(
            earnablePoints = earnable,
            isPrestigeAvailable = earnable > 0L,
            currentBtcToLose = state.btcBigDecimal
                .add(state.autoSellPendingBtcBigDecimal, GameNumber.MATH_CONTEXT)
                .toPlainString(),
            currentUsdToLose = state.usd,
            minersCountToLose = minersCount,
            upgradesCountToLose = upgradesCount,
            currentSatoshiPoints = state.satoshiPoints,
            newSatoshiPointsTotal = saturatingAdd(state.satoshiPoints, earnable)
        )
    }

    /**
     * Executes hard economy reset per Decision D015 while preserving permanent prestige progression.
     */
    fun applyPrestige(state: GameState): GameState {
        val earnable = calculateEarnablePoints(state)
        if (earnable <= 0L) return state // Guard against accidental 0-reward reset

        val startingUsd = if (state.purchasedPrestigeNodes.contains("cold_start")) {
            BigDecimal("500.00")
        } else {
            BigDecimal.ZERO
        }

        val startingPowerGridTier = if (state.purchasedPrestigeNodes.contains("industrial_memory")) {
            2
        } else {
            1
        }

        val updatedSatoshiPoints = saturatingAdd(state.satoshiPoints, earnable)
        val updatedLifetimeSp = saturatingAdd(state.stats.lifetimeSatoshiPointsEarned, earnable)
        val updatedEarnedSinceV2 = saturatingAdd(state.stats.prestigePointsEarnedSinceV2, earnable)
        val updatedPrestiges = saturatingAdd(state.stats.totalPrestiges, 1L)

        val updatedStats = state.stats.copy(
            totalPrestiges = updatedPrestiges,
            lifetimeSatoshiPointsEarned = updatedLifetimeSp,
            prestigePointsEarnedSinceV2 = updatedEarnedSinceV2
        )

        val resetState = state.copy(
            btc = "0",
            usd = startingUsd.toPlainString(),
            manualHashStrength = GameState().manualHashStrength,
            miners = emptyMap(),
            purchasedUpgrades = emptySet(),
            powerGridTier = startingPowerGridTier,
            coolingTier = 1,
            activeEvents = emptyList(),
            autoSellEnabled = false,
            autoSellPendingBtc = "0",
            satoshiPoints = updatedSatoshiPoints,
            stats = updatedStats
        )

        val (stateWithAchievements, _) = AchievementEngine.evaluate(resetState)
        return stateWithAchievements
    }

    /**
     * Atomically purchases a permanent node in the Satoshi Legacy Tree.
     */
    fun buyPrestigeNode(state: GameState, nodeId: String): GameState {
        val node = PrestigeNodes.getById(nodeId) ?: return state

        // Already owned
        if (nodeId in state.purchasedPrestigeNodes) return state

        // Check prerequisites
        val prereqsSatisfied = node.prerequisiteNodeIds.all { it in state.purchasedPrestigeNodes }
        if (!prereqsSatisfied) return state

        // Check affordability
        if (state.satoshiPoints < node.costSp) return state

        val updatedSp = state.satoshiPoints - node.costSp
        val updatedNodes = state.purchasedPrestigeNodes + nodeId

        val updatedState = state.copy(
            satoshiPoints = updatedSp,
            purchasedPrestigeNodes = updatedNodes
        )

        val (stateWithAchievements, _) = AchievementEngine.evaluate(updatedState)
        return stateWithAchievements
    }

    private fun saturatingAdd(a: Long, b: Long): Long =
        if (b > 0L && a > Long.MAX_VALUE - b) Long.MAX_VALUE else (a + b).coerceAtLeast(0L)

}
