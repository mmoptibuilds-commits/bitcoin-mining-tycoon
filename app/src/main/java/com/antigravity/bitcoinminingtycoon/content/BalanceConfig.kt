package com.antigravity.bitcoinminingtycoon.content

import com.antigravity.bitcoinminingtycoon.model.MarketTrend
import java.math.BigDecimal
import java.security.MessageDigest

data class MinerTuning(
    val baseCostUsd: String,
    val growthRate: Double,
    val baseHashrate: String,
    val powerDrawKw: Double,
    val heatLoad: Double,
    val unlockLifetimeBtc: String = "0"
)

data class UpgradeTuning(
    val costUsd: String,
    val multiplier: Double = 1.0,
    val targetMinerId: String? = null,
    val prerequisiteUpgradeId: String? = null,
    val unlockLifetimeBtc: String = "0",
    val specialEffects: List<UpgradeSpecialEffect> = emptyList()
)

data class InfrastructureTuning(
    val magnitude: Double,
    val costUsd: String
)

data class MarketTrendTuning(
    val transitionWeights: Map<MarketTrend, Double>,
    val deltaMin: Double,
    val deltaMax: Double
)

/** Numeric design inputs shared by game content, engines, and reproducible balance reports. */
object BalanceConfig {
    const val BALANCE_RULES_VERSION = 2
    const val INITIAL_MANUAL_HASHRATE = "50000"
    const val BTC_PER_HASH = "0.00000000005"
    const val MARKET_MIN_USD = "1000.00"
    const val MARKET_MAX_USD = "1000000.00"
    const val MARKET_INITIAL_USD = "50000.00"
    const val MARKET_HISTORY_INITIAL_USD = "50000"
    const val MARKET_INITIAL_STATS_USD = "50000"
    const val MARKET_TICK_SECONDS = 2.0
    const val MARKET_BULL_EVENT_DELTA = 0.03
    const val MARKET_CRASH_EVENT_DELTA = -0.05
    const val MAX_OFFLINE_SECONDS = 43_200.0
    const val DAILY_CLAIM_COOLDOWN_HOURS = 20L
    const val DAY_ONE_CASH_USD = "3.00"
    const val DAILY_REWARD_TARGET_SECONDS = 45.0
    const val DAILY_REWARD_BTC_CAP_STAGE_ZERO = "0.001"
    const val DAILY_REWARD_USD_CAP_STAGE_ZERO = "50.00"
    const val PRESTIGE_LIFETIME_BTC_EXPONENT = 0.5
    const val PRESTIGE_POINT_HASHRATE_BONUS = 0.01
    const val PRESTIGE_POINT_HASHRATE_BONUS_WITH_VISION = 0.05
    const val EFFICIENT_SILICON_HASHRATE_MULTIPLIER = 5.0
    const val SATOSHI_VISION_HASHRATE_MULTIPLIER = 0.05
    const val QUANTUM_LEGACY_HASHRATE_MULTIPLIER = 3.0
    const val MARKET_INITIAL_TIMER_SECONDS = 0.0
    const val EVENT_INITIAL_TIMER_SECONDS = 120.0
    const val EVENT_MIN_INTERVAL_SECONDS = 120.0
    const val EVENT_MAX_INTERVAL_SECONDS = 240.0
    const val EVENT_POSITIVE_WEIGHT = 0.50
    const val EVENT_NEGATIVE_WEIGHT = 0.25
    const val POWER_SAMPLE_INTERVAL_SECONDS = 300L
    const val POWER_HISTORY_CAPACITY = 288
    const val AUTO_SELL_INITIAL_THRESHOLD_USD = "60000"
    const val AMBIENT_TEMPERATURE_C = 25.0
    const val THERMAL_EXCESS_TEMP_PER_HEAT = 0.5
    const val THERMAL_FULL_OUTPUT_BELOW_C = 70.0
    const val THERMAL_FIRST_BAND_END_C = 80.0
    const val THERMAL_SECOND_BAND_END_C = 90.0
    const val THERMAL_MIN_OUTPUT_FACTOR = 0.50
    const val THERMAL_FIRST_BAND_DROP = 0.10
    const val THERMAL_SECOND_BAND_DROP = 0.15
    const val THERMAL_HIGH_BAND_DROP = 0.25
    const val THERMAL_STANDARD_BAND_WIDTH_C = 10.0
    const val THERMAL_HIGH_BAND_WIDTH_C = 20.0
    const val THERMAL_SECOND_BAND_FACTOR = 0.90
    const val THERMAL_HIGH_BAND_FACTOR = 0.75
    const val LUCKY_BLOCK_MIN_BTC = "0.00010000"
    const val LUCKY_BLOCK_PRODUCTION_SECONDS = 180.0
    const val PERFECT_BLOCK_MIN_USD = "250.00"
    const val PERFECT_BLOCK_PRODUCTION_SECONDS = 120.0
    const val REFERENCE_FIRST_SALE_MIN_SECONDS = 30L
    const val REFERENCE_FIRST_SALE_MAX_SECONDS = 90L
    const val REFERENCE_GPU_MIN_SECONDS = 180L
    const val REFERENCE_GPU_MAX_SECONDS = 360L
    const val REFERENCE_INFRA_MIN_SECONDS = 420L
    const val REFERENCE_INFRA_MAX_SECONDS = 720L
    const val REFERENCE_ASIC_MIN_SECONDS = 720L
    const val REFERENCE_ASIC_MAX_SECONDS = 1_200L
    const val REFERENCE_PRESTIGE_MIN_SECONDS = 1_500L
    const val REFERENCE_PRESTIGE_MAX_SECONDS = 2_100L
    const val REFERENCE_NEXT_RUN_MAX_RATIO = 0.75

