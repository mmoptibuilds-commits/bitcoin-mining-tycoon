package com.antigravity.bitcoinminingtycoon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.bitcoinminingtycoon.content.Infrastructure
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.content.FacilityStageCatalog
import com.antigravity.bitcoinminingtycoon.data.GameRepository
import com.antigravity.bitcoinminingtycoon.data.MutationDurability
import com.antigravity.bitcoinminingtycoon.data.MutationResult
import com.antigravity.bitcoinminingtycoon.data.SaveReadiness
import com.antigravity.bitcoinminingtycoon.engine.EconomyEngine
import com.antigravity.bitcoinminingtycoon.engine.MarketEngine
import com.antigravity.bitcoinminingtycoon.engine.GameEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.TeachingCueIds
import com.antigravity.bitcoinminingtycoon.platform.ClockProvider
import com.antigravity.bitcoinminingtycoon.platform.NoOpSoundPlayer
import com.antigravity.bitcoinminingtycoon.platform.HapticSignal
import com.antigravity.bitcoinminingtycoon.platform.Haptics
import com.antigravity.bitcoinminingtycoon.platform.NoOpHaptics
import com.antigravity.bitcoinminingtycoon.platform.SoundEffect
import com.antigravity.bitcoinminingtycoon.platform.SoundPlayer
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicLong

data class GameUiState(
    val btcFormatted: String = "0.00000000 BTC",
    val usdFormatted: String = "$ 0.00",
    val hashrateFormatted: String = "0.00 H/s",
    val btcPerSecFormatted: String = "+0.00000000 BTC/s",
    val powerDemandKw: Double = 0.0,
    val powerCapacityKw: Double = 0.5,
    val powerFactor: Double = 1.0,
    val equilibriumTemp: Double = 25.0,
    val thermalFactor: Double = 1.0,
    val activeEventTitle: String? = null,
    val activeEventSecondsRemaining: Long = 0L,
    val activeAmbientEvent: com.antigravity.bitcoinminingtycoon.model.ActiveEventState? = null,
    val activeWindfallEvent: com.antigravity.bitcoinminingtycoon.model.ActiveEventState? = null,
    val unlockedAchievement: com.antigravity.bitcoinminingtycoon.content.AchievementDefinition? = null,
    val offlineReport: com.antigravity.bitcoinminingtycoon.engine.OfflineReport? = null,
    val dailyRewardSheetVisible: Boolean = false,
    val canClaimDailyReward: Boolean = false,
    val dailyRewardCooldownMillis: Long = 0L,
    val prestigePreview: com.antigravity.bitcoinminingtycoon.engine.PrestigePreview? = null,
    val currentWallMillis: Long = 0L,
    val bulkMode: com.antigravity.bitcoinminingtycoon.engine.BulkMode = com.antigravity.bitcoinminingtycoon.engine.BulkMode.X1,
    val saveReadiness: SaveReadiness = SaveReadiness.Loading,
    val gameState: GameState = GameState()
)

