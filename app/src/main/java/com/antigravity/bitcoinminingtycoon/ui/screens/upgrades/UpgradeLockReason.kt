package com.antigravity.bitcoinminingtycoon.ui.screens.upgrades

import com.antigravity.bitcoinminingtycoon.content.UpgradeDefinition
import com.antigravity.bitcoinminingtycoon.content.Upgrades
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter

object UpgradeLockReason {
    fun describe(upgrade: UpgradeDefinition, state: GameState): String {
        if (state.stats.lifetimeBtcBigDecimal < upgrade.unlockLifetimeBtc) {
            return "Unlocks after ${NumberFormatter.formatBtc(upgrade.unlockLifetimeBtc, state.settings.numberFormat)} lifetime mined"
        }
        val prerequisiteId = upgrade.prerequisiteUpgradeId ?: return "Prerequisite is not available"
        val prerequisiteName = Upgrades.getById(prerequisiteId)?.name ?: prerequisiteId
        return "Requires $prerequisiteName"
    }
}
