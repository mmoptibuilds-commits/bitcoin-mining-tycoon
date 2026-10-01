package com.antigravity.bitcoinminingtycoon.journey

import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.data.FakeSaveDataSource
import com.antigravity.bitcoinminingtycoon.data.GameRepository
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.model.SettingsState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import com.antigravity.bitcoinminingtycoon.platform.FakeClockProvider
import com.antigravity.bitcoinminingtycoon.platform.SoundEffect
import com.antigravity.bitcoinminingtycoon.platform.SoundPlayer
import com.antigravity.bitcoinminingtycoon.util.NumberFormatPreference
import com.antigravity.bitcoinminingtycoon.viewmodel.GameViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsAndStatsJourneyTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val clock = FakeClockProvider(wallMillis = 1_700_000_000_000L)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSettingsTogglingAndPersistence() = testScope.runTest {
        val dataSource = FakeSaveDataSource()
        val repo = GameRepository(dataSource, clock, this)
        val viewModel = GameViewModel(repo, clock)
        advanceUntilIdle()

        // Default initial settings
        val initialSettings = repo.gameState.value.settings
        assertTrue(initialSettings.soundEnabled)
        assertTrue(initialSettings.hapticsEnabled)
        assertFalse(initialSettings.reducedMotion)
        assertEquals(NumberFormatPreference.COMPACT_SUFFIX, initialSettings.numberFormat)
        assertFalse(repo.gameState.value.onboardingCompleted)

        // Complete onboarding
        viewModel.onCompleteOnboarding()
        advanceUntilIdle()
        assertTrue(repo.gameState.value.onboardingCompleted)

        // Update settings
        val updated = SettingsState(
            soundEnabled = false,
            hapticsEnabled = false,
            reducedMotion = true,
            numberFormat = NumberFormatPreference.SCIENTIFIC
        )
        viewModel.onUpdateSettings { updated }
        advanceUntilIdle()

        val persisted = repo.gameState.value.settings
        assertFalse(persisted.soundEnabled)
        assertFalse(persisted.hapticsEnabled)
        assertTrue(persisted.reducedMotion)
        assertEquals(NumberFormatPreference.SCIENTIFIC, persisted.numberFormat)
    }

    @Test
    fun testProceduralAudioTriggersAcrossGameActions() = testScope.runTest {
        val soundsPlayed = mutableListOf<SoundEffect>()
        val testSoundPlayer = object : SoundPlayer {
            override fun play(sound: SoundEffect) {
                soundsPlayed.add(sound)
            }
        }

        val initialSave = GameSave(
            btc = "10.00000000",
            usd = "5000.00",
            stats = StatsState(lifetimeBtcMined = "10.00000000")
        )
        val dataSource = FakeSaveDataSource(initialSave)
        val repo = GameRepository(dataSource, clock, this)
        val viewModel = GameViewModel(repo, clock, testSoundPlayer)
        advanceUntilIdle()

        // 1. Manual Mine Tap -> TAP
        viewModel.onManualMineTap()
        advanceUntilIdle()
        assertTrue(soundsPlayed.contains(SoundEffect.TAP))

        // 2. Buy hardware successfully -> BUY
        soundsPlayed.clear()
        viewModel.onBuyMiner(Miners.ALL[0].id)
        advanceUntilIdle()
        assertTrue(soundsPlayed.contains(SoundEffect.BUY))

        // 3. Buy expensive hardware failure -> INVALID
        soundsPlayed.clear()
        viewModel.onBuyMiner(Miners.ALL.last().id) // Costs millions
        advanceUntilIdle()
        assertTrue(soundsPlayed.contains(SoundEffect.INVALID))

        // 4. Daily Reward Claim -> DAILY_REWARD
        soundsPlayed.clear()
        viewModel.onClaimDailyReward()
        advanceUntilIdle()
        assertTrue(soundsPlayed.contains(SoundEffect.DAILY_REWARD))

        // 5. Prestige Execution -> PRESTIGE
        soundsPlayed.clear()
        viewModel.onConfirmPrestige()
        advanceUntilIdle()
        assertTrue(soundsPlayed.contains(SoundEffect.PRESTIGE))
    }

    @Test
    fun testFactoryResetCompletelyClearsFacilityData() = testScope.runTest {
        val populatedSave = GameSave(
            btc = "5.50000000",
            usd = "125000.00",
            satoshiPoints = 25L,
            purchasedPrestigeNodes = setOf("efficient_silicon", "cold_start"),
            achievements = setOf("first_hash", "first_rig"),
            miners = mapOf("usb_stick" to 10L),
            purchasedUpgrades = setOf("copper_heatpipe"),
            stats = StatsState(
                lifetimeBtcMined = "50.00000000",
                lifetimeUsdEarned = "500000.00",
                lifetimeSatoshiPointsEarned = 50L
            )
        )
        val dataSource = FakeSaveDataSource(populatedSave)
        val repo = GameRepository(dataSource, clock, this)
        val viewModel = GameViewModel(repo, clock)
        advanceUntilIdle()

        // Precondition checks
        assertEquals(0, BigDecimal("5.50000000").compareTo(repo.gameState.value.btcBigDecimal))
        assertEquals(25L, repo.gameState.value.satoshiPoints)
        assertEquals(2, repo.gameState.value.achievements.size)

        // Execute factory reset
        viewModel.onFactoryReset()
        advanceUntilIdle()

        // Post-reset verification: datacenter completely cleared
        val cleanState = repo.gameState.value
        assertEquals(0, BigDecimal.ZERO.compareTo(cleanState.btcBigDecimal))
        assertEquals(0, BigDecimal.ZERO.compareTo(cleanState.usdBigDecimal))
        assertEquals(0L, cleanState.satoshiPoints)
        assertTrue(cleanState.purchasedPrestigeNodes.isEmpty())
        assertTrue(cleanState.achievements.isEmpty())
        assertTrue(cleanState.miners.isEmpty())
        assertTrue(cleanState.purchasedUpgrades.isEmpty())
        assertEquals(0L, cleanState.stats.lifetimeSatoshiPointsEarned)
        assertEquals(0L, cleanState.stats.totalManualTaps)
    }

    @Test
    fun testTelemetryStatsTrackingConsistency() = testScope.runTest {
        val dataSource = FakeSaveDataSource()
        val repo = GameRepository(dataSource, clock, this)
        val viewModel = GameViewModel(repo, clock)
        advanceUntilIdle()

        // 10 manual taps
        repeat(10) {
            viewModel.onManualMineTap()
        }
        advanceUntilIdle()

        assertEquals(10L, repo.gameState.value.stats.totalManualTaps)
        assertTrue(repo.gameState.value.stats.lifetimeBtcBigDecimal > BigDecimal.ZERO)
    }
}
