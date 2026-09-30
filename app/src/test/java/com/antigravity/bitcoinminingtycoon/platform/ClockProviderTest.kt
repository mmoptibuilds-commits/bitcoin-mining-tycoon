package com.antigravity.bitcoinminingtycoon.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockProviderTest {

    @Test
    fun calculateOfflineSeconds_clampsNegativeToZero() {
        val clock = FakeClockProvider(wallMillis = 1000L)
        // Saved time is in the future relative to current time (clock set backward)
        val offlineSeconds = clock.calculateOfflineSeconds(lastSavedWallMillis = 5000L, currentWallMillis = 1000L)
        assertEquals(0L, offlineSeconds)
    }

    @Test
    fun calculateOfflineSeconds_calculatesNormalElapsedInterval() {
        val clock = FakeClockProvider(wallMillis = 1_700_014_400_000L)
        // Exactly 4 hours (14,400 seconds)
        val offlineSeconds = clock.calculateOfflineSeconds(lastSavedWallMillis = 1_700_000_000_000L)
        assertEquals(14_400L, offlineSeconds)
    }

    @Test
    fun calculateOfflineSeconds_clampsOver12HoursTo12Hours() {
        val clock = FakeClockProvider(wallMillis = 1_700_100_000_000L)
        // 100,000 seconds passed (exceeds 12h = 43,200s)
        val offlineSeconds = clock.calculateOfflineSeconds(lastSavedWallMillis = 1_700_000_000_000L)
        assertEquals(43_200L, offlineSeconds)
    }
}
