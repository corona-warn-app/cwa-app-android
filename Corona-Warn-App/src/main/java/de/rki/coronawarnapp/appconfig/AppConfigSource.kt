package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.download.AppConfigServer
import de.rki.coronawarnapp.appconfig.download.AppConfigStorage
import de.rki.coronawarnapp.appconfig.download.DefaultAppConfigSource
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigSource @Inject constructor(
    private val server: AppConfigServer,
    private val storage: AppConfigStorage,
    private val parser: ConfigParser,
    private val defaultAppConfig: DefaultAppConfigSource,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun retrieveConfig(): ConfigData = withContext(dispatcherProvider.IO) {
        Timber.v("retrieveConfig()")
        val (serverBytes, serverError) = try {
            server.downloadAppConfig() to null
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to download AppConfig from server .")
            null to e
        }

        var parsedConfig: ConfigData? = serverBytes?.let { configDownload ->
            try {
                parser.parse(configDownload.rawData).let {
                    Timber.tag(TAG).d("Got a valid AppConfig from server, saving.")
                    storage.setStoredConfig(configDownload)
                    DefaultConfigData(
                        mappedConfig = it,
                        serverTime = configDownload.serverTime,
                        localOffset = configDownload.localOffset,
                        configType = ConfigData.Type.FROM_SERVER
                    )
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to parse AppConfig from server, trying fallback.")
                null
            }
        }

        if (parsedConfig == null) {
            parsedConfig = storage.getStoredConfig()?.let { storedDownloadConfig ->
                try {
                    storedDownloadConfig.let {
                        DefaultConfigData(
                            mappedConfig = parser.parse(it.rawData),
                            serverTime = it.serverTime,
                            localOffset = it.localOffset,
                            configType = ConfigData.Type.FALLBACK_LAST_RETRIEVED
                        )
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Fallback config exists but could not be parsed!")
                    throw e
                }
            }
        }

        if (parsedConfig == null) {
            Timber.tag(TAG).w("Current or fallback config was unavailable, using default.")
            parsedConfig = DefaultConfigData(
                mappedConfig = parser.parse(defaultAppConfig.getRawDefaultConfig()),
                serverTime = Instant.EPOCH,
                localOffset = Duration.standardHours(12),
                configType = ConfigData.Type.FALLBACK_LOCAL_DEFAULT
            )
        }

        return@withContext parsedConfig
    }

    suspend fun clear() {
        storage.setStoredConfig(null)

        server.clearCache()
    }

    companion object {
        private const val TAG = "AppConfigRetriever"
    }
}
