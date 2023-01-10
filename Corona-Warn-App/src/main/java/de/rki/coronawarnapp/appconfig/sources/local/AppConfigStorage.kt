package de.rki.coronawarnapp.appconfig.sources.local

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.appconfig.internal.InternalConfigData
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.coronawarnapp.util.serialization.writeValue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigStorage @Inject constructor(
    @AppContext context: Context,
    @BaseJackson private val mapper: ObjectMapper
) {

    private val configDir = File(context.filesDir, "appconfig_storage")

    // This is just the raw protobuf data
    private val legacyConfigFile = File(configDir, "appconfig")
    private val configFile = File(configDir, "appconfig.json")
    private val mutex = Mutex()

    suspend fun getStoredConfig(): InternalConfigData? = mutex.withLock {
        Timber.v("get() AppConfig")

        if (!configFile.exists() && legacyConfigFile.exists()) {
            Timber.i("Returning legacy config.")
            return@withLock try {
                InternalConfigData(
                    rawData = legacyConfigFile.readBytes(),
                    serverTime = Instant.ofEpochMilli(legacyConfigFile.lastModified()),
                    localOffset = Duration.ZERO,
                    etag = "legacy.migration",
                    cacheValidity = Duration.ofSeconds(0)
                )
            } catch (e: Exception) {
                Timber.e(e, "Legacy config exits but couldn't be read.")
                null
            }
        }

        return@withLock try {
            mapper.readValue<InternalConfigData>(configFile).also {
                requireNotNull(it.rawData)
                Timber.v("Loaded stored config, serverTime=%s", it.serverTime)
            }
        } catch (e: Exception) {
            Timber.e(e, "Couldn't load config.")
            if (configFile.delete()) Timber.w("Config file was deleted.")
            null
        }
    }

    suspend fun setStoredConfig(value: InternalConfigData?): Unit = mutex.withLock {
        Timber.v("set(...) AppConfig: %s", value)

        if (configDir.mkdirs()) Timber.v("Parent folder created.")

        if (configFile.exists()) {
            Timber.v("Overwriting %d from %s", configFile.length(), configFile.lastModified())
        }

        if (legacyConfigFile.exists() && legacyConfigFile.delete()) {
            Timber.i("Legacy config file deleted, superseeded.")
        }

        if (value == null) {
            if (configFile.delete()) Timber.d("Config file was deleted (value=null).")
            return
        }

        try {
            mapper.writeValue(value, configFile)
        } catch (e: Exception) {
            // We'll not rethrow as we could still keep working just with the remote config,
            // but we will notify the user.
            Timber.e(e, "Failed to config data to local storage.")
            e.report(ExceptionCategory.INTERNAL)
        }
    }
}
