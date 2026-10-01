package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.EventType
import com.antigravity.bitcoinminingtycoon.content.Events
import com.antigravity.bitcoinminingtycoon.content.BalanceConfig
import com.antigravity.bitcoinminingtycoon.content.GameEventDefinition
import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

data class WindfallReward(
    val btcGain: BigDecimal = BigDecimal.ZERO,
    val usdGain: BigDecimal = BigDecimal.ZERO,
    val message: String = ""
)

object EventEngine {

    const val MAX_SIMULTANEOUS_AMBIENT_EVENTS = 2
    const val MAX_EVENT_DURATION_MILLIS = 120_000L // 2 minutes max extension cap

    /**
     * Ticks event countdown timer and expires outdated events deterministically.
     */
    fun tick(
        state: GameState,
        deltaSeconds: Double,
        wallMillis: Long,
        rng: Random = Random(state.rngSeed)
    ): GameState {
        // 1. Purge expired events
        val validEvents = state.activeEvents.filter { it.expiresAtWallMillis > wallMillis }
        var current = if (validEvents.size != state.activeEvents.size) {
            state.copy(activeEvents = validEvents)
        } else {
            state
        }

        // 2. Decrement event roll timer
        var timer = current.eventTimerSeconds - deltaSeconds
        if (timer <= 0.0) {
            val (rolledState, _) = rollRandomEvent(current, wallMillis, rng)
            current = rolledState
            // Next roll in 120 to 240 seconds
            timer = BalanceConfig.EVENT_MIN_INTERVAL_SECONDS +
                (rng.nextDouble() * (BalanceConfig.EVENT_MAX_INTERVAL_SECONDS - BalanceConfig.EVENT_MIN_INTERVAL_SECONDS))
        }

        return current.copy(eventTimerSeconds = timer)
    }

    /**
     * Rolls a pseudo-random event and applies deterministic stacking rules.
     */
    fun rollRandomEvent(
        state: GameState,
        wallMillis: Long,
        rng: Random = Random(state.rngSeed)
    ): Pair<GameState, GameEventDefinition?> {
        val roll = rng.nextDouble()
        val candidateEvents = when {
            roll < BalanceConfig.EVENT_POSITIVE_WEIGHT -> Events.ALL.filter { it.type == EventType.AMBIENT_POSITIVE }
            roll < BalanceConfig.EVENT_POSITIVE_WEIGHT + BalanceConfig.EVENT_NEGATIVE_WEIGHT -> Events.ALL.filter { it.type == EventType.AMBIENT_NEGATIVE }
            else -> Events.ALL.filter { it.type == EventType.WINDFALL }
        }

        if (candidateEvents.isEmpty()) return Pair(state, null)
        val selected = candidateEvents[rng.nextInt(candidateEvents.size)]

        return Pair(applyEvent(state, selected, wallMillis), selected)
    }

    /**
     * Applies an event according to strict non-corrupting stacking invariants.
     */
    fun applyEvent(
        state: GameState,
        event: GameEventDefinition,
        wallMillis: Long
    ): GameState {
        val currentEvents = state.activeEvents.toMutableList()

        if (event.isWindfall) {
            // Only allow 1 active windfall on screen at a time
            if (currentEvents.any { it.isWindfall }) {
                return state
            }
            currentEvents.add(
                ActiveEventState(
                    eventId = event.id,
                    expiresAtWallMillis = wallMillis + (event.durationSeconds * 1000L),
                    multiplier = event.multiplier,
                    powerModifier = event.powerModifier,
                    heatModifier = event.heatModifier,
                    isWindfall = true
                )
            )
        } else {
            // Ambient event
            val existingIndex = currentEvents.indexOfFirst { it.eventId == event.id }
            if (existingIndex >= 0) {
                // Extend duration up to MAX_EVENT_DURATION_MILLIS
                val existing = currentEvents[existingIndex]
                val extendedExpiry = minOf(
                    existing.expiresAtWallMillis + (event.durationSeconds * 1000L),
                    wallMillis + MAX_EVENT_DURATION_MILLIS
                )
                currentEvents[existingIndex] = existing.copy(expiresAtWallMillis = extendedExpiry)
            } else {
                val ambientEvents = currentEvents.filter { !it.isWindfall }
                if (ambientEvents.size >= MAX_SIMULTANEOUS_AMBIENT_EVENTS) {
                    // Replace the ambient event closest to expiration
                    val closest = ambientEvents.minByOrNull { it.expiresAtWallMillis }
                    currentEvents.remove(closest)
                }
                currentEvents.add(
                    ActiveEventState(
                        eventId = event.id,
                        expiresAtWallMillis = wallMillis + (event.durationSeconds * 1000L),
                        multiplier = event.multiplier,
                        powerModifier = event.powerModifier,
                        heatModifier = event.heatModifier,
                        isWindfall = false
                    )
                )
            }
        }

        val updatedStats = state.stats.copy(
            totalEventsTriggered = saturatingAdd(state.stats.totalEventsTriggered, 1L)
        )

        return state.copy(
            activeEvents = currentEvents,
            stats = updatedStats
        )
    }

