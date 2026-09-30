package com.antigravity.bitcoinminingtycoon.platform

import android.content.Context
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.EnumMap

enum class HapticSignal(
    val predefinedEffect: Int,
    val fallbackDurationMillis: Long,
    val minimumIntervalMillis: Long
) {
    TAP(VibrationEffect.EFFECT_TICK, 18L, 45L),
    PURCHASE(VibrationEffect.EFFECT_CLICK, 32L, 80L),
    INVALID(VibrationEffect.EFFECT_TICK, 42L, 120L),
    MILESTONE(VibrationEffect.EFFECT_DOUBLE_CLICK, 70L, 250L),
    PRESTIGE(VibrationEffect.EFFECT_HEAVY_CLICK, 110L, 500L)
}

interface Haptics {
    fun play(signal: HapticSignal)
}

object NoOpHaptics : Haptics {
    override fun play(signal: HapticSignal) = Unit
}

/** Monotonic per-signal gate. A clock rollback suppresses output until its prior interval catches up. */
class HapticRateLimiter(private val monotonicMillis: () -> Long) {
    private val lastPlayedAt = EnumMap<HapticSignal, Long>(HapticSignal::class.java)

    @Synchronized
    fun shouldPlay(signal: HapticSignal): Boolean {
        val now = monotonicMillis().coerceAtLeast(0L)
        val previous = lastPlayedAt[signal]
        if (previous != null && (now < previous || now - previous < signal.minimumIntervalMillis)) return false
        lastPlayedAt[signal] = now
        return true
    }

    companion object {
        val MINIMUM_INTERVALS_MILLIS: Map<HapticSignal, Long> = HapticSignal.entries.associateWith { it.minimumIntervalMillis }
    }
}

/** Capability-aware, rate-limited output seam used by Android and deterministic JVM tests. */
class RateLimitedHaptics(
    private val hasVibrator: () -> Boolean,
    private val limiter: HapticRateLimiter,
    private val output: (HapticSignal) -> Unit
) : Haptics {
    override fun play(signal: HapticSignal) {
        if (!runCatching(hasVibrator).getOrDefault(false) || !limiter.shouldPlay(signal)) return
        runCatching { output(signal) }
    }
}

/** Android API 31+ predefined effects with a short one-shot fallback for unsupported patterns. */
class AndroidHaptics(context: Context) : Haptics {
    private val vibrator: Vibrator? = runCatching {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    }.getOrNull()
    private val bounded = RateLimitedHaptics(
        hasVibrator = { vibrator?.hasVibrator() == true },
        limiter = HapticRateLimiter(SystemClock::elapsedRealtime),
        output = ::perform
    )

    override fun play(signal: HapticSignal) = bounded.play(signal)

    private fun perform(signal: HapticSignal) {
        val device = vibrator ?: return
        runCatching {
            device.vibrate(VibrationEffect.createPredefined(signal.predefinedEffect))
        }.recoverCatching {
            device.vibrate(VibrationEffect.createOneShot(signal.fallbackDurationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
