package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.AchievementDefinition
import com.antigravity.bitcoinminingtycoon.content.Achievements
import com.antigravity.bitcoinminingtycoon.model.GameState

object AchievementEngine {

    /**
     * Evaluates state against all achievement conditions and unlocks any eligible achievements.
     * Enforces strict one-time-unlock and idempotency invariants.
     */
    fun evaluate(state: GameState): Pair<GameState, List<AchievementDefinition>> {
        val newlyUnlocked = mutableListOf<AchievementDefinition>()

        for (achievement in Achievements.ALL) {
            if (achievement.id !in state.achievements) {
                if (achievement.isSatisfied(state)) {
                    newlyUnlocked.add(achievement)
                }
            }
        }

        if (newlyUnlocked.isEmpty()) {
            return Pair(state, emptyList())
        }

        val updatedAchievements = state.achievements + newlyUnlocked.map { it.id }.toSet()
        val updatedState = state.copy(achievements = updatedAchievements)

        return Pair(updatedState, newlyUnlocked)
    }
}
