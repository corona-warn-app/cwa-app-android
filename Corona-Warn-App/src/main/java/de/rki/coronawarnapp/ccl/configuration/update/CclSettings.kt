package de.rki.coronawarnapp.ccl.configuration.update

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.Reusable
import de.rki.coronawarnapp.ccl.dccadmission.model.Scenario
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.joda.time.Instant
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@Reusable
class CclSettings @Inject constructor(
    @CclSettingsDataStore private val dataStore: DataStore<Preferences>,
    @AppScope private val appScope: CoroutineScope,
) : Resettable {

    private val dataStoreFlow = dataStore.data
        .catch { e ->
            Timber.tag(TAG).e(e, "Failed to read CclSettings")
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }

    val admissionScenarioId = dataStoreFlow.map { prefs ->
        prefs[ADMISSION_SCENARIO_ID_KEY] ?: Scenario.DEFAULT_ID
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
    suspend fun admissionScenarioId(): String = admissionScenarioId.first()

    /**
     * Stores admission scenario identifier
     */
    fun saveAdmissionScenarioId(admissionScenarioId: String) = appScope.launch {
        runCatching {
            dataStore.edit { prefs -> prefs[ADMISSION_SCENARIO_ID_KEY] = admissionScenarioId }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "Failed to set admissionScenarioId.")
        }
    }

    /**
     * @returns admission check scenarios, by default null
     */
    val admissionCheckScenarios: Flow<String?> = dataStoreFlow
        .map { prefs -> prefs[ADMISSION_CHECK_SCENARIOS_KEY] }

    /**
     * Stores admission check scenarios
     */
    suspend fun setAdmissionCheckScenarios(json: String?) {
        runCatching {
            dataStore.edit { prefs -> prefs[ADMISSION_CHECK_SCENARIOS_KEY] = json ?: "" }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "Failed to set admission check scenarios.")
        }
    }

    /**
     * Get the default value (true) of force ccl calculation and set to false, subsequent calls will false
     */
    suspend fun shouldTriggerRecalculation(): Boolean {
        val force = dataStoreFlow.map { prefs -> prefs[FORCE_CCL_CALCULATION_KEY] ?: true }.first()
        return force.also { Timber.tag(TAG).d("forceCclCalculation() -> $it") }
    }

    override suspend fun reset() {
        Timber.tag(TAG).d("Clearing CCL Settings data store.")
        runCatching {
            dataStore.edit { prefs -> prefs.clear() }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "Failed to clear ccl settings.")
        }
    }

    companion object {
        internal val FORCE_CCL_CALCULATION_KEY = booleanPreferencesKey("ccl.settings.forceCclCalculation")
        internal val LAST_EXECUTION_TIME_KEY = longPreferencesKey("ccl.settings.lastexecutiontime")
        internal val ADMISSION_SCENARIO_ID_KEY = stringPreferencesKey("ccl.settings.admissionScenarioId")
        internal val ADMISSION_CHECK_SCENARIOS_KEY = stringPreferencesKey("ccl.settings.admissionCheckScenarios")
        private val TAG = tag<CclSettings>()
    }
}
