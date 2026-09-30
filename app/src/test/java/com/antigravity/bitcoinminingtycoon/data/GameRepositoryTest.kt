package com.antigravity.bitcoinminingtycoon.data

import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.platform.FakeClockProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class FakeSaveDataSource(initial: GameSave = GameSave()) : SaveDataSource {
    private val _flow = MutableStateFlow(initial)
    override val saveFlow: Flow<GameSave> = _flow.asStateFlow()

    override suspend fun update(transform: suspend (GameSave) -> GameSave): GameSave {
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

        repo.saveImmediate(modified)

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
        repo.saveImmediate(GameState(btc = "10.0", usd = "500000.0"))

        val resetState = repo.resetAllData()
        assertEquals("0", resetState.btc)
        assertEquals("0", resetState.usd)
        assertEquals(BigDecimal.ZERO, resetState.btcBigDecimal)
    }
}