    val INITIAL_CASH_USD: BigDecimal = BigDecimal.ZERO
    val INITIAL_MARKET_PRICE_USD: BigDecimal = BigDecimal(MARKET_INITIAL_USD)

    val MARKET_TRENDS: Map<MarketTrend, MarketTrendTuning> = linkedMapOf(
        MarketTrend.NEUTRAL to MarketTrendTuning(linkedMapOf(
            MarketTrend.NEUTRAL to 0.70, MarketTrend.BULL to 0.13, MarketTrend.BEAR to 0.13,
            MarketTrend.VOLATILE to 0.02, MarketTrend.PUMP to 0.01, MarketTrend.CRASH to 0.01
        ), -0.01, 0.01),
        MarketTrend.BULL to MarketTrendTuning(linkedMapOf(
            MarketTrend.BULL to 0.72, MarketTrend.NEUTRAL to 0.18, MarketTrend.VOLATILE to 0.05,
            MarketTrend.PUMP to 0.03, MarketTrend.BEAR to 0.02
        ), 0.005, 0.03),
        MarketTrend.BEAR to MarketTrendTuning(linkedMapOf(
            MarketTrend.BEAR to 0.72, MarketTrend.NEUTRAL to 0.18, MarketTrend.VOLATILE to 0.05,
            MarketTrend.CRASH to 0.03, MarketTrend.BULL to 0.02
        ), -0.03, -0.005),
        MarketTrend.VOLATILE to MarketTrendTuning(linkedMapOf(
            MarketTrend.VOLATILE to 0.55, MarketTrend.NEUTRAL to 0.20, MarketTrend.BULL to 0.10,
            MarketTrend.BEAR to 0.10, MarketTrend.PUMP to 0.025, MarketTrend.CRASH to 0.025
        ), -0.04, 0.04),
        MarketTrend.CRASH to MarketTrendTuning(linkedMapOf(
            MarketTrend.CRASH to 0.40, MarketTrend.BEAR to 0.30, MarketTrend.VOLATILE to 0.20,
            MarketTrend.NEUTRAL to 0.10
        ), -0.10, -0.04),
        MarketTrend.PUMP to MarketTrendTuning(linkedMapOf(
            MarketTrend.PUMP to 0.40, MarketTrend.BULL to 0.30, MarketTrend.VOLATILE to 0.20,
            MarketTrend.NEUTRAL to 0.10
        ), 0.04, 0.10)
    )

