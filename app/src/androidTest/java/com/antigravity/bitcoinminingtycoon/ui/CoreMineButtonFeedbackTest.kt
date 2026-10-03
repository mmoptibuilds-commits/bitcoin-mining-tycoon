package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.ui.components.CoreMineButton
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreMineButtonFeedbackTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun batteryFriendlyModeSuppressesDecorativeMiningParticles() {
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                CoreMineButton(onMineClick = {}, batteryFriendly = true)
            }
        }

        repeat(3) {
            composeTestRule.onNodeWithContentDescription("Mine Bitcoin manually. Generates +10 H/s").performClick()
        }

        assertTrue(
            composeTestRule.onAllNodesWithTag("mine-feedback-particle", useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun rapidTapsKeepTheParticleAnimationQueueAtTwentyFour() {
        composeTestRule.mainClock.autoAdvance = false
        var receivedTaps = 0
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                CoreMineButton(onMineClick = { receivedTaps++ })
            }
        }

        repeat(40) {
            composeTestRule.onNodeWithContentDescription("Mine Bitcoin manually. Generates +10 H/s").performClick()
        }

        val particleCount = composeTestRule
            .onAllNodesWithTag("mine-feedback-particle", useUnmergedTree = true)
            .fetchSemanticsNodes().size
        assertEquals(40, receivedTaps)
        assertTrue("At most 24 live tap particles are kept, found $particleCount", particleCount <= 24)
    }
}
