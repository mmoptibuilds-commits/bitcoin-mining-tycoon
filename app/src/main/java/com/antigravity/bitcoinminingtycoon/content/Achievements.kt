package com.antigravity.bitcoinminingtycoon.content

import com.antigravity.bitcoinminingtycoon.model.GameState
import java.math.BigDecimal

enum class AchievementCategory {
    HASH,
    BTC,
    USD,
    HARDWARE,
    UPGRADES,
    INFRASTRUCTURE,
    EVENTS,
    PRESTIGE
}

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val isSatisfied: (GameState) -> Boolean
)

object Achievements {

    // --- HASH & MANUAL MINING (6) ---
    val FIRST_HASH = AchievementDefinition(
        id = "first_hash",
        title = "Genesis Entropy",
        description = "Execute your first manual cryptographic hash.",
        category = AchievementCategory.HASH,
        isSatisfied = { it.stats.totalManualTaps >= 1L }
    )

    val MANUAL_CENTURY = AchievementDefinition(
        id = "manual_century",
        title = "Manual Labor",
        description = "Perform 100 manual MINE operations.",
        category = AchievementCategory.HASH,
        isSatisfied = { it.stats.totalManualTaps >= 100L }
    )

    val MANUAL_MILLENNIUM = AchievementDefinition(
        id = "manual_millennium",
        title = "Silicon Fingerprints",
        description = "Perform 1,000 manual MINE operations.",
        category = AchievementCategory.HASH,
        isSatisfied = { it.stats.totalManualTaps >= 1000L }
    )

    val MEGAHASH_BARRIER = AchievementDefinition(
        id = "megahash_barrier",
        title = "Megahash Barrier",
        description = "Attain an effective hashrate of 1.00 MH/s (1,000,000 H/s).",
        category = AchievementCategory.HASH,
        isSatisfied = { it.stats.peakHashrateBigDecimal >= BigDecimal("1000000") }
    )

    val GIGAHASH_FRONTIER = AchievementDefinition(
        id = "gigahash_frontier",
        title = "Gigahash Frontier",
        description = "Attain an effective hashrate of 1.00 GH/s.",
        category = AchievementCategory.HASH,
        isSatisfied = { it.stats.peakHashrateBigDecimal >= BigDecimal("1000000000") }
    )

    val TERAHASH_TITAN = AchievementDefinition(
        id = "terahash_titan",
        title = "Terahash Titan",
        description = "Attain an effective hashrate of 1.00 TH/s.",
        category = AchievementCategory.HASH,
        isSatisfied = { it.stats.peakHashrateBigDecimal >= BigDecimal("1000000000000") }
    )

    // --- BITCOIN ACCUMULATION (6) ---
    val FIRST_SATOSHI = AchievementDefinition(
        id = "first_satoshi",
        title = "First Satoshi",
        description = "Mine at least 0.00000001 BTC.",
        category = AchievementCategory.BTC,
        isSatisfied = { it.stats.lifetimeBtcBigDecimal >= BigDecimal("0.00000001") }
    )

    val DECI_COINER = AchievementDefinition(
        id = "deci_coiner",
        title = "Deci-Coiner",
        description = "Mine 0.10000000 BTC lifetime.",
        category = AchievementCategory.BTC,
        isSatisfied = { it.stats.lifetimeBtcBigDecimal >= BigDecimal("0.10000000") }
    )

    val WHOLE_COINER = AchievementDefinition(
        id = "whole_coiner",
        title = "Whole Coiner",
        description = "Hold 1.00000000 BTC in liquid balance simultaneously.",
        category = AchievementCategory.BTC,
        isSatisfied = { it.btcBigDecimal >= BigDecimal("1.00000000") }
    )

    val TEN_COINS = AchievementDefinition(
        id = "ten_coins",
        title = "Ten Club",
        description = "Mine 10.00000000 BTC lifetime.",
        category = AchievementCategory.BTC,
        isSatisfied = { it.stats.lifetimeBtcBigDecimal >= BigDecimal("10.00000000") }
    )

