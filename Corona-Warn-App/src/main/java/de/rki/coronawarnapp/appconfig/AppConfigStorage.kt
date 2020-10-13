package de.rki.coronawarnapp.appconfig

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigStorage @Inject constructor(
    @AppContext context: Context
) {
    private val configDir = File(context.filesDir, "appconfig_storage")
    private val configFile = File(configDir, "appconfig")
    private val mutex = Mutex()

    suspend fun isAppConfigAvailable(): Boolean = mutex.withLock {
        configFile.exists() && configFile.length() > MIN_VALID_CONFIG_BYTES
    }

    suspend fun getAppConfigRaw(): ByteArray? = mutex.withLock {
        Timber.v("get() AppConfig")
        if (!configFile.exists()) return null

        val value = configFile.readBytes()
        Timber.v("Read AppConfig of size %s and date %s", value.size, configFile.lastModified())
        return value
    }

    suspend fun setAppConfigRaw(value: ByteArray?): Unit = mutex.withLock {
        Timber.v("set(...) AppConfig: %dB", value?.size)

        if (configDir.mkdirs()) Timber.v("Parent folder created.")

        if (configFile.exists()) {
            Timber.v("Overwriting %d from %s", configFile.length(), configFile.lastModified())
        }
        if (value != null) {
            configFile.writeBytes(value)
        } else {
            configFile.delete()
        }
    }

    companion object {
        // The normal config is ~512B+, we just need to check for a non 0 value, 128 is fine.
        private const val MIN_VALID_CONFIG_BYTES = 128
    }
}
