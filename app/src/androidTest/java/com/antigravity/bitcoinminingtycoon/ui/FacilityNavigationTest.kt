package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.data.SaveReadiness
import com.antigravity.bitcoinminingtycoon.model.SettingsState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import com.antigravity.bitcoinminingtycoon.ui.navigation.AppNavHost
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FacilityNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun facilityTabsUtilitiesAndPrimaryTargetsAreAccessible() {
        val harness = UiGameHarness()
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                AppNavHost(viewModel = harness.viewModel)
            }
        }
        composeTestRule.waitUntil(10_000) {
            harness.viewModel.uiState.value.saveReadiness == SaveReadiness.Ready
        }

        val mineTab = composeTestRule.onNodeWithContentDescription("Mine tab")
        mineTab.assertIsDisplayed().assertHasClickAction().assertIsSelected()
        assertAtLeast48Dp(mineTab.fetchSemanticsNode().size.height)
        composeTestRule.onNodeWithText("Your facility").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Hardware tab").performClick()
        composeTestRule.onNodeWithText("ANCIENT CPU").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Upgrades tab").performClick()
        composeTestRule.onNodeWithText("POWER GRID INFRASTRUCTURE").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Stats").assertHasClickAction().performClick()
        composeTestRule.onNodeWithText("FACILITY TELEMETRY").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        val statsAction = composeTestRule.onNodeWithContentDescription("Stats")
        statsAction.assertIsDisplayed()
        assertAtLeast48Dp(statsAction.fetchSemanticsNode().size.height)
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("FACILITY PROTOCOLS").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Navigate back to facility dashboard").performClick()
        composeTestRule.onNodeWithContentDescription("Mine tab").performClick()
        composeTestRule.onNodeWithText("Your facility").assertIsDisplayed()
    }

    @Test
    fun returningFacilityKeepsSettingsAndDoesNotReplayNewPlayerTeaching() {
        val settings = SettingsState(soundEnabled = false, hapticsEnabled = false, reducedMotion = true)
        val harness = UiGameHarness(
            GameSave(
                btc = "0.00025",
                miners = mapOf("ancient_cpu" to 1L),
                stats = StatsState(lifetimeBtcMined = "0.00025", totalMinersPurchased = 1L),
                onboardingCompleted = true,
                settings = settings
            )
        )
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                AppNavHost(viewModel = harness.viewModel)
            }
        }
        composeTestRule.waitUntil(10_000) {
            harness.viewModel.uiState.value.saveReadiness == SaveReadiness.Ready
        }

        composeTestRule.onNodeWithText("Your machines are working").assertIsDisplayed()
        composeTestRule.onNodeWithText("Make your first Bitcoin").assertDoesNotExist()
        assertEquals(settings, harness.repository.gameState.value.settings)
        assertEquals(true, harness.repository.gameState.value.onboardingCompleted)
    }

    private fun assertAtLeast48Dp(heightPx: Int) {
        val heightDp = heightPx / composeTestRule.density.density
        assertTrue("Expected at least a 48dp target; was ${heightDp}dp", heightDp >= 48f)
    }
}
