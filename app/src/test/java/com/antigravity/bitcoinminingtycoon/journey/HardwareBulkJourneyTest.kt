package com.antigravity.bitcoinminingtycoon.journey

import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.engine.BulkMode
import com.antigravity.bitcoinminingtycoon.engine.EconomyEngine
import com.antigravity.bitcoinminingtycoon.engine.FleetEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/**
 * Automated Journey verification for journeys/03-hardware-bulk.md:
 * - Start with funded test state
 * - Open Hardware and buy x1, x10, x25, and MAX
 * - Verify owned counts, USD, hashrate, power and heat update atomically
 * - Verify unaffordable and locked purchases produce zero state mutation
 */
class HardwareBulkJourneyTest {

    @Test
    fun executeHardwareBulkJourney_endToEnd() {
        val cpuMiner = Miners.ALL[0] // Ancient CPU: $10 base, 50,000 H/s, 0.05 kW, 0.1 °C
        val gamingCpu = Miners.ALL[1] // Gaming CPU: $35 base, 50,000 H/s, 0.15 kW, 0.3 °C
        val gpuMiner = Miners.ALL[2] // Gaming GPU: $95 base, 300,000 H/s, 0.30 kW, 0.8 °C
        val dysonSwarm = Miners.ALL[19] // Dyson Hash Swarm: locked (1,000,000 BTC)

        // 1. Funded test state
        var state = GameState(
            usd = "50000.00",
            btc = "0.01000000",
            stats = StatsState(lifetimeBtcMined = "0.05000000"),
            powerGridTier = 3, // 100 kW grid
            coolingTier = 3 // 50 dissipation
        )

        val initialHashrate = EconomyEngine.calculateEffectiveHashrate(state)
        assertEquals(BigDecimal.ZERO, initialHashrate)

        // 2. Buy x1 Ancient CPU
        state = FleetEngine.buyMiner(state, cpuMiner.id, BulkMode.X1)
        assertEquals(1L, state.miners[cpuMiner.id])
        assertEquals(BigDecimal("49990.00"), state.usdBigDecimal)
        val hashAfter1 = EconomyEngine.calculateEffectiveHashrate(state)
        assertEquals(BigDecimal("50000"), hashAfter1)

        // 3. Buy x10 Ancient CPU
        state = FleetEngine.buyMiner(state, cpuMiner.id, BulkMode.X10)
        assertEquals(11L, state.miners[cpuMiner.id])
        assertTrue("USD should have decreased", state.usdBigDecimal < BigDecimal("49990.00"))
        val hashAfter11 = EconomyEngine.calculateEffectiveHashrate(state)
        assertEquals(BigDecimal("550000"), hashAfter11)

        // 4. Buy x25 Gaming CPU
        assertTrue(FleetEngine.isUnlocked(gamingCpu, state))
        state = FleetEngine.buyMiner(state, gamingCpu.id, BulkMode.X25)
        assertEquals(25L, state.miners[gamingCpu.id])
        // Hashrate = 11*50,000 + 25*50,000 = 1,800,000
        val hashAfterGamingCpu = EconomyEngine.calculateEffectiveHashrate(state)
        assertEquals(BigDecimal("1800000"), hashAfterGamingCpu)

        // 5. Buy MAX Gaming GPU
        val usdBeforeGpuMax = state.usdBigDecimal
        state = FleetEngine.buyMiner(state, gpuMiner.id, BulkMode.MAX)
        val ownedGpus = state.miners[gpuMiner.id] ?: 0L
        assertTrue("Should have purchased multiple GPUs with MAX", ownedGpus > 10L)
        assertTrue("USD should have decreased significantly", state.usdBigDecimal < usdBeforeGpuMax)
        // Verify remaining USD cannot afford even 1 more GPU
        val (_, costForOneMore) = FleetEngine.calculatePurchase(gpuMiner, ownedGpus, BulkMode.X1, state.usdBigDecimal)
        assertTrue("Remaining USD must be less than next unit cost", state.usdBigDecimal < costForOneMore)

        // 6. Verify power demand and heat load updated deterministically
        val totalPower = EconomyEngine.calculatePowerDemand(state)
        val (temp, _) = EconomyEngine.calculateThermalState(state)
        assertTrue("Fleet power demand must be positive", totalPower > 0.0)
        assertTrue("Equilibrium temperature must be at least ambient 25°C", temp >= 25.0)

        // Verify with minimal cooling tier 1 that temp rises above 25°C
        val (hotTemp, _) = EconomyEngine.calculateThermalState(state.copy(coolingTier = 1))
        assertTrue("With tier 1 cooling, temperature must exceed 25°C", hotTemp > 25.0)

        // 7. Attempt locked purchase: Dyson Hash Swarm
        assertFalse("Dyson swarm must be locked", FleetEngine.isUnlocked(dysonSwarm, state))
        val stateAfterLockedAttempt = FleetEngine.buyMiner(state, dysonSwarm.id, BulkMode.X1)
        assertEquals("State must not mutate when trying to purchase locked hardware", state, stateAfterLockedAttempt)

        // 8. Attempt purchase with 0 USD
        val zeroCashState = state.copy(usd = "0.00")
        val stateAfterZeroCashAttempt = FleetEngine.buyMiner(zeroCashState, cpuMiner.id, BulkMode.X1)
        assertEquals("State must not mutate when cash is 0", zeroCashState, stateAfterZeroCashAttempt)
    }
}
