package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class EconomyEngineTest {

    @Test
    fun calculateRawHashrate_sumsOwnedMinersCorrectly() {
        val state = GameState(
            miners = mapOf(
                "ancient_cpu" to 10L, // 10 * 50,000 = 500,000 H/s
                "gaming_cpu" to 5L    // 5 * 50,000 = 250,000 H/s
            )
        )
        val raw = EconomyEngine.calculateRawHashrate(state)
        assertEquals(BigDecimal("750000"), raw)
    }

    @Test
    fun calculatePowerFactor_scalesDownUnderDeficit() {
        // Base power capacity for tier 1 is 0.5 kW
        // 25 ancient CPUs = 25 * 0.02 = 0.50 kW (Exactly at capacity)
        val stateOptimal = GameState(
            powerGridTier = 1,
            miners = mapOf("ancient_cpu" to 25L)
        )
        val factorOptimal = EconomyEngine.calculatePowerFactor(stateOptimal)
        assertEquals(1.0, factorOptimal, 0.001)

        // 50 ancient CPUs = 50 * 0.02 = 1.0 kW (2x capacity demand -> 0.5 factor)
        val stateDeficit = GameState(
            powerGridTier = 1,
            miners = mapOf("ancient_cpu" to 50L)
        )
        val factorDeficit = EconomyEngine.calculatePowerFactor(stateDeficit)
        assertEquals(0.5, factorDeficit, 0.001)
    }

    @Test
    fun calculateThermalState_appliesDocumentedBands() {
        // Tier 1 cooling has 0.5 dissipation
        // 5 ancient cpus = 5 * 0.1 = 0.5 heat (zero excess -> 25°C ambient)
        val stateOptimal = GameState(
            coolingTier = 1,
            miners = mapOf("ancient_cpu" to 5L)
        )
        val (tempOptimal, factorOptimal) = EconomyEngine.calculateThermalState(stateOptimal)
        assertEquals(25.0, tempOptimal, 0.1)
        assertEquals(1.0, factorOptimal, 0.001)

        // Massive heat to reach >75°C
        // Excess heat = 105.0 -> temp = 25 + 52.5 = 77.5°C
        val stateHot = GameState(
            coolingTier = 1,
            miners = mapOf("ancient_cpu" to 1055L) // 105.5 heat - 0.5 = 105 excess
        )
        val (tempHot, factorHot) = EconomyEngine.calculateThermalState(stateHot)
        assertEquals(77.5, tempHot, 0.1)
        assertTrue("Factor at 77.5C should be in [0.9, 1.0]", factorHot in 0.90..1.0)
    }

    @Test
    fun calculateEffectiveHashrate_appliesModifiersDeterministically() {
        val state = GameState(
            powerGridTier = 2, // 10 kW capacity (2 Gaming GPUs demand 0.60 kW)
            coolingTier = 2,   // 5.0 dissipation (2 Gaming GPUs demand 1.6 heat)
            miners = mapOf("gaming_gpu" to 2L), // 2 * 300,000 = 600,000 H/s
            satoshiPoints = 50L, // +50% prestige bonus (1.5x)
            activeEvents = listOf(
                ActiveEventState("lucky_block", 9999999999999L, multiplier = 2.0)
            )
        )

        val effective = EconomyEngine.calculateEffectiveHashrate(state)
        // 600,000 H/s * 1.0 power * 1.0 thermal * 2.0 event * 1.5 prestige.
        assertEquals(0, BigDecimal("1800000").compareTo(effective))
    }

    @Test
    fun eventResponseUpgradeBoostsKnownPositiveProductionEventsButNotMarketOnlyEvents() {
        val without = GameState(miners = mapOf("ancient_cpu" to 1L))
        val controller = without.copy(purchasedUpgrades = setOf("event_response_automation"))
        val asicBreakthrough = controller.copy(
            activeEvents = listOf(ActiveEventState("asic_breakthrough", Long.MAX_VALUE, multiplier = 1.35))
        )
        val bullRun = controller.copy(
            activeEvents = listOf(ActiveEventState("bull_run", Long.MAX_VALUE, multiplier = 1.0))
        )
        val withoutController = asicBreakthrough.copy(purchasedUpgrades = emptySet())

        val eventRate = EconomyEngine.calculateEffectiveHashrate(asicBreakthrough)
        val plainEventRate = EconomyEngine.calculateEffectiveHashrate(withoutController)
        val marketOnlyRate = EconomyEngine.calculateEffectiveHashrate(bullRun)
        val plainRate = EconomyEngine.calculateEffectiveHashrate(without)

        assertEquals(0, plainEventRate.multiply(BigDecimal("1.25")).compareTo(eventRate))
        assertEquals(0, plainRate.compareTo(marketOnlyRate))
    }

    @Test
    fun calculateMinedBtc_accurateOverDeltaSeconds() {
        val hashrate = BigDecimal("10000000000") // 10 GH/s
        val delta = 1.0 // 1 second
        // 10^10 hashes * 5e-11 BTC/hash = 0.5 BTC
        val btc = EconomyEngine.calculateMinedBtc(hashrate, delta)
        assertEquals(0, BigDecimal("0.5").compareTo(btc))
    }
}
