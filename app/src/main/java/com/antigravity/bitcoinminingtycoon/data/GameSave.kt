package com.antigravity.bitcoinminingtycoon.data

import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.MarketTrend
import com.antigravity.bitcoinminingtycoon.model.PendingOfflineSummary
import com.antigravity.bitcoinminingtycoon.model.PowerEnergySample
import com.antigravity.bitcoinminingtycoon.model.SettingsState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Persisted save file data transfer object.
 * Every schema version increment is accompanied by a migration in [SaveMigrations].
 */
@Serializable
data class GameSave(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val btc: String = "0",
    val usd: String = "0",
    val manualHashStrength: String = "50000",
    val miners: Map<String, Long> = emptyMap(),
    val purchasedUpgrades: Set<String> = emptySet(),
    val powerGridTier: Int = 1,
    val coolingTier: Int = 1,
    val marketPrice: String = "50000",
    val marketTrend: MarketTrend = MarketTrend.NEUTRAL,
    val marketHistory: List<String> = listOf("50000"),
    val marketTimerSeconds: Double = 0.0,
    val eventTimerSeconds: Double = 120.0,
    val autoSellEnabled: Boolean = false,
    val autoSellThresholdUsd: String = "60000",
    val activeEvents: List<ActiveEventState> = emptyList(),
    val achievements: Set<String> = emptySet(),
    val satoshiPoints: Long = 0L,
    val purchasedPrestigeNodes: Set<String> = emptySet(),
    val dailyRewardDay: Int = 1,
    val lastDailyClaimWallMillis: Long = 0L,
    val stats: StatsState = StatsState(),
    val onboardingCompleted: Boolean = false,
    val settings: SettingsState = SettingsState(),
    val lastSaveWallMillis: Long = 0L,
    val rngSeed: Long = 1337L,
    val balanceRulesVersion: Int = 1,
    val completedTeachingCueIds: Set<String> = emptySet(),
    val highestDiscoveredFacilityStage: Int = 0,
    val batteryFriendlyAnimations: Boolean = false,
    val pendingOfflineSummary: PendingOfflineSummary? = null,
    /** In-memory marker that makes DataStore commit serializer migrations on startup. */
    @Transient val requiresSchemaCommit: Boolean = false
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 2

        fun fromGameState(state: GameState, currentWallMillis: Long): GameSave =
            GameSave(
                schemaVersion = CURRENT_SCHEMA_VERSION,
                btc = state.btc,
                usd = state.usd,
                manualHashStrength = state.manualHashStrength,
                miners = state.miners,
                purchasedUpgrades = state.purchasedUpgrades,
                powerGridTier = state.powerGridTier,
                coolingTier = state.coolingTier,
                marketPrice = state.marketPrice,
                marketTrend = state.marketTrend,
                marketHistory = state.marketHistory,
                marketTimerSeconds = state.marketTimerSeconds,
                eventTimerSeconds = state.eventTimerSeconds,
                autoSellEnabled = state.autoSellEnabled,
                autoSellThresholdUsd = state.autoSellThresholdUsd,
                activeEvents = state.activeEvents,
                achievements = state.achievements,
                satoshiPoints = state.satoshiPoints,
                purchasedPrestigeNodes = state.purchasedPrestigeNodes,
                dailyRewardDay = state.dailyRewardDay,
                lastDailyClaimWallMillis = state.lastDailyClaimWallMillis,
                stats = state.stats,
                onboardingCompleted = state.onboardingCompleted,
                settings = state.settings,
                lastSaveWallMillis = currentWallMillis,
                rngSeed = state.rngSeed,
                balanceRulesVersion = state.balanceRulesVersion,
                completedTeachingCueIds = state.completedTeachingCueIds,
                highestDiscoveredFacilityStage = state.highestDiscoveredFacilityStage,
                batteryFriendlyAnimations = state.batteryFriendlyAnimations,
                pendingOfflineSummary = state.pendingOfflineSummary
            )
    }

    fun toGameState(): GameState =
        GameState(
            schemaVersion = schemaVersion,
            btc = btc,
            usd = usd,
            manualHashStrength = manualHashStrength,
            miners = miners,
            purchasedUpgrades = purchasedUpgrades,
            powerGridTier = powerGridTier,
            coolingTier = coolingTier,
            marketPrice = marketPrice,
            marketTrend = marketTrend,
            marketHistory = marketHistory,
            marketTimerSeconds = marketTimerSeconds,
            eventTimerSeconds = eventTimerSeconds,
            autoSellEnabled = autoSellEnabled,
            autoSellThresholdUsd = autoSellThresholdUsd,
            activeEvents = activeEvents,
            achievements = achievements,
            satoshiPoints = satoshiPoints,
            purchasedPrestigeNodes = purchasedPrestigeNodes,
            dailyRewardDay = dailyRewardDay,
            lastDailyClaimWallMillis = lastDailyClaimWallMillis,
            stats = stats,
            onboardingCompleted = onboardingCompleted,
            settings = settings,
            lastSaveWallMillis = lastSaveWallMillis,
            rngSeed = rngSeed,
            balanceRulesVersion = balanceRulesVersion,
            completedTeachingCueIds = completedTeachingCueIds,
            highestDiscoveredFacilityStage = highestDiscoveredFacilityStage,
            batteryFriendlyAnimations = batteryFriendlyAnimations,
            pendingOfflineSummary = pendingOfflineSummary
        )
}
