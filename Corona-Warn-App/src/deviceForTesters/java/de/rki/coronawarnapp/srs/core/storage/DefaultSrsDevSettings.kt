package de.rki.coronawarnapp.srs.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.srs.core.SrsDevSettingsDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSrsDevSettings @Inject constructor(
    @SrsDevSettingsDataStore private val dataStore: DataStore<Preferences>
) : SrsDevSettings {

    private val dataStoreFlow = dataStore.data
        .catch { e ->
            Timber.tag(SrsSubmissionSettings.TAG).e(e, "Failed to read Srs DEV settings")
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }

    override val checkLocalPrerequisites = dataStoreFlow.map { prefs ->
        prefs[SRS_LOCAL_PREREQUISITES] ?: true
    }

    override val forceAndroidIdAcceptance = dataStoreFlow.map { prefs ->
        prefs[SRS_ANDROID_ID_ACCEPTANCE] ?: false
    }

    override val deviceTimeState = dataStoreFlow.map { prefs ->
        ConfigData.DeviceTimeState.values().find { it.key == prefs[SRS_DEVICE_TIME_STATE] }
    }

    override suspend fun checkLocalPrerequisites(check: Boolean) {
        dataStore.edit { prefs ->
            prefs[SRS_LOCAL_PREREQUISITES] = check
        }
    }

    override suspend fun forceAndroidIdAcceptance(force: Boolean) {
        dataStore.edit { prefs ->
            prefs[SRS_ANDROID_ID_ACCEPTANCE] = force
        }
    }

    override suspend fun deviceTimeState(state: ConfigData.DeviceTimeState?) {
        dataStore.edit { prefs ->
            if (state != null) {
                prefs[SRS_DEVICE_TIME_STATE] = state.key
            } else {
                prefs.remove(SRS_DEVICE_TIME_STATE)
            }
        }
    }

    companion object {
        internal val SRS_LOCAL_PREREQUISITES = booleanPreferencesKey("srs.dev.settings.checkLocalPrerequisites")
        internal val SRS_ANDROID_ID_ACCEPTANCE = booleanPreferencesKey("srs.dev.settings.forceAndroidIdAcceptance")
        internal val SRS_DEVICE_TIME_STATE = stringPreferencesKey("srs.dev.settings.deviceTimeState")
    }
}
