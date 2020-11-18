package de.rki.coronawarnapp.appconfig.sources.local

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAppConfigSource @Inject constructor(
    private val storage: AppConfigStorage,
    private val parser: ConfigParser,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun getConfigData(): ConfigData? = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).v("retrieveConfig()")

        val configDownload = storage.getStoredConfig()
        if (configDownload == null) {
            Timber.tag(TAG).d("No stored config available.")
            return@withContext null
        }

        return@withContext try {
            configDownload.let {
                ConfigDataContainer(
                    mappedConfig = parser.parse(it.rawData),
                    serverTime = it.serverTime,
                    localOffset = it.localOffset,
                    identifier = it.etag,
                    configType = ConfigData.Type.LAST_RETRIEVED,
                    cacheValidity = it.cacheValidity
                )
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Fallback config exists but could not be parsed!")
            null
        }
    }

    suspend fun clear() {
        storage.setStoredConfig(null)
    }

    companion object {
        private const val TAG = "LocalAppConfigSource"
    }
}
