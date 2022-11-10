package de.rki.coronawarnapp.appconfig.sources.local

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAppConfigSource @Inject constructor(
    private val storage: AppConfigStorage,
    private val parser: ConfigParser,
    private val dispatcherProvider: DispatcherProvider,
    private val srsDevSettings: SrsDevSettings,
) {

    suspend fun getConfigData(): ConfigDataContainer? = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).v("retrieveConfig()")

        val internalConfigData = storage.getStoredConfig()
        if (internalConfigData == null) {
            Timber.tag(TAG).d("No stored config available.")
            return@withContext null
        }

        return@withContext try {
            internalConfigData.let {
                ConfigDataContainer(
                    mappedConfig = parser.parse(it.rawData),
                    serverTime = it.serverTime,
                    localOffset = it.localOffset,
                    identifier = it.etag,
                    configType = ConfigData.Type.LAST_RETRIEVED,
                    cacheValidity = it.cacheValidity,
                    devDeviceTimeDeviceState = srsDevSettings.deviceTimeState(),
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
