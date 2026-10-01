package com.antigravity.bitcoinminingtycoon.ui.screens.stats

import com.antigravity.bitcoinminingtycoon.content.Achievements
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.content.Upgrades
import com.antigravity.bitcoinminingtycoon.engine.EconomyEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import com.antigravity.bitcoinminingtycoon.util.NumberFormatPreference
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import java.util.concurrent.TimeUnit

data class StatsRow(val label: String, val value: String)

data class StatsSection(val title: String, val rows: List<StatsRow>)

data class AchievementProgress(
    val current: BigDecimal,
    val target: BigDecimal,
    val unit: String
) {
    fun displayValue(numberFormat: NumberFormatPreference): String {
        val currentText = format(current, numberFormat)
        val targetText = format(target, numberFormat)
        val suffix = if (unit in setOf("BTC", "USD", "H/s", "seconds")) "" else " $unit"
        return "$currentText / $targetText$suffix"
    }

    private fun format(value: BigDecimal, preference: NumberFormatPreference): String = when (unit) {
        "BTC" -> NumberFormatter.formatBtc(value, preference)
        "USD" -> NumberFormatter.formatUsd(value, preference)
        "H/s" -> NumberFormatter.formatHashrate(value)
        "seconds" -> formatProgressDuration(value.toLong())
        else -> value.toBigInteger().toString()
    }
}

private fun formatProgressDuration(totalSeconds: Long): String {
    val seconds = totalSeconds.coerceAtLeast(0L)
    val hours = TimeUnit.SECONDS.toHours(seconds)
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val remainder = seconds % 60
    return "${hours}h ${minutes}m ${remainder}s"
}

/** Honest, read-only statistics presentation derived from saved game state. */
object StatsBreakdown {
    private const val POWER_HISTORY_CAPACITY = 288

