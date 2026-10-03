package com.antigravity.bitcoinminingtycoon.ui

import com.antigravity.bitcoinminingtycoon.data.GameRepository
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.data.SaveDataSource
import com.antigravity.bitcoinminingtycoon.platform.FakeClockProvider
import com.antigravity.bitcoinminingtycoon.viewmodel.GameViewModel
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class InMemoryUiSaveDataSource(initialSave: GameSave = GameSave()) : SaveDataSource {
    private val mutex = Mutex()
    private val save = MutableStateFlow(initialSave)
    override val saveFlow: Flow<GameSave> = save.asStateFlow()

    override suspend fun update(transform: suspend (GameSave) -> GameSave): GameSave = mutex.withLock {
        transform(save.value).also { save.value = it }
    }

    override suspend fun clear() {
        update { GameSave() }
    }
}

internal class UiGameHarness(initialSave: GameSave = GameSave()) {
    val clock = FakeClockProvider(wallMillis = 1_700_000_000_000L)
    val saveDataSource = InMemoryUiSaveDataSource(initialSave)
    val repository = GameRepository(
        dataSource = saveDataSource,
        clockProvider = clock,
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    )
    val viewModel = GameViewModel(repository, clock)
}

/** Scrolls through a screen as a player would until a lazy/off-screen element is visible. */
internal fun ComposeContentTestRule.scrollUntilTextDisplayed(
    text: String,
    substring: Boolean = false,
    maxSwipes: Int = 60
) {
    scrollUntilDisplayed(maxSwipes) {
        onNodeWithText(text, substring = substring, useUnmergedTree = true)
            .assertIsDisplayed()
    }
}

internal fun ComposeContentTestRule.scrollUntilContentDescriptionDisplayed(
    description: String,
    substring: Boolean = false,
    maxSwipes: Int = 60
) {
    scrollUntilDisplayed(maxSwipes) {
        onNodeWithContentDescription(description, substring, useUnmergedTree = true)
            .assertIsDisplayed()
    }
}

internal fun ComposeContentTestRule.scrollToTop(maxSwipes: Int = 20) {
    repeat(maxSwipes) {
        onRoot().performTouchInput { swipeDown() }
        waitForIdle()
    }
}

private fun ComposeContentTestRule.scrollUntilDisplayed(maxSwipes: Int, assertDisplayed: () -> Unit) {
    repeat(maxSwipes) {
        if (runCatching(assertDisplayed).isSuccess) return
        val root = onRoot()
        val rootHeight = root.fetchSemanticsNode().boundsInRoot.height
        root.performTouchInput {
            swipeUp(startY = rootHeight * 0.72f, endY = rootHeight * 0.52f, durationMillis = 250)
        }
        waitForIdle()
    }
    onRoot().printToLog("ScrollUntilTextDisplayed")
    assertDisplayed()
}
