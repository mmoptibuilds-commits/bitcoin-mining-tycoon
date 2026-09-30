package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.Infrastructure
import com.antigravity.bitcoinminingtycoon.content.BalanceConfig
import com.antigravity.bitcoinminingtycoon.content.Events
import com.antigravity.bitcoinminingtycoon.content.EventType
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

/**
 * Pure deterministic economy engine calculating hashrate, power factor, thermal equilibrium,
 * and BTC mining progression.
 */
object EconomyEngine {

    // 1 Hash produces 10^-10 BTC (0.0000000001 BTC)
    val BTC_PER_HASH_COEFFICIENT: BigDecimal = BigDecimal(BalanceConfig.BTC_PER_HASH)

    fun calculateRawHashrate(state: GameState): BigDecimal {
        var total = BigDecimal.ZERO

        for ((minerId, count) in state.miners) {
            if (count <= 0L) continue
            val def = Miners.getById(minerId) ?: continue
            val minerMultiplier = UpgradeEngine.calculateMinerMultiplier(state, minerId)
            val minerTotal = def.baseHashrate.multiply(BigDecimal(count), GameNumber.MATH_CONTEXT)
            val boostedTotal = GameNumber.multiply(minerTotal, minerMultiplier)
            total = total.add(boostedTotal, GameNumber.MATH_CONTEXT)
        }

        val globalMultiplier = UpgradeEngine.calculateGlobalHashrateMultiplier(state)
        return GameNumber.multiply(total, globalMultiplier).setScale(0, RoundingMode.HALF_UP)
    }


    fun calculatePowerDemand(state: GameState): Double =
        PowerEngine.calculateDemandKw(state)

    fun calculatePowerFactor(state: GameState): Double =
        PowerEngine.calculatePowerFactor(state)

    fun calculateHeatDemand(state: GameState): Double =
        ThermalEngine.calculateHeatDemand(state)

    fun calculateThermalState(state: GameState): Pair<Double, Double> {
        val temp = ThermalEngine.calculateEquilibriumTemp(state)
        val factor = ThermalEngine.calculateThermalFactor(temp)
        return Pair(temp, factor)
    }

    /**
     * Production formula from GAME_DESIGN.md:
     * effectiveHashrate = rawHashrate * powerFactor * thermalFactor * eventFactor * prestigeFactor
     */
    fun calculateEffectiveHashrate(state: GameState): BigDecimal {
        val raw = calculateRawHashrate(state)
        if (raw <= BigDecimal.ZERO) return BigDecimal.ZERO
        return GameNumber.multiply(raw, calculateCombinedHashrateMultiplier(state)).setScale(0, RoundingMode.HALF_UP)
    }

    /** Effective contribution used by hardware inspection; mining tick output stays calculated above. */
    fun calculateEffectiveMinerHashrate(state: GameState, minerId: String): BigDecimal {
        val definition = Miners.getById(minerId) ?: return BigDecimal.ZERO
        val count = state.miners[minerId]?.takeIf { it > 0L } ?: return BigDecimal.ZERO
        val baseContribution = definition.baseHashrate
            .multiply(BigDecimal(count), GameNumber.MATH_CONTEXT)
        val minerContribution = GameNumber.multiply(baseContribution, UpgradeEngine.calculateMinerMultiplier(state, minerId))
        val rawContribution = GameNumber.multiply(minerContribution, UpgradeEngine.calculateGlobalHashrateMultiplier(state))
        return GameNumber.multiply(rawContribution, calculateCombinedHashrateMultiplier(state))
    }

    private fun calculateCombinedHashrateMultiplier(state: GameState): Double {

        val powerFactor = calculatePowerFactor(state)
        val (_, thermalFactor) = calculateThermalState(state)

        var eventMultiplier = 1.0
        for (event in state.activeEvents) {
            if (Events.getById(event.eventId) != null) eventMultiplier *= event.multiplier
        }

        // Prestige multiplier: 1% per permanent Satoshi Point (5% if satoshi_vision unlocked)
        val baseSpMultiplier = if (state.purchasedPrestigeNodes.contains("satoshi_vision")) {
            BalanceConfig.PRESTIGE_POINT_HASHRATE_BONUS_WITH_VISION
        } else BalanceConfig.PRESTIGE_POINT_HASHRATE_BONUS
        var prestigeMultiplier = 1.0 + (state.satoshiPoints * baseSpMultiplier)
        if (state.purchasedPrestigeNodes.contains("efficient_silicon")) {
            prestigeMultiplier *= BalanceConfig.EFFICIENT_SILICON_HASHRATE_MULTIPLIER
        }
        if (state.purchasedPrestigeNodes.contains("quantum_legacy")) {
            prestigeMultiplier *= BalanceConfig.QUANTUM_LEGACY_HASHRATE_MULTIPLIER
        }

        val hasPositiveProductionEvent = state.activeEvents.any { active ->
            val definition = Events.getById(active.eventId)
            definition?.type == EventType.AMBIENT_POSITIVE &&
                (active.multiplier > 1.0 || active.powerModifier < 1.0 || active.heatModifier < 1.0)
        }
        if (hasPositiveProductionEvent) {
            eventMultiplier *= UpgradeEngine.calculatePositiveEventProductionMultiplier(state)
        }

        return powerFactor * thermalFactor * eventMultiplier * prestigeMultiplier
    }

    fun calculateMinedBtc(effectiveHashrate: BigDecimal, deltaSeconds: Double): BigDecimal {
        if (effectiveHashrate <= BigDecimal.ZERO || deltaSeconds <= 0.0) return BigDecimal.ZERO

        val deltaBigDecimal = BigDecimal(deltaSeconds.toString(), GameNumber.MATH_CONTEXT)
        val hashes = effectiveHashrate.multiply(deltaBigDecimal, GameNumber.MATH_CONTEXT)
        return hashes.multiply(BTC_PER_HASH_COEFFICIENT, GameNumber.MATH_CONTEXT)
    }

    fun calculateManualTapOutput(state: GameState): BigDecimal {
        val baseTap = state.manualHashBigDecimal
        var tapMultiplier = UpgradeEngine.calculateTapMultiplier(state)
        if (state.purchasedPrestigeNodes.contains("quantum_firmware")) {
            tapMultiplier *= 2.0
        }
        val boostedTap = GameNumber.multiply(baseTap, tapMultiplier)
        return boostedTap.multiply(BTC_PER_HASH_COEFFICIENT, GameNumber.MATH_CONTEXT)
    }
}
