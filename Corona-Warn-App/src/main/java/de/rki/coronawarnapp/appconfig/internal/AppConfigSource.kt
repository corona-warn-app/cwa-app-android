package de.rki.coronawarnapp.appconfig.internal

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.ConfigData.DeviceTimeState.CORRECT
import de.rki.coronawarnapp.appconfig.sources.fallback.DefaultAppConfigSource
import de.rki.coronawarnapp.appconfig.sources.local.LocalAppConfigSource
import de.rki.coronawarnapp.appconfig.sources.remote.RemoteAppConfigSource
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class AppConfigSource @Inject constructor(
    private val remoteAppConfigSource: RemoteAppConfigSource,
    private val localAppConfigSource: LocalAppConfigSource,
    private val defaultAppConfigSource: DefaultAppConfigSource,
    private val cwaSettings: CWASettings,
    private val timeStamper: TimeStamper
) {

    suspend fun getConfigData(offlineMode: Boolean = false): ConfigData {
        Timber.tag(TAG).d("getConfigData(offlineMode=$offlineMode)")

        val localConfig = localAppConfigSource.getConfigData()
        val nowUTC = timeStamper.nowUTC
        Timber.tag(TAG).d("nowUTC=%s localConfig.updatedAt=%s", nowUTC, localConfig?.updatedAt)

        if (localConfig != null && localConfig.isValid(nowUTC)) {
            Timber.tag(TAG).d("Returning local config, still valid.")
            return localConfig
        }

        Timber.tag(TAG).d("Local app config was unavailable(${localConfig == null}) or invalid.")
        val remoteConfig = if (!offlineMode) remoteAppConfigSource.getConfigData() else null

        return when {
            remoteConfig != null -> {
                Timber.tag(TAG).d("Returning remote config.")
                onRemoteConfigAvailable(remoteConfig)
                remoteConfig
            }
            localConfig != null -> {
                Timber.tag(TAG).d("Remote config was unavailable, returning local config, even if expired.")
                localConfig.copy(localOffset = Duration.ZERO)
            }
            else -> {
                Timber.tag(TAG).w("Remote & Local config unavailable! Returning DEFAULT!")
                defaultAppConfigSource.getConfigData()
            }
        }
    }

    private suspend fun onRemoteConfigAvailable(remoteConfig: ConfigData) {
        if (!remoteConfig.isDeviceTimeCorrect) {
            Timber.tag(TAG).w(
                "Device time is incorrect, offset=%dmin",
                remoteConfig.localOffset.toMinutes()
            )
        }
        if (remoteConfig.isDeviceTimeCorrect && cwaSettings.wasDeviceTimeIncorrectAcknowledged.first()) {
            Timber.tag(TAG).i("Resetting previous incorrect device time acknowledgement.")
            cwaSettings.updateWasDeviceTimeIncorrectAcknowledged(false)
        }
        if (remoteConfig.deviceTimeState == CORRECT && cwaSettings.firstReliableDeviceTime.first() == Instant.EPOCH) {
            Timber.tag(TAG).i("Setting firstReliableDeviceTime to NOW (UTC). ")
            cwaSettings.updateFirstReliableDeviceTime(timeStamper.nowUTC)
        }
        if (remoteConfig.deviceTimeState != cwaSettings.lastDeviceTimeStateChangeState.first()) {
            Timber.tag(TAG).i(
                "New device time state, saving timestamp (old=%s(%s), new=%s#)",
                cwaSettings.lastDeviceTimeStateChangeState,
                cwaSettings.lastDeviceTimeStateChangeAt,
                remoteConfig.deviceTimeState
            )
            cwaSettings.updateLastDeviceTimeStateChangeState(remoteConfig.deviceTimeState)
            cwaSettings.updateLastDeviceTimeStateChangeAt(timeStamper.nowUTC)
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).d("clear()")
        localAppConfigSource.clear()
        remoteAppConfigSource.clear()
    }

    companion object {
        private const val TAG = "AppConfigSource"
    }
}
