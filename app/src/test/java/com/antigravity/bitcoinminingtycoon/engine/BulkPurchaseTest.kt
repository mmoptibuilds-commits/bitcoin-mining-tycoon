package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class BulkPurchaseTest {

    @Test
    fun bulkPurchase_costMatchesIterativeSum() {
        val miner = Miners.ALL[0] // Ancient CPU: base $10.00, growth 1.12
        val owned = 5L

        // Check x10 cost via closed-form
        val bulk10Cost = GameNumber.calculateBulkCost(miner.baseCostUsd, miner.growthRate, owned, 10L)

        // Calculate iterative sum
        var iterativeSum = BigDecimal.ZERO
        for (k in 0 until 10) {
            val itemCost = GameNumber.calculateBulkCost(miner.baseCostUsd, miner.growthRate, owned + k, 1L)
            iterativeSum = iterativeSum.add(itemCost)
        }

        assertEquals(iterativeSum, bulk10Cost)
    }

    @Test
    fun calculateMaxAffordable_exactBoundaryAndNeverOverdrafts() {
        val miner = Miners.ALL[2] // Gaming GPU: base $95.00, growth 1.14
        val owned = 0L

        // Available: one cent below base cost -> 0 affordable
        val underPrice = miner.baseCostUsd.subtract(BigDecimal("0.01"))
        val countUnder = GameNumber.calculateMaxAffordable(underPrice, miner.baseCostUsd, miner.growthRate, owned)
        assertEquals(0L, countUnder)

        // Exact base cost -> 1 affordable
        val countExact = GameNumber.calculateMaxAffordable(miner.baseCostUsd, miner.baseCostUsd, miner.growthRate, owned)
        assertEquals(1L, countExact)

        // Available: $10,000.00
        val count10k = GameNumber.calculateMaxAffordable(BigDecimal("10000.00"), miner.baseCostUsd, miner.growthRate, owned)
        assertTrue(count10k > 1L)
        val costForCount = GameNumber.calculateBulkCost(miner.baseCostUsd, miner.growthRate, owned, count10k)
        assertTrue("Cost $costForCount must not exceed 10000", costForCount <= BigDecimal("10000.00"))

        val costForOneMore = GameNumber.calculateBulkCost(miner.baseCostUsd, miner.growthRate, owned, count10k + 1L)
        assertTrue("Cost for one more $costForOneMore must exceed 10000", costForOneMore > BigDecimal("10000.00"))
    }

    @Test
    fun calculateMaxAffordable_withAstronomicalFundsDoesNotHang() {
        val miner = Miners.ALL[19] // Dyson Hash Swarm
        val massiveFunds = BigDecimal("10").pow(50)

        val startTime = System.currentTimeMillis()
        val count = GameNumber.calculateMaxAffordable(massiveFunds, miner.baseCostUsd, miner.growthRate, 0L)
        val elapsed = System.currentTimeMillis() - startTime

        assertTrue("MAX calculation with 10^50 must complete under 100ms", elapsed < 100)
        assertTrue(count > 0L)
    }

    @Test
    fun buyMiner_atomicStateUpdateAndOverdraftPrevention() {
        val miner = Miners.ALL[0] // Ancient CPU: $10.00
        val initialState = GameState(
            usd = "1000.00",
            miners = emptyMap()
        )

        // Buy x1 ($10.00)
        val stateAfter1 = FleetEngine.buyMiner(initialState, miner.id, BulkMode.X1)
        assertEquals(BigDecimal("990.00"), stateAfter1.usdBigDecimal)
        assertEquals(1L, stateAfter1.miners[miner.id])
        assertEquals(1L, stateAfter1.stats.totalMinersPurchased)

        // Buy x10 (starting from owned=1)
        val cost10 = GameNumber.calculateBulkCost(miner.baseCostUsd, miner.growthRate, 1L, 10L)
        val stateAfter10 = FleetEngine.buyMiner(stateAfter1, miner.id, BulkMode.X10)
        assertEquals(BigDecimal("990.00").subtract(cost10), stateAfter10.usdBigDecimal)
        assertEquals(11L, stateAfter10.miners[miner.id])
        assertEquals(11L, stateAfter10.stats.totalMinersPurchased)

        // Try to buy when unaffordable: must return unchanged state
        val poorState = GameState(usd = "1.00", miners = mapOf(miner.id to 11L))
        val poorBuy = FleetEngine.buyMiner(poorState, miner.id, BulkMode.X1)
        assertEquals(poorState, poorBuy)
    }

    @Test
    fun buyMiner_lockedTierCannotBePurchased() {
        val lockedMiner = Miners.ALL[1] // Gaming CPU: requires 0.00001000 BTC lifetime
        val stateWithNoLifetimeBtc = GameState(
            usd = "10000.00",
            stats = StatsState(lifetimeBtcMined = "0.00000000")
        )

        assertFalse(FleetEngine.isUnlocked(lockedMiner, stateWithNoLifetimeBtc))
        val buyAttempt = FleetEngine.buyMiner(stateWithNoLifetimeBtc, lockedMiner.id, BulkMode.X1)
        assertEquals(stateWithNoLifetimeBtc, buyAttempt)

        // Now with sufficient lifetime BTC
        val stateUnlocked = stateWithNoLifetimeBtc.copy(
            stats = StatsState(lifetimeBtcMined = "0.00001000")
        )
        assertTrue(FleetEngine.isUnlocked(lockedMiner, stateUnlocked))
        val successfulBuy = FleetEngine.buyMiner(stateUnlocked, lockedMiner.id, BulkMode.X1)
        assertEquals(1L, successfulBuy.miners[lockedMiner.id])
        assertEquals(BigDecimal("9965.00"), successfulBuy.usdBigDecimal) // 10,000 - configured $35
    }

    @Test
    fun extremeCountsNeverWrapOrThrowAndLifetimeCounterSaturates() {
        val miner = Miners.ALL[0]
        val extremeCost = GameNumber.calculateBulkCost(
            miner.baseCostUsd,
            miner.growthRate,
            Long.MAX_VALUE - 1L,
            10L
        )
        assertTrue("non-finite geometric costs use a finite sentinel", extremeCost > BigDecimal("1E+1000"))
        val start = System.nanoTime()
        val hugeFundsCount = GameNumber.calculateMaxAffordable(
            BigDecimal("1E+100000"), miner.baseCostUsd, miner.growthRate, 0L
        )
        assertTrue("MAX remains useful with extreme decimal balances", hugeFundsCount > 0L)
        assertTrue("MAX must stay inside the supported geometric-cost range", hugeFundsCount < Long.MAX_VALUE)
        assertTrue("extreme MAX search must stay bounded", System.nanoTime() - start < 1_000_000_000L)

        val nearCapacity = GameState(
            usd = "1E+100000",
            miners = mapOf(miner.id to (Long.MAX_VALUE - 1L))
        )
        assertEquals(nearCapacity, FleetEngine.buyMiner(nearCapacity, miner.id, BulkMode.X10))

        val saturatedStats = GameState(
            usd = "10",
            stats = StatsState(totalMinersPurchased = Long.MAX_VALUE)
        )
        val bought = FleetEngine.buyMiner(saturatedStats, miner.id, BulkMode.X1)
        assertEquals(1L, bought.miners[miner.id])
        assertEquals(Long.MAX_VALUE, bought.stats.totalMinersPurchased)
    }
}
