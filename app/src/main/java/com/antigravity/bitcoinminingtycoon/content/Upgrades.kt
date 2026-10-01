package com.antigravity.bitcoinminingtycoon.content

import java.math.BigDecimal

enum class UpgradeCategory(val label: String) {
    ALL("All"),
    TAP("Tapping"),
    COMPUTE("Compute"),
    POWER("Power"),
    COOLING("Cooling"),
    AUTOMATION("Automation")
}

enum class UpgradeSpecialEffectType {
    AUTO_SELL_UNLOCK,
    GLOBAL_HASHRATE_BONUS,
    POWER_EFFICIENCY_BONUS,
    OFFLINE_PRODUCTION_BONUS,
    POSITIVE_EVENT_PRODUCTION_BONUS,
    CRITICAL_TAP
}

sealed interface UpgradeSpecialEffect {
    val type: UpgradeSpecialEffectType

    data object AutoSellUnlock : UpgradeSpecialEffect {
        override val type = UpgradeSpecialEffectType.AUTO_SELL_UNLOCK
    }

    data class GlobalHashrateBonus(val multiplier: Double) : UpgradeSpecialEffect {
        override val type = UpgradeSpecialEffectType.GLOBAL_HASHRATE_BONUS
    }

    data class PowerEfficiencyBonus(val multiplier: Double) : UpgradeSpecialEffect {
        override val type = UpgradeSpecialEffectType.POWER_EFFICIENCY_BONUS
    }

    data class OfflineProductionBonus(val multiplier: Double) : UpgradeSpecialEffect {
        override val type = UpgradeSpecialEffectType.OFFLINE_PRODUCTION_BONUS
    }

    data class PositiveEventProductionBonus(val multiplier: Double) : UpgradeSpecialEffect {
        override val type = UpgradeSpecialEffectType.POSITIVE_EVENT_PRODUCTION_BONUS
    }

    data class CriticalTap(val chance: Double, val payoutMultiplier: Double) : UpgradeSpecialEffect {
        override val type = UpgradeSpecialEffectType.CRITICAL_TAP
    }
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
    val unlockLifetimeBtc: BigDecimal = BigDecimal.ZERO,
    val specialEffects: List<UpgradeSpecialEffect> = emptyList()
)

private data class UpgradeCopy(
    val id: String,
    val name: String,
    val description: String,
    val category: UpgradeCategory
)

