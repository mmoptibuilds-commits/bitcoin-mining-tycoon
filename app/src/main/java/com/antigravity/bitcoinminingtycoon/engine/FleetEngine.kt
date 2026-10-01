package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.MinerDefinition
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal
import java.math.RoundingMode

enum class BulkMode(val count: Int, val label: String) {
    X1(1, "x1"),
    X10(10, "x10"),
    X25(25, "x25"),
    MAX(-1, "MAX")
}

/**
 * Pure Kotlin deterministic engine for Hardware Fleet operations and bulk purchases.
 * Invariant 1: UI composables never contain economy calculations.
 * Invariant 2: Determinstic and atomic state mutations.
 * Invariant 3: Overdraft prevention — USD balance can never drop below zero.
 */
object FleetEngine {

    /**
     * Calculates the count and cost for a given miner and bulk mode.
     * @return Pair of (countToBuy, totalCost)
     */
    fun calculatePurchase(
        miner: MinerDefinition,
        owned: Long,
        bulkMode: BulkMode,
        availableUsd: BigDecimal
    ): Pair<Long, BigDecimal> {
        return if (bulkMode == BulkMode.MAX) {
            val maxCount = GameNumber.calculateMaxAffordable(
                availableFunds = availableUsd,
                baseCost = miner.baseCostUsd,
                growthRate = miner.growthRate,
                owned = owned
            )
            if (maxCount > 0L) {
                val cost = GameNumber.calculateBulkCost(miner.baseCostUsd, miner.growthRate, owned, maxCount)
                Pair(maxCount, cost)
            } else {
                // If 0 affordable, show cost of next 1 unit so user sees the goal
                val nextOneCost = GameNumber.calculateBulkCost(miner.baseCostUsd, miner.growthRate, owned, 1L)
                Pair(0L, nextOneCost)
            }
        } else {
            val count = bulkMode.count.toLong()
            val cost = GameNumber.calculateBulkCost(miner.baseCostUsd, miner.growthRate, owned, count)
            Pair(count, cost)
        }
    }

    /**
     * Checks if a miner tier is unlocked given lifetime BTC mined.
     */
    fun isUnlocked(miner: MinerDefinition, state: GameState): Boolean {
        return state.stats.lifetimeBtcBigDecimal >= miner.unlockLifetimeBtc
    }

    /**
     * Atomically executes a hardware miner purchase.
     * Returns updated GameState or original GameState if purchase is invalid.
     */
    fun buyMiner(
        state: GameState,
        minerId: String,
        bulkMode: BulkMode
    ): GameState {
        val miner = Miners.getById(minerId) ?: return state

        // 1. Verify unlock threshold
        if (!isUnlocked(miner, state)) return state

        val owned = state.miners[minerId] ?: 0L
        val availableUsd = state.usdBigDecimal

        val (countToBuy, totalCost) = calculatePurchase(miner, owned, bulkMode, availableUsd)

        // 2. Validate affordability
        if (countToBuy <= 0L || availableUsd < totalCost) {
            return state
        }

        // Do not allow corrupted or extreme saves to wrap owned/purchased counters.
        if (countToBuy > Long.MAX_VALUE - owned) return state

        // 3. Atomically update balances and owned counts
        val nextUsd = availableUsd.subtract(totalCost, GameNumber.MATH_CONTEXT).max(BigDecimal.ZERO)
        val nextOwned = owned + countToBuy
        val nextMiners = state.miners + (minerId to nextOwned)

        val nextTotalPurchased = if (countToBuy > Long.MAX_VALUE - state.stats.totalMinersPurchased) {
            Long.MAX_VALUE
        } else {
            state.stats.totalMinersPurchased + countToBuy
        }
        val updatedStats = state.stats.copy(
            totalMinersPurchased = nextTotalPurchased
        )

        return state.copy(
            usd = nextUsd.setScale(2, RoundingMode.HALF_UP).toPlainString(),
            miners = nextMiners,
            stats = updatedStats
        )
    }
}
