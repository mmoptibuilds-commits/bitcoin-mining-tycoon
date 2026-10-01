package com.antigravity.bitcoinminingtycoon.platform

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundEffect {
    TAP,
    BUY,
    INVALID,
    ACHIEVEMENT,
    EVENT,
    PRESTIGE,
    DAILY_REWARD
}

interface SoundPlayer {
    fun play(sound: SoundEffect)
}

interface AudioTrackPort {
    fun write(samples: ShortArray)
    fun play()
    fun stop()
    fun release()
}

fun interface AudioTrackPortFactory {
    fun create(samples: ShortArray): AudioTrackPort
}

object NoOpSoundPlayer : SoundPlayer {
    override fun play(sound: SoundEffect) {}
}

/**
 * Procedural audio synthesizer implementing Decision D019.
 * Programmatically generates PCM soundwaves via AudioTrack with zero audio assets.
 */
class AudioTrackSoundPlayer(
    private val isSoundEnabled: () -> Boolean = { true },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val trackFactory: AudioTrackPortFactory = AndroidAudioTrackPortFactory
) : SoundPlayer {

    companion object {
        private const val SAMPLE_RATE = 44100
    }

    override fun play(sound: SoundEffect) {
        if (!isSoundEnabled()) return

        scope.launch {
            try {
                val samples = generateSamples(sound)
                playPcm(samples)
            } catch (_: Throwable) {
                // Audio synthesis failure should never crash the game
            }
        }
    }

    private fun generateSamples(sound: SoundEffect): ShortArray {
        return when (sound) {
            SoundEffect.TAP -> generateChirp(frequencyHz = 880.0, durationMs = 30)
            SoundEffect.BUY -> generateDualTone(f1 = 440.0, f2 = 660.0, durationMs = 60)
            SoundEffect.INVALID -> generateBuzz(frequencyHz = 150.0, durationMs = 80)
            SoundEffect.ACHIEVEMENT -> generateArpeggio(
                frequencies = listOf(523.25, 659.25, 783.99), // C5, E5, G5
                noteDurationMs = 80
            )
            SoundEffect.EVENT -> generateSweep(startHz = 400.0, endHz = 900.0, durationMs = 200)
            SoundEffect.PRESTIGE -> generateChord(frequencies = listOf(220.0, 329.63, 440.0), durationMs = 400)
            SoundEffect.DAILY_REWARD -> generateDualTone(f1 = 587.33, f2 = 880.0, durationMs = 200)
        }
    }

    private fun generateChirp(frequencyHz: Double, durationMs: Int): ShortArray {
        val count = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 80.0)
            val sample = sin(2.0 * PI * frequencyHz * t) * decay
            buffer[i] = (sample * Short.MAX_VALUE * 0.4).toInt().toShort()
        }
        return buffer
    }

    private fun generateDualTone(f1: Double, f2: Double, durationMs: Int): ShortArray {
        val totalCount = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val half = totalCount / 2
        val buffer = ShortArray(totalCount)
        for (i in 0 until totalCount) {
            val freq = if (i < half) f1 else f2
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = 1.0 - (i.toDouble() / totalCount)
            val sample = sin(2.0 * PI * freq * t) * envelope
            buffer[i] = (sample * Short.MAX_VALUE * 0.45).toInt().toShort()
        }
        return buffer
    }

    private fun generateBuzz(frequencyHz: Double, durationMs: Int): ShortArray {
        val count = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val sine = sin(2.0 * PI * frequencyHz * t)
            // Square wave distortion for industrial buzz
            val square = if (sine >= 0.0) 0.6 else -0.6
            val envelope = 1.0 - (i.toDouble() / count)
            buffer[i] = (square * envelope * Short.MAX_VALUE * 0.35).toInt().toShort()
        }
        return buffer
    }

    private fun generateArpeggio(frequencies: List<Double>, noteDurationMs: Int): ShortArray {
        val noteSamples = (SAMPLE_RATE * (noteDurationMs / 1000.0)).toInt()
        val totalSamples = noteSamples * frequencies.size
        val buffer = ShortArray(totalSamples)

        for ((index, freq) in frequencies.withIndex()) {
            val offset = index * noteSamples
            for (i in 0 until noteSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val decay = exp(-t * 20.0)
                val sample = sin(2.0 * PI * freq * t) * decay
                buffer[offset + i] = (sample * Short.MAX_VALUE * 0.5).toInt().toShort()
            }
        }
        return buffer
    }

    private fun generateSweep(startHz: Double, endHz: Double, durationMs: Int): ShortArray {
        val count = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(count)
        val durationSec = durationMs / 1000.0

        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / durationSec
            val currentFreq = startHz + (endHz - startHz) * progress
            val envelope = sin(PI * progress) // Bell envelope
            val sample = sin(2.0 * PI * currentFreq * t) * envelope
            buffer[i] = (sample * Short.MAX_VALUE * 0.45).toInt().toShort()
        }
        return buffer
    }

    private fun generateChord(frequencies: List<Double>, durationMs: Int): ShortArray {
        val count = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(count)

        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 6.0)
            var sampleSum = 0.0
            for (freq in frequencies) {
                sampleSum += sin(2.0 * PI * freq * t)
            }
            val averaged = (sampleSum / frequencies.size) * decay
            buffer[i] = (averaged * Short.MAX_VALUE * 0.5).toInt().toShort()
        }
        return buffer
    }

    private suspend fun playPcm(samples: ShortArray) {
        val audioTrack = trackFactory.create(samples)
        var playAttempted = false
        try {
            audioTrack.write(samples)
            playAttempted = true
            audioTrack.play()
            val durationMillis = ((samples.size.toDouble() / SAMPLE_RATE) * 1000.0).toLong() + 50L
            kotlinx.coroutines.delay(durationMillis)
        } finally {
            if (playAttempted) runCatching { audioTrack.stop() }
            runCatching { audioTrack.release() }
        }
    }
}

object AndroidAudioTrackPortFactory : AudioTrackPortFactory {
    override fun create(samples: ShortArray): AudioTrackPort {
        val bufferSize = samples.size * 2 // 16-bit PCM = 2 bytes per sample
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        return object : AudioTrackPort {
            override fun write(samples: ShortArray) {
                track.write(samples, 0, samples.size)
            }

            override fun play() = track.play()
            override fun stop() = track.stop()
            override fun release() = track.release()
        }
    }
}
