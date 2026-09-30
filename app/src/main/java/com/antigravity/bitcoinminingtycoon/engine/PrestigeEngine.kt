package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.PrestigeNodes
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import java.math.BigDecimal
import kotlin.math.floor
import kotlin.math.sqrt

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
        if (lifetimeBtc < BigDecimal.ONE) return 0L
        val btcDouble = lifetimeBtc.toDouble()
        if (btcDouble.isNaN() || btcDouble.isInfinite() || btcDouble <= 0.0) return 0L
        return floor(sqrt(btcDouble)).toLong().coerceAtLeast(0L)
    }

    /**
     * Calculates additional Satoshi Points claimable based on progress past previous prestiges.
     */
    fun calculateEarnablePoints(state: GameState): Long {
        val totalPoints = calculateTotalPointsFromLifetimeBtc(state.stats.lifetimeBtcBigDecimal)
        val alreadyAwarded = state.stats.lifetimeSatoshiPointsEarned
        return (totalPoints - alreadyAwarded).coerceAtLeast(0L)
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
            currentBtcToLose = state.btc,
            currentUsdToLose = state.usd,
            minersCountToLose = minersCount,
            upgradesCountToLose = upgradesCount,
            currentSatoshiPoints = state.satoshiPoints,
            newSatoshiPointsTotal = state.satoshiPoints + earnable
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

        val updatedSatoshiPoints = state.satoshiPoints + earnable
        val updatedLifetimeSp = state.stats.lifetimeSatoshiPointsEarned + earnable
        val updatedPrestiges = state.stats.totalPrestiges + 1L

        val updatedStats = state.stats.copy(
            totalPrestiges = updatedPrestiges,
            lifetimeSatoshiPointsEarned = updatedLifetimeSp
        )

        val resetState = state.copy(
            btc = "0",
            usd = startingUsd.toPlainString(),
            manualHashStrength = "10",
            miners = emptyMap(),
            purchasedUpgrades = emptySet(),
            powerGridTier = startingPowerGridTier,
            coolingTier = 1,
            activeEvents = emptyList(),
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
}
