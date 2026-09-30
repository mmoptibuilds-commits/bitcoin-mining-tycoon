package com.antigravity.bitcoinminingtycoon.data

import androidx.datastore.core.DataStoreFactory
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.engine.BulkMode
import com.antigravity.bitcoinminingtycoon.engine.FleetEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.platform.FakeClockProvider
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class FakeSaveDataSource(initial: GameSave = GameSave()) : SaveDataSource {
    private val _flow = MutableStateFlow(initial)
    override val saveFlow: Flow<GameSave> = _flow.asStateFlow()
    var failWrites: Boolean = false

    override suspend fun update(transform: suspend (GameSave) -> GameSave): GameSave {
        check(!failWrites) { "simulated durable write failure" }
        val updated = transform(_flow.value)
        _flow.value = updated
        return updated
    }

    override suspend fun clear() {
        _flow.value = GameSave()
    }
}

class GameRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val clock = FakeClockProvider()

    @Test
    fun saveImmediate_persistsStateAtomically() = testScope.runTest {
        val dataSource = FakeSaveDataSource()
        val repo = GameRepository(dataSource, clock, this)

        repo.initialize()
        val modified = GameState(
            btc = "0.005",
            usd = "450.00",
            miners = mapOf("ancient_cpu" to 5L)
        )

        repo.mutateLatest(MutationDurability.IMMEDIATE) { modified }

        assertEquals("0.005", repo.gameState.value.btc)
        assertEquals("450.00", repo.gameState.value.usd)
        assertEquals(5L, repo.gameState.value.miners["ancient_cpu"])

        // Simulate process death: create new repo instance pointing to the same storage
        val newRepo = GameRepository(dataSource, clock, this)
        val restored = newRepo.initialize()

        assertEquals("0.005", restored.btc)
        assertEquals("450.00", restored.usd)
        assertEquals(5L, restored.miners["ancient_cpu"])
    }

    @Test
    fun resetAllData_clearsStateToCleanDefault() = testScope.runTest {
        val dataSource = FakeSaveDataSource()
        val repo = GameRepository(dataSource, clock, this)

        repo.initialize()
        repo.mutateLatest(MutationDurability.IMMEDIATE) { GameState(btc = "10.0", usd = "500000.0") }

        val resetState = repo.resetAllData()
        assertEquals("0", resetState.btc)
        assertEquals("0", resetState.usd)
        assertEquals(BigDecimal.ZERO, resetState.btcBigDecimal)
    }

    @Test
    fun concurrentImmediateMutationsReduceFromLatestCommittedState() = testScope.runTest {
        val dataSource = FakeSaveDataSource()
        val repo = GameRepository(dataSource, clock, this)
        repo.initialize()

        val first = async { repo.mutateLatest(MutationDurability.IMMEDIATE) { state ->
            state.copy(stats = state.stats.copy(totalManualTaps = state.stats.totalManualTaps + 1))
        } }
        val second = async { repo.mutateLatest(MutationDurability.IMMEDIATE) { state ->
            state.copy(stats = state.stats.copy(totalManualTaps = state.stats.totalManualTaps + 1))
        } }
        first.await()
        second.await()

        assertEquals(2L, repo.gameState.value.stats.totalManualTaps)
        assertEquals(2L, dataSource.saveFlow.first().stats.totalManualTaps)
    }

    @Test
    fun failedImmediateMutationDoesNotPublishUncommittedSuccess() = testScope.runTest {
        val dataSource = FakeSaveDataSource()
        val repo = GameRepository(dataSource, clock, this)
        repo.initialize()
        val before = repo.gameState.value
        dataSource.failWrites = true

        val result = repo.mutateLatest(MutationDurability.IMMEDIATE) { it.copy(btc = "99") }

        assertTrue(result is MutationResult.PersistenceFailed)
        assertEquals(before, repo.gameState.value)
        assertEquals("0", dataSource.saveFlow.first().btc)
    }

    @Test
    fun initializeMigratesSchemaOneAndCommitsSchemaTwoBeforeReady() = testScope.runTest {
        val directory = java.nio.file.Files.createTempDirectory("bmt-v1-datastore").toFile()
        val dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val saveFile = java.io.File(directory, "datastore/game_save.json")
            saveFile.parentFile?.mkdirs()
            val oldPayload = javaClass.getResourceAsStream("/saves/v1/mid.json")!!.bufferedReader().use { it.readText() }
            saveFile.writeText(oldPayload)
            val checkpoint = SaveRecoveryCheckpoint(directory)
            val serializer = GameSaveSerializer(checkpoint)
            val store = DataStoreFactory.create(serializer = serializer, scope = dataStoreScope, produceFile = { saveFile })
            val source = DataStoreSaveDataSource(store, checkpoint)
            val repository = GameRepository(source, clock, this)

            val loaded = withContext(Dispatchers.Default) {
                withTimeout(5_000L) { repository.initialize() }
            }
            val persisted = withContext(Dispatchers.Default) {
                withTimeout(5_000L) { source.saveFlow.first() }
            }
            val committed = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString(GameSave.serializer(), saveFile.readText())

            assertEquals(SaveReadiness.Ready, repository.saveReadiness.value)
            assertEquals("12.340056789", loaded.btc)
            assertEquals("7654321.125", loaded.manualHashStrength)
            assertEquals(GameSave.CURRENT_SCHEMA_VERSION, persisted.schemaVersion)
            assertEquals("persisted=$persisted raw=${saveFile.readText()}",
                GameSave.CURRENT_SCHEMA_VERSION, committed.schemaVersion)
            assertEquals(loaded.btc, committed.btc)
            assertFalse(java.io.File(directory, "save_recovery/corrupt-save.json").exists())
        } finally {
            // DataStore has no close API; cancel its owned scope before removing the temporary directory.
            dataStoreScope.cancel()
            directory.deleteRecursively()
        }
    }

    @Test
    fun twoConcurrentMaxPurchasesCannotSpendTheSameBalance() = testScope.runTest {
        val miner = Miners.ALL.first()
        val source = FakeSaveDataSource(GameSave(usd = miner.baseCostUsd.toPlainString()))
        val repo = GameRepository(source, clock, this)
        repo.initialize()

        val purchaseOne = async {
            repo.mutateLatest(MutationDurability.IMMEDIATE) { FleetEngine.buyMiner(it, miner.id, BulkMode.MAX) }
        }
        val purchaseTwo = async {
            repo.mutateLatest(MutationDurability.IMMEDIATE) { FleetEngine.buyMiner(it, miner.id, BulkMode.MAX) }
        }
        purchaseOne.await()
        purchaseTwo.await()

        assertEquals(1L, repo.gameState.value.miners[miner.id])
        assertEquals(0, BigDecimal.ZERO.compareTo(repo.gameState.value.usdBigDecimal))
    }

    @Test
    fun startupCreditsShortOfflineGapOnceAndDoesNotShowShortSummary() = testScope.runTest {
        val now = clock.wallMillis()
        val startState = GameState(
            miners = mapOf(Miners.ALL.first().id to 5L),
            lastSaveWallMillis = now - 59_000L
        )
        val source = FakeSaveDataSource(GameSave.fromGameState(startState, startState.lastSaveWallMillis))
        val repository = GameRepository(source, clock, this)

        val firstLoad = repository.initialize()
        val creditedBtc = firstLoad.btc
        assertTrue(GameNumber.fromString(firstLoad.stats.offlineBtc) > BigDecimal.ZERO)
        assertEquals(null, firstLoad.pendingOfflineSummary)
        assertEquals(now, firstLoad.lastSaveWallMillis)

        val restored = GameRepository(source, clock, this).initialize()
        assertEquals(creditedBtc, restored.btc)
        assertEquals(creditedBtc, restored.stats.offlineBtc)
    }

    @Test
    fun startupSummaryAcknowledgementNeverCreditsBtcAgain() = testScope.runTest {
        val now = clock.wallMillis()
        val startState = GameState(
            miners = mapOf(Miners.ALL.first().id to 5L),
            lastSaveWallMillis = now - 3_600_000L
        )
        val source = FakeSaveDataSource(GameSave.fromGameState(startState, startState.lastSaveWallMillis))
        val repository = GameRepository(source, clock, this)
        val credited = repository.initialize()
        val balanceAfterCredit = credited.btc
        assertTrue(credited.pendingOfflineSummary != null)

        repository.mutateLatest(MutationDurability.IMMEDIATE) { it.copy(pendingOfflineSummary = null) }
        val restored = GameRepository(source, clock, this).initialize()

        assertEquals(balanceAfterCredit, restored.btc)
        assertEquals(null, restored.pendingOfflineSummary)
    }

    @Test
    fun recoveryReadinessBlocksEveryStateMutationWithoutTouchingStoredData() = testScope.runTest {
        var writes = 0
        val recovery = SaveRecoveryException(SaveReadiness.CorruptUncheckpointed("checkpoint failed"))
        val source = object : SaveDataSource {
            override val saveFlow = flow<GameSave> { throw recovery }
            override suspend fun update(transform: suspend (GameSave) -> GameSave): GameSave {
                writes += 1
                error("recovery must block writes")
            }
            override suspend fun clear() {
                writes += 1
            }
        }
        val repository = GameRepository(source, clock, this)
        repository.initialize()

        val result = repository.mutateLatest(MutationDurability.IMMEDIATE) { it.copy(btc = "5") }
        assertEquals(SaveReadiness.CorruptUncheckpointed("checkpoint failed"), repository.saveReadiness.value)
        assertTrue(result is MutationResult.Blocked)
        assertEquals(0, writes)
    }

    @Test
    fun failedStartupCreditCommitDoesNotPublishUncommittedOfflineBalances() = testScope.runTest {
        val now = clock.wallMillis()
        val initial = GameSave(
            btc = "7.5",
            lastSaveWallMillis = now - 600_000L,
            miners = mapOf(Miners.ALL.first().id to 5L)
        )
        val source = FakeSaveDataSource(initial).apply { failWrites = true }
        val repository = GameRepository(source, clock, this)

        repository.initialize()

        assertEquals("0", repository.gameState.value.btc)
        assertEquals(SaveReadiness.PersistenceFailed("simulated durable write failure"), repository.saveReadiness.value)
        assertEquals("7.5", source.saveFlow.first().btc)
    }

    @Test
    fun startupWallClockRollbackNeverMovesPersistedBoundaryBackwards() = testScope.runTest {
        val savedAt = clock.wallMillis()
        val source = FakeSaveDataSource(GameSave(lastSaveWallMillis = savedAt + 60_000L))
        val repository = GameRepository(source, clock, this)

        val state = repository.initialize()

        assertEquals(savedAt + 60_000L, state.lastSaveWallMillis)
        assertEquals(savedAt + 60_000L, source.saveFlow.first().lastSaveWallMillis)
    }

    @Test
    fun explicitRecoveryMarkerIsAppliedOnlyOnNextDataStoreLaunch() = testScope.runTest {
        val directory = java.nio.file.Files.createTempDirectory("bmt-datastore-recovery").toFile()
        val restartStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val saveFile = java.io.File(directory, "datastore/game_save.json")
            saveFile.parentFile?.mkdirs()
            val originalPayload = "{broken-original".toByteArray()
            saveFile.writeBytes(originalPayload)
            val checkpoint = SaveRecoveryCheckpoint(directory)
            val originalSerializer = GameSaveSerializer(checkpoint)
            val corrupt = try {
                originalSerializer.readFrom(java.io.ByteArrayInputStream(originalPayload))
                error("Malformed save must stay blocked before confirmation")
            } catch (failure: androidx.datastore.core.CorruptionException) {
                failure.findSaveRecoveryException()!!
            }
            assertTrue(corrupt.readiness is SaveReadiness.CorruptCheckpointed)
            assertTrue(saveFile.readBytes().contentEquals(originalPayload))

            val corruptSource = object : SaveDataSource, CheckpointedRecoveryDataSource {
                override val saveFlow = flow<GameSave> {
                    throw SaveRecoveryException(SaveReadiness.CorruptCheckpointed("Malformed payload"))
                }
                override suspend fun update(transform: suspend (GameSave) -> GameSave): GameSave =
                    error("A corrupt save must not be written before explicit confirmation")
                override suspend fun clear(): Unit = error("A corrupt save must not be cleared implicitly")
                override suspend fun authorizeFreshSaveOnNextLaunch(): Boolean =
                    checkpoint.authorizeFreshSaveOnNextLaunch()
                override fun acknowledgeFreshSaveCommitted() = checkpoint.acknowledgeFreshSaveCommitted()
            }
            val originalRepository = GameRepository(corruptSource, clock, this)
            originalRepository.initialize()
            assertTrue(originalRepository.saveReadiness.value is SaveReadiness.CorruptCheckpointed)

            val confirmation = originalRepository.startNewSaveFromCheckpoint()
            assertTrue(confirmation is MutationResult.Applied)
            assertEquals(SaveReadiness.RecoveryRestartRequired, originalRepository.saveReadiness.value)
            assertTrue(saveFile.readBytes().contentEquals(originalPayload))
            assertTrue(checkpoint.hasFreshSaveAuthorization())

            val relaunchedSerializer = GameSaveSerializer(checkpoint, checkpoint.hasFreshSaveAuthorization())
            assertEquals(
                GameSave(),
                relaunchedSerializer.readFrom(java.io.ByteArrayInputStream(originalPayload))
            )
            val store = DataStoreFactory.create(
                serializer = relaunchedSerializer,
                scope = restartStoreScope,
                produceFile = { saveFile }
            )
            val source = DataStoreSaveDataSource(store, checkpoint)
            val repository = GameRepository(source, clock, this)

            val recovery = withContext(Dispatchers.Default) {
                withTimeout(5_000L) { repository.initialize() }
            }
            assertEquals(SaveReadiness.Ready, repository.saveReadiness.value)
            assertEquals("0", recovery.btc)
            val migrated = withContext(Dispatchers.Default) {
                withTimeout(5_000L) { source.saveFlow.first() }
            }
            assertEquals(GameSave.CURRENT_SCHEMA_VERSION, migrated.schemaVersion)
            assertFalse(checkpoint.hasFreshSaveAuthorization())
            assertTrue(java.io.File(directory, "save_recovery/corrupt-save.json").readBytes().contentEquals(originalPayload))
        } finally {
            restartStoreScope.cancel()
            directory.deleteRecursively()
        }
    }
}