    val MINER_TUNING: Map<String, MinerTuning> = linkedMapOf(
        "ancient_cpu" to MinerTuning("10.00", 1.12, "50000", 0.02, 0.1),
        "gaming_cpu" to MinerTuning("35.00", 1.13, "50000", 0.05, 0.3, "0.00001000"),
        "gaming_gpu" to MinerTuning("115.00", 1.14, "300000", 0.30, 0.8, "0.00003000"),
        "dual_gpu_rig" to MinerTuning("450.00", 1.14, "600000", 0.30, 1.8, "0.01000000"),
        "gpu_rig_6x" to MinerTuning("2500.00", 1.15, "1800000", 0.75, 5.0, "0.05000000"),
        "entry_asic" to MinerTuning("2200.00", 1.15, "20000000", 1.5, 90.0, "0.02000000"),
        "industrial_asic" to MinerTuning("25000.00", 1.15, "130000000", 7.0, 180.0, "0.01000000"),
        "asic_rack" to MinerTuning("140000.00", 1.15, "900000000", 25.0, 600.0, "0.05000000"),
        "server_room" to MinerTuning("800000.00", 1.15, "6000000000", 90.0, 2000.0, "0.25000000"),
        "mining_warehouse" to MinerTuning("5000000.00", 1.15, "40000000000", 350.0, 7500.0, "1.00000000"),
        "mining_farm" to MinerTuning("30000000.00", 1.15, "300000000000", 1500.0, 30000.0, "5.00000000"),
        "hydro_facility" to MinerTuning("180000000.00", 1.15, "2500000000000", 6000.0, 80000.0, "25.00000000"),
        "geothermal_complex" to MinerTuning("1200000000.00", 1.15, "20000000000000", 25000.0, 250000.0, "100.00000000"),
        "nuclear_campus" to MinerTuning("8000000000.00", 1.15, "150000000000000", 100000.0, 900000.0, "500.00000000"),
        "immersion_megafarm" to MinerTuning("60000000000.00", 1.15, "1200000000000000", 400000.0, 2500000.0, "2500.00000000"),
        "fusion_complex" to MinerTuning("450000000000.00", 1.15, "10000000000000000", 1500000.0, 8000000.0, "10000.00000000"),
        "orbital_solar_miner" to MinerTuning("3500000000000.00", 1.15, "90000000000000000", 6000000.0, 25000000.0, "50000.00000000"),
        "lunar_mining_array" to MinerTuning("28000000000000.00", 1.15, "900000000000000000", 30000000.0, 100000000.0, "250000.00000000"),
        "quantum_hash_facility" to MinerTuning("250000000000000.00", 1.15, "12000000000000000000", 150000000.0, 400000000.0, "1000000.00000000"),
        "dyson_hash_swarm" to MinerTuning("2500000000000000.00", 1.15, "150000000000000000000", 800000000.0, 1500000000.0, "5000000.00000000")
    )

    val POWER_STAGES: List<InfrastructureTuning> = listOf(
        InfrastructureTuning(0.5, "0"),
        InfrastructureTuning(10.0, "80.00"),
        InfrastructureTuning(100.0, "1200.00"),
        InfrastructureTuning(1000.0, "12000.00"),
        InfrastructureTuning(10000.0, "150000.00"),
        InfrastructureTuning(100000.0, "2500000.00"),
        InfrastructureTuning(1000000.0, "50000000.00"),
        InfrastructureTuning(10000000.0, "1200000000.00"),
        InfrastructureTuning(100000000.0, "35000000000.00"),
        InfrastructureTuning(1000000000.0, "2500000000000.00")
    )

    val COOLING_STAGES: List<InfrastructureTuning> = listOf(
        InfrastructureTuning(0.5, "0"),
        InfrastructureTuning(5.0, "120.00"),
        InfrastructureTuning(50.0, "1200.00"),
        InfrastructureTuning(500.0, "12000.00"),
        InfrastructureTuning(5000.0, "180000.00"),
        InfrastructureTuning(50000.0, "2000000.00"),
        InfrastructureTuning(500000.0, "20000000.00")
    )

