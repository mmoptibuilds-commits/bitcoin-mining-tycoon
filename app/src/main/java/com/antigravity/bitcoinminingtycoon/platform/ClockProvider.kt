package com.antigravity.bitcoinminingtycoon.platform

import android.os.SystemClock

/**
 * Clock abstraction enforcing Invariant 4:
 * Monotonic clock for foreground tick delta time;
 * Defensive wall clock for offline progression with [0, 12h] clamping.
 */
interface ClockProvider {
    fun monotonicNanos(): Long
    fun wallMillis(): Long

    /**
     * Calculates offline duration in seconds with defensive boundary clamping.
     * Negative intervals (clock rolled back) clamp to 0.
     * Intervals exceeding 12 hours (43,200 seconds) clamp to 43,200.
     */
    fun calculateOfflineSeconds(lastSavedWallMillis: Long, currentWallMillis: Long = wallMillis()): Long {
        if (lastSavedWallMillis <= 0L || currentWallMillis <= lastSavedWallMillis) {
            return 0L
        }
        val elapsedMillis = currentWallMillis - lastSavedWallMillis
        val elapsedSeconds = elapsedMillis / 1000L
        val maxSeconds = 12L * 3600L // 43,200 seconds (12 hours)
        return elapsedSeconds.coerceIn(0L, maxSeconds)
    }
}

/**
 * Production Android clock provider.
 */
class SystemClockProvider : ClockProvider {
    override fun monotonicNanos(): Long = SystemClock.elapsedRealtimeNanos()
    override fun wallMillis(): Long = System.currentTimeMillis()
}

/**
 * Deterministic mock clock for unit tests and JVM simulations.
 */
class FakeClockProvider(
    private var monotonicNanos: Long = 0L,
    private var wallMillis: Long = 1_700_000_000_000L
) : ClockProvider {
    override fun monotonicNanos(): Long = monotonicNanos
    override fun wallMillis(): Long = wallMillis

    fun advanceMonotonicNanos(nanos: Long) {
        if (nanos > 0L) {
            monotonicNanos += nanos
        }
    }

    fun advanceMonotonicSeconds(seconds: Double) {
        if (seconds > 0.0) {
            monotonicNanos += (seconds * 1_000_000_000.0).toLong()
        }
    }

    fun setWallMillis(millis: Long) {
        wallMillis = millis
    }

    fun advanceWallMillis(millis: Long) {
        wallMillis += millis
    }
}