object Upgrades {
    private val copy = listOf(
        // Tapping.
        UpgradeCopy("copper_fingers", "Copper Fingers", "Conductive copper pads improve each manual hash submission.", UpgradeCategory.TAP),
        UpgradeCopy("mechanical_switches", "Mechanical Switches", "A firm, reliable key action doubles manual hash output.", UpgradeCategory.TAP),
        UpgradeCopy("tactile_keycaps", "PBT Industrial Keycaps", "Textured keycaps make each manual mining pulse more productive.", UpgradeCategory.TAP),
        UpgradeCopy("macro_coprocessor", "Macro Coprocessor", "A local input buffer multiplies the hash work per tap.", UpgradeCategory.TAP),
        UpgradeCopy("optical_trigger", "Optical Trigger", "A low-latency optical switch sharpens every manual pulse.", UpgradeCategory.TAP),
        UpgradeCopy("neural_tap_relay", "Neural Tap Relay", "A fictional direct-control interface multiplies tap output.", UpgradeCategory.TAP),
        UpgradeCopy("precision_actuation", "Precision Actuation", "Calibrated input timing raises manual hash output again.", UpgradeCategory.TAP),
        UpgradeCopy("input_pipeline", "Input Pipeline", "A deeper local work queue improves each deliberate tap.", UpgradeCategory.TAP),
        UpgradeCopy("quantum_fingerprints", "Quantum Fingerprints", "A late-game hash template can produce a rare amplified tap.", UpgradeCategory.TAP),

        // Compute: targeted hardware and fleet-wide compute.
        UpgradeCopy("cpu_overclock_1", "CPU BIOS Voltage Mod", "A safe voltage profile improves Ancient CPU throughput.", UpgradeCategory.COMPUTE),
        UpgradeCopy("cpu_overclock_2", "Multi-Thread Scheduler", "Improved core affinity raises Gaming CPU throughput.", UpgradeCategory.COMPUTE),
        UpgradeCopy("gpu_vram_tuning", "GDDR Timing Straps", "Tighter memory timing improves Gaming GPU throughput.", UpgradeCategory.COMPUTE),
        UpgradeCopy("dual_gpu_crosslink", "High-Bandwidth Interconnect", "A direct link removes a bottleneck in dual GPU rigs.", UpgradeCategory.COMPUTE),
        UpgradeCopy("gpu_6x_custom_os", "Mining Linux Kernel", "A lean local operating system improves 6× GPU rig output.", UpgradeCategory.COMPUTE),
        UpgradeCopy("asic_custom_firmware", "AsicBoost Firmware", "Optimized entry ASIC firmware improves useful hash work.", UpgradeCategory.COMPUTE),
        UpgradeCopy("industrial_asic_tuning", "Silicon Binning Program", "Selected dies improve Industrial ASIC throughput.", UpgradeCategory.COMPUTE),
        UpgradeCopy("asic_rack_backplane", "Copper Busbar Backplane", "A low-loss backplane improves ASIC Rack throughput.", UpgradeCategory.COMPUTE),
        UpgradeCopy("server_room_redundancy", "Redundant PSU Trays", "Stable power delivery improves Server Room output.", UpgradeCategory.COMPUTE),
        UpgradeCopy("warehouse_air_curtains", "Air Containment Corridors", "Separated hot and cold aisles improve Warehouse uptime.", UpgradeCategory.COMPUTE),
        UpgradeCopy("mining_pool_syndicate", "Local Pool Coordinator", "A simulated pool scheduler raises output across the fleet.", UpgradeCategory.COMPUTE),
        UpgradeCopy("custom_sha_fpga", "FPGA SHA-256 Pipeline", "Reprogrammable local logic improves fleet-wide hash work.", UpgradeCategory.COMPUTE),
        UpgradeCopy("optical_computing_coprocessor", "Photonic Computing Waveguides", "Local photonic lanes improve fleet-wide compute.", UpgradeCategory.COMPUTE),
        UpgradeCopy("asic_firmware_2", "Adaptive ASIC Firmware", "A second firmware pass tunes Entry ASIC batch sizes.", UpgradeCategory.COMPUTE),
        UpgradeCopy("rack_thermal_routing", "Rack Thermal Routing", "Balanced board lanes improve ASIC Rack throughput.", UpgradeCategory.COMPUTE),
        UpgradeCopy("warehouse_scheduler", "Warehouse Scheduler", "A facility-wide work queue improves mining warehouse output.", UpgradeCategory.COMPUTE),
        UpgradeCopy("hydro_fluid_cooling", "Hydro Fluid Manifolds", "A local manifold improves compute throughput at the hydro site.", UpgradeCategory.COMPUTE),
        UpgradeCopy("geothermal_hash_mesh", "Geothermal Hash Mesh", "Heat-aware local lanes improve Geothermal Complex output.", UpgradeCategory.COMPUTE),
        UpgradeCopy("nuclear_fpga_farm", "Nuclear FPGA Farm", "Dedicated logic improves Nuclear Campus throughput.", UpgradeCategory.COMPUTE),
        UpgradeCopy("fusion_batcher", "Fusion Batch Controller", "A reactor-synchronized queue improves Fusion Complex output.", UpgradeCategory.COMPUTE),
        UpgradeCopy("orbital_shard_cache", "Orbital Shard Cache", "Distributed local memory improves Orbital Miner output.", UpgradeCategory.COMPUTE),
        UpgradeCopy("lunar_quantum_scheduler", "Lunar Quantum Scheduler", "Cryogenic job scheduling improves Quantum Facility output.", UpgradeCategory.COMPUTE),

        // Power and cooling infrastructure.
        UpgradeCopy("power_undervolting", "Precision Undervolting", "Lower operating voltage reduces fleet power demand.", UpgradeCategory.POWER),
        UpgradeCopy("gold_rated_psus", "80-Plus Gold PSUs", "Efficient local power supplies reduce wasted draw.", UpgradeCategory.POWER),
        UpgradeCopy("titanium_power_dist", "Titanium Power Distribution", "High-voltage conversion reduces line losses.", UpgradeCategory.POWER),
        UpgradeCopy("smart_grid_inverters", "Three-Phase Smart Inverters", "Balanced local circuits lower power demand.", UpgradeCategory.POWER),
        UpgradeCopy("superconducting_busbars", "Superconducting DC Busbars", "Low-resistance distribution reduces fleet draw.", UpgradeCategory.POWER),
        UpgradeCopy("demand_response_grid", "Demand Response Grid", "A local load controller reduces peak grid draw.", UpgradeCategory.POWER),
        UpgradeCopy("solar_tracking_inverters", "Solar Tracking Inverters", "Better local conversion reduces power losses.", UpgradeCategory.POWER),
        UpgradeCopy("fusion_load_balancer", "Fusion Load Balancer", "A distributed bus lowers late-stage power demand.", UpgradeCategory.POWER),
        UpgradeCopy("thermal_paste_upgrade", "Carbon Nanotube Thermal Paste", "Better contact improves cooling dissipation.", UpgradeCategory.COOLING),
        UpgradeCopy("high_static_pressure_fans", "High-Pressure Fans", "Directed airflow improves cooling capacity.", UpgradeCategory.COOLING),
        UpgradeCopy("phase_change_tim", "Phase-Change Interface Pads", "Adaptive interface material improves heat transfer.", UpgradeCategory.COOLING),
        UpgradeCopy("evaporative_water_towers", "Evaporative Cooling Towers", "A recirculating loop improves large-facility cooling.", UpgradeCategory.COOLING),
        UpgradeCopy("microchannel_cold_plates", "Direct-Die Cold Plates", "Microchannels improve heat removal at the die.", UpgradeCategory.COOLING),
        UpgradeCopy("cold_plate_lattice", "Cold Plate Lattice", "A denser local lattice improves cooling dissipation.", UpgradeCategory.COOLING),
        UpgradeCopy("immersion_circulation", "Immersion Circulation", "Balanced dielectric flow improves immersion cooling.", UpgradeCategory.COOLING),
        UpgradeCopy("cryogenic_heat_recovery", "Cryogenic Heat Recovery", "A closed loop increases late-stage cooling capacity.", UpgradeCategory.COOLING),

        // Automation and simulated market tools. These operate entirely offline.
        UpgradeCopy("market_ticker_display", "Simulated Market Ticker", "A local market model improves sale proceeds slightly.", UpgradeCategory.AUTOMATION),
        UpgradeCopy("limit_order_bot", "Local Limit Order Routine", "A deterministic simulated order rule improves sale proceeds.", UpgradeCategory.AUTOMATION),
        UpgradeCopy("institutional_otc_desk", "Simulated Liquidity Desk", "A fictional local routing desk improves simulated proceeds.", UpgradeCategory.AUTOMATION),
        UpgradeCopy("auto_sell_controller", "Auto-Sell Controller", "Unlocks local threshold-based conversion of mined Bitcoin to cash.", UpgradeCategory.AUTOMATION),
        UpgradeCopy("fleet_scheduler", "Fleet Scheduler", "A local work scheduler improves total mining output.", UpgradeCategory.AUTOMATION),
        UpgradeCopy("market_spread_router", "Market Spread Router", "A local sale rule improves simulated sale proceeds.", UpgradeCategory.AUTOMATION),
        UpgradeCopy("energy_aware_dispatch", "Energy-Aware Dispatch", "A local scheduler reduces peak fleet power demand.", UpgradeCategory.AUTOMATION),
        UpgradeCopy("offline_mining_buffer", "Offline Mining Buffer", "A bounded work buffer improves offline production.", UpgradeCategory.AUTOMATION),
        UpgradeCopy("event_response_automation", "Event Response Automation", "A local controller improves positive event production bonuses.", UpgradeCategory.AUTOMATION)
    )

    val ALL: List<UpgradeDefinition> = copy.map { item ->
        val tuning = BalanceConfig.upgrade(item.id)
        UpgradeDefinition(
            id = item.id,
            name = item.name,
            description = item.description,
            category = item.category,
            costUsd = BigDecimal(tuning.costUsd),
            multiplier = tuning.multiplier,
            targetMinerId = tuning.targetMinerId,
            prerequisiteUpgradeId = tuning.prerequisiteUpgradeId,
            unlockLifetimeBtc = BigDecimal(tuning.unlockLifetimeBtc),
            specialEffects = tuning.specialEffects
        )
    }

    private val mapById = ALL.associateBy { it.id }
    fun getById(id: String): UpgradeDefinition? = mapById[id]
}
