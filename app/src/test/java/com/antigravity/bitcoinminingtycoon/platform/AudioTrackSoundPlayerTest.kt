package com.antigravity.bitcoinminingtycoon.platform

import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AudioTrackSoundPlayerTest {

    private class RecordingTrack(private val failOnWrite: Boolean = false) : AudioTrackPort {
        var writes = 0
        var plays = 0
        var stops = 0
        var releases = 0

        override fun write(samples: ShortArray) {
            writes++
            check(!failOnWrite) { "synthetic AudioTrack write failure" }
        }

        override fun play() { plays++ }
        override fun stop() { stops++ }
        override fun release() { releases++ }
    }

    private class RecordingFactory(private val track: RecordingTrack) : AudioTrackPortFactory {
        var creates = 0
        override fun create(samples: ShortArray): AudioTrackPort {
            creates++
            return track
        }
    }

    @Test
    fun disabledSoundNeverCreatesAnAudioTrack() = runTest {
        val track = RecordingTrack()
        val factory = RecordingFactory(track)
        val player = AudioTrackSoundPlayer(isSoundEnabled = { false }, scope = this, trackFactory = factory)

        player.play(SoundEffect.TAP)
        advanceUntilIdle()

        assertEquals(0, factory.creates)
        assertEquals(0, track.releases)
    }

    @Test
    fun completedAndFailedPlaybackBothReleaseTheirAudioTrack() = runTest {
        val completedTrack = RecordingTrack()
        val completedPlayer = AudioTrackSoundPlayer(
            isSoundEnabled = { true }, scope = this, trackFactory = RecordingFactory(completedTrack)
        )
        completedPlayer.play(SoundEffect.TAP)
        advanceUntilIdle()
        assertEquals(1, completedTrack.plays)
        assertEquals(1, completedTrack.stops)
        assertEquals(1, completedTrack.releases)

        val failingTrack = RecordingTrack(failOnWrite = true)
        val failingPlayer = AudioTrackSoundPlayer(
            isSoundEnabled = { true }, scope = this, trackFactory = RecordingFactory(failingTrack)
        )
        failingPlayer.play(SoundEffect.TAP)
        advanceUntilIdle()
        assertEquals(1, failingTrack.writes)
        assertEquals(0, failingTrack.plays)
        assertEquals(1, failingTrack.releases)
    }
}
