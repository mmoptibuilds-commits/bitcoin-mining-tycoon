package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.Infrastructure
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PowerThermalTest {

    @Test
    fun powerDemandAndFactor_scalesCorrectly() {
        val cpu = Miners.ALL[0] // 0.05 kW
        val gpu = Miners.ALL[2] // 0.30 kW

        val state = GameState(
            miners = mapOf(cpu.id to 10L, gpu.id to 5L), // 10*0.05 + 5*0.30 = 0.50 + 1.50 = 2.00 kW
            powerGridTier = 1 // House Outlet: 0.5 kW capacity
        )

        val demand = PowerEngine.calculateDemandKw(state)
        assertEquals(2.00, demand, 0.001)

        val capacity = PowerEngine.calculateCapacityKw(state)
        assertEquals(0.5, capacity, 0.001)

        // Factor = 0.5 / 2.0 = 0.25
        val factor = PowerEngine.calculatePowerFactor(state)
        assertEquals(0.25, factor, 0.001)

        // Upgrade power grid to Tier 2 (Commercial Grid: 10.0 kW)
        val stateTier2 = state.copy(powerGridTier = 2)
        val factorTier2 = PowerEngine.calculatePowerFactor(stateTier2)
        assertEquals(1.0, factorTier2, 0.001) // 10.0 kW > 2.0 kW -> 100% efficiency
    }

    @Test
    fun powerGridUpgrade_deductsCostAndIncrementsTier() {
        val initial = GameState(
            powerGridTier = 1,
            usd = "500.00"
        )
        val nextStage = Infrastructure.getPowerStage(2) // $200.00

        assertTrue(PowerEngine.canUpgradePowerGrid(initial))
        val upgraded = PowerEngine.upgradePowerGrid(initial)

        assertEquals(2, upgraded.powerGridTier)
        assertEquals(BigDecimal("300.00"), upgraded.usdBigDecimal) // 500 - 200

        // Attempt upgrade when funds insufficient
        val poorState = upgraded.copy(usd = "50.00")
        val failedUpgrade = PowerEngine.upgradePowerGrid(poorState)
        assertEquals(poorState, failedUpgrade)
    }

    @Test
    fun thermalEquilibriumAndDegradation_curveFollowsSpec() {
        // Test factor transitions
        assertEquals(1.00, ThermalEngine.calculateThermalFactor(65.0), 0.001)
        assertEquals(1.00, ThermalEngine.calculateThermalFactor(70.0), 0.001)
        assertEquals(0.95, ThermalEngine.calculateThermalFactor(75.0), 0.001) // 1.0 - 0.10 * 0.5
        assertEquals(0.90, ThermalEngine.calculateThermalFactor(80.0), 0.001)
        assertEquals(0.825, ThermalEngine.calculateThermalFactor(85.0), 0.001) // 0.90 - 0.15 * 0.5
        assertEquals(0.75, ThermalEngine.calculateThermalFactor(90.0), 0.001)
        assertEquals(0.625, ThermalEngine.calculateThermalFactor(100.0), 0.001) // 0.75 - 0.25 * 0.5
        assertEquals(0.50, ThermalEngine.calculateThermalFactor(110.0), 0.001) // clamped at floor 0.50
        assertEquals(0.50, ThermalEngine.calculateThermalFactor(500.0), 0.001) // extreme heat clamped at 0.50

        // Test equilibrium temp calculation
        val gpu = Miners.ALL[2] // heatLoad 0.8
        val state = GameState(
            miners = mapOf(gpu.id to 100L), // 80.0 heat units
            coolingTier = 3 // Industrial HVAC: 50.0 dissipation
        )

        // Excess = 80 - 50 = 30. Temp = 25.0 + 30 * 0.5 = 40.0°C
        val temp = ThermalEngine.calculateEquilibriumTemp(state)
        assertEquals(40.0, temp, 0.001)
        assertEquals(1.0, ThermalEngine.calculateThermalFactor(temp), 0.001)
    }

    @Test
    fun coolingUpgrade_deductsCostAndIncrementsTier() {
        val initial = GameState(
            coolingTier = 1,
            usd = "500.00"
        )
        val nextStage = Infrastructure.getCoolingStage(2) // Commercial AC: $150.00

        assertTrue(ThermalEngine.canUpgradeCooling(initial))
        val upgraded = ThermalEngine.upgradeCooling(initial)

        assertEquals(2, upgraded.coolingTier)
        assertEquals(BigDecimal("350.00"), upgraded.usdBigDecimal) // 500 - 150
    }

    @Test
    fun maxTierBoundary_cannotUpgradePastCap() {
        val maxPower = GameState(powerGridTier = 10, usd = "1000000000000000.00")
        assertFalse(PowerEngine.canUpgradePowerGrid(maxPower))
        assertEquals(maxPower, PowerEngine.upgradePowerGrid(maxPower))

        val maxCooling = GameState(coolingTier = 7, usd = "1000000000000000.00")
        assertFalse(ThermalEngine.canUpgradeCooling(maxCooling))
        assertEquals(maxCooling, ThermalEngine.upgradeCooling(maxCooling))
    }
}
