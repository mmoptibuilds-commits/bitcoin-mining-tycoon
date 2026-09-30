package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.Infrastructure
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.content.PowerGridStage
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min

/**
 * Pure Kotlin deterministic simulation engine for Power Grid capacity and soft throttling.
 * Invariant 1: Power deficit never destroys hardware; it scales hashrate proportionally.
 * Invariant 2: Deterministic calculations based purely on fleet and grid tier.
 */
object PowerEngine {

    fun calculateDemandKw(state: GameState): Double {
        var total = 0.0
        for ((minerId, count) in state.miners) {
            if (count <= 0L) continue
            val def = Miners.getById(minerId) ?: continue
            total += def.powerDrawKw * count
        }

        // Active event modifier
        for (event in state.activeEvents) {
            total *= event.powerModifier
        }

        // Upgrades efficiency modifier
        total *= UpgradeEngine.calculatePowerModifier(state)

        return total
    }

    fun calculateCapacityKw(state: GameState): Double {
        return Infrastructure.getPowerStage(state.powerGridTier).capacityKw
    }

    fun calculatePowerFactor(state: GameState): Double {
        val demand = calculateDemandKw(state)
        if (demand <= 0.0) return 1.0
        val capacity = calculateCapacityKw(state)
        return min(1.0, capacity / demand)
    }

    fun canUpgradePowerGrid(state: GameState): Boolean {
        return state.powerGridTier < Infrastructure.POWER_STAGES.size
    }

    fun getNextPowerStage(state: GameState): PowerGridStage? {
        return if (canUpgradePowerGrid(state)) {
            Infrastructure.getPowerStage(state.powerGridTier + 1)
        } else null
    }

    fun upgradePowerGrid(state: GameState): GameState {
        val nextStage = getNextPowerStage(state) ?: return state
        val availableUsd = state.usdBigDecimal
        if (availableUsd < nextStage.costUsd) return state

        val nextUsd = availableUsd.subtract(nextStage.costUsd, GameNumber.MATH_CONTEXT).max(BigDecimal.ZERO)
        return state.copy(
            usd = nextUsd.setScale(2, RoundingMode.HALF_UP).toPlainString(),
            powerGridTier = state.powerGridTier + 1
        )
    }
}
