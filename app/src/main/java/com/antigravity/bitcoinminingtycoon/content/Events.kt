package com.antigravity.bitcoinminingtycoon.content

enum class EventType {
    AMBIENT_POSITIVE,
    AMBIENT_NEGATIVE,
    WINDFALL
}

data class GameEventDefinition(
    val id: String,
    val title: String,
    val description: String,
    val durationSeconds: Long,
    val type: EventType,
    val multiplier: Double = 1.0,
    val powerModifier: Double = 1.0,
    val heatModifier: Double = 1.0,
    val badgeText: String
) {
    val isWindfall: Boolean get() = type == EventType.WINDFALL
}

object Events {
    val BULL_RUN = GameEventDefinition(
        id = "bull_run",
        title = "Bull Run",
        description = "Market-wide buying pressure accelerates spot price growth.",
        durationSeconds = 45L,
        type = EventType.AMBIENT_POSITIVE,
        badgeText = "+3% PRICE"
    )

    val CHEAP_ELECTRICITY = GameEventDefinition(
        id = "cheap_electricity",
        title = "Cheap Electricity",
        description = "Municipal industrial energy surplus lowers grid consumption.",
        durationSeconds = 60L,
        type = EventType.AMBIENT_POSITIVE,
        powerModifier = 0.60,
        badgeText = "-40% POWER"
    )

    val LUCKY_BLOCK = GameEventDefinition(
        id = "lucky_block",
        title = "Lucky Block",
        description = "Found an orphan block! Tap immediately to claim a Bitcoin windfall.",
        durationSeconds = 20L,
        type = EventType.WINDFALL,
        badgeText = "WINDFALL BTC"
    )

    val ASIC_BREAKTHROUGH = GameEventDefinition(
        id = "asic_breakthrough",
        title = "ASIC Breakthrough",
        description = "Custom microcode firmware unlock boosts mining throughput.",
        durationSeconds = 45L,
        type = EventType.AMBIENT_POSITIVE,
        multiplier = 1.35,
        badgeText = "+35% HASH"
    )

    val COOLING_WEATHER = GameEventDefinition(
        id = "cooling_weather",
        title = "Cooling Weather",
        description = "Ambient atmospheric cold front relieves datacenter thermal stress.",
        durationSeconds = 60L,
        type = EventType.AMBIENT_POSITIVE,
        heatModifier = 0.60,
        badgeText = "-40% HEAT"
    )

    val MARKET_CRASH = GameEventDefinition(
        id = "market_crash",
        title = "Market Crash",
        description = "Flash liquidation cascade suppresses simulated Bitcoin spot price.",
        durationSeconds = 40L,
        type = EventType.AMBIENT_NEGATIVE,
        badgeText = "-5% PRICE"
    )

    val HEAT_WAVE = GameEventDefinition(
        id = "heat_wave",
        title = "Heat Wave",
        description = "High summer ambient temperatures increase datacenter thermal load.",
        durationSeconds = 45L,
        type = EventType.AMBIENT_NEGATIVE,
        heatModifier = 1.30,
        badgeText = "+30% HEAT"
    )

    val GRID_FAILURE = GameEventDefinition(
        id = "grid_failure",
        title = "Grid Strain",
        description = "Regional utility distribution bottleneck increases power draw overhead.",
        durationSeconds = 30L,
        type = EventType.AMBIENT_NEGATIVE,
        powerModifier = 1.30,
        badgeText = "+30% POWER"
    )

    val DIFFICULTY_SPIKE = GameEventDefinition(
        id = "difficulty_spike",
        title = "Difficulty Spike",
        description = "Global network difficulty adjustment temporarily reduces reward yield.",
        durationSeconds = 45L,
        type = EventType.AMBIENT_NEGATIVE,
        multiplier = 0.80,
        badgeText = "-20% HASH"
    )

    val PERFECT_BLOCK = GameEventDefinition(
        id = "perfect_block",
        title = "Perfect Block",
        description = "High-priority institutional transaction fees detected! Tap to claim USD bounty.",
        durationSeconds = 20L,
        type = EventType.WINDFALL,
        badgeText = "WINDFALL USD"
    )

    val ALL: List<GameEventDefinition> = listOf(
        BULL_RUN,
        CHEAP_ELECTRICITY,
        LUCKY_BLOCK,
        ASIC_BREAKTHROUGH,
        COOLING_WEATHER,
        MARKET_CRASH,
        HEAT_WAVE,
        GRID_FAILURE,
        DIFFICULTY_SPIKE,
        PERFECT_BLOCK
    )

    private val BY_ID: Map<String, GameEventDefinition> = ALL.associateBy { it.id }

    fun getById(id: String): GameEventDefinition? = BY_ID[id]
}
