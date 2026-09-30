package com.antigravity.bitcoinminingtycoon.viewmodel

import com.antigravity.bitcoinminingtycoon.data.FakeSaveDataSource
import com.antigravity.bitcoinminingtycoon.data.GameRepository
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.platform.SoundEffect
import com.antigravity.bitcoinminingtycoon.platform.SoundPlayer
import com.antigravity.bitcoinminingtycoon.platform.FakeClockProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private class RecordingSoundPlayer : SoundPlayer {
        val played = mutableListOf<SoundEffect>()
        override fun play(sound: SoundEffect) { played += sound }
    }

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val clock = FakeClockProvider()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onManualMineTap_incrementsBtcBalance() = testScope.runTest {
        val dataSource = FakeSaveDataSource()
        val repo = GameRepository(dataSource, clock, this)
        val viewModel = GameViewModel(repo, clock)
        advanceUntilIdle()

        viewModel.onManualMineTap()
        advanceUntilIdle()

        assertTrue(repo.gameState.value.btcBigDecimal > BigDecimal.ZERO)
        assertEquals(1L, repo.gameState.value.stats.totalManualTaps)
    }

    @Test
    fun onQuickSell_convertsExactPercentageToUsd() = testScope.runTest {
        val initialSave = GameSave(
            btc = "1.00000000",
            usd = "0.00",
            marketPrice = "50000.00"
        )
        val dataSource = FakeSaveDataSource(initialSave)
        val repo = GameRepository(dataSource, clock, this)
        val viewModel = GameViewModel(repo, clock)
        advanceUntilIdle()

        // Sell 50%
        viewModel.onQuickSell(50)
        advanceUntilIdle()

        val after50 = repo.gameState.value
        assertEquals(0, BigDecimal("0.50000000").compareTo(after50.btcBigDecimal))
        assertEquals(0, BigDecimal("25000.00").compareTo(after50.usdBigDecimal))

        // Sell remaining 100% (MAX)
        viewModel.onQuickSell(100)
        advanceUntilIdle()

        val afterMax = repo.gameState.value
        assertEquals(0, BigDecimal.ZERO.compareTo(afterMax.btcBigDecimal))
        assertEquals(0, BigDecimal("50000.00").compareTo(afterMax.usdBigDecimal))
    }

    @Test
    fun concurrentDailyRewardClaimsAwardTheCumulativePointOnce() = testScope.runTest {
        val source = FakeSaveDataSource(
            GameSave(
                dailyRewardDay = 7,
                miners = mapOf(Miners.ALL.first().id to 1L)
            )
        )
        val repo = GameRepository(source, clock, this)
        val sound = RecordingSoundPlayer()
        val viewModel = GameViewModel(repo, clock, sound)
        advanceUntilIdle()

        viewModel.onClaimDailyReward()
        viewModel.onClaimDailyReward()
        advanceUntilIdle()

        assertEquals(1L, repo.gameState.value.satoshiPoints)
        assertEquals(1L, repo.gameState.value.stats.dailyPointsEarnedSinceV2)
        assertEquals(1, repo.gameState.value.dailyRewardDay)
        assertEquals(1, sound.played.count { it == SoundEffect.DAILY_REWARD })
    }

    @Test
    fun failedPurchaseCommitDoesNotPublishOrPlaySuccessSound() = testScope.runTest {
        val miner = Miners.ALL.first()
        val source = FakeSaveDataSource(GameSave(usd = miner.baseCostUsd.toPlainString()))
        val repo = GameRepository(source, clock, this)
        val sound = RecordingSoundPlayer()
        val viewModel = GameViewModel(repo, clock, sound)
        advanceUntilIdle()
        val before = repo.gameState.value
        source.failWrites = true

        viewModel.onBuyMiner(miner.id)
        advanceUntilIdle()

        assertEquals(before, repo.gameState.value)
        assertEquals(0, sound.played.count { it == SoundEffect.BUY })
    }
}
