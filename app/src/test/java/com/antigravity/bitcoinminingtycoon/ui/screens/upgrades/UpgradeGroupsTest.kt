package com.antigravity.bitcoinminingtycoon.ui.screens.upgrades

import com.antigravity.bitcoinminingtycoon.content.UpgradeCategory
import com.antigravity.bitcoinminingtycoon.content.Upgrades
import com.antigravity.bitcoinminingtycoon.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpgradeGroupsTest {

    @Test
    fun fourInvestmentTracksRetainEveryUpgradeExactlyOnce() {
        val groups = listOf(
            UpgradeGroup.TAPPING,
            UpgradeGroup.COMPUTE,
            UpgradeGroup.INFRASTRUCTURE,
            UpgradeGroup.AUTOMATION
        )
        val grouped = groups.flatMap { it.upgrades() }

        assertEquals(56, UpgradeGroup.ALL.upgrades().size)
        assertEquals(56, grouped.size)
        assertEquals(Upgrades.ALL.map { it.id }.toSet(), grouped.map { it.id }.toSet())
        assertEquals(56, grouped.map { it.id }.distinct().size)
        assertEquals(9, UpgradeGroup.TAPPING.upgrades().size)
        assertEquals(22, UpgradeGroup.COMPUTE.upgrades().size)
        assertEquals(9, UpgradeGroup.AUTOMATION.upgrades().size)
    }

    @Test
    fun infrastructureTrackKeepsPowerAndCoolingAsSeparateSubgroups() {
        val subgroups = UpgradeGroup.INFRASTRUCTURE.subgroups()

        assertEquals(listOf(UpgradeCategory.POWER, UpgradeCategory.COOLING), subgroups.map { it.category })
        assertEquals(listOf(8, 8), subgroups.map { it.upgrades.size })
        assertTrue(subgroups.all { it.upgrades.all { upgrade -> upgrade.category == it.category } })
    }

    @Test
    fun unavailableUpgradesExplainTheActualLifetimeOrResearchRequirement() {
        val thresholdUpgrade = Upgrades.ALL.first().copy(unlockLifetimeBtc = java.math.BigDecimal("0.1"))
        val prerequisiteUpgrade = Upgrades.ALL.first { it.prerequisiteUpgradeId != null }

        assertTrue(UpgradeLockReason.describe(thresholdUpgrade, GameState()).contains("0.10000000 BTC"))
        assertEquals(
            "Requires ${Upgrades.getById(prerequisiteUpgrade.prerequisiteUpgradeId!!)!!.name}",
            UpgradeLockReason.describe(prerequisiteUpgrade, GameState())
        )
    }
}
