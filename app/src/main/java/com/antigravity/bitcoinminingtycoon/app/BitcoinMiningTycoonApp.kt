package com.antigravity.bitcoinminingtycoon.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.antigravity.bitcoinminingtycoon.data.DataStoreSaveDataSource
import com.antigravity.bitcoinminingtycoon.data.GameRepository
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.data.GameSaveSerializer
import com.antigravity.bitcoinminingtycoon.data.SaveRecoveryCheckpoint
import com.antigravity.bitcoinminingtycoon.platform.AudioTrackSoundPlayer
import com.antigravity.bitcoinminingtycoon.platform.AndroidHaptics
import com.antigravity.bitcoinminingtycoon.platform.Haptics
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

    lateinit var haptics: Haptics
        private set

    override fun onCreate() {
        super.onCreate()
        clockProvider = SystemClockProvider()
        val recoveryCheckpoint = SaveRecoveryCheckpoint(filesDir)
        val serializer = GameSaveSerializer(
            recoveryCheckpoint = recoveryCheckpoint,
            freshSaveAuthorizedOnLaunch = recoveryCheckpoint.hasFreshSaveAuthorization()
        )
        val dataStore: DataStore<GameSave> = DataStoreFactory.create(
            serializer = serializer,
            produceFile = { File(filesDir, "datastore/game_save.json") }
        )
        val dataSource = DataStoreSaveDataSource(dataStore, recoveryCheckpoint)
        repository = GameRepository(dataSource, clockProvider)
        haptics = AndroidHaptics(applicationContext)
        soundPlayer = AudioTrackSoundPlayer(
            isSoundEnabled = { repository.gameState.value.settings.soundEnabled }
        )
    }
}
