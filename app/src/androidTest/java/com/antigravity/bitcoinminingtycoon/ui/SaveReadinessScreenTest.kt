package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.data.SaveReadiness
import com.antigravity.bitcoinminingtycoon.ui.screens.SaveReadinessScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaveReadinessScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun corruptSaveConfirmationExplainsRestartAndKeepsRecoveryCopy() {
        var readiness by mutableStateOf<SaveReadiness>(SaveReadiness.CorruptCheckpointed("Malformed payload"))

        composeTestRule.setContent {
            SaveReadinessScreen(
                readiness = readiness,
                onStartNewSave = { readiness = SaveReadiness.RecoveryRestartRequired },
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("A private recovery copy was kept.", substring = true).assertIsDisplayed()
        val confirm = composeTestRule.onNodeWithContentDescription(
            "Confirm start a new save; the recovery copy is preserved"
        )
        confirm.assertIsDisplayed()
        confirm.assertHasClickAction()
        confirm.performClick()

        composeTestRule.onNodeWithText("Restart to start a new save").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your confirmation is saved.", substring = true).assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Start a new save").assertCountEquals(0)
    }

    @Test
    fun unsupportedSaveStaysReadOnlyWithoutRecoveryAction() {
        composeTestRule.setContent {
            SaveReadinessScreen(
                readiness = SaveReadiness.UnsupportedSchema(77),
                onStartNewSave = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("This save needs a newer app").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save schema 77 is newer than this app.", substring = true).assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Start a new save").assertCountEquals(0)
    }
}
