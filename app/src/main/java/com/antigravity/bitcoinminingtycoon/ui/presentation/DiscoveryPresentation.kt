package com.antigravity.bitcoinminingtycoon.ui.presentation

import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.engine.EconomyEngine
import com.antigravity.bitcoinminingtycoon.engine.PowerEngine
import com.antigravity.bitcoinminingtycoon.engine.ThermalEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import com.antigravity.bitcoinminingtycoon.model.TeachingCueIds
import java.math.BigDecimal

data class DiscoveryCue(
    val id: String,
    val title: String,
    val body: String,
    val actionLabel: String,
    val firstMachineCostLabel: String? = null
)

data class DiscoveryGoal(
    val title: String,
    val body: String,
    val actionLabel: String
)

/** State-derived, deterministic teaching for the first Mine → Bitcoin → Cash → machine loop. */
object DiscoveryPresentation {
    const val MINE_BITCOIN = TeachingCueIds.MINE_BITCOIN
    const val SELL_BITCOIN = TeachingCueIds.SELL_BITCOIN
    const val BUY_FIRST_MACHINE = TeachingCueIds.BUY_FIRST_MACHINE
    const val PASSIVE_MINING = TeachingCueIds.PASSIVE_MINING

    private val firstMachineCost: BigDecimal = Miners.ALL.first().baseCostUsd
    private val firstMachineCostLabel = "$${firstMachineCost.stripTrailingZeros().toPlainString()}"

    fun nextCue(state: GameState): DiscoveryCue? {
        if (state.onboardingCompleted) return null

        val hasMachine = state.miners.values.any { it > 0L }
        val stats = state.stats
        val hasPriorProgress = hasMachine || state.btcBigDecimal.signum() > 0 || state.usdBigDecimal.signum() > 0 ||
            state.purchasedUpgrades.isNotEmpty() || state.satoshiPoints > 0L || state.achievements.isNotEmpty() ||
            stats.lifetimeBtcBigDecimal.signum() > 0 || stats.lifetimeUsdBigDecimal.signum() > 0 ||
            stats.totalManualTaps > 0L || stats.totalBtcSoldBigDecimal.signum() > 0 ||
            stats.totalMinersPurchased > 0L || stats.totalUpgradesPurchased > 0L || stats.totalPrestiges > 0L ||
            stats.totalPlaytimeSeconds > 0L || stats.totalEventsTriggered > 0L ||
            state.lastDailyClaimWallMillis > 0L || state.dailyRewardDay > 1 || state.pendingOfflineSummary != null
        // Old saves keep their current systems; do not restart the first-session teaching flow.
        if (hasPriorProgress && state.completedTeachingCueIds.isEmpty()) return null

        val cue = when {
            !hasMachine && state.usdBigDecimal >= firstMachineCost -> DiscoveryCue(
                id = BUY_FIRST_MACHINE,
                title = "Buy your first machine",
                body = "Cash buys machines. The Ancient CPU costs $firstMachineCostLabel and adds mining speed over time.",
                actionLabel = "Browse hardware",
                firstMachineCostLabel = firstMachineCostLabel
            )
            !hasMachine && state.btcBigDecimal.signum() > 0 -> DiscoveryCue(
                id = SELL_BITCOIN,
                title = "Turn Bitcoin into Cash",
                body = "Sell mined Bitcoin for Cash. The sale control shows the exact amount before you sell.",
                actionLabel = "Sell MAX",
                firstMachineCostLabel = firstMachineCostLabel
            )
            !hasMachine -> DiscoveryCue(
                id = MINE_BITCOIN,
                title = "Make your first Bitcoin",
                body = "Tap Mine to create simulated Bitcoin. Sell it for Cash when you are ready to buy a machine.",
                actionLabel = "Mine",
                firstMachineCostLabel = firstMachineCostLabel
            )
            else -> DiscoveryCue(
                id = PASSIVE_MINING,
                title = "Your machine is mining",
                body = "Mining speed adds Bitcoin over time. Hardware keeps working within the game's offline time limit.",
                actionLabel = "Got it"
            )
        }
        return cue.takeUnless { it.id in state.completedTeachingCueIds }
    }

    fun nextGoal(state: GameState): DiscoveryGoal {
        val hasMachine = state.miners.values.any { it > 0L }
        if (!hasMachine) {
            return when {
                state.usdBigDecimal >= firstMachineCost -> DiscoveryGoal(
                    "Buy your first machine",
                    "An Ancient CPU costs $firstMachineCostLabel.",
                    "Hardware"
                )
                state.btcBigDecimal.signum() > 0 -> DiscoveryGoal(
                    "Sell Bitcoin for Cash",
                    "Your first machine costs $firstMachineCostLabel. Sell or mine more to reach it.",
                    "Sell MAX"
                )
                else -> DiscoveryGoal(
                    "Make your first Bitcoin",
                    "Tap Mine to get started.",
                    "Mine"
                )
            }
        }

        if (PowerEngine.calculatePowerFactor(state) < 0.999) {
            return DiscoveryGoal("Power is limiting mining speed", "Upgrade Power to restore full machine output.", "Upgrades")
        }
        if (EconomyEngine.calculateThermalState(state).second < 0.999) {
            return DiscoveryGoal("Cooling is limiting mining speed", "Upgrade Cooling to bring the facility temperature down.", "Upgrades")
        }
        val nextMiner = Miners.ALL.firstOrNull { it.unlockLifetimeBtc > state.stats.lifetimeBtcBigDecimal }
        return if (GameNumber.fromString(state.stats.foregroundPassiveBtc).signum() == 0) {
            DiscoveryGoal("Your machines are working", "Mining speed adds Bitcoin automatically.", "Hardware")
        } else {
            DiscoveryGoal(
                "Grow your mining speed",
                nextMiner?.let { "Keep mining to unlock ${it.name}." } ?: "Explore Hardware and Upgrades to grow your facility.",
                "Hardware"
            )
        }
    }
}
