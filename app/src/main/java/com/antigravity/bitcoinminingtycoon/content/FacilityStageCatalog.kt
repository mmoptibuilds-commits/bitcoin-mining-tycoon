package com.antigravity.bitcoinminingtycoon.content

/** Stable miner-to-scene groups shared by migration, state updates and the Compose presentation. */
object FacilityStageCatalog {
    val minerIdsByStage: List<Set<String>> = listOf(
        setOf("ancient_cpu", "gaming_cpu"),
        setOf("gaming_gpu"),
        setOf("dual_gpu_rig", "gpu_rig_6x"),
        setOf("entry_asic", "industrial_asic"),
        setOf("asic_rack", "server_room", "mining_warehouse"),
        setOf("mining_farm", "hydro_facility", "geothermal_complex", "nuclear_campus"),
        setOf("immersion_megafarm", "fusion_complex"),
        setOf("orbital_solar_miner"),
        setOf("lunar_mining_array", "quantum_hash_facility"),
        setOf("dyson_hash_swarm")
    )

    private val stageByMinerId: Map<String, Int> = minerIdsByStage
        .flatMapIndexed { stage, ids -> ids.map { id -> id to stage } }
        .toMap()

    fun stageIndexForMiner(minerId: String): Int? = stageByMinerId[minerId]

    fun highestOwnedStage(miners: Map<String, Long>): Int = minerIdsByStage.indexOfLast { ids ->
        ids.any { id -> (miners[id] ?: 0L) > 0L }
    }.coerceAtLeast(0)
}
