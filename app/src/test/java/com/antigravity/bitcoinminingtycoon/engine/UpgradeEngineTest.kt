package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.content.Upgrades
import com.antigravity.bitcoinminingtycoon.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class UpgradeEngineTest {

    @Test
    fun tapMultipliers_aggregateAndBoostTapOutput() {
        val baseState = GameState(
            manualHashStrength = "10",
            purchasedUpgrades = emptySet()
        )

        val tap1 = EconomyEngine.calculateManualTapOutput(baseState)

        // Purchase copper_fingers (2.0x)
        val state1 = baseState.copy(purchasedUpgrades = setOf("copper_fingers"))
        val tap2 = EconomyEngine.calculateManualTapOutput(state1)
        assertEquals(tap1.multiply(BigDecimal("2")), tap2)

        // Purchase mechanical_switches (2.0x) -> total 4.0x
        val state2 = state1.copy(purchasedUpgrades = setOf("copper_fingers", "mechanical_switches"))
        val tap3 = EconomyEngine.calculateManualTapOutput(state2)
        assertEquals(tap1.multiply(BigDecimal("4")), tap3)
    }

    @Test
    fun minerSpecificAndGlobalHashrate_applyCorrectly() {
        val cpu = Miners.ALL[0] // Ancient CPU: 1 H/s
        val gpu = Miners.ALL[2] // Gaming GPU: 50 H/s

        val baseState = GameState(
            miners = mapOf(cpu.id to 10L, gpu.id to 2L), // 10*1 + 2*50 = 110 H/s
            powerGridTier = 3,
            coolingTier = 3
        )

        val baseHash = EconomyEngine.calculateEffectiveHashrate(baseState)
        assertEquals(BigDecimal("110"), baseHash)

        // Apply GPU boost: gpu_vram_tuning (1.35x on GPU only)
        // 10*1 + 2*50*1.35 = 10 + 135 = 145 H/s
        val stateGpuBoost = baseState.copy(purchasedUpgrades = setOf("gpu_vram_tuning"))
        val boostedHash = EconomyEngine.calculateEffectiveHashrate(stateGpuBoost)
        assertEquals(BigDecimal("145"), boostedHash)

        // Apply Global boost: mining_pool_syndicate (1.20x on total)
        // 145 * 1.20 = 174 H/s
        val stateGlobalBoost = stateGpuBoost.copy(purchasedUpgrades = setOf("gpu_vram_tuning", "mining_pool_syndicate"))
        val finalHash = EconomyEngine.calculateEffectiveHashrate(stateGlobalBoost)
        assertEquals(BigDecimal("174"), finalHash)
    }

    @Test
    fun powerAndCoolingModifiers_applyToEnvironmentalSubsystems() {
        val gpu = Miners.ALL[2] // 0.30 kW, 0.8 heatLoad
        val baseState = GameState(
            miners = mapOf(gpu.id to 10L), // 3.0 kW demand, 8.0 heat
            powerGridTier = 2,
            coolingTier = 2 // Commercial AC: 5.0 dissipation
        )

        // Base power demand: 3.0 kW
        assertEquals(3.0, PowerEngine.calculateDemandKw(baseState), 0.001)

        // Buy power_undervolting (0.90x power draw) -> 3.0 * 0.90 = 2.70 kW
        val stateUndervoleted = baseState.copy(purchasedUpgrades = setOf("power_undervolting"))
        assertEquals(2.70, PowerEngine.calculateDemandKw(stateUndervoleted), 0.001)

        // Base cooling dissipation: 5.0
        assertEquals(5.0, ThermalEngine.calculateDissipation(baseState), 0.001)

        // Buy thermal_paste_upgrade (1.25x dissipation) -> 5.0 * 1.25 = 6.25
        val stateCooled = baseState.copy(purchasedUpgrades = setOf("thermal_paste_upgrade"))
        assertEquals(6.25, ThermalEngine.calculateDissipation(stateCooled), 0.001)
    }

    @Test
    fun prerequisiteUnlockingAndDuplicatePrevention() {
        val upgrade1 = Upgrades.getById("copper_fingers")!!
        val upgrade2 = Upgrades.getById("mechanical_switches")!! // Requires copper_fingers

        val stateWithoutPrereq = GameState(usd = "1000.00", purchasedUpgrades = emptySet())

        // Upgrade 2 should be locked without prerequisite
        assertFalse(UpgradeEngine.isUnlocked(upgrade2, stateWithoutPrereq))
        val failedBuy = UpgradeEngine.buyUpgrade(stateWithoutPrereq, upgrade2.id)
        assertEquals(stateWithoutPrereq, failedBuy)

        // Buy Upgrade 1
        assertTrue(UpgradeEngine.isUnlocked(upgrade1, stateWithoutPrereq))
        val stateWithU1 = UpgradeEngine.buyUpgrade(stateWithoutPrereq, upgrade1.id)
        assertTrue(upgrade1.id in stateWithU1.purchasedUpgrades)
        assertEquals(1L, stateWithU1.stats.totalUpgradesPurchased)

        // Now Upgrade 2 is unlocked
        assertTrue(UpgradeEngine.isUnlocked(upgrade2, stateWithU1))
        val stateWithU2 = UpgradeEngine.buyUpgrade(stateWithU1, upgrade2.id)
        assertTrue(upgrade2.id in stateWithU2.purchasedUpgrades)
        assertEquals(2L, stateWithU2.stats.totalUpgradesPurchased)

        // Attempt duplicate purchase of Upgrade 1: must be no-op
        val duplicateAttempt = UpgradeEngine.buyUpgrade(stateWithU2, upgrade1.id)
        assertEquals(stateWithU2, duplicateAttempt)
    }
}
