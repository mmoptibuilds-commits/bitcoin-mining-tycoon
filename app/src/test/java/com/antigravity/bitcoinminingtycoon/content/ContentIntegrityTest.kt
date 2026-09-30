package com.antigravity.bitcoinminingtycoon.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentIntegrityTest {

    @Test
    fun stableContentIdsRemainUniqueAndReferencesResolve() {
        assertEquals(20, Miners.ALL.size)
        assertEquals(10, Infrastructure.POWER_STAGES.size)
        assertEquals(7, Infrastructure.COOLING_STAGES.size)
        assertEquals(10, Events.ALL.size)

        val minerIds = Miners.ALL.map { it.id }.toSet()
        val upgradeById = Upgrades.ALL.associateBy { it.id }
        assertEquals(Upgrades.ALL.size, upgradeById.size)
        assertEquals(Miners.ALL.size, minerIds.size)
        assertEquals(Achievements.ALL.size, Achievements.ALL.map { it.id }.toSet().size)
        assertEquals(Events.ALL.size, Events.ALL.map { it.id }.toSet().size)
        assertEquals(PrestigeNodes.ALL.size, PrestigeNodes.ALL.map { it.id }.toSet().size)

        Upgrades.ALL.forEach { upgrade ->
            assertTrue("Unknown miner target on ${upgrade.id}", upgrade.targetMinerId == null || upgrade.targetMinerId in minerIds)
            assertTrue("Unknown prerequisite on ${upgrade.id}", upgrade.prerequisiteUpgradeId == null || upgrade.prerequisiteUpgradeId in upgradeById)
        }
        assertTrue(hasNoUpgradePrerequisiteCycles(upgradeById))
    }

    private fun hasNoUpgradePrerequisiteCycles(upgrades: Map<String, UpgradeDefinition>): Boolean {
        val complete = mutableSetOf<String>()
        val active = mutableSetOf<String>()
        fun visit(id: String): Boolean {
            if (id in active) return false
            if (id in complete) return true
            active += id
            val prerequisite = upgrades[id]?.prerequisiteUpgradeId
            if (prerequisite != null && !visit(prerequisite)) return false
            active -= id
            complete += id
            return true
        }
        return upgrades.keys.all(::visit)
    }
}
