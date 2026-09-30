package com.antigravity.bitcoinminingtycoon.adversarial

import com.antigravity.bitcoinminingtycoon.content.Events
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.content.PrestigeNodes
import com.antigravity.bitcoinminingtycoon.content.Upgrades
import com.antigravity.bitcoinminingtycoon.data.FakeSaveDataSource
import com.antigravity.bitcoinminingtycoon.data.GameRepository
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.engine.BulkMode
import com.antigravity.bitcoinminingtycoon.engine.EconomyEngine
import com.antigravity.bitcoinminingtycoon.engine.EventEngine
import com.antigravity.bitcoinminingtycoon.engine.FleetEngine
import com.antigravity.bitcoinminingtycoon.engine.GameEngine
import com.antigravity.bitcoinminingtycoon.engine.MarketEngine
import com.antigravity.bitcoinminingtycoon.engine.PrestigeEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.platform.FakeClockProvider
import com.antigravity.bitcoinminingtycoon.util.NumberFormatPreference
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class AdversarialBugHuntTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val clock = FakeClockProvider(wallMillis = 1_700_000_000_000L)

    @Test
    fun testPurchaseExactlyAtPriceBoundary() {
        val miner = Miners.ALL[0]
        val exactCost = miner.baseCostUsd
        val state = GameState().withUsd(exactCost)

        val nextState = FleetEngine.buyMiner(state, miner.id, BulkMode.X1)
        assertEquals(1L, nextState.miners[miner.id])
        assertEquals(0, BigDecimal.ZERO.compareTo(nextState.usdBigDecimal))
    }

    @Test
    fun testPurchaseJustBelowPriceBoundaryRejectsCleanly() {
        val miner = Miners.ALL[0]
        val justBelowCost = miner.baseCostUsd.subtract(BigDecimal("0.01"))
        val state = GameState().withUsd(justBelowCost)

        val nextState = FleetEngine.buyMiner(state, miner.id, BulkMode.X1)
        assertEquals(0L, nextState.miners[miner.id] ?: 0L)
        assertEquals(0, justBelowCost.compareTo(nextState.usdBigDecimal))
    }

    @Test
    fun testBulkMaxWithAbsurdBalancesDoesNotOverflow() {
        val miner = Miners.ALL[0]
        // $10^24 USD balance
        val hugeUsd = BigDecimal("1000000000000000000000000.00")
        val state = GameState().withUsd(hugeUsd)

        val (maxCount, totalCost) = FleetEngine.calculatePurchase(miner, 0L, BulkMode.MAX, hugeUsd)
        assertTrue("Bulk MAX count must be positive", maxCount > 0L)
        assertTrue("Bulk MAX cost must not exceed balance", totalCost <= hugeUsd)
        assertTrue("Bulk MAX cost must be positive", totalCost > BigDecimal.ZERO)

        val nextState = FleetEngine.buyMiner(state, miner.id, BulkMode.MAX)
        assertTrue(nextState.miners[miner.id] ?: 0L > 0L)
        assertTrue(nextState.usdBigDecimal >= BigDecimal.ZERO)
    }

    @Test
    fun testZeroBtcSaleDoesNotCrashOrYieldUsd() {
        val state = GameState().withBtc(BigDecimal.ZERO).withUsd(BigDecimal("50.00"))
        val nextState = MarketEngine.sellBtc(state, percentage = 100)

        assertEquals(0, BigDecimal.ZERO.compareTo(nextState.btcBigDecimal))
        assertEquals(0, BigDecimal("50.00").compareTo(nextState.usdBigDecimal))
    }

    @Test
    fun testFractionalBtcSalePreservesSatoshiPrecision() {
        // Exactly 1 Satoshi (0.00000001 BTC)
        val oneSat = BigDecimal("0.00000001")
        val state = GameState(marketPrice = "100000.00").withBtc(oneSat).withUsd(BigDecimal.ZERO)

        val nextState = MarketEngine.sellBtc(state, percentage = 100)
        assertEquals(0, BigDecimal.ZERO.compareTo(nextState.btcBigDecimal))
        // 0.00000001 * 100000 = $0.001 -> rounds to $0.00 in 2-decimal fiat or preserves
        assertTrue(nextState.usdBigDecimal >= BigDecimal.ZERO)
    }

    @Test
    fun testPrestigeEligibilityBoundaryConditions() {
        // Less than 1 BTC lifetime -> 0 SP
        val subOneBtcState = GameState(stats = com.antigravity.bitcoinminingtycoon.model.StatsState(lifetimeBtcMined = "0.99999999"))
        assertEquals(0L, PrestigeEngine.calculateEarnablePoints(subOneBtcState))
        assertFalse(PrestigeEngine.previewPrestige(subOneBtcState).isPrestigeAvailable)

        // Exactly 1 BTC lifetime -> 1 SP
        val exactOneBtcState = GameState(stats = com.antigravity.bitcoinminingtycoon.model.StatsState(lifetimeBtcMined = "1.00000000"))
        assertEquals(1L, PrestigeEngine.calculateEarnablePoints(exactOneBtcState))
        assertTrue(PrestigeEngine.previewPrestige(exactOneBtcState).isPrestigeAvailable)

        // 10,000 BTC lifetime -> 100 SP
        val tenThousandBtcState = GameState(stats = com.antigravity.bitcoinminingtycoon.model.StatsState(lifetimeBtcMined = "10000.00000000"))
        assertEquals(100L, PrestigeEngine.calculateEarnablePoints(tenThousandBtcState))

        // 1,000,000,000,000 BTC lifetime -> 1,000,000 SP (no overflow)
        val trillionBtcState = GameState(stats = com.antigravity.bitcoinminingtycoon.model.StatsState(lifetimeBtcMined = "1000000000000.00000000"))
        assertEquals(1_000_000L, PrestigeEngine.calculateTotalPointsFromLifetimeBtc(trillionBtcState.stats.lifetimeBtcBigDecimal))
    }

    @Test
    fun testOfflineSecondsDefensiveClamping() {
        val clock = FakeClockProvider(wallMillis = 1_000_000L)

        // Time travel backward: lastSaved is 10 minutes in the future
        val negativeDelta = clock.calculateOfflineSeconds(lastSavedWallMillis = 1_600_000L, currentWallMillis = 1_000_000L)
        assertEquals(0L, negativeDelta)

        // Time travel years into the future (100 hours)
        val hugeDelta = clock.calculateOfflineSeconds(lastSavedWallMillis = 1_000_000L, currentWallMillis = 1_000_000L + (100L * 3600 * 1000))
        // Clamped to 12 hours (43,200 seconds)
        assertEquals(43_200L, hugeDelta)
    }

    @Test
    fun testCombinedMultipliersCompoundWithoutNanOrNegative() {
        var state = GameState(
            miners = mapOf("ancient_cpu" to 10L),
            purchasedUpgrades = setOf(Upgrades.ALL[0].id),
            purchasedPrestigeNodes = setOf(PrestigeNodes.ALL[0].id)
        )
        // Add ambient bull event
        state = EventEngine.applyEvent(state, Events.BULL_RUN, clock.wallMillis())

        val effectiveHashrate = EconomyEngine.calculateEffectiveHashrate(state)
        assertTrue("Effective hashrate must be strictly positive", effectiveHashrate > BigDecimal.ZERO)
        assertFalse("Effective hashrate string cannot contain NaN", effectiveHashrate.toPlainString().contains("NaN"))

        // Run 50 ticks to verify stability
        var cur = state
        var wall = clock.wallMillis()
        repeat(50) {
            wall += 100L
            cur = GameEngine.tick(cur, deltaSeconds = 0.1, wallMillis = wall)
            assertTrue(cur.btcBigDecimal >= BigDecimal.ZERO)
            assertTrue(cur.usdBigDecimal >= BigDecimal.ZERO)
        }
    }

    @Test
    fun testLargeNumberFormattingNeverEmitsNanOrCrashes() {
        val formats = listOf(NumberFormatPreference.COMPACT_SUFFIX, NumberFormatPreference.SCIENTIFIC)

        val testValues = listOf(
            BigDecimal.ZERO,
            BigDecimal("0.00000001"),
            BigDecimal("1.50"),
            BigDecimal("999.99"),
            BigDecimal("1234567.89"),
            BigDecimal("999999999999999999.99"),
            BigDecimal("1e30"),
            BigDecimal("1e50")
        )

        for (fmt in formats) {
            for (v in testValues) {
                val btcStr = NumberFormatter.formatBtc(v, fmt)
                val usdStr = NumberFormatter.formatUsd(v, fmt)
                val hashStr = NumberFormatter.formatHashrate(v)

                assertFalse(btcStr.contains("NaN"))
                assertFalse(usdStr.contains("NaN"))
                assertFalse(hashStr.contains("NaN"))
                assertFalse(btcStr.contains("Infinity"))
                assertFalse(usdStr.contains("Infinity"))
                assertFalse(hashStr.contains("Infinity"))
            }
        }
    }

    @Test
    fun testNoDoublePurchaseOverdraftOnConcurrentCalls() = testScope.runTest {
        // Player has enough for exactly 1 Ancient CPU ($10.00)
        val miner = Miners.ALL[0]
        val cost = miner.baseCostUsd
        val initialSave = GameSave(usd = cost.toPlainString())
        val dataSource = FakeSaveDataSource(initialSave)
        val repo = GameRepository(dataSource, clock, this)
        repo.initialize()
        advanceUntilIdle()

        repo.mutateLatest(com.antigravity.bitcoinminingtycoon.data.MutationDurability.IMMEDIATE) {
            FleetEngine.buyMiner(it, miner.id, BulkMode.X1)
        }
        repo.mutateLatest(com.antigravity.bitcoinminingtycoon.data.MutationDurability.IMMEDIATE) {
            FleetEngine.buyMiner(it, miner.id, BulkMode.X1)
        }

        assertEquals(1L, repo.gameState.value.miners[miner.id])
        assertEquals(0, BigDecimal.ZERO.compareTo(repo.gameState.value.usdBigDecimal))
    }
}
