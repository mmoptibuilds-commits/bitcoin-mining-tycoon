package com.antigravity.bitcoinminingtycoon.ui.presentation

import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class DiscoveryPresentationTest {

    @Test
    fun newSaveTeachesTheMineBitcoinSellCashMachineLoopInOrder() {
        val fresh = GameState()
        assertEquals(DiscoveryPresentation.MINE_BITCOIN, DiscoveryPresentation.nextCue(fresh)?.id)

        val mined = fresh.copy(
            btc = "0.000005",
            completedTeachingCueIds = setOf(DiscoveryPresentation.MINE_BITCOIN),
            stats = StatsState(lifetimeBtcMined = "0.000005", totalManualTaps = 1L)
        )
        assertEquals(DiscoveryPresentation.SELL_BITCOIN, DiscoveryPresentation.nextCue(mined)?.id)

        val cashReady = mined.copy(
            btc = "0",
            usd = "10.00",
            completedTeachingCueIds = mined.completedTeachingCueIds + DiscoveryPresentation.SELL_BITCOIN
        )
        assertEquals(DiscoveryPresentation.BUY_FIRST_MACHINE, DiscoveryPresentation.nextCue(cashReady)?.id)

        val firstMachine = cashReady.copy(
            usd = "0",
            miners = mapOf("ancient_cpu" to 1L),
            completedTeachingCueIds = cashReady.completedTeachingCueIds + DiscoveryPresentation.BUY_FIRST_MACHINE
        )
        assertEquals(DiscoveryPresentation.PASSIVE_MINING, DiscoveryPresentation.nextCue(firstMachine)?.id)

        val taught = firstMachine.copy(
            completedTeachingCueIds = firstMachine.completedTeachingCueIds + DiscoveryPresentation.PASSIVE_MINING
        )
        assertNull(DiscoveryPresentation.nextCue(taught))
    }

    @Test
    fun migratedPlayerWithExistingProgressDoesNotReceiveFreshPlayerTeaching() {
        val migrated = GameState(
            btc = "0.00000045",
            miners = mapOf("ancient_cpu" to 1L),
            stats = StatsState(lifetimeBtcMined = "0.00000045", totalManualTaps = 1L)
        )

        assertNull(DiscoveryPresentation.nextCue(migrated))
        assertNotNull(DiscoveryPresentation.nextGoal(migrated))
    }

    @Test
    fun migratedBalanceWithoutNewStatisticsDoesNotRestartFirstSessionTeaching() {
        val migrated = GameState(btc = "0.005", usd = "20.00")

        assertNull(DiscoveryPresentation.nextCue(migrated))
    }

    @Test
    fun sellingCueIsAvailableWithoutEnoughCashAndNamesTheFirstMachineCost() {
        val state = GameState(
            btc = "0.000001",
            stats = StatsState(lifetimeBtcMined = "0.000001", totalManualTaps = 1L),
            completedTeachingCueIds = setOf(DiscoveryPresentation.MINE_BITCOIN)
        )

        val cue = DiscoveryPresentation.nextCue(state)
        assertEquals(DiscoveryPresentation.SELL_BITCOIN, cue?.id)
        assertEquals("$10", cue?.firstMachineCostLabel)
    }
}
