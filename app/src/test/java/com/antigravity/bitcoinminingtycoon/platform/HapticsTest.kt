package com.antigravity.bitcoinminingtycoon.platform

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HapticsTest {

    @Test
    fun rateLimitedPlayerChecksCapabilityAndBoundsRepeatedSignals() {
        var nowMillis = 1_000L
        var hasVibrator = false
        val played = mutableListOf<HapticSignal>()
        val limiter = HapticRateLimiter(monotonicMillis = { nowMillis })
        val haptics = RateLimitedHaptics(
            hasVibrator = { hasVibrator },
            limiter = limiter,
            output = { played += it }
        )

        haptics.play(HapticSignal.TAP)
        assertTrue(played.isEmpty())

        hasVibrator = true
        haptics.play(HapticSignal.TAP)
        haptics.play(HapticSignal.TAP)
        assertEquals(listOf(HapticSignal.TAP), played)

        nowMillis += HapticRateLimiter.MINIMUM_INTERVALS_MILLIS.getValue(HapticSignal.TAP)
        haptics.play(HapticSignal.TAP)
        haptics.play(HapticSignal.PURCHASE)
        haptics.play(HapticSignal.INVALID)
        haptics.play(HapticSignal.MILESTONE)
        haptics.play(HapticSignal.PRESTIGE)
        assertEquals(
            listOf(HapticSignal.TAP, HapticSignal.TAP, HapticSignal.PURCHASE, HapticSignal.INVALID,
                HapticSignal.MILESTONE, HapticSignal.PRESTIGE),
            played
        )
        assertFalse(limiter.shouldPlay(HapticSignal.TAP))
    }
}
