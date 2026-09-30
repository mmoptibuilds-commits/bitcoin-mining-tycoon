package com.antigravity.bitcoinminingtycoon.content

import java.math.BigDecimal

data class MinerDefinition(
    val id: String,
    val name: String,
    val description: String,
    val baseCostUsd: BigDecimal,
    val growthRate: Double,
    val baseHashrate: BigDecimal,
    val powerDrawKw: Double,
    val heatLoad: Double,
    val unlockLifetimeBtc: BigDecimal
)

private data class MinerCopy(val id: String, val name: String, val description: String)

object Miners {
    private val copy = listOf(
        MinerCopy("ancient_cpu", "Ancient CPU", "A salvaged single-core desktop processor running a small mining script."),
        MinerCopy("gaming_cpu", "Gaming CPU", "An overclocked multi-core desktop processor handling more hash work."),
        MinerCopy("gaming_gpu", "Gaming GPU", "A graphics card repurposed for parallel mining."),
        MinerCopy("dual_gpu_rig", "Dual GPU Rig", "A custom open-frame chassis with two linked graphics cards."),
        MinerCopy("gpu_rig_6x", "6× GPU Rig", "A dedicated open-frame rig housing six tuned graphics cards."),
        MinerCopy("entry_asic", "Entry ASIC", "A first-generation application-specific chip built for SHA-256."),
        MinerCopy("industrial_asic", "Industrial ASIC", "Commercial-grade ASIC hardware with high-density hash boards."),
        MinerCopy("asic_rack", "ASIC Rack", "A standard data-center rack packed with hash boards."),
        MinerCopy("server_room", "Server Room", "A dedicated compute room with managed airflow and redundant power."),
        MinerCopy("mining_warehouse", "Mining Warehouse", "An industrial facility filled with organized mining racks."),
        MinerCopy("mining_farm", "Mining Farm", "A multi-building mining site beside heavy power infrastructure."),
        MinerCopy("hydro_facility", "Hydro Mining Facility", "Containerized hash modules beside a hydropower source."),
        MinerCopy("geothermal_complex", "Geothermal Complex", "Deep geothermal wells powering a large compute complex."),
        MinerCopy("nuclear_campus", "Nuclear Mining Campus", "A dedicated small-reactor campus supporting large-scale compute."),
        MinerCopy("immersion_megafarm", "Immersion Megafarm", "A broad array of hash blades submerged in dielectric cooling fluid."),
        MinerCopy("fusion_complex", "Fusion Complex", "A fictional fusion plant powering a continental compute site."),
        MinerCopy("orbital_solar_miner", "Orbital Solar Miner", "Satellite arrays harvesting uninterrupted sunlight in orbit."),
        MinerCopy("lunar_mining_array", "Lunar Mining Array", "Vacuum-cooled lunar data centers built beside regolith works."),
        MinerCopy("quantum_hash_facility", "Quantum Hash Facility", "A fictional superconducting compute array in a cryogenic base."),
        MinerCopy("dyson_hash_swarm", "Dyson Hash Swarm", "A science-fiction swarm of solar collectors around a star.")
    )

    val ALL: List<MinerDefinition> = copy.map { item ->
        val tuning = BalanceConfig.miner(item.id)
        MinerDefinition(
            id = item.id,
            name = item.name,
            description = item.description,
            baseCostUsd = BigDecimal(tuning.baseCostUsd),
            growthRate = tuning.growthRate,
            baseHashrate = BigDecimal(tuning.baseHashrate),
            powerDrawKw = tuning.powerDrawKw,
            heatLoad = tuning.heatLoad,
            unlockLifetimeBtc = BigDecimal(tuning.unlockLifetimeBtc)
        )
    }

    private val mapById = ALL.associateBy { it.id }
    fun getById(id: String): MinerDefinition? = mapById[id]
}
