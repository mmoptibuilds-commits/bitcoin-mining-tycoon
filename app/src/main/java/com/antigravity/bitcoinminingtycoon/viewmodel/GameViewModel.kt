package com.antigravity.bitcoinminingtycoon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.bitcoinminingtycoon.content.Infrastructure
import com.antigravity.bitcoinminingtycoon.data.GameRepository
import com.antigravity.bitcoinminingtycoon.engine.EconomyEngine
import com.antigravity.bitcoinminingtycoon.engine.GameEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.platform.ClockProvider
import com.antigravity.bitcoinminingtycoon.platform.NoOpSoundPlayer
import com.antigravity.bitcoinminingtycoon.platform.SoundEffect
import com.antigravity.bitcoinminingtycoon.platform.SoundPlayer
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
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
    val gameState: GameState = GameState()
)

class GameViewModel(
    val repository: GameRepository,
    val clockProvider: ClockProvider,
    val soundPlayer: SoundPlayer = NoOpSoundPlayer
) : ViewModel() {

    private var tickerJob: Job? = null
    private var lastMonotonicNanos: Long = 0L
    private val _bulkMode = MutableStateFlow(com.antigravity.bitcoinminingtycoon.engine.BulkMode.X1)
    private val _unlockedAchievement = MutableStateFlow<com.antigravity.bitcoinminingtycoon.content.AchievementDefinition?>(null)
    private val _offlineReport = MutableStateFlow<com.antigravity.bitcoinminingtycoon.engine.OfflineReport?>(null)
    private val _dailyRewardSheetVisible = MutableStateFlow(false)
    private val _prestigeSheetVisible = MutableStateFlow(false)
    val prestigeSheetVisible: StateFlow<Boolean> = _prestigeSheetVisible.asStateFlow()

    val uiState: StateFlow<GameUiState> = kotlinx.coroutines.flow.combine(
        repository.gameState,
        _bulkMode,
        _unlockedAchievement,
        _offlineReport,
        _dailyRewardSheetVisible
    ) { state, bulk, achievement, offline, dailyVisible ->
        val effectiveHashrate = EconomyEngine.calculateEffectiveHashrate(state)
        val btcPerSec = EconomyEngine.calculateMinedBtc(effectiveHashrate, 1.0)
        val powerDemand = EconomyEngine.calculatePowerDemand(state)
        val powerCapacity = Infrastructure.getPowerStage(state.powerGridTier).capacityKw
        val powerFactor = EconomyEngine.calculatePowerFactor(state)
        val (temp, thermalFactor) = EconomyEngine.calculateThermalState(state)

        val wallMillis = clockProvider.wallMillis()
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
            offlineReport = offline,
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

    init {
        viewModelScope.launch {
            repository.initialize()
            val current = repository.gameState.value
            val report = com.antigravity.bitcoinminingtycoon.engine.OfflineEngine.calculateOfflineProgress(
                state = current,
                lastSavedWallMillis = current.lastSaveWallMillis,
                currentWallMillis = clockProvider.wallMillis()
            )
            if (report.minedBtc > java.math.BigDecimal.ZERO) {
                _offlineReport.value = report
            }
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
                    val current = repository.gameState.value
                    val nextState = GameEngine.tick(
                        state = current,
                        deltaSeconds = deltaSeconds,
                        wallMillis = clockProvider.wallMillis()
                    )
                    if (nextState.achievements.size > current.achievements.size) {
                        val newId = (nextState.achievements - current.achievements).firstOrNull()
                        if (newId != null) {
                            _unlockedAchievement.value = com.antigravity.bitcoinminingtycoon.content.Achievements.getById(newId)
                            soundPlayer.play(SoundEffect.ACHIEVEMENT)
                        }
                    }
                    repository.updateInMemory(nextState)
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
            val current = repository.gameState.value
            val (nextState, _) = com.antigravity.bitcoinminingtycoon.engine.EventEngine.claimWindfall(
                state = current,
                eventId = eventId,
                wallMillis = clockProvider.wallMillis()
            )
            if (nextState != current) {
                soundPlayer.play(SoundEffect.EVENT)
                repository.saveImmediate(nextState)
            }
        }
    }

    fun onManualMineTap() {
        soundPlayer.play(SoundEffect.TAP)
        val current = repository.gameState.value
        val updated = GameEngine.performManualTap(current)
        if (updated.achievements.size > current.achievements.size) {
            val newId = (updated.achievements - current.achievements).firstOrNull()
            if (newId != null) {
                _unlockedAchievement.value = com.antigravity.bitcoinminingtycoon.content.Achievements.getById(newId)
                soundPlayer.play(SoundEffect.ACHIEVEMENT)
            }
        }
        repository.updateInMemory(updated)
    }

    fun onQuickSell(percentage: Int) {
        viewModelScope.launch {
            val current = repository.gameState.value
            val nextState = com.antigravity.bitcoinminingtycoon.engine.MarketEngine.sellBtc(current, percentage)
            if (nextState != current) {
                repository.saveImmediate(nextState)
            }
        }
    }

    fun onToggleAutoSell() {
        viewModelScope.launch {
            val current = repository.gameState.value
            val nextState = current.copy(autoSellEnabled = !current.autoSellEnabled)
            repository.saveImmediate(nextState)
        }
    }

    fun onSetAutoSellThreshold(thresholdUsd: BigDecimal) {
        viewModelScope.launch {
            val current = repository.gameState.value
            val nextState = current.copy(autoSellThresholdUsd = thresholdUsd.toPlainString())
            repository.saveImmediate(nextState)
        }
    }

    fun onSetBulkMode(mode: com.antigravity.bitcoinminingtycoon.engine.BulkMode) {
        _bulkMode.value = mode
    }

    fun onBuyMiner(minerId: String) {
        viewModelScope.launch {
            val current = repository.gameState.value
            val nextState = com.antigravity.bitcoinminingtycoon.engine.FleetEngine.buyMiner(
                state = current,
                minerId = minerId,
                bulkMode = _bulkMode.value
            )
            if (nextState != current) {
                soundPlayer.play(SoundEffect.BUY)
                repository.saveImmediate(nextState)
            } else {
                soundPlayer.play(SoundEffect.INVALID)
            }
        }
    }

    fun onBuyUpgrade(upgradeId: String) {
        viewModelScope.launch {
            val current = repository.gameState.value
            val nextState = com.antigravity.bitcoinminingtycoon.engine.UpgradeEngine.buyUpgrade(current, upgradeId)
            if (nextState != current) {
                soundPlayer.play(SoundEffect.BUY)
                repository.saveImmediate(nextState)
            } else {
                soundPlayer.play(SoundEffect.INVALID)
            }
        }
    }

    fun onUpgradePowerGrid() {
        viewModelScope.launch {
            val current = repository.gameState.value
            val nextState = com.antigravity.bitcoinminingtycoon.engine.PowerEngine.upgradePowerGrid(current)
            if (nextState != current) {
                soundPlayer.play(SoundEffect.BUY)
                repository.saveImmediate(nextState)
            } else {
                soundPlayer.play(SoundEffect.INVALID)
            }
        }
    }

    fun onUpgradeCooling() {
        viewModelScope.launch {
            val current = repository.gameState.value
            val nextState = com.antigravity.bitcoinminingtycoon.engine.ThermalEngine.upgradeCooling(current)
            if (nextState != current) {
                soundPlayer.play(SoundEffect.BUY)
                repository.saveImmediate(nextState)
            } else {
                soundPlayer.play(SoundEffect.INVALID)
            }
        }
    }

    fun onCollectOfflineReward() {
        viewModelScope.launch {
            val report = _offlineReport.value ?: return@launch
            val current = repository.gameState.value
            val nextState = com.antigravity.bitcoinminingtycoon.engine.OfflineEngine.applyOfflineReward(current, report)
            _offlineReport.value = null
            soundPlayer.play(SoundEffect.BUY)
            repository.saveImmediate(nextState)
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
            val current = repository.gameState.value
            val (nextState, reward) = com.antigravity.bitcoinminingtycoon.content.DailyRewards.claim(current, clockProvider.wallMillis())
            if (reward != null) {
                soundPlayer.play(SoundEffect.DAILY_REWARD)
                repository.saveImmediate(nextState)
            }
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
            val current = repository.gameState.value
            val resetState = com.antigravity.bitcoinminingtycoon.engine.PrestigeEngine.applyPrestige(current)
            if (resetState != current) {
                _prestigeSheetVisible.value = false
                soundPlayer.play(SoundEffect.PRESTIGE)
                repository.saveImmediate(resetState)
            }
        }
    }

    fun onBuyPrestigeNode(nodeId: String) {
        viewModelScope.launch {
            val current = repository.gameState.value
            val nextState = com.antigravity.bitcoinminingtycoon.engine.PrestigeEngine.buyPrestigeNode(current, nodeId)
            if (nextState != current) {
                soundPlayer.play(SoundEffect.BUY)
                repository.saveImmediate(nextState)
            } else {
                soundPlayer.play(SoundEffect.INVALID)
            }
        }
    }

    fun onUpdateSettings(newSettings: com.antigravity.bitcoinminingtycoon.model.SettingsState) {
        viewModelScope.launch {
            val current = repository.gameState.value
            val next = current.copy(settings = newSettings)
            repository.saveImmediate(next)
        }
    }

    fun onCompleteOnboarding() {
        viewModelScope.launch {
            val current = repository.gameState.value
            val initialUsd = if (current.usdBigDecimal == java.math.BigDecimal.ZERO) "15.00" else current.usd
            val next = current.copy(
                onboardingCompleted = true,
                usd = initialUsd
            )
            repository.saveImmediate(next)
        }
    }

    fun onFactoryReset() {
        viewModelScope.launch {
            val reset = GameState(onboardingCompleted = true)
            repository.saveImmediate(reset)
        }
    }
}

