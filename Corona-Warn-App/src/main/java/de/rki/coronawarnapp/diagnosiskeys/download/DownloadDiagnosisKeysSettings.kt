package de.rki.coronawarnapp.diagnosiskeys.download

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsExposureWindowsDataStore
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadDiagnosisKeysSettings @Inject constructor(
    @BaseGson private val gson: Gson,
    @AnalyticsExposureWindowsDataStore private val dataStore: DataStore<Preferences>
) : Resettable {

    val lastDownloadDays = dataStore.dataRecovering.distinctUntilChanged(
        key = KEY_LAST_DOWNLOAD_DAYS, defaultValue = ""
    ).map { value ->
        if (value.isNotEmpty()) gson.fromJson<LastDownload>(value) else null
    }

    suspend fun updateLastDownloadDays(value: LastDownload?) = dataStore.trySetValue(
        preferencesKey = KEY_LAST_DOWNLOAD_DAYS,
        value = value?.let { gson.toJson(it) } ?: ""
    )

    val lastDownloadHours = dataStore.dataRecovering.distinctUntilChanged(
        key = KEY_LAST_DOWNLOAD_HOURS, defaultValue = ""
    ).map { value ->
        if (value.isNotEmpty()) gson.fromJson<LastDownload>(value) else null
    }

    suspend fun updateLastDownloadHours(value: LastDownload?) = dataStore.trySetValue(
        preferencesKey = KEY_LAST_DOWNLOAD_HOURS,
        value = value?.let { gson.toJson(it) } ?: ""
    )

    val lastVersionCode = dataStore.dataRecovering.distinctUntilChanged(
        key = KEY_LAST_VERSION_CODE, defaultValue = -1L
    )

    suspend fun updateLastVersionCode(value: Long) = dataStore.trySetValue(
        preferencesKey = KEY_LAST_VERSION_CODE, value = value
    )

    /**
     * true if, and only if, since last runtime the app was updated from 1.7 (or earlier) to a version >= 1.8.0
     */
    suspend fun isUpdateToEnfV2(): Boolean {
        return lastVersionCode.first() < VERSION_CODE_FIRST_POSSIBLE_1_8_RELEASE
    }

    suspend fun updateLastVersionCodeToCurrent() {
        updateLastVersionCode(
            BuildConfigWrap.VERSION_CODE.also {
                Timber.d("lastVersionCode updated to %d", it)
            }
        )
    }

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }

    data class LastDownload(
        val startedAt: Instant,
        val finishedAt: Instant? = null,
        val successful: Boolean = false,
        val newData: Boolean = false
    )

    companion object {
        val KEY_LAST_DOWNLOAD_DAYS = stringPreferencesKey("download.last.days")
        val KEY_LAST_DOWNLOAD_HOURS = stringPreferencesKey("download.last.hours")
        val KEY_LAST_VERSION_CODE = longPreferencesKey("download.task.last.versionCode")
        internal const val VERSION_CODE_FIRST_POSSIBLE_1_8_RELEASE = 1080000
    }
}
