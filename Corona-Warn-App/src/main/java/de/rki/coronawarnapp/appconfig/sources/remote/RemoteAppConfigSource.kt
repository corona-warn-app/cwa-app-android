package de.rki.coronawarnapp.appconfig.sources.remote

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.appconfig.sources.local.AppConfigStorage
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAppConfigSource @Inject constructor(
    private val server: AppConfigServer,
    private val storage: AppConfigStorage,
    private val parser: ConfigParser,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun getConfigData(): ConfigData? = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).v("retrieveConfig()")

        val configDownload = try {
            server.downloadAppConfig()
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to download AppConfig from server .")
            return@withContext null
        }

        return@withContext try {
            parser.parse(configDownload.rawData).let {
                Timber.tag(TAG).d("Got a valid AppConfig from server, saving.")
                storage.setStoredConfig(configDownload)
                ConfigDataContainer(
                    mappedConfig = it,
                    serverTime = configDownload.serverTime,
                    localOffset = configDownload.localOffset,
                    identifier = configDownload.etag,
                    configType = ConfigData.Type.FROM_SERVER,
                    cacheValidity = configDownload.cacheValidity
                )
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to parse AppConfig from server, trying fallback.")
            null
        }
    }

    fun clear() {
        server.clearCache()
    }

    companion object {
        private const val TAG = "AppConfigRetriever"
    }
}
