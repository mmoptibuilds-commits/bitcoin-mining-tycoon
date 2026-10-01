package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
        composeTestRule.onNodeWithText("Make your first Bitcoin").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Mine. Make your first Bitcoin").performClick()
        composeTestRule.waitUntil(10_000) {
            harness.repository.gameState.value.stats.totalManualTaps == 1L
        }
        assertTrue(TeachingCueIds.MINE_BITCOIN in harness.repository.gameState.value.completedTeachingCueIds)
        composeTestRule.onNodeWithText("Turn Bitcoin into Cash").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Sell all of mined Bitcoin", substring = true).performClick()
        composeTestRule.waitUntil(10_000) {
            harness.repository.gameState.value.usdBigDecimal == BigDecimal("5.00") &&
                harness.repository.gameState.value.btcBigDecimal.signum() == 0
        }

        // The first $5 sale explains that Bitcoin can be mined again to reach the first machine cost.
        composeTestRule.onNodeWithContentDescription("Mine Bitcoin manually", substring = true).performClick()
        composeTestRule.waitUntil(10_000) {
            harness.repository.gameState.value.stats.totalManualTaps == 2L
        }
        composeTestRule.onNodeWithContentDescription("Sell all of mined Bitcoin", substring = true).performClick()
        composeTestRule.waitUntil(10_000) {
            harness.repository.gameState.value.usdBigDecimal >= Miners.ALL.first().baseCostUsd &&
                TeachingCueIds.SELL_BITCOIN in harness.repository.gameState.value.completedTeachingCueIds
        }

        composeTestRule.onNodeWithText("Buy your first machine").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Browse hardware. Buy your first machine").performClick()
        composeTestRule.onNodeWithText("ANCIENT CPU").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Purchase 1 Ancient CPU for", substring = true)
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(10_000) {
            (harness.repository.gameState.value.miners[Miners.ALL.first().id] ?: 0L) == 1L
        }
        assertTrue(TeachingCueIds.BUY_FIRST_MACHINE in harness.repository.gameState.value.completedTeachingCueIds)

        composeTestRule.onNodeWithContentDescription("Mine tab").performClick()
        composeTestRule.onNodeWithText("Your machine is mining").assertIsDisplayed()
        assertEquals(1L, harness.repository.gameState.value.miners["ancient_cpu"])
    }
}
