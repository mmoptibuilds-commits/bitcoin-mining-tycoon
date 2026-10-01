package com.antigravity.bitcoinminingtycoon.ui.screens.upgrades

import com.antigravity.bitcoinminingtycoon.content.UpgradeCategory
import com.antigravity.bitcoinminingtycoon.content.UpgradeDefinition
import com.antigravity.bitcoinminingtycoon.content.Upgrades

enum class UpgradeGroup(val label: String, private val categoryOrder: List<UpgradeCategory>) {
    ALL("All tracks", listOf(UpgradeCategory.TAP, UpgradeCategory.COMPUTE, UpgradeCategory.POWER, UpgradeCategory.COOLING, UpgradeCategory.AUTOMATION)),
    TAPPING("Tapping", listOf(UpgradeCategory.TAP)),
    COMPUTE("Compute", listOf(UpgradeCategory.COMPUTE)),
    INFRASTRUCTURE("Infrastructure", listOf(UpgradeCategory.POWER, UpgradeCategory.COOLING)),
    AUTOMATION("Automation", listOf(UpgradeCategory.AUTOMATION));

    fun subgroups(source: List<UpgradeDefinition> = Upgrades.ALL): List<UpgradeSubgroup> =
        categoryOrder.mapNotNull { category ->
            source.filter { it.category == category }
                .takeIf { it.isNotEmpty() }
                ?.let { UpgradeSubgroup(category, it) }
        }

    fun upgrades(source: List<UpgradeDefinition> = Upgrades.ALL): List<UpgradeDefinition> =
        subgroups(source).flatMap { it.upgrades }
}

data class UpgradeSubgroup(
    val category: UpgradeCategory,
    val upgrades: List<UpgradeDefinition>
)
