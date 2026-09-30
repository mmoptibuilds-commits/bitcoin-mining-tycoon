package com.antigravity.bitcoinminingtycoon.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.antigravity.bitcoinminingtycoon.data.DataStoreSaveDataSource
import com.antigravity.bitcoinminingtycoon.data.GameRepository
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.data.GameSaveSerializer
import com.antigravity.bitcoinminingtycoon.platform.AudioTrackSoundPlayer
import com.antigravity.bitcoinminingtycoon.platform.SoundPlayer
import com.antigravity.bitcoinminingtycoon.platform.SystemClockProvider
import java.io.File

class BitcoinMiningTycoonApp : Application() {

    lateinit var clockProvider: SystemClockProvider
        private set

    lateinit var repository: GameRepository
        private set

    lateinit var soundPlayer: SoundPlayer
        private set

    override fun onCreate() {
        super.onCreate()
        clockProvider = SystemClockProvider()
        val dataStore: DataStore<GameSave> = DataStoreFactory.create(
            serializer = GameSaveSerializer,
            produceFile = { File(filesDir, "datastore/game_save.json") }
        )
        val dataSource = DataStoreSaveDataSource(dataStore)
        repository = GameRepository(dataSource, clockProvider)
        soundPlayer = AudioTrackSoundPlayer(
            isSoundEnabled = { repository.gameState.value.settings.soundEnabled }
        )
    }
}
