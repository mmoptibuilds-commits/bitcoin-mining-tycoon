package com.antigravity.bitcoinminingtycoon.data

import com.antigravity.bitcoinminingtycoon.engine.OfflineEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.platform.ClockProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class MutationDurability { COALESCED, IMMEDIATE }

sealed interface MutationResult {
    data class Applied(val state: GameState, val changed: Boolean) : MutationResult
    data class Blocked(val readiness: SaveReadiness) : MutationResult
    data class PersistenceFailed(val cause: Throwable) : MutationResult
}

/** Serializes every state transition and makes DataStore the commit point for critical actions. */
class GameRepository(
    private val dataSource: SaveDataSource,
    private val clockProvider: ClockProvider,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _saveReadiness = MutableStateFlow<SaveReadiness>(SaveReadiness.Loading)
    val saveReadiness: StateFlow<SaveReadiness> = _saveReadiness.asStateFlow()

    private val saveMutex = Mutex()
    private var debounceJob: Job? = null
    private var dirty = false

    suspend fun initialize(): GameState = saveMutex.withLock {
        if (_saveReadiness.value == SaveReadiness.Ready) return@withLock _gameState.value
        _saveReadiness.value = SaveReadiness.Loading
        try {
            val loadedSave = dataSource.saveFlow.first()
            if (loadedSave.schemaVersion != GameSave.CURRENT_SCHEMA_VERSION) {
                _saveReadiness.value = SaveReadiness.UnsupportedSchema(loadedSave.schemaVersion)
                return@withLock _gameState.value
            }
            val loadedState = loadedSave.toGameState()
            val now = clockProvider.wallMillis().coerceAtLeast(0L)
            val report = OfflineEngine.calculateOfflineProgress(
                loadedState,
                loadedState.lastSaveWallMillis,
                now
            )
            val withOffline = OfflineEngine.applyOfflineReward(loadedState, report)
            val committedWallMillis = maxOf(loadedState.lastSaveWallMillis, now)
            val committedState = withOffline.copy(lastSaveWallMillis = committedWallMillis)
            dataSource.update { GameSave.fromGameState(committedState, committedWallMillis) }
            (dataSource as? CheckpointedRecoveryDataSource)?.acknowledgeFreshSaveCommitted()
            _gameState.value = committedState
            dirty = false
            _saveReadiness.value = SaveReadiness.Ready
            committedState
        } catch (exception: Throwable) {
            if (exception is CancellationException) throw exception
            val recovery = exception.findRecoveryException()
            _saveReadiness.value = recovery?.readiness ?: SaveReadiness.PersistenceFailed(exception.message ?: exception::class.java.simpleName)
            _gameState.value
        }
    }

    /** Reduces from the latest state while holding the same lock used by ticks and disk writes. */
    suspend fun mutateLatest(
        durability: MutationDurability,
        reducer: (GameState) -> GameState
    ): MutationResult = saveMutex.withLock {
        val readiness = _saveReadiness.value
        if (readiness != SaveReadiness.Ready) return@withLock MutationResult.Blocked(readiness)

        val before = _gameState.value
        val next = reducer(before)
        if (next == before) return@withLock MutationResult.Applied(before, changed = false)

        if (durability == MutationDurability.COALESCED) {
            _gameState.value = next
            dirty = true
            scheduleCoalescedSave()
            return@withLock MutationResult.Applied(next, changed = true)
        }

        debounceJob?.cancel()
        val committedAt = clockProvider.wallMillis().coerceAtLeast(before.lastSaveWallMillis)
        val committed = next.copy(lastSaveWallMillis = committedAt)
        return@withLock try {
            dataSource.update { GameSave.fromGameState(committed, committedAt) }
            _gameState.value = committed
            dirty = false
            MutationResult.Applied(committed, changed = true)
        } catch (exception: Throwable) {
            if (exception is CancellationException) throw exception
            if (dirty) scheduleCoalescedSave()
            MutationResult.PersistenceFailed(exception)
        }
    }

    private fun scheduleCoalescedSave() {
        if (debounceJob?.isActive == true) return
        debounceJob = coroutineScope.launch {
            delay(COALESCE_DELAY_MILLIS)
            saveMutex.withLock {
                if (dirty && _saveReadiness.value == SaveReadiness.Ready) {
                    try {
                        persistLatestLocked()
                    } catch (exception: Throwable) {
                        if (exception is CancellationException) throw exception
                        _saveReadiness.value = SaveReadiness.PersistenceFailed(
                            exception.message ?: exception::class.java.simpleName
                        )
                    }
                }
            }
        }
    }

    private suspend fun persistLatestLocked() {
        val current = _gameState.value
        val committedAt = maxOf(clockProvider.wallMillis(), current.lastSaveWallMillis)
        val committed = current.copy(lastSaveWallMillis = committedAt)
        dataSource.update { GameSave.fromGameState(committed, committedAt) }
        _gameState.value = committed
        dirty = false
    }

    /** Lifecycle flush persists the newest in-memory state and moves the offline boundary atomically. */
    suspend fun flush() {
        saveMutex.withLock {
            if (_saveReadiness.value != SaveReadiness.Ready) return
            debounceJob?.cancel()
            persistLatestLocked()
        }
    }

    suspend fun resetAllData(): GameState {
        return when (val result = mutateLatest(MutationDurability.IMMEDIATE) {
            GameState(lastSaveWallMillis = clockProvider.wallMillis().coerceAtLeast(0L))
        }) {
            is MutationResult.Applied -> result.state
            is MutationResult.Blocked, is MutationResult.PersistenceFailed -> _gameState.value
        }
    }

    /** Records explicit consent; the new DataStore process applies it after the user restarts the app. */
    suspend fun startNewSaveFromCheckpoint(): MutationResult = saveMutex.withLock {
        if (_saveReadiness.value !is SaveReadiness.CorruptCheckpointed) {
            return@withLock MutationResult.Blocked(_saveReadiness.value)
        }
        val recoverable = dataSource as? CheckpointedRecoveryDataSource
            ?: return@withLock MutationResult.Blocked(_saveReadiness.value)
        val authorized = recoverable.authorizeFreshSaveOnNextLaunch()
        if (!authorized) {
            val blocked = SaveReadiness.CorruptUncheckpointed("Recovery checkpoint is unavailable")
            _saveReadiness.value = blocked
            return@withLock MutationResult.Blocked(blocked)
        }
        _saveReadiness.value = SaveReadiness.RecoveryRestartRequired
        MutationResult.Applied(_gameState.value, changed = true)
    }

    private fun Throwable.findRecoveryException(): SaveRecoveryException? {
        var current: Throwable? = this
        while (current != null) {
            if (current is SaveRecoveryException) return current
            current = current.cause
        }
        return null
    }

    companion object {
        const val COALESCE_DELAY_MILLIS = 10_000L
    }
}
