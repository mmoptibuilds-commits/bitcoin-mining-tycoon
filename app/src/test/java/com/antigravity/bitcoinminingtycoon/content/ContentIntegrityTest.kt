package com.antigravity.bitcoinminingtycoon.content

import com.antigravity.bitcoinminingtycoon.model.GameState
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

        assertTrue(Achievements.getById("asic_vanguard")!!.isSatisfied(GameState(miners = mapOf("entry_asic" to 1L))))
        assertTrue(Achievements.getById("modern_density")!!.isSatisfied(GameState(miners = mapOf("industrial_asic" to 1L))))
    }

    @Test
    fun v12UpgradeCatalogueRetainsAllLegacyIdsAndAddsTheSpecifiedGroupedTracks() {
        val retainedIds = setOf(
            "copper_fingers", "mechanical_switches", "tactile_keycaps", "macro_coprocessor",
            "optical_trigger", "neural_tap_relay", "cpu_overclock_1", "cpu_overclock_2",
            "gpu_vram_tuning", "dual_gpu_crosslink", "gpu_6x_custom_os", "asic_custom_firmware",
            "industrial_asic_tuning", "asic_rack_backplane", "server_room_redundancy",
            "warehouse_air_curtains", "power_undervolting", "gold_rated_psus", "titanium_power_dist",
            "smart_grid_inverters", "superconducting_busbars", "thermal_paste_upgrade",
            "high_static_pressure_fans", "phase_change_tim", "evaporative_water_towers",
            "microchannel_cold_plates", "market_ticker_display", "limit_order_bot",
            "institutional_otc_desk", "mining_pool_syndicate", "custom_sha_fpga",
            "optical_computing_coprocessor"
        )
        val addedIds = setOf(
            "precision_actuation", "input_pipeline", "quantum_fingerprints",
            "asic_firmware_2", "rack_thermal_routing", "warehouse_scheduler", "hydro_fluid_cooling",
            "geothermal_hash_mesh", "nuclear_fpga_farm", "fusion_batcher", "orbital_shard_cache",
            "lunar_quantum_scheduler", "demand_response_grid", "solar_tracking_inverters",
            "fusion_load_balancer", "cold_plate_lattice", "immersion_circulation",
            "cryogenic_heat_recovery", "auto_sell_controller", "fleet_scheduler", "market_spread_router",
            "energy_aware_dispatch", "offline_mining_buffer", "event_response_automation"
        )

        val allIds = Upgrades.ALL.map { it.id }.toSet()
        assertEquals(56, Upgrades.ALL.size)
        assertEquals(56, allIds.size)
        assertTrue("Legacy IDs must remain save-compatible", allIds.containsAll(retainedIds))
        assertEquals("New IDs must match the approved catalogue", addedIds, allIds - retainedIds)

        val counts = Upgrades.ALL.groupingBy { it.category.name }.eachCount()
        assertEquals(9, counts["TAP"])
        assertEquals(22, counts["COMPUTE"])
        assertEquals(8, counts["POWER"])
        assertEquals(8, counts["COOLING"])
        assertEquals(9, counts["AUTOMATION"])
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