    /**
     * Claims an interactive windfall badge reward.
     */
    fun claimWindfall(
        state: GameState,
        eventId: String,
        wallMillis: Long
    ): Pair<GameState, WindfallReward> {
        val windfall = state.activeEvents.firstOrNull { it.eventId == eventId && it.isWindfall }
            ?: return Pair(state, WindfallReward())

        if (windfall.expiresAtWallMillis <= wallMillis) {
            // Expired before tap
            val remainingEvents = state.activeEvents.filter { it.eventId != eventId }
            return Pair(state.copy(activeEvents = remainingEvents), WindfallReward())
        }

        val eventDef = Events.getById(eventId)
        val remainingEvents = state.activeEvents.filter { it.eventId != eventId }

        val effectiveHashrate = EconomyEngine.calculateEffectiveHashrate(state)
        val currentPrice = state.marketPriceBigDecimal

        return when (eventId) {
            "lucky_block" -> {
                // Award 180 seconds of mining output, or minimum 0.00010000 BTC
                val minedBtc = EconomyEngine.calculateMinedBtc(effectiveHashrate, BalanceConfig.LUCKY_BLOCK_PRODUCTION_SECONDS)
                val minBtc = BigDecimal(BalanceConfig.LUCKY_BLOCK_MIN_BTC)
                val btcGain = maxOf(minedBtc, minBtc)

                val newBtc = state.btcBigDecimal.add(btcGain, GameNumber.MATH_CONTEXT)
                val newLifetimeBtc = state.stats.lifetimeBtcBigDecimal.add(btcGain, GameNumber.MATH_CONTEXT)
                val updatedStats = state.stats.copy(
                    lifetimeBtcMined = newLifetimeBtc.toPlainString(),
                    windfallBtc = GameNumber.fromString(state.stats.windfallBtc)
                        .add(btcGain, GameNumber.MATH_CONTEXT).toPlainString()
                )

                val updatedState = state.copy(
                    btc = newBtc.toPlainString(),
                    activeEvents = remainingEvents,
                    stats = updatedStats
                )
                Pair(
                    updatedState,
                    WindfallReward(
                        btcGain = btcGain,
                        message = "Lucky Block claimed: +${NumberFormatter.formatBtc(btcGain)}!"
                    )
                )
            }

            "perfect_block" -> {
                // Award currentPrice * 120s of mining output, or minimum $250.00 USD
                val minedBtc = EconomyEngine.calculateMinedBtc(effectiveHashrate, BalanceConfig.PERFECT_BLOCK_PRODUCTION_SECONDS)
                val rawUsd = minedBtc.multiply(currentPrice, GameNumber.MATH_CONTEXT)
                val minUsd = BigDecimal(BalanceConfig.PERFECT_BLOCK_MIN_USD)
                val usdGain = maxOf(rawUsd, minUsd).setScale(2, RoundingMode.HALF_UP)

                val newUsd = state.usdBigDecimal.add(usdGain, GameNumber.MATH_CONTEXT)
                val newLifetimeUsd = state.stats.lifetimeUsdBigDecimal.add(usdGain, GameNumber.MATH_CONTEXT)
                val updatedStats = state.stats.copy(
                    lifetimeUsdEarned = newLifetimeUsd.toPlainString()
                )

                val updatedState = state.copy(
                    usd = newUsd.toPlainString(),
                    activeEvents = remainingEvents,
                    stats = updatedStats
                )
                Pair(
                    updatedState,
                    WindfallReward(
                        usdGain = usdGain,
                        message = "Perfect Block claimed: +${NumberFormatter.formatUsd(usdGain)}!"
                    )
                )
            }

            else -> {
                // Fallback windfall
                val updatedState = state.copy(activeEvents = remainingEvents)
                Pair(updatedState, WindfallReward(message = "Event claimed: ${eventDef?.title}"))
            }
        }
    }

    private fun saturatingAdd(left: Long, right: Long): Long =
        if (right > 0L && left > Long.MAX_VALUE - right) Long.MAX_VALUE else (left + right).coerceAtLeast(0L)
}
