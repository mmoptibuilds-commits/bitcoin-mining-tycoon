package com.antigravity.bitcoinminingtycoon.content

import java.math.BigDecimal

enum class UpgradeCategory(val label: String) {
    ALL("All"),
    TAP("Tap"),
    HARDWARE("Hardware"),
    POWER("Power"),
    COOLING("Cooling"),
    MARKET("Market")
}

data class UpgradeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val category: UpgradeCategory,
    val costUsd: BigDecimal,
    val multiplier: Double = 1.0,
    val targetMinerId: String? = null,
    val prerequisiteUpgradeId: String? = null,
    val unlockLifetimeBtc: BigDecimal = BigDecimal.ZERO
)

object Upgrades {

    val ALL: List<UpgradeDefinition> = listOf(
        // === TAP ENHANCEMENTS ===
        UpgradeDefinition(
            id = "copper_fingers",
            name = "Copper Fingers",
            description = "Conductive copper finger pads increase manual hash submission accuracy.",
            category = UpgradeCategory.TAP,
            costUsd = BigDecimal("15.00"),
            multiplier = 2.0 // +100% manual tap strength
        ),
        UpgradeDefinition(
            id = "mechanical_switches",
            name = "Mechanical Switches",
            description = "High-cycle mechanical keyboard switches reduce debounce delay during manual hashing.",
            category = UpgradeCategory.TAP,
            costUsd = BigDecimal("75.00"),
            multiplier = 2.0,
            prerequisiteUpgradeId = "copper_fingers"
        ),
        UpgradeDefinition(
            id = "tactile_keycaps",
            name = "PBT Industrial Keycaps",
            description = "Textured keycaps allow uninterrupted rapid-fire manual hashing.",
            category = UpgradeCategory.TAP,
            costUsd = BigDecimal("350.00"),
            multiplier = 2.0,
            prerequisiteUpgradeId = "mechanical_switches"
        ),
        UpgradeDefinition(
            id = "macro_coprocessor",
            name = "Macro Coprocessor",
            description = "Hardware-level input buffer triples manual hash output per pulse.",
            category = UpgradeCategory.TAP,
            costUsd = BigDecimal("2500.00"),
            multiplier = 3.0,
            prerequisiteUpgradeId = "tactile_keycaps"
        ),
        UpgradeDefinition(
            id = "optical_trigger",
            name = "Optical Laser Trigger",
            description = "Zero-latency optical switch actuation for blisteringly fast hash pulses.",
            category = UpgradeCategory.TAP,
            costUsd = BigDecimal("20000.00"),
            multiplier = 3.0,
            prerequisiteUpgradeId = "macro_coprocessor"
        ),
        UpgradeDefinition(
            id = "neural_tap_relay",
            name = "Neural Tap Relay",
            description = "Direct neural feedback pulse interface multiplies tap power by 5x.",
            category = UpgradeCategory.TAP,
            costUsd = BigDecimal("250000.00"),
            multiplier = 5.0,
            prerequisiteUpgradeId = "optical_trigger"
        ),

        // === HARDWARE FAMILY BOOSTS ===
        UpgradeDefinition(
            id = "cpu_overclock_1",
            name = "CPU BIOS Voltage Mod",
            description = "Slightly overvolts ancient and gaming CPUs, increasing their hash output by 50%.",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("40.00"),
            multiplier = 1.5,
            targetMinerId = "ancient_cpu"
        ),
        UpgradeDefinition(
            id = "cpu_overclock_2",
            name = "Multi-Thread Scheduler",
            description = "Optimizes core affinity for gaming CPUs (+50% hashrate).",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("150.00"),
            multiplier = 1.5,
            targetMinerId = "gaming_cpu"
        ),
        UpgradeDefinition(
            id = "gpu_vram_tuning",
            name = "GDDR Timing Straps",
            description = "Tightens memory timings on gaming GPUs, yielding +35% compute throughput.",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("600.00"),
            multiplier = 1.35,
            targetMinerId = "gaming_gpu"
        ),
        UpgradeDefinition(
            id = "dual_gpu_crosslink",
            name = "High-Bandwidth Interconnect",
            description = "Eliminates PCIe bottleneck between dual GPU rigs (+40% hashrate).",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("2800.00"),
            multiplier = 1.40,
            targetMinerId = "dual_gpu_rig"
        ),
        UpgradeDefinition(
            id = "gpu_6x_custom_os",
            name = "Mining Linux Kernel",
            description = "Stripped-down headless Linux OS maximizes 6x GPU rig uptime (+50% hashrate).",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("12000.00"),
            multiplier = 1.50,
            targetMinerId = "gpu_rig_6x"
        ),
        UpgradeDefinition(
            id = "asic_custom_firmware",
            name = "AsicBoost Firmware",
            description = "Custom firmware exploits SHA-256 mid-state caching on Entry ASICs (+30% hashrate).",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("50000.00"),
            multiplier = 1.30,
            targetMinerId = "entry_asic"
        ),
        UpgradeDefinition(
            id = "industrial_asic_tuning",
            name = "Silicon Binning Program",
            description = "Hand-selects top 5% silicon dies for industrial ASICs (+35% hashrate).",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("220000.00"),
            multiplier = 1.35,
            targetMinerId = "industrial_asic"
        ),
        UpgradeDefinition(
            id = "asic_rack_backplane",
            name = "Copper Busbar Backplane",
            description = "Direct copper power delivery reduces ripple across entire ASIC racks (+40% hashrate).",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("1000000.00"),
            multiplier = 1.40,
            targetMinerId = "asic_rack"
        ),
        UpgradeDefinition(
            id = "server_room_redundancy",
            name = "Redundant PSU Trays",
            description = "Prevents transient voltage drops across Server Rooms (+30% hashrate).",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("5000000.00"),
            multiplier = 1.30,
            targetMinerId = "server_room"
        ),
        UpgradeDefinition(
            id = "warehouse_air_curtains",
            name = "Air Containment Corridors",
            description = "Separates hot and cold aisles inside Mining Warehouses (+35% hashrate).",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("25000000.00"),
            multiplier = 1.35,
            targetMinerId = "mining_warehouse"
        ),

        // === POWER EFFICIENCY ===
        UpgradeDefinition(
            id = "power_undervolting",
            name = "Precision Undervolting",
            description = "Reduces operational core voltages, lowering fleet-wide power draw by 10%.",
            category = UpgradeCategory.POWER,
            costUsd = BigDecimal("180.00"),
            multiplier = 0.90 // 10% less kW draw
        ),
        UpgradeDefinition(
            id = "gold_rated_psus",
            name = "80-Plus Gold PSUs",
            description = "Replaces bargain power supplies with 90% efficient units (-10% power draw).",
            category = UpgradeCategory.POWER,
            costUsd = BigDecimal("1200.00"),
            multiplier = 0.90,
            prerequisiteUpgradeId = "power_undervolting"
        ),
        UpgradeDefinition(
            id = "titanium_power_dist",
            name = "Titanium-Grade Step-Down Transformers",
            description = "High-voltage distribution eliminates line dissipation (-15% power draw).",
            category = UpgradeCategory.POWER,
            costUsd = BigDecimal("15000.00"),
            multiplier = 0.85,
            prerequisiteUpgradeId = "gold_rated_psus"
        ),
        UpgradeDefinition(
            id = "smart_grid_inverters",
            name = "Three-Phase Smart Inverters",
            description = "Automated phase-balancing across industrial circuits (-15% power draw).",
            category = UpgradeCategory.POWER,
            costUsd = BigDecimal("120000.00"),
            multiplier = 0.85,
            prerequisiteUpgradeId = "titanium_power_dist"
        ),
        UpgradeDefinition(
            id = "superconducting_busbars",
            name = "Superconducting DC Busbars",
            description = "Near-zero resistance power distribution throughout data centers (-20% power draw).",
            category = UpgradeCategory.POWER,
            costUsd = BigDecimal("1500000.00"),
            multiplier = 0.80,
            prerequisiteUpgradeId = "smart_grid_inverters"
        ),

        // === COOLING ENHANCEMENTS ===
        UpgradeDefinition(
            id = "thermal_paste_upgrade",
            name = "Carbon-Nanotube Thermal Paste",
            description = "Improves thermal contact between silicon and heatsinks (+25% cooling dissipation).",
            category = UpgradeCategory.COOLING,
            costUsd = BigDecimal("120.00"),
            multiplier = 1.25
        ),
        UpgradeDefinition(
            id = "high_static_pressure_fans",
            name = "High-Static Pressure Fans",
            description = "Pushes airflow through dense heatsink fins (+30% cooling dissipation).",
            category = UpgradeCategory.COOLING,
            costUsd = BigDecimal("850.00"),
            multiplier = 1.30,
            prerequisiteUpgradeId = "thermal_paste_upgrade"
        ),
        UpgradeDefinition(
            id = "phase_change_tim",
            name = "Phase-Change TIM Pads",
            description = "Thermal interface material solidifies at room temp and liquefies under load (+35% dissipation).",
            category = UpgradeCategory.COOLING,
            costUsd = BigDecimal("9500.00"),
            multiplier = 1.35,
            prerequisiteUpgradeId = "high_static_pressure_fans"
        ),
        UpgradeDefinition(
            id = "evaporative_water_towers",
            name = "Evaporative Cooling Towers",
            description = "Industrial evaporative towers cool liquid loops before heat exchangers (+40% dissipation).",
            category = UpgradeCategory.COOLING,
            costUsd = BigDecimal("80000.00"),
            multiplier = 1.40,
            prerequisiteUpgradeId = "phase_change_tim"
        ),
        UpgradeDefinition(
            id = "microchannel_cold_plates",
            name = "Direct-Die Microchannel Plates",
            description = "Micro-machined liquid channels carve directly into silicon substrate (+50% dissipation).",
            category = UpgradeCategory.COOLING,
            costUsd = BigDecimal("900000.00"),
            multiplier = 1.50,
            prerequisiteUpgradeId = "evaporative_water_towers"
        ),

        // === MARKET AUTOMATION & GLOBAL HASHRATE ===
        UpgradeDefinition(
            id = "market_ticker_display",
            name = "Low-Latency Market Terminal",
            description = "Direct WebSocket uplink to regional exchanges increases sell proceeds by 5%.",
            category = UpgradeCategory.MARKET,
            costUsd = BigDecimal("500.00"),
            multiplier = 1.05
        ),
        UpgradeDefinition(
            id = "limit_order_bot",
            name = "Algorithmic Limit Order Engine",
            description = "Executes sales at microsecond market peaks, increasing sell prices by 10%.",
            category = UpgradeCategory.MARKET,
            costUsd = BigDecimal("4500.00"),
            multiplier = 1.10,
            prerequisiteUpgradeId = "market_ticker_display"
        ),
        UpgradeDefinition(
            id = "institutional_otc_desk",
            name = "Institutional OTC Liquidity Desk",
            description = "Bypasses exchange orderbook slippage, permanently increasing sell prices by 15%.",
            category = UpgradeCategory.MARKET,
            costUsd = BigDecimal("65000.00"),
            multiplier = 1.15,
            prerequisiteUpgradeId = "limit_order_bot"
        ),
        UpgradeDefinition(
            id = "mining_pool_syndicate",
            name = "Stratum V2 Protocol Pool",
            description = "Upgrades mining fleet to Stratum V2 protocol, boosting global hashrate by 20%.",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("2500.00"),
            multiplier = 1.20
        ),
        UpgradeDefinition(
            id = "custom_sha_fpga",
            name = "FPGA SHA-256 Pipeline",
            description = "Reprogrammable gate arrays accelerate block header hashing across all miners (+25% global hashrate).",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("35000.00"),
            multiplier = 1.25,
            prerequisiteUpgradeId = "mining_pool_syndicate"
        ),
        UpgradeDefinition(
            id = "optical_computing_coprocessor",
            name = "Photonic Computing Waveguides",
            description = "Light-speed photonic waveguides compute parallel hashes (+35% global hashrate).",
            category = UpgradeCategory.HARDWARE,
            costUsd = BigDecimal("450000.00"),
            multiplier = 1.35,
            prerequisiteUpgradeId = "custom_sha_fpga"
        )
    )

    private val mapById = ALL.associateBy { it.id }
    fun getById(id: String): UpgradeDefinition? = mapById[id]
}