    val HUNDRED_COINS = AchievementDefinition(
        id = "hundred_coins",
        title = "Whale Reserve",
        description = "Mine 100.00000000 BTC lifetime.",
        category = AchievementCategory.BTC,
        isSatisfied = { it.stats.lifetimeBtcBigDecimal >= BigDecimal("100.00000000") }
    )

    val THOUSAND_COINS = AchievementDefinition(
        id = "thousand_coins",
        title = "Satoshi Stash",
        description = "Mine 1,000.00000000 BTC lifetime.",
        category = AchievementCategory.BTC,
        isSatisfied = { it.stats.lifetimeBtcBigDecimal >= BigDecimal("1000.00000000") }
    )

    // --- COMMERCE & MARKETS (5) ---
    val FIRST_LIQUIDATION = AchievementDefinition(
        id = "first_liquidation",
        title = "First Liquidation",
        description = "Execute a market sale of mined Bitcoin.",
        category = AchievementCategory.USD,
        isSatisfied = { it.stats.totalBtcSoldBigDecimal > BigDecimal.ZERO }
    )

    val FIVE_FIGURE_EXIT = AchievementDefinition(
        id = "five_figure_exit",
        title = "Five-Figure Exit",
        description = "Generate $10,000 USD in lifetime revenue.",
        category = AchievementCategory.USD,
        isSatisfied = { it.stats.lifetimeUsdBigDecimal >= BigDecimal("10000") }
    )

    val SIX_FIGURE_TRADE = AchievementDefinition(
        id = "six_figure_trade",
        title = "Six-Figure Balance",
        description = "Generate $100,000 USD in lifetime revenue.",
        category = AchievementCategory.USD,
        isSatisfied = { it.stats.lifetimeUsdBigDecimal >= BigDecimal("100000") }
    )

    val MINING_MOGUL = AchievementDefinition(
        id = "mining_mogul",
        title = "Mining Mogul",
        description = "Generate $1,000,000 USD in lifetime revenue.",
        category = AchievementCategory.USD,
        isSatisfied = { it.stats.lifetimeUsdBigDecimal >= BigDecimal("1000000") }
    )

    val PEAK_MARKET_TIMING = AchievementDefinition(
        id = "peak_market_timing",
        title = "Peak Profit",
        description = "Observe simulated BTC spot price exceed $100,000.",
        category = AchievementCategory.USD,
        isSatisfied = { it.stats.highestPriceBigDecimal >= BigDecimal("100000") }
    )

    // --- HARDWARE & FLEET (6) ---
    val FIRST_RIG = AchievementDefinition(
        id = "first_rig",
        title = "Silicon Dawn",
        description = "Acquire your first automated mining unit.",
        category = AchievementCategory.HARDWARE,
        isSatisfied = { it.stats.totalMinersPurchased >= 1L }
    )

    val TEN_RIGS = AchievementDefinition(
        id = "ten_rigs",
        title = "Farm Foundations",
        description = "Own 10 total mining rigs.",
        category = AchievementCategory.HARDWARE,
        isSatisfied = { it.miners.values.sum() >= 10L }
    )

    val FIFTY_RIGS = AchievementDefinition(
        id = "fifty_rigs",
        title = "Industrial Rack",
        description = "Own 50 total mining rigs.",
        category = AchievementCategory.HARDWARE,
        isSatisfied = { it.miners.values.sum() >= 50L }
    )

    val HUNDRED_RIGS = AchievementDefinition(
        id = "hundred_rigs",
        title = "Server Room",
        description = "Own 100 total mining rigs.",
        category = AchievementCategory.HARDWARE,
        isSatisfied = { it.miners.values.sum() >= 100L }
    )

    val ASIC_VANGUARD = AchievementDefinition(
        id = "asic_vanguard",
        title = "ASIC Vanguard",
        description = "Acquire an Antminer S1 dedicated ASIC unit.",
        category = AchievementCategory.HARDWARE,
        isSatisfied = { (it.miners["antminer_s1"] ?: 0L) >= 1L }
    )

