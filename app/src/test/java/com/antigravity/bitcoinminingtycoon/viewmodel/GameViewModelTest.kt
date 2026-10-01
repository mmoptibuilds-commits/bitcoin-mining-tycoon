package com.antigravity.bitcoinminingtycoon.viewmodel

import com.antigravity.bitcoinminingtycoon.data.FakeSaveDataSource
import com.antigravity.bitcoinminingtycoon.data.GameRepository
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.platform.SoundEffect
import com.antigravity.bitcoinminingtycoon.platform.SoundPlayer
import com.antigravity.bitcoinminingtycoon.platform.FakeClockProvider
import com.antigravity.bitcoinminingtycoon.platform.HapticSignal
import com.antigravity.bitcoinminingtycoon.platform.Haptics
import com.antigravity.bitcoinminingtycoon.model.TeachingCueIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private class RecordingHaptics : Haptics {
        val played = mutableListOf<HapticSignal>()
        override fun play(signal: HapticSignal) { played += signal }
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
    fun batteryFriendlyPreferencePersistsWithoutChangingOtherSettings() = testScope.runTest {
        val initialSettings = com.antigravity.bitcoinminingtycoon.model.SettingsState(
            soundEnabled = false,
            hapticsEnabled = false,
            reducedMotion = true
        )
        val source = FakeSaveDataSource(GameSave(settings = initialSettings))
        val repository = GameRepository(source, clock, this)
        val viewModel = GameViewModel(repository, clock)
        advanceUntilIdle()

        viewModel.onSetBatteryFriendlyAnimations(true)
        advanceUntilIdle()

        assertTrue(repository.gameState.value.batteryFriendlyAnimations)
        assertEquals(initialSettings, repository.gameState.value.settings)
        assertTrue(source.saveFlow.first().batteryFriendlyAnimations)
    }

    @Test
    fun simultaneousSettingsFieldChangesMergeAgainstLatestSavedSettings() = testScope.runTest {
        val initialSettings = com.antigravity.bitcoinminingtycoon.model.SettingsState(
            soundEnabled = true,
            hapticsEnabled = true,
            reducedMotion = false
        )
        val source = FakeSaveDataSource(GameSave(settings = initialSettings))
        val repository = GameRepository(source, clock, this)
        val viewModel = GameViewModel(repository, clock)
        advanceUntilIdle()

        viewModel.onUpdateSettings { it.copy(soundEnabled = false) }
        viewModel.onUpdateSettings { it.copy(hapticsEnabled = false) }
        advanceUntilIdle()

        val saved = source.saveFlow.first().settings
        assertFalse(saved.soundEnabled)
        assertFalse(saved.hapticsEnabled)
        assertFalse(repository.gameState.value.settings.soundEnabled)
        assertFalse(repository.gameState.value.settings.hapticsEnabled)
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
    fun autoSellThresholdCannotBeChangedBeforeControllerUnlock() = testScope.runTest {
        val source = FakeSaveDataSource()
        val repository = GameRepository(source, clock, this)
        val viewModel = GameViewModel(repository, clock)
        advanceUntilIdle()
        val originalThreshold = repository.gameState.value.autoSellThresholdUsd

        assertFalse(viewModel.onSetAutoSellThreshold(BigDecimal("65000.75")))
        advanceUntilIdle()

        assertEquals(originalThreshold, repository.gameState.value.autoSellThresholdUsd)
        assertEquals(originalThreshold, source.saveFlow.first().autoSellThresholdUsd)
    }

    @Test
    fun autoSellThresholdRejectsNegativeInputWhenControllerIsUnlocked() = testScope.runTest {
        val source = FakeSaveDataSource(
            GameSave(purchasedUpgrades = setOf("auto_sell_controller"))
        )
        val repository = GameRepository(source, clock, this)
        val viewModel = GameViewModel(repository, clock)
        advanceUntilIdle()
        val originalThreshold = repository.gameState.value.autoSellThresholdUsd

        assertFalse(viewModel.onSetAutoSellThreshold(BigDecimal("-1")))
        advanceUntilIdle()

        assertEquals(originalThreshold, repository.gameState.value.autoSellThresholdUsd)
        assertEquals(originalThreshold, source.saveFlow.first().autoSellThresholdUsd)
    }

    @Test
    fun autoSellThresholdPersistsOnlyAfterImmediateCommitSucceeds() = testScope.runTest {
        val source = FakeSaveDataSource(
            GameSave(purchasedUpgrades = setOf("auto_sell_controller"))
        )
        val repository = GameRepository(source, clock, this)
        val viewModel = GameViewModel(repository, clock)
        advanceUntilIdle()

        assertTrue(viewModel.onSetAutoSellThreshold(BigDecimal("65000.75")))
        assertEquals("65000.75", repository.gameState.value.autoSellThresholdUsd)
        assertEquals("65000.75", source.saveFlow.first().autoSellThresholdUsd)

        val committedThreshold = repository.gameState.value.autoSellThresholdUsd
        source.failWrites = true
        assertFalse(viewModel.onSetAutoSellThreshold(BigDecimal("66000")))
        assertEquals(committedThreshold, repository.gameState.value.autoSellThresholdUsd)
    }

    @Test
    fun disablingAutoSellReturnsQueuedBitcoinToWallet() = testScope.runTest {
        val pendingBtc = "0.0000000125"
        val repository = GameRepository(
            FakeSaveDataSource(
                GameSave(
                    purchasedUpgrades = setOf("auto_sell_controller"),
                    autoSellEnabled = true,
                    autoSellPendingBtc = pendingBtc
                )
            ),
            clock,
            this
        )
        val viewModel = GameViewModel(repository, clock)
        advanceUntilIdle()

        viewModel.onToggleAutoSell()
        advanceUntilIdle()

        assertFalse(repository.gameState.value.autoSellEnabled)
        assertEquals(BigDecimal(pendingBtc), repository.gameState.value.btcBigDecimal)
        assertEquals(BigDecimal.ZERO, repository.gameState.value.autoSellPendingBtcBigDecimal)
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

    @Test
    fun firstSessionTeachingCuesCommitWithTapSaleAndFirstMachineActions() = testScope.runTest {
        val tapRepository = GameRepository(FakeSaveDataSource(), clock, this)
        val tapViewModel = GameViewModel(tapRepository, clock)
        advanceUntilIdle()

        tapViewModel.onManualMineTap()
        advanceUntilIdle()
        assertTrue(TeachingCueIds.MINE_BITCOIN in tapRepository.gameState.value.completedTeachingCueIds)

        val saleRepository = GameRepository(FakeSaveDataSource(GameSave(btc = "1.00000000")), clock, this)
        val saleViewModel = GameViewModel(saleRepository, clock)
        advanceUntilIdle()
        saleViewModel.onQuickSell(100)
        advanceUntilIdle()
        assertTrue(TeachingCueIds.SELL_BITCOIN in saleRepository.gameState.value.completedTeachingCueIds)

        val miner = Miners.ALL.first()
        saleViewModel.onBuyMiner(miner.id)
        advanceUntilIdle()
        assertEquals(1L, saleRepository.gameState.value.miners[miner.id])
        assertTrue(TeachingCueIds.BUY_FIRST_MACHINE in saleRepository.gameState.value.completedTeachingCueIds)

        saleViewModel.onCompleteTeachingCue(TeachingCueIds.PASSIVE_MINING)
        advanceUntilIdle()
        assertTrue(TeachingCueIds.PASSIVE_MINING in saleRepository.gameState.value.completedTeachingCueIds)
    }

    @Test
    fun successfulPurchasePersistsTheHighestDiscoveredFacilityStage() = testScope.runTest {
        val source = FakeSaveDataSource(
            GameSave(
                usd = "2500.00",
                stats = com.antigravity.bitcoinminingtycoon.model.StatsState(lifetimeBtcMined = "0.05")
            )
        )
        val repository = GameRepository(source, clock, this)
        val viewModel = GameViewModel(repository, clock)
        advanceUntilIdle()

        viewModel.onBuyMiner("gpu_rig_6x")
        advanceUntilIdle()

        assertEquals(1L, repository.gameState.value.miners["gpu_rig_6x"])
        assertEquals(2, repository.gameState.value.highestDiscoveredFacilityStage)
        assertEquals(2, source.saveFlow.first().highestDiscoveredFacilityStage)
    }

    @Test
    fun hapticSignalsFollowActionsAndRespectTheSavedSetting() = testScope.runTest {
        val enabledSource = FakeSaveDataSource()
        val enabledRepository = GameRepository(enabledSource, clock, this)
        val enabledHaptics = RecordingHaptics()
        val enabledViewModel = GameViewModel(enabledRepository, clock, haptics = enabledHaptics)
        advanceUntilIdle()

        enabledViewModel.onManualMineTap()
        advanceUntilIdle()
        assertEquals(listOf(HapticSignal.TAP, HapticSignal.MILESTONE), enabledHaptics.played)

        val purchaseSource = FakeSaveDataSource(
            GameSave(
                usd = "10.00",
                stats = com.antigravity.bitcoinminingtycoon.model.StatsState(lifetimeBtcMined = "0.1")
            )
        )
        val purchaseRepository = GameRepository(purchaseSource, clock, this)
        val purchaseHaptics = RecordingHaptics()
        val purchaseViewModel = GameViewModel(purchaseRepository, clock, haptics = purchaseHaptics)
        advanceUntilIdle()
        purchaseViewModel.onBuyMiner("ancient_cpu")
        advanceUntilIdle()
        assertEquals(listOf(HapticSignal.PURCHASE), purchaseHaptics.played)

        val disabledSource = FakeSaveDataSource(
            GameSave(settings = com.antigravity.bitcoinminingtycoon.model.SettingsState(hapticsEnabled = false))
        )
        val disabledRepository = GameRepository(disabledSource, clock, this)
        val disabledHaptics = RecordingHaptics()
        val disabledViewModel = GameViewModel(disabledRepository, clock, haptics = disabledHaptics)
        advanceUntilIdle()

        disabledViewModel.onManualMineTap()
        advanceUntilIdle()
        assertTrue(disabledHaptics.played.isEmpty())
    }

    @Test
    fun passiveAchievementUsesMilestoneHaptic() = testScope.runTest {
        val repository = GameRepository(
            FakeSaveDataSource(GameSave(stats = com.antigravity.bitcoinminingtycoon.model.StatsState(totalManualTaps = 1L))),
            clock,
            this
        )
        val haptics = RecordingHaptics()
        val viewModel = GameViewModel(repository, clock, haptics = haptics)
        advanceUntilIdle()

        viewModel.startTicker()
        clock.advanceMonotonicNanos(100_000_000L)
        advanceTimeBy(100L)
        runCurrent()
        viewModel.stopTicker()
        advanceUntilIdle()

        assertEquals(listOf(HapticSignal.MILESTONE), haptics.played)
    }

    @Test
    fun foregroundEventExpiryUsesMonotonicTimeWhenWallClockMovesBackward() = testScope.runTest {
        val startWallMillis = clock.wallMillis()
        val initialSave = GameSave(
            miners = mapOf("ancient_cpu" to 100L),
            activeEvents = listOf(
                ActiveEventState(
                    eventId = "bull_run",
                    expiresAtWallMillis = startWallMillis + 150L,
                    multiplier = 2.0
                )
            ),
            eventTimerSeconds = 1000.0
        )
        val repository = GameRepository(FakeSaveDataSource(initialSave), clock, this)
        val viewModel = GameViewModel(repository, clock)
        advanceUntilIdle()
        val baseRate = com.antigravity.bitcoinminingtycoon.engine.EconomyEngine.calculateEffectiveHashrate(
            repository.gameState.value.copy(activeEvents = emptyList())
        )
        val expectedBtc = com.antigravity.bitcoinminingtycoon.engine.EconomyEngine
            .calculateMinedBtc(baseRate.multiply(BigDecimal("2")), 0.15)
            .add(
                com.antigravity.bitcoinminingtycoon.engine.EconomyEngine.calculateMinedBtc(baseRate, 0.05)
            )

        viewModel.startTicker()
        repeat(2) {
            clock.advanceMonotonicNanos(100_000_000L)
            clock.setWallMillis(startWallMillis - 10_000L)
            advanceTimeBy(100L)
            runCurrent()
        }
        viewModel.stopTicker()
        advanceUntilIdle()

        assertEquals(0, expectedBtc.compareTo(repository.gameState.value.btcBigDecimal))
        assertTrue(repository.gameState.value.activeEvents.isEmpty())
    }

    @Test
    fun confirmedPrestigeUsesPrestigeHaptic() = testScope.runTest {
        val repository = GameRepository(
            FakeSaveDataSource(
                GameSave(stats = com.antigravity.bitcoinminingtycoon.model.StatsState(lifetimeBtcMined = "1"))
            ),
            clock,
            this
        )
        val haptics = RecordingHaptics()
        val viewModel = GameViewModel(repository, clock, haptics = haptics)
        advanceUntilIdle()

        viewModel.onShowPrestigeSheet()
        viewModel.onConfirmPrestige()
        advanceUntilIdle()

        assertEquals(1L, repository.gameState.value.satoshiPoints)
        assertEquals(listOf(HapticSignal.PRESTIGE), haptics.played)
    }

    @Test
    fun successfulMinePublishesTheActualProducedBitcoinDelta() = testScope.runTest {
        val repository = GameRepository(FakeSaveDataSource(), clock, this)
        val viewModel = GameViewModel(repository, clock)
        advanceUntilIdle()
        val nextFeedback = async(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
            viewModel.gameplayFeedback.first()
        }

        viewModel.onManualMineTap()
        advanceUntilIdle()
        val event = nextFeedback.await()

        assertEquals(1L, event.sequence)
        assertEquals(0, BigDecimal("0.0000025").compareTo(BigDecimal(event.btcDelta)))
    }
}
