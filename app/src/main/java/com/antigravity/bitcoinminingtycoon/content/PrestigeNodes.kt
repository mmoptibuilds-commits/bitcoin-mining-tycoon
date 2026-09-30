package com.antigravity.bitcoinminingtycoon.content

data class PrestigeNodeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val costSp: Long,
    val tier: Int,
    val prerequisiteNodeIds: List<String> = emptyList()
)

object PrestigeNodes {

    // --- TIER 1: FOUNDATIONS (Cost: 1-2 SP) ---
    val EFFICIENT_SILICON = PrestigeNodeDefinition(
        id = "efficient_silicon",
        title = "Efficient Silicon",
        description = "+400% global effective hashrate across all mining operations.",
        costSp = 1L,
        tier = 1
    )

    val COLD_START = PrestigeNodeDefinition(
        id = "cold_start",
        title = "Cold Start",
        description = "Begin every prestige cycle with $500.00 USD initial operating capital.",
        costSp = 1L,
        tier = 1
    )

    val CHEAP_ENERGY = PrestigeNodeDefinition(
        id = "cheap_energy",
        title = "Cheap Energy",
        description = "Permanent -15% power consumption across all automated mining units.",
        costSp = 2L,
        tier = 1
    )

    // --- TIER 2: EXPANSION (Cost: 5-10 SP) ---
    val DIAMOND_HANDS = PrestigeNodeDefinition(
        id = "diamond_hands",
        title = "Diamond Hands",
        description = "+15% market sale price bonus on all simulated Bitcoin sales.",
        costSp = 5L,
        tier = 2,
        prerequisiteNodeIds = listOf("cold_start")
    )

    val CRYO_SUBLIMATION = PrestigeNodeDefinition(
        id = "cryo_sublimation",
        title = "Cryo Sublimation",
        description = "+30% thermal dissipation rate across all cooling infrastructure.",
        costSp = 5L,
        tier = 2,
        prerequisiteNodeIds = listOf("cheap_energy")
    )

    val INDUSTRIAL_MEMORY = PrestigeNodeDefinition(
        id = "industrial_memory",
        title = "Industrial Memory",
        description = "Begin every prestige cycle with Tier 2 (Commercial) Substation unlocked.",
        costSp = 10L,
        tier = 2,
        prerequisiteNodeIds = listOf("efficient_silicon")
    )

    // --- TIER 3: MASTERY (Cost: 25-50 SP) ---
    val QUANTUM_FIRMWARE = PrestigeNodeDefinition(
        id = "quantum_firmware",
        title = "Quantum Firmware",
        description = "+100% manual hashing output per MINE tap.",
        costSp = 25L,
        tier = 3,
        prerequisiteNodeIds = listOf("diamond_hands")
    )

    val ZERO_LOSS_GRID = PrestigeNodeDefinition(
        id = "zero_loss_grid",
        title = "Superconducting Grid",
        description = "Power deficit penalty softened by 50% under grid overload.",
        costSp = 30L,
        tier = 3,
        prerequisiteNodeIds = listOf("cryo_sublimation")
    )

    val AUTO_BROKER = PrestigeNodeDefinition(
        id = "auto_broker",
        title = "Autonomous Broker",
        description = "Automated market sales execute at a +10% price premium.",
        costSp = 40L,
        tier = 3,
        prerequisiteNodeIds = listOf("diamond_hands")
    )

    val DEEP_COLD = PrestigeNodeDefinition(
        id = "deep_cold",
        title = "Sub-Zero Arrays",
        description = "Datacenter equilibrium temperature reduced by 5.0°C.",
        costSp = 50L,
        tier = 3,
        prerequisiteNodeIds = listOf("industrial_memory")
    )

    // --- TIER 4: SINGULARITY (Cost: 100-250 SP) ---
    val QUANTUM_LEGACY = PrestigeNodeDefinition(
        id = "quantum_legacy",
        title = "Quantum Legacy",
        description = "All automated hardware throughput multiplied by 3.0x permanently.",
        costSp = 100L,
        tier = 4,
        prerequisiteNodeIds = listOf("quantum_firmware", "zero_loss_grid")
    )

    val SATOSHI_VISION = PrestigeNodeDefinition(
        id = "satoshi_vision",
        title = "Satoshi's Vision",
        description = "Unspent Satoshi Points grant +5% hashrate each (up from +1%).",
        costSp = 250L,
        tier = 4,
        prerequisiteNodeIds = listOf("quantum_legacy", "deep_cold")
    )

    val ALL: List<PrestigeNodeDefinition> = listOf(
        EFFICIENT_SILICON, COLD_START, CHEAP_ENERGY,
        DIAMOND_HANDS, CRYO_SUBLIMATION, INDUSTRIAL_MEMORY,
        QUANTUM_FIRMWARE, ZERO_LOSS_GRID, AUTO_BROKER, DEEP_COLD,
        QUANTUM_LEGACY, SATOSHI_VISION
    )

    private val BY_ID: Map<String, PrestigeNodeDefinition> = ALL.associateBy { it.id }

    fun getById(id: String): PrestigeNodeDefinition? = BY_ID[id]
}
