package de.rki.coronawarnapp.ccl.configuration.update

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import dagger.Reusable
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CCLSettings @Inject constructor(
    @CCLSettingsDataStore private val dataStore: DataStore<Preferences>,
    @AppScope private val appScope: CoroutineScope
) {

    /**
     * @returns the instant of the last time the CCL Settings were updated, or null if they haven't been updated yet
     */
    suspend fun getLastExecutionTime(): Instant? {
        Timber.d("Try to get last CCL execution time from data store.")
        return dataStore.data.map { prefs ->
            prefs[LAST_EXECUTION_TIME_KEY]
        }.map { lastExecutionTime ->
            if (lastExecutionTime != null) {
                Timber.d("Last CCL execution time found in data store: %s", lastExecutionTime)
                Instant.ofEpochSecond(lastExecutionTime)
            } else {
                Timber.d("No CCL execution time stored yet.")
                null
            }
        }.first()
    }

    /**
     * Stores the execution time of the CCL update in the data store
     */
    fun setExecutionTimeToNow(executionTime: Instant = Instant.now()) = appScope.launch {
        Timber.d("Storing executionTime to CCL settings data store: %s", executionTime)
        dataStore.edit { prefs ->
            prefs[LAST_EXECUTION_TIME_KEY] = executionTime.seconds
        }
    }

    suspend fun clear() {
        Timber.d("Clearing CCL Settings data store.")
        dataStore.edit { prefs -> prefs.clear() }
    }

    companion object {
        internal val LAST_EXECUTION_TIME_KEY = longPreferencesKey("ccl.settings.lastexecutiontime")
    }
}
