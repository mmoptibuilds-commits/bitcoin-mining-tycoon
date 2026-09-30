package com.antigravity.bitcoinminingtycoon.platform

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundPlayerTest {

    @Test
    fun testAllSoundEffectsHandledByNoOpPlayer() {
        // NoOpSoundPlayer should silently accept all enum variants without error
        for (effect in SoundEffect.entries) {
            NoOpSoundPlayer.play(effect)
        }
    }

    @Test
    fun testSoundEffectEnumCompleteness() {
        val effectNames = SoundEffect.entries.map { it.name }.toSet()
        val expectedEffects = setOf(
            "TAP",
            "BUY",
            "INVALID",
            "ACHIEVEMENT",
            "EVENT",
            "PRESTIGE",
            "DAILY_REWARD"
        )
        assertEquals(expectedEffects, effectNames)
    }

    @Test
    fun testRecordingSoundPlayerTracksInvocations() {
        val played = mutableListOf<SoundEffect>()
        val recordingPlayer = object : SoundPlayer {
            override fun play(sound: SoundEffect) {
                played.add(sound)
            }
        }

        recordingPlayer.play(SoundEffect.TAP)
        recordingPlayer.play(SoundEffect.BUY)
        recordingPlayer.play(SoundEffect.ACHIEVEMENT)

        assertEquals(listOf(SoundEffect.TAP, SoundEffect.BUY, SoundEffect.ACHIEVEMENT), played)
    }
}
