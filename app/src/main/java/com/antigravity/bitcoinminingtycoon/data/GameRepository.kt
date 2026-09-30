package com.antigravity.bitcoinminingtycoon.data

import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.platform.ClockProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repository owning [GameState] with atomic persistence and write-coalescing.
 * Ticker frames update in-memory state; periodic coalescing and immediate transaction saves
 * ensure zero data loss without unbounded per-frame I/O.
 */
class GameRepository(
    private val dataSource: SaveDataSource,
    private val clockProvider: ClockProvider,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val saveMutex = Mutex()
    private var debounceJob: Job? = null

    suspend fun initialize(): GameState {
        val loadedSave = dataSource.saveFlow.first()
        val loadedState = loadedSave.toGameState()
        _gameState.value = loadedState
        return loadedState
    }

    /**
     * Ticker updates in-memory state at 4-10Hz and schedules a coalesced periodic save.
     */
    fun updateInMemory(newState: GameState) {
        _gameState.value = newState
        scheduleCoalescedSave()
    }

    /**
     * Critical player transaction (purchase, sell, prestige, claim, settings change)
     * triggers an immediate persisted save.
     */
    suspend fun saveImmediate(state: GameState = _gameState.value): GameState {
        saveMutex.withLock {
            debounceJob?.cancel()
            _gameState.value = state
            val save = GameSave.fromGameState(state, clockProvider.wallMillis())
            dataSource.update { save }
        }
        return _gameState.value
    }

    /**
     * Schedules a coalesced background save after 10 seconds of passive mining.
     */
    private fun scheduleCoalescedSave() {
        if (debounceJob?.isActive == true) return

        debounceJob = coroutineScope.launch {
            delay(10_000L) // Coalesce per-tick updates to at most once per 10s
            saveMutex.withLock {
                val current = _gameState.value
                val save = GameSave.fromGameState(current, clockProvider.wallMillis())
                dataSource.update { save }
            }
        }
    }

    /**
     * Flushes any pending unpersisted state (e.g. on app background lifecycle).
     */
    suspend fun flush() {
        saveImmediate(_gameState.value)
    }

    /**
     * Destructive reset operation.
     */
    suspend fun resetAllData(): GameState {
        saveMutex.withLock {
            debounceJob?.cancel()
            dataSource.clear()
            val freshState = GameState(lastSaveWallMillis = clockProvider.wallMillis())
            _gameState.value = freshState
            dataSource.update { GameSave.fromGameState(freshState, clockProvider.wallMillis()) }
        }
        return _gameState.value
    }
}