    fun sections(
        state: GameState,
        currentEffectiveHashrate: BigDecimal = EconomyEngine.calculateEffectiveHashrate(state)
    ): List<StatsSection> {
        val stats = state.stats
        val preference = state.settings.numberFormat
        val minersOwned = state.totalOwnedMinerCount
        val knownTiersOwned = Miners.ALL.count { (state.miners[it.id] ?: 0L) > 0L }
        val history = stats.powerEnergyHistory
        val recordedEnergy = history.fold(BigDecimal.ZERO) { total, sample ->
            total.add(GameNumber.fromString(sample.energyKwh), GameNumber.MATH_CONTEXT)
        }
        val latestSample = history.lastOrNull()
        val lifetimeAchievementCount = Achievements.ALL.size

        return listOf(
            StatsSection(
                "Current operation",
                listOf(
                    StatsRow("Current Bitcoin", NumberFormatter.formatBtc(state.btcBigDecimal, preference)),
                    StatsRow("Cash on hand", NumberFormatter.formatUsd(state.usdBigDecimal, preference)),
                    StatsRow("Current mining speed", NumberFormatter.formatHashrate(currentEffectiveHashrate)),
                    StatsRow(
                        "Current Bitcoin rate",
                        "${NumberFormatter.formatBtc(EconomyEngine.calculateMinedBtc(currentEffectiveHashrate, 1.0), preference)}/s"
                    )
                )
            ),
            StatsSection(
                "Lifetime totals",
                listOf(
                    StatsRow("Lifetime Bitcoin mined", NumberFormatter.formatBtc(stats.lifetimeBtcBigDecimal, preference)),
                    StatsRow("Lifetime cash earned", NumberFormatter.formatUsd(stats.lifetimeUsdBigDecimal, preference)),
                    StatsRow("Bitcoin sold", NumberFormatter.formatBtc(stats.totalBtcSoldBigDecimal, preference)),
                    StatsRow("Highest market price", NumberFormatter.formatUsd(stats.highestPriceBigDecimal, preference)),
                    StatsRow("Lowest market price", NumberFormatter.formatUsd(stats.lowestPriceBigDecimal, preference))
                )
            ),
            StatsSection(
                "Mining sources",
                listOf(
                    StatsRow("Mining source coverage", sourceCoverage(stats)),
                    StatsRow("Manual mining", NumberFormatter.formatBtc(GameNumber.fromString(stats.manualBtc), preference)),
                    StatsRow("Foreground machines", NumberFormatter.formatBtc(GameNumber.fromString(stats.foregroundPassiveBtc), preference)),
                    StatsRow("Offline production", NumberFormatter.formatBtc(GameNumber.fromString(stats.offlineBtc), preference)),
                    StatsRow("Daily rewards", NumberFormatter.formatBtc(GameNumber.fromString(stats.dailyRewardBtc), preference)),
                    StatsRow("Event windfalls", NumberFormatter.formatBtc(GameNumber.fromString(stats.windfallBtc), preference)),
                    StatsRow("Achievement rewards", NumberFormatter.formatBtc(GameNumber.fromString(stats.achievementRewardBtc), preference))
                )
            ),
            StatsSection(
                "Assets and milestones",
                listOf(
                    StatsRow("Hardware currently owned", integerText(BigDecimal(minersOwned))),
                    StatsRow("Hardware tiers in use", "$knownTiersOwned / ${Miners.ALL.size}"),
                    StatsRow("Hardware units ever purchased", integerText(BigDecimal(stats.totalMinersPurchased.coerceAtLeast(0L)))),
                    StatsRow("Upgrades currently installed", "${state.purchasedUpgrades.size} / ${Upgrades.ALL.size}"),
                    StatsRow("Upgrades ever purchased", integerText(BigDecimal(stats.totalUpgradesPurchased.coerceAtLeast(0L)))),
                    StatsRow("Manual mining taps", integerText(BigDecimal(stats.totalManualTaps.coerceAtLeast(0L)))),
                    StatsRow("Events encountered", integerText(BigDecimal(stats.totalEventsTriggered.coerceAtLeast(0L)))),
                    StatsRow("Achievements unlocked", "${state.achievements.size} / $lifetimeAchievementCount"),
                    StatsRow("Prestiges completed", integerText(BigDecimal(stats.totalPrestiges.coerceAtLeast(0L)))),
                    StatsRow("Satoshi Points available", integerText(BigDecimal(state.satoshiPoints.coerceAtLeast(0L)))),
                    StatsRow("Lifetime Satoshi Points earned", integerText(BigDecimal(stats.lifetimeSatoshiPointsEarned.coerceAtLeast(0L))))
                )
            ),
            StatsSection(
                "Activity and infrastructure",
                listOf(
                    StatsRow("Active play time", formatActivePlayTime(stats)),
                    StatsRow("Peak mining speed", NumberFormatter.formatHashrate(stats.peakHashrateBigDecimal)),
                    StatsRow("Current operating temperature", String.format(Locale.US, "%.1f °C", EconomyEngine.calculateThermalState(state).first)),
                    StatsRow("Peak temperature", String.format(Locale.US, "%.1f °C", stats.peakTemperatureC)),
                    StatsRow("Current power demand", "${scalar(EconomyEngine.calculatePowerDemand(state), preference)} kW"),
                    StatsRow("Power history retained", "${history.size} / $POWER_HISTORY_CAPACITY samples"),
                    StatsRow("Latest recorded power load", latestSample?.let {
                        "${scalar(GameNumber.fromString(it.demandKw).toDouble(), preference)} / ${scalar(GameNumber.fromString(it.capacityKw).toDouble(), preference)} kW"
                    } ?: "No power samples yet"),
                    StatsRow("Energy in retained samples", "${formatEnergy(recordedEnergy, preference)} kWh")
                )
            )
        )
    }

