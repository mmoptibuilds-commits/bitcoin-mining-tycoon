package com.antigravity.bitcoinminingtycoon.ui.components

/** Pure presentation policy: cosmetic motion never reads or changes game economy state. */
object FeedbackMotionPolicy {
    const val PRESS_TRANSITION_MILLIS = 100

    fun pressScale(isPressed: Boolean, reducedMotion: Boolean): Float =
        if (isPressed && !reducedMotion) 0.95f else 1.0f

    fun allowsCosmeticMotion(reducedMotion: Boolean, batteryFriendly: Boolean): Boolean =
        !reducedMotion && !batteryFriendly

    fun shouldAnimateFacilityFans(
        reducedMotion: Boolean,
        batteryFriendly: Boolean,
        isResumed: Boolean,
        visibleUnitCount: Int
    ): Boolean = allowsCosmeticMotion(reducedMotion, batteryFriendly) && isResumed && visibleUnitCount > 0
}
