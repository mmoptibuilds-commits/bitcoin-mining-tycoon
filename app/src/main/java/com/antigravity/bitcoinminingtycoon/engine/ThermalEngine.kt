package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.CoolingStage
import com.antigravity.bitcoinminingtycoon.content.Infrastructure
import com.antigravity.bitcoinminingtycoon.content.BalanceConfig
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.content.Events
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

    const val AMBIENT_TEMP_C = BalanceConfig.AMBIENT_TEMPERATURE_C

    fun calculateHeatDemand(state: GameState): Double {
        var total = 0.0
        for ((minerId, count) in state.miners) {
            if (count <= 0L) continue
            val def = Miners.getById(minerId) ?: continue
            total += def.heatLoad * count
        }

        // Active event modifier
        for (event in state.activeEvents) {
            if (Events.getById(event.eventId) != null) total *= event.heatModifier
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
        return AMBIENT_TEMP_C + (excess * BalanceConfig.THERMAL_EXCESS_TEMP_PER_HEAT)
    }

    fun calculateThermalFactor(temp: Double): Double {
        val factor = when {
            temp < BalanceConfig.THERMAL_FULL_OUTPUT_BELOW_C -> 1.0
            temp < BalanceConfig.THERMAL_FIRST_BAND_END_C ->
                1.0 - BalanceConfig.THERMAL_FIRST_BAND_DROP *
                ((temp - BalanceConfig.THERMAL_FULL_OUTPUT_BELOW_C) / BalanceConfig.THERMAL_STANDARD_BAND_WIDTH_C)
            temp < BalanceConfig.THERMAL_SECOND_BAND_END_C ->
                BalanceConfig.THERMAL_SECOND_BAND_FACTOR - BalanceConfig.THERMAL_SECOND_BAND_DROP *
                ((temp - BalanceConfig.THERMAL_FIRST_BAND_END_C) / BalanceConfig.THERMAL_STANDARD_BAND_WIDTH_C)
            else -> max(
                BalanceConfig.THERMAL_MIN_OUTPUT_FACTOR,
                BalanceConfig.THERMAL_HIGH_BAND_FACTOR - BalanceConfig.THERMAL_HIGH_BAND_DROP *
                    ((temp - BalanceConfig.THERMAL_SECOND_BAND_END_C) / BalanceConfig.THERMAL_HIGH_BAND_WIDTH_C)
            )
        }
        return factor.coerceIn(BalanceConfig.THERMAL_MIN_OUTPUT_FACTOR, 1.0)
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
