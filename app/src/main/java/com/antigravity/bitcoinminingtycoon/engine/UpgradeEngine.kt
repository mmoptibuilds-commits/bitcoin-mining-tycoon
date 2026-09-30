package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.UpgradeCategory
import com.antigravity.bitcoinminingtycoon.content.UpgradeDefinition
import com.antigravity.bitcoinminingtycoon.content.UpgradeSpecialEffect
import com.antigravity.bitcoinminingtycoon.content.UpgradeSpecialEffectType
import com.antigravity.bitcoinminingtycoon.content.Upgrades
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure Kotlin deterministic simulation engine for purchasing upgrades and aggregating modifiers.
 * Invariant 1: Upgrades are purchased strictly once.
 * Invariant 2: Overdraft prevention — USD balance never drops below zero.
 */
object UpgradeEngine {

    val HANDLED_SPECIAL_EFFECT_TYPES: Set<UpgradeSpecialEffectType> = UpgradeSpecialEffectType.values().toSet()

    fun calculateTapMultiplier(state: GameState): Double {
        var multiplier = 1.0
        for (upgradeId in state.purchasedUpgrades) {
            val def = Upgrades.getById(upgradeId) ?: continue
            if (def.category == UpgradeCategory.TAP) {
                multiplier *= def.multiplier
            }
        }
        return multiplier
    }

    fun calculateGlobalHashrateMultiplier(state: GameState): Double {
        var multiplier = 1.0
        for (upgradeId in state.purchasedUpgrades) {
            val def = Upgrades.getById(upgradeId) ?: continue
            if (def.category == UpgradeCategory.COMPUTE && def.targetMinerId == null) {
                multiplier *= def.multiplier
            }
            def.specialEffects.filterIsInstance<UpgradeSpecialEffect.GlobalHashrateBonus>().forEach {
                multiplier *= it.multiplier
            }
        }
        return multiplier
    }

    fun calculateMinerMultiplier(state: GameState, minerId: String): Double {
        var multiplier = 1.0
        for (upgradeId in state.purchasedUpgrades) {
            val def = Upgrades.getById(upgradeId) ?: continue
            if (def.targetMinerId == minerId) {
                multiplier *= def.multiplier
            }
        }
        return multiplier
    }

    fun calculatePowerModifier(state: GameState): Double {
        var modifier = 1.0
        for (upgradeId in state.purchasedUpgrades) {
            val def = Upgrades.getById(upgradeId) ?: continue
            if (def.category == UpgradeCategory.POWER) {
                modifier *= def.multiplier
            }
            def.specialEffects.filterIsInstance<UpgradeSpecialEffect.PowerEfficiencyBonus>().forEach {
                modifier *= it.multiplier
            }
        }
        return modifier
    }

    fun calculateCoolingModifier(state: GameState): Double {
        var modifier = 1.0
        for (upgradeId in state.purchasedUpgrades) {
            val def = Upgrades.getById(upgradeId) ?: continue
            if (def.category == UpgradeCategory.COOLING) {
                modifier *= def.multiplier
            }
        }
        return modifier
    }

    fun calculateMarketMultiplier(state: GameState): Double {
        var multiplier = 1.0
        for (upgradeId in state.purchasedUpgrades) {
            val def = Upgrades.getById(upgradeId) ?: continue
            if (def.category == UpgradeCategory.AUTOMATION) {
                multiplier *= def.multiplier
            }
        }
        return multiplier
    }

    fun isUnlocked(upgrade: UpgradeDefinition, state: GameState): Boolean {
        if (state.stats.lifetimeBtcBigDecimal < upgrade.unlockLifetimeBtc) return false
        val prereq = upgrade.prerequisiteUpgradeId ?: return true
        return prereq in state.purchasedUpgrades
    }

    fun canAfford(upgrade: UpgradeDefinition, state: GameState): Boolean {
        return state.usdBigDecimal >= upgrade.costUsd
    }

    fun hasSpecialEffect(state: GameState, type: UpgradeSpecialEffectType): Boolean =
        state.purchasedUpgrades.asSequence()
            .mapNotNull(Upgrades::getById)
            .flatMap { it.specialEffects.asSequence() }
            .any { it.type == type }

    fun canEnableAutoSell(state: GameState): Boolean =
        state.autoSellEnabled || hasSpecialEffect(state, UpgradeSpecialEffectType.AUTO_SELL_UNLOCK)

    fun calculateOfflineProductionMultiplier(state: GameState): Double =
        state.purchasedUpgrades.asSequence()
            .mapNotNull(Upgrades::getById)
            .flatMap { it.specialEffects.asSequence() }
            .filterIsInstance<UpgradeSpecialEffect.OfflineProductionBonus>()
            .fold(1.0) { result, effect -> result * effect.multiplier }
            .coerceIn(1.0, 2.0)

    fun calculatePositiveEventProductionMultiplier(state: GameState): Double =
        state.purchasedUpgrades.asSequence()
            .mapNotNull(Upgrades::getById)
            .flatMap { it.specialEffects.asSequence() }
            .filterIsInstance<UpgradeSpecialEffect.PositiveEventProductionBonus>()
            .fold(1.0) { result, effect -> result * effect.multiplier }
            .coerceIn(1.0, 2.0)

    fun criticalTapEffect(state: GameState): UpgradeSpecialEffect.CriticalTap? =
        state.purchasedUpgrades.asSequence()
            .mapNotNull(Upgrades::getById)
            .flatMap { it.specialEffects.asSequence() }
            .filterIsInstance<UpgradeSpecialEffect.CriticalTap>()
            .firstOrNull()

    fun buyUpgrade(state: GameState, upgradeId: String): GameState {
        if (upgradeId in state.purchasedUpgrades) return state

        val upgrade = Upgrades.getById(upgradeId) ?: return state
        if (!isUnlocked(upgrade, state)) return state
        if (!canAfford(upgrade, state)) return state

        val availableUsd = state.usdBigDecimal
        val nextUsd = availableUsd.subtract(upgrade.costUsd, GameNumber.MATH_CONTEXT).max(BigDecimal.ZERO)
        val nextPurchased = state.purchasedUpgrades + upgradeId
        val nextTotalPurchased = if (state.stats.totalUpgradesPurchased == Long.MAX_VALUE) Long.MAX_VALUE
        else state.stats.totalUpgradesPurchased + 1L

        val updatedStats = state.stats.copy(
            totalUpgradesPurchased = nextTotalPurchased
        )

        return state.copy(
            usd = nextUsd.setScale(2, RoundingMode.HALF_UP).toPlainString(),
            purchasedUpgrades = nextPurchased,
            stats = updatedStats
        )
    }
}