class GameViewModel(
    val repository: GameRepository,
    val clockProvider: ClockProvider,
    val soundPlayer: SoundPlayer = NoOpSoundPlayer,
    private val haptics: Haptics = NoOpHaptics
) : ViewModel() {

    private val feedbackBus = GameplayFeedbackBus()
    private val feedbackSequence = AtomicLong(0L)
    val gameplayFeedback = feedbackBus.events

    private var tickerJob: Job? = null
    private var lastMonotonicNanos: Long = 0L
    private var logicalGameplayWallMillis: Long? = null
    private var logicalGameplayWallMonotonicNanos: Long = 0L
    private val _bulkMode = MutableStateFlow(com.antigravity.bitcoinminingtycoon.engine.BulkMode.X1)
    private val _unlockedAchievement = MutableStateFlow<com.antigravity.bitcoinminingtycoon.content.AchievementDefinition?>(null)
    private val _dailyRewardSheetVisible = MutableStateFlow(false)
    private val _prestigeSheetVisible = MutableStateFlow(false)
    val prestigeSheetVisible: StateFlow<Boolean> = _prestigeSheetVisible.asStateFlow()

    private val gameUiState: StateFlow<GameUiState> = kotlinx.coroutines.flow.combine(
        repository.gameState,
        _bulkMode,
        _unlockedAchievement,
        _dailyRewardSheetVisible
    ) { state, bulk, achievement, dailyVisible ->
        val effectiveHashrate = EconomyEngine.calculateEffectiveHashrate(state)
        val btcPerSec = EconomyEngine.calculateMinedBtc(effectiveHashrate, 1.0)
        val powerDemand = EconomyEngine.calculatePowerDemand(state)
        val powerCapacity = Infrastructure.getPowerStage(state.powerGridTier).capacityKw
        val powerFactor = EconomyEngine.calculatePowerFactor(state)
        val (temp, thermalFactor) = EconomyEngine.calculateThermalState(state)

        val wallMillis = gameplayWallMillis()
        val ambientEvent = state.activeEvents.firstOrNull { !it.isWindfall && it.expiresAtWallMillis > wallMillis }
        val windfallEvent = state.activeEvents.firstOrNull { it.isWindfall && it.expiresAtWallMillis > wallMillis }
        val eventSecRemaining = if (ambientEvent != null) {
            maxOf(0L, (ambientEvent.expiresAtWallMillis - wallMillis) / 1000L)
        } else 0L

        val canClaimDaily = com.antigravity.bitcoinminingtycoon.content.DailyRewards.canClaim(state, wallMillis)
        val cooldownRemaining = com.antigravity.bitcoinminingtycoon.content.DailyRewards.millisUntilNextClaim(state, wallMillis)

        GameUiState(
            btcFormatted = NumberFormatter.formatBtc(state.btcBigDecimal, state.settings.numberFormat),
            usdFormatted = NumberFormatter.formatUsd(state.usdBigDecimal, state.settings.numberFormat),
            hashrateFormatted = NumberFormatter.formatHashrate(effectiveHashrate),
            btcPerSecFormatted = "+${NumberFormatter.formatBtc(btcPerSec, state.settings.numberFormat)}/s",
            powerDemandKw = powerDemand,
            powerCapacityKw = powerCapacity,
            powerFactor = powerFactor,
            equilibriumTemp = temp,
            thermalFactor = thermalFactor,
            activeEventTitle = ambientEvent?.eventId?.replace('_', ' ')?.uppercase(),
            activeEventSecondsRemaining = eventSecRemaining,
            activeAmbientEvent = ambientEvent,
            activeWindfallEvent = windfallEvent,
            unlockedAchievement = achievement,
            offlineReport = state.pendingOfflineSummary?.let {
                com.antigravity.bitcoinminingtycoon.engine.OfflineReport(
                    durationSeconds = it.durationSeconds,
                    effectiveHashrate = it.averageEffectiveHashrate?.let(::BigDecimal),
                    minedBtc = BigDecimal(it.creditedBtc),
                    creditedThroughWallMillis = it.creditedAtWallMillis
                )
            },
            dailyRewardSheetVisible = dailyVisible,
            canClaimDailyReward = canClaimDaily,
            dailyRewardCooldownMillis = cooldownRemaining,
            prestigePreview = com.antigravity.bitcoinminingtycoon.engine.PrestigeEngine.previewPrestige(state),
            currentWallMillis = wallMillis,
            bulkMode = bulk,
            gameState = state
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = GameUiState()
    )

    val uiState: StateFlow<GameUiState> = kotlinx.coroutines.flow.combine(gameUiState, repository.saveReadiness) { state, readiness ->
        state.copy(saveReadiness = readiness)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GameUiState())

    init {
        viewModelScope.launch {
            repository.initialize()
        }
    }

    fun startTicker() {
        if (tickerJob?.isActive == true) return

        lastMonotonicNanos = clockProvider.monotonicNanos()

        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(100L) // 10Hz logical game tick
                val now = clockProvider.monotonicNanos()
                val deltaNanos = now - lastMonotonicNanos
                lastMonotonicNanos = now

                if (deltaNanos > 0L) {
                    val deltaSeconds = deltaNanos.toDouble() / 1_000_000_000.0
                    val tickWallMillis = gameplayWallMillis()
                    var unlocked: com.antigravity.bitcoinminingtycoon.content.AchievementDefinition? = null
                    val result = repository.mutateLatest(MutationDurability.COALESCED) { current ->
                        GameEngine.tick(current, deltaSeconds, tickWallMillis).let { ticked ->
                            ticked.copy(lastSaveWallMillis = maxOf(current.lastSaveWallMillis, tickWallMillis))
                        }.also { next ->
                            val id = (next.achievements - current.achievements).firstOrNull()
                            if (id != null) unlocked = com.antigravity.bitcoinminingtycoon.content.Achievements.getById(id)
                        }
                    }
                    if (result is MutationResult.Applied && unlocked != null) {
                        _unlockedAchievement.value = unlocked
                        playHaptic(result.state, HapticSignal.MILESTONE)
                        soundPlayer.play(SoundEffect.ACHIEVEMENT)
                    }
                }
            }
        }
    }

    fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun onDismissAchievement() {
        _unlockedAchievement.value = null
    }

    fun onClaimWindfall(eventId: String) {
        viewModelScope.launch {
            val result = repository.mutateLatest(MutationDurability.IMMEDIATE) { current ->
                com.antigravity.bitcoinminingtycoon.engine.EventEngine.claimWindfall(current, eventId, gameplayWallMillis()).first
            }
            playMutationFeedback(result, SoundEffect.EVENT, SoundEffect.INVALID)
        }
    }

    fun onManualMineTap() {
        viewModelScope.launch {
            var unlocked: com.antigravity.bitcoinminingtycoon.content.AchievementDefinition? = null
            var btcDelta = BigDecimal.ZERO
            val result = repository.mutateLatest(MutationDurability.COALESCED) { current ->
                GameEngine.performManualTap(current).copy(
                    completedTeachingCueIds = current.completedTeachingCueIds + TeachingCueIds.MINE_BITCOIN
                ).also { next ->
                    btcDelta = next.btcBigDecimal.subtract(current.btcBigDecimal)
                    val id = (next.achievements - current.achievements).firstOrNull()
                    if (id != null) unlocked = com.antigravity.bitcoinminingtycoon.content.Achievements.getById(id)
                }
            }
            if (result is MutationResult.Applied && result.changed) {
                feedbackBus.publish(
                    MiningFeedbackEvent(
                        sequence = feedbackSequence.incrementAndGet(),
                        btcDelta = btcDelta.toPlainString()
                    )
                )
                playHaptic(result.state, HapticSignal.TAP)
                soundPlayer.play(SoundEffect.TAP)
                if (unlocked != null) {
                    _unlockedAchievement.value = unlocked
                    playHaptic(result.state, HapticSignal.MILESTONE)
                    soundPlayer.play(SoundEffect.ACHIEVEMENT)
                }
            }
        }
    }

    fun onQuickSell(percentage: Int) {
        viewModelScope.launch {
            val result = repository.mutateLatest(MutationDurability.IMMEDIATE) {
                val sold = MarketEngine.sellBtc(it, percentage)
                if (sold.stats.totalBtcSoldBigDecimal > it.stats.totalBtcSoldBigDecimal &&
                    sold.usdBigDecimal >= Miners.ALL.first().baseCostUsd) {
                    sold.copy(completedTeachingCueIds = sold.completedTeachingCueIds + TeachingCueIds.SELL_BITCOIN)
                } else sold
            }
            playMutationFeedback(result, success = null, invalid = null)
        }
    }

    fun onToggleAutoSell() {
        viewModelScope.launch {
            val result = repository.mutateLatest(MutationDurability.IMMEDIATE) {
                if (com.antigravity.bitcoinminingtycoon.engine.UpgradeEngine.canEnableAutoSell(it)) {
                    if (it.autoSellEnabled) {
                        val returnedBtc = GameNumber.add(it.btcBigDecimal, it.autoSellPendingBtcBigDecimal)
                        it.copy(
                            autoSellEnabled = false,
                            btc = returnedBtc.toPlainString(),
                            autoSellPendingBtc = "0"
                        )
                    } else {
                        it.copy(autoSellEnabled = true)
                    }
                } else it
            }
            playMutationFeedback(result, success = null, invalid = SoundEffect.INVALID)
        }
    }

    suspend fun onSetAutoSellThreshold(thresholdUsd: BigDecimal): Boolean {
        if (thresholdUsd.signum() < 0 ||
            thresholdUsd.precision() > 34 ||
            thresholdUsd.scale() !in -10_000..10_000
        ) return false

        var acceptedByCurrentState = false
        val result = repository.mutateLatest(MutationDurability.IMMEDIATE) { state ->
            if (com.antigravity.bitcoinminingtycoon.engine.UpgradeEngine.canEnableAutoSell(state)) {
                acceptedByCurrentState = true
                state.copy(autoSellThresholdUsd = thresholdUsd.toPlainString())
            } else {
                state
            }
        }
        return acceptedByCurrentState && result is MutationResult.Applied
    }

    fun onSetBulkMode(mode: com.antigravity.bitcoinminingtycoon.engine.BulkMode) {
        _bulkMode.value = mode
    }

    fun onBuyMiner(minerId: String) {
        viewModelScope.launch {
            val result = repository.mutateLatest(MutationDurability.IMMEDIATE) {
                val bought = com.antigravity.bitcoinminingtycoon.engine.FleetEngine.buyMiner(it, minerId, _bulkMode.value)
                if (bought == it) return@mutateLatest it

                val discoveredStage = FacilityStageCatalog.stageIndexForMiner(minerId)
                bought.copy(
                    completedTeachingCueIds = if (it.miners.values.none { count -> count > 0L }) {
                        bought.completedTeachingCueIds + TeachingCueIds.BUY_FIRST_MACHINE
                    } else bought.completedTeachingCueIds,
                    highestDiscoveredFacilityStage = discoveredStage?.let {
                        maxOf(bought.highestDiscoveredFacilityStage, it)
                    } ?: bought.highestDiscoveredFacilityStage
                )
            }
            playMutationFeedback(result, SoundEffect.BUY, SoundEffect.INVALID)
        }
    }

    fun onBuyUpgrade(upgradeId: String) {
        viewModelScope.launch {
            val result = repository.mutateLatest(MutationDurability.IMMEDIATE) {
                com.antigravity.bitcoinminingtycoon.engine.UpgradeEngine.buyUpgrade(it, upgradeId)
            }
            playMutationFeedback(result, SoundEffect.BUY, SoundEffect.INVALID)
        }
    }

    fun onUpgradePowerGrid() {
        viewModelScope.launch {
            val result = repository.mutateLatest(MutationDurability.IMMEDIATE) {
                com.antigravity.bitcoinminingtycoon.engine.PowerEngine.upgradePowerGrid(it)
            }
            playMutationFeedback(result, SoundEffect.BUY, SoundEffect.INVALID)
        }
    }

    fun onUpgradeCooling() {
        viewModelScope.launch {
            val result = repository.mutateLatest(MutationDurability.IMMEDIATE) {
                com.antigravity.bitcoinminingtycoon.engine.ThermalEngine.upgradeCooling(it)
            }
            playMutationFeedback(result, SoundEffect.BUY, SoundEffect.INVALID)
        }
    }

    fun onCollectOfflineReward() {
        viewModelScope.launch {
            repository.mutateLatest(MutationDurability.IMMEDIATE) { current ->
                if (current.pendingOfflineSummary == null) current else current.copy(pendingOfflineSummary = null)
            }
        }
    }

    fun onShowDailyRewardSheet() {
        _dailyRewardSheetVisible.value = true
    }

    fun onDismissDailyRewardSheet() {
        _dailyRewardSheetVisible.value = false
    }

    fun onClaimDailyReward() {
        viewModelScope.launch {
            var claimed = false
            val result = repository.mutateLatest(MutationDurability.IMMEDIATE) { current ->
                com.antigravity.bitcoinminingtycoon.content.DailyRewards.claim(current, gameplayWallMillis()).let { (next, reward) ->
                    claimed = reward != null
                    next
                }
            }
            if (claimed) playMutationFeedback(result, SoundEffect.DAILY_REWARD, invalid = null)
        }
    }

    fun onShowPrestigeSheet() {
        _prestigeSheetVisible.value = true
    }

    fun onDismissPrestigeSheet() {
        _prestigeSheetVisible.value = false
    }

    fun onConfirmPrestige() {
        viewModelScope.launch {
            val result = repository.mutateLatest(MutationDurability.IMMEDIATE) {
                com.antigravity.bitcoinminingtycoon.engine.PrestigeEngine.applyPrestige(it)
            }
            if (result is MutationResult.Applied && result.changed) {
                _prestigeSheetVisible.value = false
                playHaptic(result.state, HapticSignal.PRESTIGE)
                soundPlayer.play(SoundEffect.PRESTIGE)
            }
        }
    }

    fun onBuyPrestigeNode(nodeId: String) {
        viewModelScope.launch {
            val result = repository.mutateLatest(MutationDurability.IMMEDIATE) {
                com.antigravity.bitcoinminingtycoon.engine.PrestigeEngine.buyPrestigeNode(it, nodeId)
            }
            playMutationFeedback(result, SoundEffect.BUY, SoundEffect.INVALID)
        }
    }

    fun onUpdateSettings(update: (com.antigravity.bitcoinminingtycoon.model.SettingsState) -> com.antigravity.bitcoinminingtycoon.model.SettingsState) {
        viewModelScope.launch {
            repository.mutateLatest(MutationDurability.IMMEDIATE) { latest ->
                latest.copy(settings = update(latest.settings))
            }
        }
    }

    fun onSetBatteryFriendlyAnimations(enabled: Boolean) {
        viewModelScope.launch {
            repository.mutateLatest(MutationDurability.IMMEDIATE) {
                it.copy(batteryFriendlyAnimations = enabled)
            }
        }
    }

    fun onCompleteOnboarding() {
        viewModelScope.launch {
            repository.mutateLatest(MutationDurability.IMMEDIATE) { it.copy(onboardingCompleted = true) }
        }
    }

    fun onCompleteTeachingCue(cueId: String) {
        if (cueId !in TeachingCueIds.ALL) return
        viewModelScope.launch {
            repository.mutateLatest(MutationDurability.COALESCED) {
                it.copy(completedTeachingCueIds = it.completedTeachingCueIds + cueId)
            }
        }
    }

    fun onFactoryReset() {
        viewModelScope.launch {
            repository.mutateLatest(MutationDurability.IMMEDIATE) {
                GameState(onboardingCompleted = false, lastSaveWallMillis = it.lastSaveWallMillis)
            }
        }
    }

    fun onStartNewSaveFromCheckpoint() {
        viewModelScope.launch { repository.startNewSaveFromCheckpoint() }
    }

    fun onRetrySaveLoading() {
        viewModelScope.launch { repository.initialize() }
    }

    /** Foreground gameplay time follows the monotonic clock and accepts wall-clock jumps forward only. */
    @Synchronized
    private fun gameplayWallMillis(): Long {
        val monotonicNow = clockProvider.monotonicNanos()
        val persistedFloor = repository.gameState.value.lastSaveWallMillis.coerceAtLeast(0L)
        val observedWall = clockProvider.wallMillis().coerceAtLeast(0L)
        val previous = logicalGameplayWallMillis
        val logicalNow = if (previous == null) {
            maxOf(persistedFloor, observedWall)
        } else {
            val elapsedNanos = (monotonicNow - logicalGameplayWallMonotonicNanos).coerceAtLeast(0L)
            val elapsedMillis = elapsedNanos / NANOS_PER_MILLISECOND
            val monotonicWall = if (Long.MAX_VALUE - previous < elapsedMillis) Long.MAX_VALUE else previous + elapsedMillis
            maxOf(previous, monotonicWall, persistedFloor, observedWall)
        }
        logicalGameplayWallMillis = logicalNow
        logicalGameplayWallMonotonicNanos = monotonicNow
        return logicalNow
    }

    private fun playMutationFeedback(result: MutationResult, success: SoundEffect?, invalid: SoundEffect?) {
        when (result) {
            is MutationResult.Applied -> if (result.changed && success != null) {
                soundPlayer.play(success)
                hapticFor(success)?.let { playHaptic(result.state, it) }
            } else if (!result.changed && invalid != null) {
                soundPlayer.play(invalid)
                hapticFor(invalid)?.let { playHaptic(result.state, it) }
            }
            is MutationResult.Blocked, is MutationResult.PersistenceFailed -> Unit
        }
    }

    private fun playHaptic(state: GameState, signal: HapticSignal) {
        if (state.settings.hapticsEnabled) haptics.play(signal)
    }

    private fun hapticFor(sound: SoundEffect): HapticSignal? = when (sound) {
        SoundEffect.TAP -> HapticSignal.TAP
        SoundEffect.BUY -> HapticSignal.PURCHASE
        SoundEffect.INVALID -> HapticSignal.INVALID
        SoundEffect.ACHIEVEMENT, SoundEffect.EVENT, SoundEffect.DAILY_REWARD -> HapticSignal.MILESTONE
        SoundEffect.PRESTIGE -> HapticSignal.PRESTIGE
    }

    private companion object {
        const val NANOS_PER_MILLISECOND = 1_000_000L
    }
}
