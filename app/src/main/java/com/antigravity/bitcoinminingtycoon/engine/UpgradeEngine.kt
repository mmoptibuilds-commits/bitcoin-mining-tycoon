package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.UpgradeCategory
import com.antigravity.bitcoinminingtycoon.content.UpgradeDefinition
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
            if (def.category == UpgradeCategory.HARDWARE && def.targetMinerId == null) {
                multiplier *= def.multiplier
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
            if (def.category == UpgradeCategory.MARKET) {
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

    fun buyUpgrade(state: GameState, upgradeId: String): GameState {
        if (upgradeId in state.purchasedUpgrades) return state

        val upgrade = Upgrades.getById(upgradeId) ?: return state
        if (!isUnlocked(upgrade, state)) return state
        if (!canAfford(upgrade, state)) return state

        val availableUsd = state.usdBigDecimal
        val nextUsd = availableUsd.subtract(upgrade.costUsd, GameNumber.MATH_CONTEXT).max(BigDecimal.ZERO)
        val nextPurchased = state.purchasedUpgrades + upgradeId
        val nextTotalPurchased = state.stats.totalUpgradesPurchased + 1L

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
