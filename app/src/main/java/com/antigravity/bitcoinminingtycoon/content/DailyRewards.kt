package com.antigravity.bitcoinminingtycoon.content

import com.antigravity.bitcoinminingtycoon.engine.AchievementEngine
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

    const val COOLDOWN_MILLIS: Long = 20L * 3600L * 1000L // 20 hours cooldown (Decision D018)

    val TRACK: List<DailyRewardDefinition> = listOf(
        DailyRewardDefinition(
            dayNumber = 1,
            title = "Hardware Grant",
            description = "$100 USD seed capital for facility operations",
            usdReward = BigDecimal("100.00")
        ),
        DailyRewardDefinition(
            dayNumber = 2,
            title = "Satoshi Injection",
            description = "0.00005000 BTC reserve payout",
            btcReward = BigDecimal("0.00005000")
        ),
        DailyRewardDefinition(
            dayNumber = 3,
            title = "Equipment Subvention",
            description = "$500 USD commercial hardware allowance",
            usdReward = BigDecimal("500.00")
        ),
        DailyRewardDefinition(
            dayNumber = 4,
            title = "Block Bounty",
            description = "0.00025000 BTC network treasury allocation",
            btcReward = BigDecimal("0.00025000")
        ),
        DailyRewardDefinition(
            dayNumber = 5,
            title = "Datacenter Liquidity",
            description = "$2,500 USD industrial expansion grant",
            usdReward = BigDecimal("2500.00")
        ),
        DailyRewardDefinition(
            dayNumber = 6,
            title = "Whale Reserve",
            description = "0.00100000 BTC high-yield reserve payout",
            btcReward = BigDecimal("0.00100000")
        ),
        DailyRewardDefinition(
            dayNumber = 7,
            title = "Satoshi Heritage",
            description = "1 Satoshi Point + $10,000 USD facility bonus",
            usdReward = BigDecimal("10000.00"),
            satoshiPointsReward = 1L
        )
    )

    fun getForDay(day: Int): DailyRewardDefinition {
        val clampedDay = ((day - 1).coerceAtLeast(0) % 7) + 1
        return TRACK[clampedDay - 1]
    }

    /**
     * Determines whether the player is currently eligible to claim their next daily reward.
     * Enforces the 20-hour non-resetting cumulative cooldown (Decision D018).
     */
    fun canClaim(state: GameState, currentWallMillis: Long): Boolean {
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

        val reward = getForDay(state.dailyRewardDay)

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
}
