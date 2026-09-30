package com.antigravity.bitcoinminingtycoon.content

import com.antigravity.bitcoinminingtycoon.engine.AchievementEngine
import com.antigravity.bitcoinminingtycoon.engine.EconomyEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal
import java.math.RoundingMode

data class DailyRewardDefinition(
    val dayNumber: Int,
    val title: String,
    val description: String,
    val usdReward: BigDecimal = BigDecimal.ZERO,
    val btcReward: BigDecimal = BigDecimal.ZERO,
    val satoshiPointsReward: Long = 0L
)

object DailyRewards {

    const val COOLDOWN_MILLIS: Long = BalanceConfig.DAILY_CLAIM_COOLDOWN_HOURS * 3600L * 1000L

    val TRACK: List<DailyRewardDefinition> = listOf(
        DailyRewardDefinition(
            dayNumber = 1,
            title = "First Shift Materials",
            description = "$3.00 starter materials after your first machine",
            usdReward = BigDecimal(BalanceConfig.DAY_ONE_CASH_USD)
        ),
        DailyRewardDefinition(
            dayNumber = 2,
            title = "Satoshi Injection",
            description = "Bitcoin credit scaled to current mining output",
        ),
        DailyRewardDefinition(
            dayNumber = 3,
            title = "Equipment Subvention",
            description = "Cash credit scaled to current mining output",
        ),
        DailyRewardDefinition(
            dayNumber = 4,
            title = "Block Bounty",
            description = "Bitcoin credit scaled to current mining output",
        ),
        DailyRewardDefinition(
            dayNumber = 5,
            title = "Datacenter Liquidity",
            description = "Cash credit scaled to current mining output",
        ),
        DailyRewardDefinition(
            dayNumber = 6,
            title = "Whale Reserve",
            description = "Bitcoin credit scaled to current mining output",
        ),
        DailyRewardDefinition(
            dayNumber = 7,
            title = "Satoshi Heritage",
            description = "Cash credit scaled to current mining output + 1 Satoshi Point",
            satoshiPointsReward = 1L
        )
    )

    fun getForDay(day: Int): DailyRewardDefinition {
        val clampedDay = ((day - 1).coerceAtLeast(0) % 7) + 1
        return TRACK[clampedDay - 1]
    }

    fun getForDay(day: Int, state: GameState): DailyRewardDefinition {
        val clampedDay = ((day - 1).coerceAtLeast(0) % 7) + 1
        val template = TRACK[clampedDay - 1]
        if (clampedDay == 1 || !hasAnyKnownMiner(state)) return template

        val stage = state.highestDiscoveredFacilityStage.coerceIn(0, 9)
        val productionBtc = EconomyEngine.calculateMinedBtc(EconomyEngine.calculateEffectiveHashrate(state), BalanceConfig.DAILY_REWARD_TARGET_SECONDS)
        val btcCap = BigDecimal(BalanceConfig.DAILY_REWARD_BTC_CAPS[stage])
        val usdCap = BigDecimal(BalanceConfig.DAILY_REWARD_USD_CAPS[stage])
        return when (clampedDay) {
            2, 4, 6 -> template.copy(
                btcReward = productionBtc.min(btcCap),
                description = "${BalanceConfig.DAILY_REWARD_TARGET_SECONDS.toInt()} seconds of capped current Bitcoin production"
            )
            3, 5, 7 -> template.copy(
                usdReward = productionBtc.multiply(state.marketPriceBigDecimal, GameNumber.MATH_CONTEXT)
                    .min(usdCap).setScale(2, RoundingMode.HALF_UP),
                description = "${BalanceConfig.DAILY_REWARD_TARGET_SECONDS.toInt()} seconds of capped current production value" +
                    if (clampedDay == 7) " + 1 Satoshi Point" else ""
            )
            else -> template
        }
    }

