package com.antigravity.bitcoinminingtycoon.ui.facility

import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.engine.PrestigeEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FacilityPresentationTest {

    @Test
    fun everyStableMinerIdBelongsToExactlyOneOfTenFacilityStages() {
        val mappedIds = FacilityStage.entries.flatMap { it.minerIds }

        assertEquals(10, FacilityStage.entries.size)
        assertEquals(20, mappedIds.size)
        assertEquals(20, mappedIds.toSet().size)
        assertEquals(Miners.ALL.map { it.id }.toSet(), mappedIds.toSet())
        assertEquals(FacilityStage.SALVAGED_PC, FacilityStage.forMiner("gaming_cpu"))
        assertEquals(FacilityStage.RIG_WORKSHOP, FacilityStage.forMiner("gpu_rig_6x"))
        assertEquals(FacilityStage.ORBITAL_ARRAY, FacilityStage.forMiner("orbital_solar_miner"))
        assertEquals(FacilityStage.LUNAR_QUANTUM_BASE, FacilityStage.forMiner("quantum_hash_facility"))
        assertEquals(FacilityStage.DYSON_SWARM, FacilityStage.forMiner("dyson_hash_swarm"))
        assertNull(FacilityStage.forMiner("unknown_miner"))
    }

    @Test
    fun strongestOwnedTierSelectsTheSceneAndOnlyPreviewsItsNextStage() {
        val state = GameState(miners = mapOf("ancient_cpu" to 8L, "gaming_gpu" to 2L))

        val scene = FacilityPresentation.present(state)

        assertEquals(FacilityStage.GPU_BENCH, scene.stage)
        assertEquals(FacilityStage.RIG_WORKSHOP, scene.previewStage)
        assertEquals(2, scene.visibleUnitCount)
        assertEquals(2, scene.detailLevel)
    }

    @Test
    fun displayedDensityAndCanvasWorkStayBoundedForExtremeOwnedCounts() {
        val scene = FacilityPresentation.present(
            GameState(miners = mapOf(
                "asic_rack" to Long.MAX_VALUE,
                "server_room" to Long.MAX_VALUE,
                "mining_warehouse" to Long.MAX_VALUE
            ))
        )

        assertEquals(FacilityStage.WAREHOUSE, scene.stage)
        assertEquals(FacilityPresentation.MAX_VISIBLE_UNITS, scene.visibleUnitCount)
        assertEquals(48, scene.canvasElementCount)
        assertTrue(scene.detailLevel <= FacilityPresentation.MAX_DETAIL_LEVEL)
        assertTrue(scene.canvasElementCount <= FacilityPresentation.MAX_CANVAS_ELEMENTS)
    }

    @Test
    fun prestigeResetsTheCurrentSceneButRetainsTheDiscoveredMilestone() {
        val late = GameState(
            miners = mapOf("dyson_hash_swarm" to 1L),
            highestDiscoveredFacilityStage = FacilityStage.DYSON_SWARM.stageIndex,
            stats = StatsState(lifetimeBtcMined = "100")
        )

        val afterPrestige = PrestigeEngine.applyPrestige(late)
        val scene = FacilityPresentation.present(afterPrestige)

        assertTrue(afterPrestige.miners.isEmpty())
        assertEquals(FacilityStage.SALVAGED_PC, scene.stage)
        assertEquals(FacilityStage.DYSON_SWARM, scene.discoveredStage)
        assertEquals(FacilityStage.GPU_BENCH, scene.previewStage)
    }
}
