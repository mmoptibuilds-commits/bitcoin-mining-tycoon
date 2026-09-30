package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.CoolingStage
import com.antigravity.bitcoinminingtycoon.content.Infrastructure
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

/**
 * Pure Kotlin deterministic simulation engine for Thermal Equilibrium and heat throttling.
 * Invariant 1: Heat never destroys hardware; it scales hashrate along a documented efficiency curve.
 * Invariant 2: Instantaneous thermal equilibrium model (Decision D013).
 */
object ThermalEngine {

    const val AMBIENT_TEMP_C = 25.0

    fun calculateHeatDemand(state: GameState): Double {
        var total = 0.0
        for ((minerId, count) in state.miners) {
            if (count <= 0L) continue
            val def = Miners.getById(minerId) ?: continue
            total += def.heatLoad * count
        }

        // Active event modifier
        for (event in state.activeEvents) {
            total *= event.heatModifier
        }

        return total
    }

    fun calculateDissipation(state: GameState): Double {
        val base = Infrastructure.getCoolingStage(state.coolingTier).dissipationRating
        val upgradeMod = UpgradeEngine.calculateCoolingModifier(state)
        return base * upgradeMod
    }

    fun calculateEquilibriumTemp(state: GameState): Double {
        val heat = calculateHeatDemand(state)
        val dissipation = calculateDissipation(state)
        val excess = max(0.0, heat - dissipation)
        return AMBIENT_TEMP_C + (excess * 0.5)
    }

    fun calculateThermalFactor(temp: Double): Double {
        val factor = when {
            temp < 70.0 -> 1.0
            temp < 80.0 -> 1.0 - 0.10 * ((temp - 70.0) / 10.0)
            temp < 90.0 -> 0.90 - 0.15 * ((temp - 80.0) / 10.0)
            else -> max(0.50, 0.75 - 0.25 * ((temp - 90.0) / 20.0))
        }
        return factor.coerceIn(0.5, 1.0)
    }

    fun canUpgradeCooling(state: GameState): Boolean {
        return state.coolingTier < Infrastructure.COOLING_STAGES.size
    }

    fun getNextCoolingStage(state: GameState): CoolingStage? {
        return if (canUpgradeCooling(state)) {
            Infrastructure.getCoolingStage(state.coolingTier + 1)
        } else null
    }

    fun upgradeCooling(state: GameState): GameState {
        val nextStage = getNextCoolingStage(state) ?: return state
        val availableUsd = state.usdBigDecimal
        if (availableUsd < nextStage.costUsd) return state

        val nextUsd = availableUsd.subtract(nextStage.costUsd, GameNumber.MATH_CONTEXT).max(BigDecimal.ZERO)
        return state.copy(
            usd = nextUsd.setScale(2, RoundingMode.HALF_UP).toPlainString(),
            coolingTier = state.coolingTier + 1
        )
    }
}
