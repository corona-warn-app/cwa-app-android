package de.rki.coronawarnapp.appconfig.internal

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.sources.fallback.DefaultAppConfigSource
import de.rki.coronawarnapp.appconfig.sources.local.LocalAppConfigSource
import de.rki.coronawarnapp.appconfig.sources.remote.RemoteAppConfigSource
import de.rki.coronawarnapp.util.TimeStamper
import timber.log.Timber
import javax.inject.Inject

@Reusable
class AppConfigSource @Inject constructor(
    private val remoteAppConfigSource: RemoteAppConfigSource,
    private val localAppConfigSource: LocalAppConfigSource,
    private val defaultAppConfigSource: DefaultAppConfigSource,
    private val timeStamper: TimeStamper
) {

    suspend fun getConfigData(): ConfigData {
        Timber.tag(TAG).d("getConfigData()")

        val localConfig = localAppConfigSource.getConfigData()
        if (localConfig != null && localConfig.isValid(timeStamper.nowUTC)) {
            Timber.tag(TAG).d("Returning local config, still valid.")
            return localConfig
        } else {
            Timber.tag(TAG).d("Local app config was unavailable(${localConfig == null} or invalid.")
        }

        val remoteConfig = remoteAppConfigSource.getConfigData()

        return when {
            remoteConfig != null -> {
                Timber.tag(TAG).d("Returning remote config.")
                remoteConfig
            }
            localConfig != null -> {
                Timber.tag(TAG).d("Remote config was unavailable, returning local config, even if expired.")
                localConfig
            }
            else -> {
                Timber.tag(TAG).w("Remote & Local config available! Returning DEFAULT!")
                defaultAppConfigSource.getConfigData()
            }
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).d("clear()")
        remoteAppConfigSource.clear()
        localAppConfigSource.clear()
    }

    companion object {
        private const val TAG = "AppConfigSource"
    }
}