    val MODERN_DENSITY = AchievementDefinition(
        id = "modern_density",
        title = "Modern Density",
        description = "Acquire an Antminer S19 Pro 110 TH/s rig.",
        category = AchievementCategory.HARDWARE,
        isSatisfied = { (it.miners["antminer_s19_pro"] ?: 0L) >= 1L }
    )

    // --- UPGRADES & R&D (5) ---
    val FIRST_UPGRADE = AchievementDefinition(
        id = "first_upgrade",
        title = "R&D Department",
        description = "Purchase your first technology upgrade.",
        category = AchievementCategory.UPGRADES,
        isSatisfied = { it.purchasedUpgrades.isNotEmpty() }
    )

    val FIVE_UPGRADES = AchievementDefinition(
        id = "five_upgrades",
        title = "Lab Expansion",
        description = "Purchase 5 technology upgrades.",
        category = AchievementCategory.UPGRADES,
        isSatisfied = { it.purchasedUpgrades.size >= 5 }
    )

    val FIFTEEN_UPGRADES = AchievementDefinition(
        id = "fifteen_upgrades",
        title = "High-Tech Facility",
        description = "Purchase 15 technology upgrades.",
        category = AchievementCategory.UPGRADES,
        isSatisfied = { it.purchasedUpgrades.size >= 15 }
    )

    val TWENTY_FIVE_UPGRADES = AchievementDefinition(
        id = "twenty_five_upgrades",
        title = "Cutting Edge",
        description = "Purchase 25 technology upgrades.",
        category = AchievementCategory.UPGRADES,
        isSatisfied = { it.purchasedUpgrades.size >= 25 }
    )

    val SINGULARITY = AchievementDefinition(
        id = "singularity",
        title = "Silicon Singularity",
        description = "Unlock 30 technology upgrades.",
        category = AchievementCategory.UPGRADES,
        isSatisfied = { it.purchasedUpgrades.size >= 30 }
    )

    // --- INFRASTRUCTURE (4) ---
    val SUBSTATION_EXPANSION = AchievementDefinition(
        id = "substation_expansion",
        title = "Grid Expansion",
        description = "Upgrade Power Substation to Tier 2 (Commercial Feed).",
        category = AchievementCategory.INFRASTRUCTURE,
        isSatisfied = { it.powerGridTier >= 2 }
    )

    val HIGH_VOLTAGE_FEED = AchievementDefinition(
        id = "high_voltage_feed",
        title = "High-Voltage Feed",
        description = "Upgrade Power Substation to Tier 3 (Industrial Primary).",
        category = AchievementCategory.INFRASTRUCTURE,
        isSatisfied = { it.powerGridTier >= 3 }
    )

    val FORCED_AIR_COOLING = AchievementDefinition(
        id = "forced_air_cooling",
        title = "Forced Air Cooling",
        description = "Upgrade Datacenter Cooling to Tier 2 (Server Fans).",
        category = AchievementCategory.INFRASTRUCTURE,
        isSatisfied = { it.coolingTier >= 2 }
    )

    val INDUSTRIAL_CHILL = AchievementDefinition(
        id = "industrial_chill",
        title = "Industrial Chill",
        description = "Upgrade Datacenter Cooling to Tier 3 (Industrial HVAC).",
        category = AchievementCategory.INFRASTRUCTURE,
        isSatisfied = { it.coolingTier >= 3 }
    )

    // --- EVENTS & DEDICATION (4) ---
    val DYNAMIC_ENVIRONMENT = AchievementDefinition(
        id = "dynamic_environment",
        title = "Operational Resilience",
        description = "Encounter your first random operational event.",
        category = AchievementCategory.EVENTS,
        isSatisfied = { it.stats.totalEventsTriggered >= 1L }
    )

