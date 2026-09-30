package com.antigravity.bitcoinminingtycoon.data

import java.io.File
import java.io.FileOutputStream

/** Stores one bounded raw save before recovery is allowed to replace it. */
class SaveRecoveryCheckpoint(private val filesDir: File) {

    private val directory: File get() = File(filesDir, "save_recovery")
    private val destination: File get() = File(directory, FILE_NAME)
    private val authorization: File get() = File(directory, AUTHORIZATION_FILE_NAME)

    fun checkpoint(raw: ByteArray): Boolean {
        if (raw.size > MAX_BYTES) return false
        if (!directory.exists() && !directory.mkdirs()) return false

        if (destination.exists()) {
            return runCatching {
                destination.length() == raw.size.toLong() && destination.readBytes().contentEquals(raw)
            }.getOrDefault(false)
        }

        val temporary = File(directory, "$FILE_NAME.tmp")
        return try {
            FileOutputStream(temporary).use { stream ->
                stream.write(raw)
                stream.fd.sync()
            }
            if (!temporary.renameTo(destination)) {
                temporary.delete()
                false
            } else {
                true
            }
        } catch (_: Exception) {
            temporary.delete()
            false
        }
    }

    /** Persist explicit user consent for the next process launch, after confirming a checkpoint exists. */
    fun authorizeFreshSaveOnNextLaunch(): Boolean {
        if (!destination.isFile || destination.length() > MAX_BYTES) return false
        if (hasFreshSaveAuthorization()) return true
        if (!directory.exists() && !directory.mkdirs()) return false
        val temporary = File(directory, "$AUTHORIZATION_FILE_NAME.tmp")
        return try {
            FileOutputStream(temporary).use { stream ->
                stream.write(AUTHORIZATION_TOKEN.toByteArray(Charsets.UTF_8))
                stream.fd.sync()
            }
            if (!temporary.renameTo(authorization)) {
                temporary.delete()
                false
            } else true
        } catch (_: Exception) {
            temporary.delete()
            false
        }
    }

    fun hasFreshSaveAuthorization(): Boolean = try {
        authorization.isFile && authorization.readText(Charsets.UTF_8) == AUTHORIZATION_TOKEN && destination.isFile
    } catch (_: Exception) {
        false
    }

    fun acknowledgeFreshSaveCommitted() {
        authorization.delete()
    }

    companion object {
        const val MAX_BYTES = 4 * 1024 * 1024
        const val FILE_NAME = "corrupt-save.json"
        const val AUTHORIZATION_FILE_NAME = "start-new-save-on-next-launch"
        private const val AUTHORIZATION_TOKEN = "explicit-empty-save-v1"
    }
}
