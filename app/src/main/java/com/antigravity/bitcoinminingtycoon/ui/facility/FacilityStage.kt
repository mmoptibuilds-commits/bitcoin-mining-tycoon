package com.antigravity.bitcoinminingtycoon.ui.facility

import com.antigravity.bitcoinminingtycoon.content.FacilityStageCatalog

enum class FacilityStage(val stageIndex: Int, val title: String, val description: String) {
    SALVAGED_PC(0, "Salvaged PC", "A small desktop starts the operation."),
    GPU_BENCH(1, "GPU bench", "Graphics cards line a workbench."),
    RIG_WORKSHOP(2, "Rig workshop", "Open-frame rigs add linked compute."),
    ASIC_ROOM(3, "ASIC room", "Purpose-built hash boards fill a cooled room."),
    WAREHOUSE(4, "Mining warehouse", "Organized racks scale into a full facility."),
    ENERGY_CAMPUS(5, "Energy campus", "Large sites pair compute with power and cooling."),
    FUSION_MEGAFARM(6, "Fusion megafarm", "Immersion systems and fictional fusion power the farm."),
    ORBITAL_ARRAY(7, "Orbital array", "Solar collectors and miners work above a planet."),
    LUNAR_QUANTUM_BASE(8, "Lunar and quantum base", "A lunar outpost supports cryogenic compute."),
    DYSON_SWARM(9, "Dyson swarm", "A restrained stellar array closes the progression." );

    val minerIds: Set<String> get() = FacilityStageCatalog.minerIdsByStage[stageIndex]

    companion object {
        fun forMiner(minerId: String): FacilityStage? =
            FacilityStageCatalog.stageIndexForMiner(minerId)?.let { entries.getOrNull(it) }
    }
}
