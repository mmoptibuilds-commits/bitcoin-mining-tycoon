package com.antigravity.bitcoinminingtycoon.content

import com.antigravity.bitcoinminingtycoon.util.GameNumber
import java.math.BigDecimal

data class MinerDefinition(
    val id: String,
    val name: String,
    val description: String,
    val baseCostUsd: BigDecimal,
    val growthRate: Double = 1.15,
    val baseHashrate: BigDecimal,
    val powerDrawKw: Double,
    val heatLoad: Double,
    val unlockLifetimeBtc: BigDecimal = BigDecimal.ZERO
)

object Miners {
    val ALL: List<MinerDefinition> = listOf(
        MinerDefinition(
            id = "ancient_cpu",
            name = "Ancient CPU",
            description = "Salvaged single-core desktop processor running an archaic mining script.",
            baseCostUsd = BigDecimal("10.00"),
            growthRate = 1.12,
            baseHashrate = BigDecimal("1"),
            powerDrawKw = 0.05,
            heatLoad = 0.1,
            unlockLifetimeBtc = BigDecimal.ZERO
        ),
        MinerDefinition(
            id = "gaming_cpu",
            name = "Gaming CPU",
            description = "Overclocked multi-core desktop CPU squeezing out extra SHA-256 rounds.",
            baseCostUsd = BigDecimal("50.00"),
            growthRate = 1.13,
            baseHashrate = BigDecimal("8"),
            powerDrawKw = 0.15,
            heatLoad = 0.3,
            unlockLifetimeBtc = BigDecimal("0.00000100")
        ),
        MinerDefinition(
            id = "gaming_gpu",
            name = "Gaming GPU",
            description = "Mid-range graphics card repurposing stream processors for parallel hashing.",
            baseCostUsd = BigDecimal("250.00"),
            growthRate = 1.14,
            baseHashrate = BigDecimal("50"),
            powerDrawKw = 0.30,
            heatLoad = 0.8,
            unlockLifetimeBtc = BigDecimal("0.00000500")
        ),
        MinerDefinition(
            id = "dual_gpu_rig",
            name = "Dual GPU Rig",
            description = "Custom open-frame chassis housing two tuned graphics cards.",
            baseCostUsd = BigDecimal("1200.00"),
            growthRate = 1.14,
            baseHashrate = BigDecimal("300"),
            powerDrawKw = 0.70,
            heatLoad = 1.8,
            unlockLifetimeBtc = BigDecimal("0.00002500")
        ),
        MinerDefinition(
            id = "gpu_rig_6x",
            name = "6× GPU Rig",
            description = "Dedicated aluminum mining rig with six high-efficiency graphics cards.",
            baseCostUsd = BigDecimal("5000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("1800"),
            powerDrawKw = 2.0,
            heatLoad = 5.0,
            unlockLifetimeBtc = BigDecimal("0.00010000")
        ),
        MinerDefinition(
            id = "entry_asic",
            name = "Entry ASIC",
            description = "First-generation application-specific integrated circuit engineered for SHA-256.",
            baseCostUsd = BigDecimal("20000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("12000"),
            powerDrawKw = 3.5,
            heatLoad = 9.0,
            unlockLifetimeBtc = BigDecimal("0.00050000")
        ),
        MinerDefinition(
            id = "industrial_asic",
            name = "Industrial ASIC",
            description = "Commercial grade ASIC with high-density silicon wafers and dual fans.",
            baseCostUsd = BigDecimal("80000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("80000"),
            powerDrawKw = 7.0,
            heatLoad = 18.0,
            unlockLifetimeBtc = BigDecimal("0.00200000")
        ),
        MinerDefinition(
            id = "asic_rack",
            name = "ASIC Rack",
            description = "Standard 42U data center server rack packed with high-throughput hash boards.",
            baseCostUsd = BigDecimal("350000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("500000"),
            powerDrawKw = 25.0,
            heatLoad = 60.0,
            unlockLifetimeBtc = BigDecimal("0.01000000")
        ),
        MinerDefinition(
            id = "server_room",
            name = "Server Room",
            description = "Dedicated commercial server room with airflow baffles and redundant power.",
            baseCostUsd = BigDecimal("1500000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("3500000"),
            powerDrawKw = 90.0,
            heatLoad = 200.0,
            unlockLifetimeBtc = BigDecimal("0.05000000")
        ),
        MinerDefinition(
            id = "mining_warehouse",
            name = "Mining Warehouse",
            description = "Industrial facility converted into a massive array of computational racks.",
            baseCostUsd = BigDecimal("7000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("25000000"),
            powerDrawKw = 350.0,
            heatLoad = 750.0,
            unlockLifetimeBtc = BigDecimal("0.25000000")
        ),
        MinerDefinition(
            id = "mining_farm",
            name = "Mining Farm",
            description = "Multi-building mega facility situated adjacent to heavy power substations.",
            baseCostUsd = BigDecimal("35000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("180000000"),
            powerDrawKw = 1500.0,
            heatLoad = 3000.0,
            unlockLifetimeBtc = BigDecimal("1.00000000")
        ),
        MinerDefinition(
            id = "hydro_facility",
            name = "Hydro Mining Facility",
            description = "Containerized hash modules drawing direct mechanical hydropower.",
            baseCostUsd = BigDecimal("180000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("1500000000"),
            powerDrawKw = 6000.0,
            heatLoad = 8000.0,
            unlockLifetimeBtc = BigDecimal("5.00000000")
        ),
        MinerDefinition(
            id = "geothermal_complex",
            name = "Geothermal Complex",
            description = "Deep volcanic crust drill rigs tapping superheated subterranean steam.",
            baseCostUsd = BigDecimal("900000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("12000000000"),
            powerDrawKw = 25000.0,
            heatLoad = 25000.0,
            unlockLifetimeBtc = BigDecimal("25.00000000")
        ),
        MinerDefinition(
            id = "nuclear_campus",
            name = "Nuclear Mining Campus",
            description = "Dedicated small modular fission reactors powering an endless sea of compute.",
            baseCostUsd = BigDecimal("5000000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("100000000000"),
            powerDrawKw = 100000.0,
            heatLoad = 90000.0,
            unlockLifetimeBtc = BigDecimal("100.00000000")
        ),
        MinerDefinition(
            id = "immersion_megafarm",
            name = "Immersion Megafarm",
            description = "Millions of custom ASIC blades submerged in dielectric fluid tanks.",
            baseCostUsd = BigDecimal("30000000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("850000000000"),
            powerDrawKw = 400000.0,
            heatLoad = 250000.0,
            unlockLifetimeBtc = BigDecimal("500.00000000")
        ),
        MinerDefinition(
            id = "fusion_complex",
            name = "Fusion Complex",
            description = "Magnetically contained deuterium-tritium plasma reactors fueling continental compute.",
            baseCostUsd = BigDecimal("200000000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("8000000000000"),
            powerDrawKw = 1500000.0,
            heatLoad = 800000.0,
            unlockLifetimeBtc = BigDecimal("2500.00000000")
        ),
        MinerDefinition(
            id = "orbital_solar_miner",
            name = "Orbital Solar Miner",
            description = "Low Earth orbit satellite arrays harvesting unfiltered solar radiation.",
            baseCostUsd = BigDecimal("1500000000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("80000000000000"),
            powerDrawKw = 6000000.0,
            heatLoad = 2500000.0,
            unlockLifetimeBtc = BigDecimal("10000.00000000")
        ),
        MinerDefinition(
            id = "lunar_mining_array",
            name = "Lunar Mining Array",
            description = "Vacuum-cooled lunar regolith data centers taking advantage of -200°C nights.",
            baseCostUsd = BigDecimal("12000000000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("800000000000000"),
            powerDrawKw = 30000000.0,
            heatLoad = 10000000.0,
            unlockLifetimeBtc = BigDecimal("50000.00000000")
        ),
        MinerDefinition(
            id = "quantum_hash_facility",
            name = "Quantum Hash Facility",
            description = "Superconducting qubit arrays computing cryptographic hashes across quantum superposition.",
            baseCostUsd = BigDecimal("100000000000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("10000000000000000"),
            powerDrawKw = 150000000.0,
            heatLoad = 40000000.0,
            unlockLifetimeBtc = BigDecimal("250000.00000000")
        ),
        MinerDefinition(
            id = "dyson_hash_swarm",
            name = "Dyson Hash Swarm",
            description = "A circumstellar swarm of solar collectors encompassing the sun in computational glory.",
            baseCostUsd = BigDecimal("1000000000000000.00"),
            growthRate = 1.15,
            baseHashrate = BigDecimal("150000000000000000"),
            powerDrawKw = 800000000.0,
            heatLoad = 150000000.0,
            unlockLifetimeBtc = BigDecimal("1000000.00000000")
        )
    )

    private val mapById = ALL.associateBy { it.id }
    fun getById(id: String): MinerDefinition? = mapById[id]
}
