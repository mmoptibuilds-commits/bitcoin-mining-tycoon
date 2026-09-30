package com.antigravity.bitcoinminingtycoon.model

import com.antigravity.bitcoinminingtycoon.content.BalanceConfig
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import com.antigravity.bitcoinminingtycoon.util.NumberFormatPreference
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
enum class MarketTrend {
    NEUTRAL,
    BULL,
    BEAR,
    VOLATILE,
    CRASH,
    PUMP
}

@Serializable
data class ActiveEventState(
    val eventId: String,
    val expiresAtWallMillis: Long,
    val multiplier: Double = 1.0,
    val powerModifier: Double = 1.0,
    val heatModifier: Double = 1.0,
    val isWindfall: Boolean = false
)

@Serializable
data class PowerEnergySample(
    val elapsedSeconds: Long,
    val demandKw: String,
    val capacityKw: String,
    val energyKwh: String
)

@Serializable
data class PendingOfflineSummary(
    val durationSeconds: Double,
    val creditedBtc: String,
    val creditedAtWallMillis: Long
)

@Serializable
data class StatsState(
    val lifetimeBtcMined: String = "0",
    val lifetimeUsdEarned: String = "0",
    val peakHashrate: String = "0",
    val totalManualTaps: Long = 0L,
    val totalMinersPurchased: Long = 0L,
    val totalUpgradesPurchased: Long = 0L,
    val totalBtcSold: String = "0",
    val highestPriceObserved: String = BalanceConfig.MARKET_INITIAL_STATS_USD,
    val lowestPriceObserved: String = BalanceConfig.MARKET_INITIAL_STATS_USD,
    val totalPrestiges: Long = 0L,
    val lifetimeSatoshiPointsEarned: Long = 0L,
    val totalPlaytimeSeconds: Long = 0L,
    val totalEventsTriggered: Long = 0L,
    val manualBtc: String = "0",
    val foregroundPassiveBtc: String = "0",
    val offlineBtc: String = "0",
    val dailyRewardBtc: String = "0",
    val windfallBtc: String = "0",
    val achievementRewardBtc: String = "0",
    val playtimeFractionalSeconds: String = "0",
    val powerEnergyHistory: List<PowerEnergySample> = emptyList(),
    val peakTemperatureC: Double = 25.0,
    val prestigePointsBaselineV2: Long = 0L,
    val prestigePointsEarnedSinceV2: Long = 0L,
    val dailyPointsEarnedSinceV2: Long = 0L
) {
    val lifetimeBtcBigDecimal: BigDecimal get() = GameNumber.fromString(lifetimeBtcMined)
    val lifetimeUsdBigDecimal: BigDecimal get() = GameNumber.fromString(lifetimeUsdEarned)
    val peakHashrateBigDecimal: BigDecimal get() = GameNumber.fromString(peakHashrate)
    val totalBtcSoldBigDecimal: BigDecimal get() = GameNumber.fromString(totalBtcSold)
    val highestPriceBigDecimal: BigDecimal get() = GameNumber.fromString(highestPriceObserved)
    val lowestPriceBigDecimal: BigDecimal get() = GameNumber.fromString(lowestPriceObserved)
}

@Serializable
data class SettingsState(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val numberFormat: NumberFormatPreference = NumberFormatPreference.COMPACT_SUFFIX
)

/**
 * Immutable single source of truth for all Bitcoin Mining Tycoon gameplay state.
 * Serialization uses deterministic string representations for compounding BigDecimals.
 */
@Serializable
data class GameState(
    val schemaVersion: Int = 2,
    val btc: String = "0",
    val usd: String = "0",
    val manualHashStrength: String = BalanceConfig.INITIAL_MANUAL_HASHRATE,
    val miners: Map<String, Long> = emptyMap(),
    val purchasedUpgrades: Set<String> = emptySet(),
    val powerGridTier: Int = 1,
    val coolingTier: Int = 1,
    val marketPrice: String = BalanceConfig.MARKET_INITIAL_USD,
    val marketTrend: MarketTrend = MarketTrend.NEUTRAL,
    val marketHistory: List<String> = listOf(BalanceConfig.MARKET_HISTORY_INITIAL_USD),
    val marketTimerSeconds: Double = BalanceConfig.MARKET_INITIAL_TIMER_SECONDS,
    val eventTimerSeconds: Double = BalanceConfig.EVENT_INITIAL_TIMER_SECONDS,
    val autoSellEnabled: Boolean = false,
    val autoSellThresholdUsd: String = BalanceConfig.AUTO_SELL_INITIAL_THRESHOLD_USD,
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
    val balanceRulesVersion: Int = BalanceConfig.BALANCE_RULES_VERSION,
    val completedTeachingCueIds: Set<String> = emptySet(),
    val highestDiscoveredFacilityStage: Int = 0,
    val batteryFriendlyAnimations: Boolean = false,
    val pendingOfflineSummary: PendingOfflineSummary? = null
) {
    val btcBigDecimal: BigDecimal get() = GameNumber.fromString(btc)
    val usdBigDecimal: BigDecimal get() = GameNumber.fromString(usd)
    val manualHashBigDecimal: BigDecimal get() = GameNumber.fromString(manualHashStrength)
    val marketPriceBigDecimal: BigDecimal get() = GameNumber.fromString(marketPrice)
    val autoSellThresholdBigDecimal: BigDecimal get() = GameNumber.fromString(autoSellThresholdUsd)

    fun withBtc(newBtc: BigDecimal): GameState =
        copy(btc = newBtc.toPlainString())

    fun withUsd(newUsd: BigDecimal): GameState =
        copy(usd = newUsd.toPlainString())

    fun withBalances(newBtc: BigDecimal, newUsd: BigDecimal): GameState =
        copy(btc = newBtc.toPlainString(), usd = newUsd.toPlainString())
}