    /**
     * Determines whether the player is currently eligible to claim their next daily reward.
     * Enforces the 20-hour non-resetting cumulative cooldown (Decision D018).
     */
    fun canClaim(state: GameState, currentWallMillis: Long): Boolean {
        if (!hasAnyKnownMiner(state)) return false
        if (state.lastDailyClaimWallMillis <= 0L) return true
        if (currentWallMillis < state.lastDailyClaimWallMillis) return false // Defensive against clock rollback
        return (currentWallMillis - state.lastDailyClaimWallMillis) >= COOLDOWN_MILLIS
    }

    /**
     * Calculates milliseconds remaining until the next claim eligibility.
     */
    fun millisUntilNextClaim(state: GameState, currentWallMillis: Long): Long {
        if (canClaim(state, currentWallMillis)) return 0L
        val elapsed = currentWallMillis - state.lastDailyClaimWallMillis
        return (COOLDOWN_MILLIS - elapsed).coerceAtLeast(0L)
    }

    /**
     * Executes the daily reward claim transition idempotently.
     */
    fun claim(state: GameState, currentWallMillis: Long): Pair<GameState, DailyRewardDefinition?> {
        if (!canClaim(state, currentWallMillis)) {
            return Pair(state, null)
        }

        val reward = getForDay(state.dailyRewardDay, state)

        var newBtc = state.btcBigDecimal
        var newLifetimeBtc = state.stats.lifetimeBtcBigDecimal
        if (reward.btcReward > BigDecimal.ZERO) {
            newBtc = newBtc.add(reward.btcReward, GameNumber.MATH_CONTEXT)
            newLifetimeBtc = newLifetimeBtc.add(reward.btcReward, GameNumber.MATH_CONTEXT)
        }

        var newUsd = state.usdBigDecimal
        var newLifetimeUsd = state.stats.lifetimeUsdBigDecimal
        if (reward.usdReward > BigDecimal.ZERO) {
            newUsd = newUsd.add(reward.usdReward, GameNumber.MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP)
            newLifetimeUsd = newLifetimeUsd.add(reward.usdReward, GameNumber.MATH_CONTEXT)
        }

        val newSatoshiPoints = saturatingAdd(state.satoshiPoints, reward.satoshiPointsReward)
        val newLifetimeSp = saturatingAdd(state.stats.lifetimeSatoshiPointsEarned, reward.satoshiPointsReward)
        val newDailyPoints = saturatingAdd(state.stats.dailyPointsEarnedSinceV2, reward.satoshiPointsReward)

        val nextDay = if (state.dailyRewardDay >= 7) 1 else state.dailyRewardDay + 1

        val updatedStats = state.stats.copy(
            lifetimeBtcMined = newLifetimeBtc.toPlainString(),
            lifetimeUsdEarned = newLifetimeUsd.toPlainString(),
            lifetimeSatoshiPointsEarned = newLifetimeSp,
            dailyRewardBtc = GameNumber.fromString(state.stats.dailyRewardBtc)
                .add(reward.btcReward, GameNumber.MATH_CONTEXT).toPlainString(),
            dailyPointsEarnedSinceV2 = newDailyPoints
        )

        val updatedState = state.copy(
            btc = newBtc.toPlainString(),
            usd = newUsd.toPlainString(),
            satoshiPoints = newSatoshiPoints,
            dailyRewardDay = nextDay,
            lastDailyClaimWallMillis = currentWallMillis,
            stats = updatedStats
        )

        val (stateWithAchievements, _) = AchievementEngine.evaluate(updatedState)
        return Pair(stateWithAchievements, reward)
    }

    private fun saturatingAdd(left: Long, right: Long): Long =
        if (right > 0L && left > Long.MAX_VALUE - right) Long.MAX_VALUE else (left + right).coerceAtLeast(0L)

    private fun hasAnyKnownMiner(state: GameState): Boolean =
        state.miners.any { (id, count) -> count > 0L && Miners.getById(id) != null }
}
