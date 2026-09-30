package com.antigravity.bitcoinminingtycoon.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackMotionPolicyTest {

    @Test
    fun minePressUsesOneHundredMillisecondCompressionUnlessReducedMotionIsEnabled() {
        assertEquals(100, FeedbackMotionPolicy.PRESS_TRANSITION_MILLIS)
        assertEquals(0.95f, FeedbackMotionPolicy.pressScale(isPressed = true, reducedMotion = false))
        assertEquals(1.0f, FeedbackMotionPolicy.pressScale(isPressed = true, reducedMotion = true))
        assertEquals(1.0f, FeedbackMotionPolicy.pressScale(isPressed = false, reducedMotion = false))
    }

    @Test
    fun optionalTapEffectsRequireBothMotionSettingsToAllowThem() {
        assertTrue(FeedbackMotionPolicy.allowsCosmeticMotion(reducedMotion = false, batteryFriendly = false))
        assertFalse(FeedbackMotionPolicy.allowsCosmeticMotion(reducedMotion = true, batteryFriendly = false))
        assertFalse(FeedbackMotionPolicy.allowsCosmeticMotion(reducedMotion = false, batteryFriendly = true))
    }

    @Test
    fun facilityFansOnlyRunForAResumedSceneWithVisibleHardware() {
        assertTrue(FeedbackMotionPolicy.shouldAnimateFacilityFans(false, false, true, 1))
        assertFalse(FeedbackMotionPolicy.shouldAnimateFacilityFans(true, false, true, 1))
        assertFalse(FeedbackMotionPolicy.shouldAnimateFacilityFans(false, true, true, 1))
        assertFalse(FeedbackMotionPolicy.shouldAnimateFacilityFans(false, false, false, 1))
        assertFalse(FeedbackMotionPolicy.shouldAnimateFacilityFans(false, false, true, 0))
    }
}
