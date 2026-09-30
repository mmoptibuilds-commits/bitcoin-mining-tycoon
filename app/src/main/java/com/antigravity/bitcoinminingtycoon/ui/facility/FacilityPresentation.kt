package com.antigravity.bitcoinminingtycoon.ui.facility

import com.antigravity.bitcoinminingtycoon.model.GameState

data class FacilitySceneModel(
    val stage: FacilityStage,
    val previewStage: FacilityStage?,
    val discoveredStage: FacilityStage,
    val visibleUnitCount: Int,
    val detailLevel: Int,
    val canvasElementCount: Int
)

/** Derives the visible scene from owned hardware and its durable discovery milestone. */
object FacilityPresentation {
    const val MAX_CANVAS_ELEMENTS = 48
    const val MAX_DETAIL_LEVEL = 4
    const val FIXED_ELEMENT_BUDGET = 12
    const val MAX_VISIBLE_UNITS = MAX_CANVAS_ELEMENTS - FIXED_ELEMENT_BUDGET

    fun present(state: GameState): FacilitySceneModel {
        val stage = FacilityStage.entries.asReversed().firstOrNull { candidate ->
            candidate.minerIds.any { (state.miners[it] ?: 0L) > 0L }
        } ?: FacilityStage.SALVAGED_PC

        val visibleCount = stage.minerIds.fold(0) { total, minerId ->
            val remaining = (MAX_VISIBLE_UNITS - total).coerceAtLeast(0)
            val owned = (state.miners[minerId] ?: 0L).coerceAtLeast(0L)
            total + minOf(owned, remaining.toLong()).toInt()
        }
        val detailLevel = when {
            visibleCount == 0 -> 0
            visibleCount == 1 -> 1
            visibleCount <= 3 -> 2
            visibleCount <= 7 -> 3
            else -> MAX_DETAIL_LEVEL
        }
        val discoveredIndex = maxOf(stage.stageIndex, state.highestDiscoveredFacilityStage)
            .coerceIn(0, FacilityStage.entries.lastIndex)
        val nextStage = FacilityStage.entries.getOrNull(stage.stageIndex + 1)

        return FacilitySceneModel(
            stage = stage,
            previewStage = nextStage,
            discoveredStage = FacilityStage.entries[discoveredIndex],
            visibleUnitCount = visibleCount,
            detailLevel = detailLevel,
            canvasElementCount = FIXED_ELEMENT_BUDGET + visibleCount
        )
    }
}