    val TEN_EVENTS = AchievementDefinition(
        id = "ten_events",
        title = "Seasoned Operator",
        description = "Experience 10 operational events.",
        category = AchievementCategory.EVENTS,
        isSatisfied = { it.stats.totalEventsTriggered >= 10L }
    )

    val ACTIVE_OPERATIONS = AchievementDefinition(
        id = "active_operations",
        title = "Uptime Champion",
        description = "Log 10 minutes (600 seconds) of active facility runtime.",
        category = AchievementCategory.EVENTS,
        isSatisfied = { it.stats.totalPlaytimeSeconds >= 600L }
    )

    val DEDICATED_MINER = AchievementDefinition(
        id = "dedicated_miner",
        title = "Industrial Continuity",
        description = "Log 1 hour (3,600 seconds) of active facility runtime.",
        category = AchievementCategory.EVENTS,
        isSatisfied = { it.stats.totalPlaytimeSeconds >= 3600L }
    )

    // --- PRESTIGE & LEGACY (4) ---
    val GENESIS_RESET = AchievementDefinition(
        id = "genesis_reset",
        title = "The Genesis Reset",
        description = "Execute your first Satoshi Prestige reset.",
        category = AchievementCategory.PRESTIGE,
        isSatisfied = { it.stats.totalPrestiges >= 1L }
    )

    val SATOSHI_INITIATE = AchievementDefinition(
        id = "satoshi_initiate",
        title = "Satoshi Initiate",
        description = "Accumulate 10 permanent Satoshi Points.",
        category = AchievementCategory.PRESTIGE,
        isSatisfied = { it.stats.lifetimeSatoshiPointsEarned >= 10L }
    )

    val LEGACY_ARCHITECT = AchievementDefinition(
        id = "legacy_architect",
        title = "Legacy Architect",
        description = "Accumulate 100 permanent Satoshi Points.",
        category = AchievementCategory.PRESTIGE,
        isSatisfied = { it.stats.lifetimeSatoshiPointsEarned >= 100L }
    )

    val QUANTUM_ASCENSION = AchievementDefinition(
        id = "quantum_ascension",
        title = "Quantum Ascension",
        description = "Accumulate 1,000 permanent Satoshi Points.",
        category = AchievementCategory.PRESTIGE,
        isSatisfied = { it.stats.lifetimeSatoshiPointsEarned >= 1000L }
    )

    val ALL: List<AchievementDefinition> = listOf(
        // Hash (6)
        FIRST_HASH, MANUAL_CENTURY, MANUAL_MILLENNIUM, MEGAHASH_BARRIER, GIGAHASH_FRONTIER, TERAHASH_TITAN,
        // BTC (6)
        FIRST_SATOSHI, DECI_COINER, WHOLE_COINER, TEN_COINS, HUNDRED_COINS, THOUSAND_COINS,
        // USD (5)
        FIRST_LIQUIDATION, FIVE_FIGURE_EXIT, SIX_FIGURE_TRADE, MINING_MOGUL, PEAK_MARKET_TIMING,
        // Hardware (6)
        FIRST_RIG, TEN_RIGS, FIFTY_RIGS, HUNDRED_RIGS, ASIC_VANGUARD, MODERN_DENSITY,
        // Upgrades (5)
        FIRST_UPGRADE, FIVE_UPGRADES, FIFTEEN_UPGRADES, TWENTY_FIVE_UPGRADES, SINGULARITY,
        // Infrastructure (4)
        SUBSTATION_EXPANSION, HIGH_VOLTAGE_FEED, FORCED_AIR_COOLING, INDUSTRIAL_CHILL,
        // Events & Dedication (4)
        DYNAMIC_ENVIRONMENT, TEN_EVENTS, ACTIVE_OPERATIONS, DEDICATED_MINER,
        // Prestige (4)
        GENESIS_RESET, SATOSHI_INITIATE, LEGACY_ARCHITECT, QUANTUM_ASCENSION
    )

    private val BY_ID: Map<String, AchievementDefinition> = ALL.associateBy { it.id }

    fun getById(id: String): AchievementDefinition? = BY_ID[id]
}
