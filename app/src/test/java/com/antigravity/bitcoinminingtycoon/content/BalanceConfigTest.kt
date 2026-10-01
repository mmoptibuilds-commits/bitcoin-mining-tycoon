package com.antigravity.bitcoinminingtycoon.content

import com.antigravity.bitcoinminingtycoon.engine.UpgradeEngine
import com.antigravity.bitcoinminingtycoon.model.MarketTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class BalanceConfigTest {

    @Test
    fun singleBalanceConfigMatchesFreshSaveAndFirstRunContract() {
        assertEquals(BigDecimal.ZERO, BalanceConfig.INITIAL_CASH_USD)
        assertEquals("50000", BalanceConfig.INITIAL_MANUAL_HASHRATE)
        assertEquals(BigDecimal("50000.00"), BalanceConfig.INITIAL_MARKET_PRICE_USD)
        assertEquals(BigDecimal("10.00"), Miners.getById("ancient_cpu")!!.baseCostUsd)
        assertEquals(BigDecimal("3.00"), BigDecimal(BalanceConfig.DAY_ONE_CASH_USD))
        assertTrue(BigDecimal(BalanceConfig.DAY_ONE_CASH_USD) <= Miners.getById("ancient_cpu")!!.baseCostUsd.multiply(BigDecimal("0.30")))
        assertEquals(BalanceConfig.INITIAL_MANUAL_HASHRATE, com.antigravity.bitcoinminingtycoon.model.GameState().manualHashStrength)
        assertEquals(BalanceConfig.INITIAL_MANUAL_HASHRATE, com.antigravity.bitcoinminingtycoon.data.GameSave().manualHashStrength)
        BalanceConfig.MARKET_TRENDS.forEach { (trend, tuning) ->
            assertEquals("${trend.name} transition weights sum to one", 1.0, tuning.transitionWeights.values.sum(), 1e-9)
            assertTrue("${trend.name} has supported transitions", tuning.transitionWeights.isNotEmpty())
            assertTrue("${trend.name} probabilities are nonnegative", tuning.transitionWeights.values.all { it >= 0.0 })
            assertTrue("${trend.name} delta bounds are ordered", tuning.deltaMin <= tuning.deltaMax)
        }
    }

    @Test
    fun contentAndTuningPreserveEveryStableTierIdAndGroupedUpgrade() {
        val minerIds = setOf(
            "ancient_cpu", "gaming_cpu", "gaming_gpu", "dual_gpu_rig", "gpu_rig_6x",
            "entry_asic", "industrial_asic", "asic_rack", "server_room", "mining_warehouse",
            "mining_farm", "hydro_facility", "geothermal_complex", "nuclear_campus",
            "immersion_megafarm", "fusion_complex", "orbital_solar_miner", "lunar_mining_array",
            "quantum_hash_facility", "dyson_hash_swarm"
        )
        assertEquals(minerIds, Miners.ALL.map { it.id }.toSet())
        assertEquals(minerIds, BalanceConfig.MINER_TUNING.keys)
        assertEquals(10, Infrastructure.POWER_STAGES.size)
        assertEquals(7, Infrastructure.COOLING_STAGES.size)
        assertEquals(56, Upgrades.ALL.size)
        assertEquals("gpu_rig_6x", Upgrades.getById("gpu_6x_custom_os")!!.targetMinerId)

        val categoryCounts = Upgrades.ALL.groupingBy { it.category }.eachCount()
        assertEquals(9, categoryCounts[UpgradeCategory.TAP])
        assertEquals(22, categoryCounts[UpgradeCategory.COMPUTE])
        assertEquals(8, categoryCounts[UpgradeCategory.POWER])
        assertEquals(8, categoryCounts[UpgradeCategory.COOLING])
        assertEquals(9, categoryCounts[UpgradeCategory.AUTOMATION])

        val effectTypes = Upgrades.ALL.flatMap { it.specialEffects }.map { it.type }.toSet()
        assertEquals(UpgradeSpecialEffectType.values().toSet(), effectTypes)
        assertEquals(effectTypes, UpgradeEngine.HANDLED_SPECIAL_EFFECT_TYPES)
        assertFalse(BalanceConfig.stableHash().isBlank())
        assertEquals(64, BalanceConfig.stableHash().length)
        assertEquals(BalanceConfig.stableHash(), BalanceConfig.stableHash())
        assertEquals(6, MarketTrend.values().size)
    }
}
