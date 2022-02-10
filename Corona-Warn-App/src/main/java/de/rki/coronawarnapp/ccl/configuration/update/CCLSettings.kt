package de.rki.coronawarnapp.ccl.configuration.update

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.Reusable
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.joda.time.Instant
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@Reusable
class CCLSettings @Inject constructor(
    @CCLSettingsDataStore private val dataStore: DataStore<Preferences>,
    @AppScope private val appScope: CoroutineScope
) {

    private val dataStoreFlow = dataStore.data
        .catch { e ->
            Timber.tag(TAG).e(e, "Failed to read CCL Settings")
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }

    /**
     * @returns the instant of the last time the CCL Settings were updated, or null if they haven't been updated yet
     */
    suspend fun getLastExecutionTime(): Instant? {
        Timber.d("Try to get last CCL execution time from data store.")
        return dataStoreFlow.map { prefs ->
            prefs[LAST_EXECUTION_TIME_KEY]
        }.map { lastExecutionTime ->
            if (lastExecutionTime != null) {
                Timber.tag(TAG).d("Last CCL execution time found in data store: %s", lastExecutionTime)
                Instant.ofEpochSecond(lastExecutionTime)
            } else {
                Timber.tag(TAG).d("No CCL execution time stored yet.")
                null
            }
        }.first()
    }

    /**
     * Stores the execution time of the CCL update in the data store
     */
    fun setExecutionTimeToNow(executionTime: Instant = Instant.now()) = appScope.launch {
        Timber.tag(TAG).d("Storing executionTime to CCL settings data store: %s", executionTime)
        runCatching {
            dataStore.edit { prefs ->
                prefs[LAST_EXECUTION_TIME_KEY] = executionTime.seconds
            }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "Failed to set ccl execution time.")
        }
    }

    /**
     * @returns admission scenario identifier, by default empty string
     */
    suspend fun getAdmissionScenarioId(): String = dataStoreFlow
        .map { prefs -> prefs[ADMISSION_SCENARIO_ID_KEY].orEmpty() }
        .first()

    /**
     * Stores admission scenario identifier
     */
    fun setAdmissionScenarioId(admissionScenarioId: String) = appScope.launch {
        runCatching {
            dataStore.edit { prefs -> prefs[ADMISSION_SCENARIO_ID_KEY] = admissionScenarioId }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "Failed to set ccl execution time.")
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).d("Clearing CCL Settings data store.")
        runCatching {
            dataStore.edit { prefs -> prefs.clear() }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "Failed to clear ccl settings.")
        }
    }

    companion object {
        internal val LAST_EXECUTION_TIME_KEY = longPreferencesKey("ccl.settings.lastexecutiontime")
        internal val ADMISSION_SCENARIO_ID_KEY = stringPreferencesKey("ccl.settings.admissionScenarioId")

        private val TAG = tag<CCLSettings>()
    }
}
