package de.rki.coronawarnapp.appconfig.download

import android.content.Context
import de.rki.coronawarnapp.util.HashExtensions.hashToMD5
import de.rki.coronawarnapp.util.HashExtensions.toMD5
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigStorage @Inject constructor(
    @AppContext context: Context,
    private val timeStamper: TimeStamper
) {
    private val configDir = File(context.filesDir, "appconfig_storage")
    private val configFile = File(configDir, "appconfig")
    private val mutex = Mutex()

    suspend fun getStoredConfig(): StoredConfig? = mutex.withLock {
        Timber.v("get() AppConfig")
        if (!configFile.exists()) return null

        val value = configFile.readBytes()
        Timber.v("Read AppConfig of size %s and date %s", value.size, configFile.lastModified())
        return StoredConfig(
            rawData = value,
            storedAt = Instant.ofEpochMilli(configFile.lastModified())
        )
    }

    suspend fun setStoredConfig(value: ByteArray?): Unit = mutex.withLock {
        Timber.v("set(...) AppConfig: %dB", value?.size)

        if (configDir.mkdirs()) Timber.v("Parent folder created.")

        val oldMD5 = try {
            configFile.hashToMD5()
        } catch (e: Exception) {
            null
        }
        val newMD5 = value?.toMD5()
        if (newMD5 == oldMD5) {
            Timber.v("Checksums match, no need to update(new=%s, old=%s)", newMD5, oldMD5)
            return@withLock
        }

        if (configFile.exists()) {
            Timber.v("Overwriting %d from %s", configFile.length(), configFile.lastModified())
        }

        if (value != null) {
            configFile.writeBytes(value)
            configFile.setLastModified(timeStamper.nowUTC.millis)
        } else {
            configFile.delete()
        }
    }

    data class StoredConfig(
        val rawData: ByteArray,
        val storedAt: Instant
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StoredConfig

            if (!rawData.contentEquals(other.rawData)) return false

            return true
        }

        override fun hashCode(): Int = rawData.contentHashCode()
    }
}
