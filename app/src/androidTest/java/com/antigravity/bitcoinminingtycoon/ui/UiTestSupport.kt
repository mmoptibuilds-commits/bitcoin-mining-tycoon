package com.antigravity.bitcoinminingtycoon.ui

import com.antigravity.bitcoinminingtycoon.data.GameRepository
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.data.SaveDataSource
import com.antigravity.bitcoinminingtycoon.platform.FakeClockProvider
import com.antigravity.bitcoinminingtycoon.viewmodel.GameViewModel
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