    val UPGRADE_TUNING: Map<String, UpgradeTuning> = linkedMapOf(
        // Tapping — legacy six plus three new steps.
        "copper_fingers" to UpgradeTuning("15.00", 2.0),
        "mechanical_switches" to UpgradeTuning("75.00", 2.0, prerequisiteUpgradeId = "copper_fingers"),
        "tactile_keycaps" to UpgradeTuning("350.00", 2.0, prerequisiteUpgradeId = "mechanical_switches"),
        "macro_coprocessor" to UpgradeTuning("2500.00", 3.0, prerequisiteUpgradeId = "tactile_keycaps"),
        "optical_trigger" to UpgradeTuning("20000.00", 3.0, prerequisiteUpgradeId = "macro_coprocessor"),
        "neural_tap_relay" to UpgradeTuning("250000.00", 5.0, prerequisiteUpgradeId = "optical_trigger"),
        "precision_actuation" to UpgradeTuning("1000000.00", 2.0, prerequisiteUpgradeId = "neural_tap_relay"),
        "input_pipeline" to UpgradeTuning("5000000.00", 2.0, prerequisiteUpgradeId = "precision_actuation"),
        "quantum_fingerprints" to UpgradeTuning(
            "20000000.00", prerequisiteUpgradeId = "input_pipeline",
            specialEffects = listOf(UpgradeSpecialEffect.CriticalTap(chance = 0.02, payoutMultiplier = 2.0))
        ),

        // Compute — the 13 existing stable upgrades and nine additions.
        "cpu_overclock_1" to UpgradeTuning("20.00", 1.5, targetMinerId = "ancient_cpu"),
        "cpu_overclock_2" to UpgradeTuning("55.00", 1.5, targetMinerId = "gaming_cpu"),
        "gpu_vram_tuning" to UpgradeTuning("200.00", 1.35, targetMinerId = "gaming_gpu"),
        "dual_gpu_crosslink" to UpgradeTuning("900.00", 1.40, targetMinerId = "dual_gpu_rig"),
        "gpu_6x_custom_os" to UpgradeTuning("4500.00", 1.50, targetMinerId = "gpu_rig_6x"),
        "asic_custom_firmware" to UpgradeTuning("25000.00", 1.30, targetMinerId = "entry_asic"),
        "industrial_asic_tuning" to UpgradeTuning("120000.00", 1.35, targetMinerId = "industrial_asic"),
        "asic_rack_backplane" to UpgradeTuning("700000.00", 1.40, targetMinerId = "asic_rack"),
        "server_room_redundancy" to UpgradeTuning("4000000.00", 1.30, targetMinerId = "server_room"),
        "warehouse_air_curtains" to UpgradeTuning("20000000.00", 1.35, targetMinerId = "mining_warehouse"),
        "mining_pool_syndicate" to UpgradeTuning("350.00", 1.20),
        "custom_sha_fpga" to UpgradeTuning("10000.00", 1.25, prerequisiteUpgradeId = "mining_pool_syndicate"),
        "optical_computing_coprocessor" to UpgradeTuning("150000.00", 1.35, prerequisiteUpgradeId = "custom_sha_fpga"),
        "asic_firmware_2" to UpgradeTuning("75000.00", 1.40, targetMinerId = "entry_asic", prerequisiteUpgradeId = "asic_custom_firmware"),
        "rack_thermal_routing" to UpgradeTuning("1500000.00", 1.35, targetMinerId = "asic_rack", prerequisiteUpgradeId = "asic_rack_backplane"),
        "warehouse_scheduler" to UpgradeTuning("6000000.00", 1.30, targetMinerId = "mining_warehouse", prerequisiteUpgradeId = "warehouse_air_curtains"),
        "hydro_fluid_cooling" to UpgradeTuning("70000000.00", 1.40, targetMinerId = "hydro_facility", prerequisiteUpgradeId = "warehouse_scheduler"),
        "geothermal_hash_mesh" to UpgradeTuning("350000000.00", 1.40, targetMinerId = "geothermal_complex", prerequisiteUpgradeId = "hydro_fluid_cooling"),
        "nuclear_fpga_farm" to UpgradeTuning("4000000000.00", 1.40, targetMinerId = "nuclear_campus", prerequisiteUpgradeId = "geothermal_hash_mesh"),
        "fusion_batcher" to UpgradeTuning("30000000000.00", 1.40, targetMinerId = "fusion_complex", prerequisiteUpgradeId = "nuclear_fpga_farm"),
        "orbital_shard_cache" to UpgradeTuning("200000000000.00", 1.50, targetMinerId = "orbital_solar_miner", prerequisiteUpgradeId = "fusion_batcher"),
        "lunar_quantum_scheduler" to UpgradeTuning("1000000000000.00", 1.50, targetMinerId = "quantum_hash_facility", prerequisiteUpgradeId = "orbital_shard_cache"),

        // Infrastructure — power and cooling have separate, deterministic modifiers.
        "power_undervolting" to UpgradeTuning("180.00", 0.90),
        "gold_rated_psus" to UpgradeTuning("1200.00", 0.90, prerequisiteUpgradeId = "power_undervolting"),
        "titanium_power_dist" to UpgradeTuning("15000.00", 0.85, prerequisiteUpgradeId = "gold_rated_psus"),
        "smart_grid_inverters" to UpgradeTuning("120000.00", 0.85, prerequisiteUpgradeId = "titanium_power_dist"),
        "superconducting_busbars" to UpgradeTuning("1500000.00", 0.80, prerequisiteUpgradeId = "smart_grid_inverters"),
        "demand_response_grid" to UpgradeTuning("12000000.00", 0.90, prerequisiteUpgradeId = "superconducting_busbars"),
        "solar_tracking_inverters" to UpgradeTuning("150000000.00", 0.90, prerequisiteUpgradeId = "demand_response_grid"),
        "fusion_load_balancer" to UpgradeTuning("2500000000.00", 0.85, prerequisiteUpgradeId = "solar_tracking_inverters"),
        "thermal_paste_upgrade" to UpgradeTuning("120.00", 1.25),
        "high_static_pressure_fans" to UpgradeTuning("850.00", 1.30, prerequisiteUpgradeId = "thermal_paste_upgrade"),
        "phase_change_tim" to UpgradeTuning("9500.00", 1.35, prerequisiteUpgradeId = "high_static_pressure_fans"),
        "evaporative_water_towers" to UpgradeTuning("80000.00", 1.40, prerequisiteUpgradeId = "phase_change_tim"),
        "microchannel_cold_plates" to UpgradeTuning("900000.00", 1.50, prerequisiteUpgradeId = "evaporative_water_towers"),
        "cold_plate_lattice" to UpgradeTuning("12000000.00", 1.45, prerequisiteUpgradeId = "microchannel_cold_plates"),
        "immersion_circulation" to UpgradeTuning("180000000.00", 1.50, prerequisiteUpgradeId = "cold_plate_lattice"),
        "cryogenic_heat_recovery" to UpgradeTuning("2500000000.00", 1.50, prerequisiteUpgradeId = "immersion_circulation"),

        // Offline-only automation; no network feed or exchange connection is implied.
        "market_ticker_display" to UpgradeTuning("500.00", 1.05),
        "limit_order_bot" to UpgradeTuning("4500.00", 1.10, prerequisiteUpgradeId = "market_ticker_display"),
        "institutional_otc_desk" to UpgradeTuning("65000.00", 1.15, prerequisiteUpgradeId = "limit_order_bot"),
        "auto_sell_controller" to UpgradeTuning(
            "250.00", specialEffects = listOf(UpgradeSpecialEffect.AutoSellUnlock)
        ),
        "fleet_scheduler" to UpgradeTuning(
            "25000.00", prerequisiteUpgradeId = "auto_sell_controller",
            specialEffects = listOf(UpgradeSpecialEffect.GlobalHashrateBonus(1.15))
        ),
        "market_spread_router" to UpgradeTuning("250000.00", 1.15, prerequisiteUpgradeId = "institutional_otc_desk"),
        "energy_aware_dispatch" to UpgradeTuning(
            "50000.00", prerequisiteUpgradeId = "power_undervolting",
            specialEffects = listOf(UpgradeSpecialEffect.PowerEfficiencyBonus(0.85))
        ),
        "offline_mining_buffer" to UpgradeTuning(
            "250000.00", prerequisiteUpgradeId = "fleet_scheduler",
            specialEffects = listOf(UpgradeSpecialEffect.OfflineProductionBonus(1.25))
        ),
        "event_response_automation" to UpgradeTuning(
            "1500000.00", prerequisiteUpgradeId = "market_spread_router",
            specialEffects = listOf(UpgradeSpecialEffect.PositiveEventProductionBonus(1.25))
        )
    )

