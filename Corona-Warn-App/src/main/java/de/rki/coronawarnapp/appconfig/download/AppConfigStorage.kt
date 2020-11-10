package de.rki.coronawarnapp.appconfig.download

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.adapter.DurationAdapter
import de.rki.coronawarnapp.util.serialization.adapter.InstantAdapter
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.util.serialization.toJson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigStorage @Inject constructor(
    @AppContext context: Context,
    private val timeStamper: TimeStamper,
    @BaseGson private val baseGson: Gson
) {

    private val gson by lazy {
        baseGson.newBuilder()
            .registerTypeAdapter(Instant::class.java, InstantAdapter())
            .registerTypeAdapter(Duration::class.java, DurationAdapter())
            .create()
    }
    private val configDir = File(context.filesDir, "appconfig_storage")

    // This is just the raw protobuf data
    private val legacyConfigFile = File(configDir, "appconfig")
    private val configFile = File(configDir, "appconfig.json")
    private val mutex = Mutex()

    suspend fun getStoredConfig(): ConfigDownload? = mutex.withLock {
        Timber.v("get() AppConfig")

        if (!configFile.exists() && legacyConfigFile.exists()) {
            Timber.i("Returning legacy config.")
            return@withLock try {
                ConfigDownload(
                    rawData = legacyConfigFile.readBytes(),
                    serverTime = timeStamper.nowUTC,
                    localOffset = Duration.ZERO,
                    etag = "legacy.migration"
                )
            } catch (e: Exception) {
                Timber.e(e, "Legacy config exits but couldn't be read.")
                null
            }
        }

        return@withLock try {
            gson.fromJson<ConfigDownload>(configFile)
        } catch (e: Exception) {
            Timber.e(e, "Couldn't load config.")
            null
        }
    }

    suspend fun setStoredConfig(value: ConfigDownload?): Unit = mutex.withLock {
        Timber.v("set(...) AppConfig: %s", value)

        if (configDir.mkdirs()) Timber.v("Parent folder created.")

        if (configFile.exists()) {
            Timber.v("Overwriting %d from %s", configFile.length(), configFile.lastModified())
        }

        if (value != null) {
            gson.toJson(value, configFile)

            if (legacyConfigFile.exists()) {
                if (legacyConfigFile.delete()) {
                    Timber.i("Legacy config file deleted, superseeded.")
                }
            }
        } else {
            configFile.delete()
        }
    }
}
