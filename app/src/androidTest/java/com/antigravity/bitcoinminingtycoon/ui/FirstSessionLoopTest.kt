package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.data.SaveReadiness
import com.antigravity.bitcoinminingtycoon.model.TeachingCueIds
import com.antigravity.bitcoinminingtycoon.ui.navigation.AppNavHost
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirstSessionLoopTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun newPlayerCanMineSellBuyFirstMachineAndSeePassiveMiningCue() {
        // A deterministic tutorial fixture keeps the real Mine → sell → hardware flow short.
        val harness = UiGameHarness(GameSave(manualHashStrength = "2000000"))
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                AppNavHost(viewModel = harness.viewModel)
            }
        }
        composeTestRule.waitUntil(10_000) {
            harness.viewModel.uiState.value.saveReadiness == SaveReadiness.Ready
        }

        composeTestRule.onNodeWithText("Your facility").assertIsDisplayed()
        composeTestRule.scrollUntilTextDisplayed("Make your first Bitcoin")
        composeTestRule.scrollUntilContentDescriptionDisplayed("Mine. Make your first Bitcoin")
        composeTestRule.onNodeWithContentDescription("Mine. Make your first Bitcoin").performClick()
        composeTestRule.waitUntil(10_000) {
            harness.repository.gameState.value.stats.totalManualTaps == 1L
        }
        assertTrue(TeachingCueIds.MINE_BITCOIN in harness.repository.gameState.value.completedTeachingCueIds)
        composeTestRule.scrollUntilTextDisplayed("Turn Bitcoin into Cash")

        composeTestRule.scrollUntilContentDescriptionDisplayed("Sell all of mined Bitcoin", substring = true)
        composeTestRule.onNodeWithContentDescription("Sell all of mined Bitcoin", substring = true).performClick()
        composeTestRule.waitUntil(10_000) {
            harness.repository.gameState.value.usdBigDecimal == BigDecimal("5.00") &&
                harness.repository.gameState.value.btcBigDecimal.signum() == 0
        }

        // The first $5 sale explains that Bitcoin can be mined again to reach the first machine cost.
        composeTestRule.scrollUntilContentDescriptionDisplayed("Mine Bitcoin manually", substring = true)
        val mineButton = composeTestRule.onNodeWithContentDescription("Mine Bitcoin manually", substring = true)
        mineButton.performScrollTo()
        composeTestRule.onRoot().performTouchInput {
            swipeDown(startY = height * 0.30f, endY = height * 0.40f, durationMillis = 250)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Mine Bitcoin manually", substring = true).performClick()
        try {
            composeTestRule.waitUntil(10_000) {
                harness.repository.gameState.value.stats.totalManualTaps == 2L
            }
        } catch (failure: Throwable) {
            throw AssertionError(
                "Expected one tap after the second Mine click; observed ${harness.repository.gameState.value.stats.totalManualTaps}; " +
                    "readiness=${harness.repository.saveReadiness.value}; btc=${harness.repository.gameState.value.btc}; " +
                    "cash=${harness.repository.gameState.value.usd}",
                failure
            )
        }
        composeTestRule.scrollUntilContentDescriptionDisplayed("Sell all of mined Bitcoin", substring = true)
        composeTestRule.onNodeWithContentDescription("Sell all of mined Bitcoin", substring = true).performClick()
        composeTestRule.waitUntil(10_000) {
            harness.repository.gameState.value.usdBigDecimal >= Miners.ALL.first().baseCostUsd &&
                TeachingCueIds.SELL_BITCOIN in harness.repository.gameState.value.completedTeachingCueIds
        }

        composeTestRule.scrollUntilTextDisplayed("Buy your first machine")
        composeTestRule.onNodeWithContentDescription("Browse hardware. Buy your first machine").performClick()
        composeTestRule.onNodeWithText("ANCIENT CPU").assertIsDisplayed()
        composeTestRule.scrollUntilContentDescriptionDisplayed("Purchase 1 Ancient CPU for", substring = true)
        composeTestRule.onNodeWithContentDescription("Purchase 1 Ancient CPU for", substring = true)
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(10_000) {
            (harness.repository.gameState.value.miners[Miners.ALL.first().id] ?: 0L) == 1L
        }
        assertTrue(TeachingCueIds.BUY_FIRST_MACHINE in harness.repository.gameState.value.completedTeachingCueIds)

        composeTestRule.onNodeWithContentDescription("Mine tab").performClick()
        composeTestRule.scrollUntilTextDisplayed("Your machine is mining")
        assertEquals(1L, harness.repository.gameState.value.miners["ancient_cpu"])
    }
}