    val DAILY_REWARD_BTC_CAPS: List<String> = listOf(
        "0.001", "0.005", "0.025", "0.125", "0.625", "3.125", "15.625", "78.125", "390.625", "1953.125"
    )
    val DAILY_REWARD_USD_CAPS: List<String> = listOf(
        "50", "250", "1250", "6250", "31250", "156250", "781250", "3906250", "19531250", "97656250"
    )

    fun miner(id: String): MinerTuning = MINER_TUNING.getValue(id)
    fun upgrade(id: String): UpgradeTuning = UPGRADE_TUNING.getValue(id)

    fun stableHash(): String {
        val canonical = buildString {
            append("rules=$BALANCE_RULES_VERSION|cash=$INITIAL_CASH_USD|manual=$INITIAL_MANUAL_HASHRATE|")
            append("btcPerHash=$BTC_PER_HASH|market=$MARKET_MIN_USD,$MARKET_MAX_USD,$MARKET_INITIAL_USD,$MARKET_HISTORY_INITIAL_USD,")
            append("$MARKET_INITIAL_STATS_USD,$MARKET_TICK_SECONDS|")
            append("marketEvents=$MARKET_BULL_EVENT_DELTA,$MARKET_CRASH_EVENT_DELTA|")
            append("offline=$MAX_OFFLINE_SECONDS|daily=$DAILY_CLAIM_COOLDOWN_HOURS,$DAY_ONE_CASH_USD,$DAILY_REWARD_TARGET_SECONDS|")
            append("prestige=$PRESTIGE_LIFETIME_BTC_EXPONENT,$PRESTIGE_POINT_HASHRATE_BONUS,$PRESTIGE_POINT_HASHRATE_BONUS_WITH_VISION,")
            append("$EFFICIENT_SILICON_HASHRATE_MULTIPLIER,$SATOSHI_VISION_HASHRATE_MULTIPLIER,$QUANTUM_LEGACY_HASHRATE_MULTIPLIER|")
            append("events=$EVENT_INITIAL_TIMER_SECONDS,$EVENT_MIN_INTERVAL_SECONDS,$EVENT_MAX_INTERVAL_SECONDS,")
            append("$EVENT_POSITIVE_WEIGHT,$EVENT_NEGATIVE_WEIGHT|")
            append("reference=$REFERENCE_FIRST_SALE_MIN_SECONDS,$REFERENCE_FIRST_SALE_MAX_SECONDS,")
            append("$REFERENCE_GPU_MIN_SECONDS,$REFERENCE_GPU_MAX_SECONDS,$REFERENCE_INFRA_MIN_SECONDS,$REFERENCE_INFRA_MAX_SECONDS,")
            append("$REFERENCE_ASIC_MIN_SECONDS,$REFERENCE_ASIC_MAX_SECONDS,$REFERENCE_PRESTIGE_MIN_SECONDS,$REFERENCE_PRESTIGE_MAX_SECONDS,")
            append("$REFERENCE_NEXT_RUN_MAX_RATIO|")
            append("powerHistory=$POWER_SAMPLE_INTERVAL_SECONDS,$POWER_HISTORY_CAPACITY|")
            append("autoSellThreshold=$AUTO_SELL_INITIAL_THRESHOLD_USD|thermal=$AMBIENT_TEMPERATURE_C,$THERMAL_EXCESS_TEMP_PER_HEAT,")
            append("$THERMAL_FULL_OUTPUT_BELOW_C,$THERMAL_FIRST_BAND_END_C,$THERMAL_SECOND_BAND_END_C,$THERMAL_MIN_OUTPUT_FACTOR,")
            append("$THERMAL_FIRST_BAND_DROP,$THERMAL_SECOND_BAND_DROP,$THERMAL_HIGH_BAND_DROP,")
            append("$THERMAL_STANDARD_BAND_WIDTH_C,$THERMAL_HIGH_BAND_WIDTH_C,$THERMAL_SECOND_BAND_FACTOR,$THERMAL_HIGH_BAND_FACTOR|")
            append("windfalls=$LUCKY_BLOCK_MIN_BTC,$LUCKY_BLOCK_PRODUCTION_SECONDS,$PERFECT_BLOCK_MIN_USD,$PERFECT_BLOCK_PRODUCTION_SECONDS|")
            MARKET_TRENDS.toSortedMap(compareBy { it.ordinal }).forEach { (trend, tuning) ->
                append("marketTrend:$trend=${tuning.deltaMin},${tuning.deltaMax}:")
                tuning.transitionWeights.forEach { (next, weight) -> append("$next=$weight,") }
                append('|')
            }
            MINER_TUNING.toSortedMap().forEach { (id, tuning) -> append("miner:$id=$tuning|") }
            UPGRADE_TUNING.toSortedMap().forEach { (id, tuning) -> append("upgrade:$id=$tuning|") }
            POWER_STAGES.forEachIndexed { index, tuning -> append("power:${index + 1}=$tuning|") }
            COOLING_STAGES.forEachIndexed { index, tuning -> append("cooling:${index + 1}=$tuning|") }
            DAILY_REWARD_BTC_CAPS.forEachIndexed { index, cap -> append("dailyBtcCap:$index=$cap|") }
            DAILY_REWARD_USD_CAPS.forEachIndexed { index, cap -> append("dailyUsdCap:$index=$cap|") }
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
    }
}