    fun achievementProgress(achievementId: String, state: GameState): AchievementProgress? {
        val stats = state.stats
        val currentAndTarget = when (achievementId) {
            "first_hash" -> count(stats.totalManualTaps, 1, "taps")
            "manual_century" -> count(stats.totalManualTaps, 100, "taps")
            "manual_millennium" -> count(stats.totalManualTaps, 1_000, "taps")
            "megahash_barrier" -> peakHashrate(state, "1000000")
            "gigahash_frontier" -> peakHashrate(state, "1000000000")
            "terahash_titan" -> peakHashrate(state, "1000000000000")
            "first_satoshi" -> lifetimeBtc(state, "0.00000001")
            "deci_coiner" -> lifetimeBtc(state, "0.1")
            "whole_coiner" -> amount(state.btcBigDecimal, "1", "BTC")
            "ten_coins" -> lifetimeBtc(state, "10")
            "hundred_coins" -> lifetimeBtc(state, "100")
            "thousand_coins" -> lifetimeBtc(state, "1000")
            "first_liquidation" -> amount(stats.totalBtcSoldBigDecimal, "0.00000001", "BTC")
            "five_figure_exit" -> lifetimeUsd(state, "10000")
            "six_figure_trade" -> lifetimeUsd(state, "100000")
            "mining_mogul" -> lifetimeUsd(state, "1000000")
            "peak_market_timing" -> amount(stats.highestPriceBigDecimal, "100000", "USD")
            "first_rig" -> count(stats.totalMinersPurchased, 1, "purchases")
            "ten_rigs" -> count(state.totalOwnedMinerCount, 10, "miners")
            "fifty_rigs" -> count(state.totalOwnedMinerCount, 50, "miners")
            "hundred_rigs" -> count(state.totalOwnedMinerCount, 100, "miners")
            "asic_vanguard" -> count(state.miners["entry_asic"] ?: 0L, 1, "units")
            "modern_density" -> count(state.miners["industrial_asic"] ?: 0L, 1, "units")
            "first_upgrade" -> count(state.purchasedUpgrades.size, 1, "upgrades")
            "five_upgrades" -> count(state.purchasedUpgrades.size, 5, "upgrades")
            "fifteen_upgrades" -> count(state.purchasedUpgrades.size, 15, "upgrades")
            "twenty_five_upgrades" -> count(state.purchasedUpgrades.size, 25, "upgrades")
            "singularity" -> count(state.purchasedUpgrades.size, 30, "upgrades")
            "substation_expansion" -> count(state.powerGridTier, 2, "tiers")
            "high_voltage_feed" -> count(state.powerGridTier, 3, "tiers")
            "forced_air_cooling" -> count(state.coolingTier, 2, "tiers")
            "industrial_chill" -> count(state.coolingTier, 3, "tiers")
            "dynamic_environment" -> count(stats.totalEventsTriggered, 1, "events")
            "ten_events" -> count(stats.totalEventsTriggered, 10, "events")
            "active_operations" -> count(stats.totalPlaytimeSeconds, 600, "seconds")
            "dedicated_miner" -> count(stats.totalPlaytimeSeconds, 3_600, "seconds")
            "genesis_reset" -> count(stats.totalPrestiges, 1, "prestiges")
            "satoshi_initiate" -> count(stats.lifetimeSatoshiPointsEarned, 10, "points")
            "legacy_architect" -> count(stats.lifetimeSatoshiPointsEarned, 100, "points")
            "quantum_ascension" -> count(stats.lifetimeSatoshiPointsEarned, 1_000, "points")
            else -> return null
        }
        return AchievementProgress(currentAndTarget.first, currentAndTarget.second, currentAndTarget.third)
    }

    fun formatActivePlayTime(stats: StatsState): String {
        val wholeSeconds = stats.totalPlaytimeSeconds.coerceAtLeast(0L)
        val hours = TimeUnit.SECONDS.toHours(wholeSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(wholeSeconds) % 60
        val seconds = wholeSeconds % 60
        val fraction = GameNumber.fromString(stats.playtimeFractionalSeconds)
            .min(BigDecimal.ONE).setScale(2, RoundingMode.DOWN).toPlainString().substringAfter('.', "00")
        return "${hours}h ${minutes}m ${seconds}.${fraction}s"
    }

    private fun sourceCoverage(stats: StatsState): String = if (stats.sourceBreakdownTrackedSinceV12) {
        "Complete for this save; tracked from v1.2"
    } else {
        "Partial; tracked since v1.2, earlier lifetime totals are unassigned"
    }

    private fun count(current: Long, target: Long, unit: String) =
        Triple(BigDecimal(current.coerceAtLeast(0L)), BigDecimal(target), unit)

    private fun count(current: Int, target: Int, unit: String) =
        Triple(BigDecimal(current.coerceAtLeast(0)), BigDecimal(target), unit)

    private fun amount(current: BigDecimal, target: String, unit: String) =
        Triple(current.max(BigDecimal.ZERO), BigDecimal(target), unit)

    private fun lifetimeBtc(state: GameState, target: String) = amount(state.stats.lifetimeBtcBigDecimal, target, "BTC")
    private fun lifetimeUsd(state: GameState, target: String) = amount(state.stats.lifetimeUsdBigDecimal, target, "USD")
    private fun peakHashrate(state: GameState, target: String) = amount(state.stats.peakHashrateBigDecimal, target, "H/s")

    private fun scalar(value: Double, preference: NumberFormatPreference): String =
        if (value.isFinite() && value >= 0.0) NumberFormatter.formatCompact(BigDecimal(value.toString()), preference) else "0.00"

    private fun integerText(value: BigDecimal): String = value.toBigInteger().toString()

    private fun formatEnergy(value: BigDecimal, preference: NumberFormatPreference): String =
        if (value < BigDecimal("1000")) {
            value.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        } else NumberFormatter.formatCompact(value, preference)

}
