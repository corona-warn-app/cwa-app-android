package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.srs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsSrsKeySubmissionDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsSrsKeySubmissionStorage @Inject constructor(
    @AnalyticsSrsKeySubmissionDataStore private val dataStore: DataStore<Preferences>
) {
    private val dataStoreFlow = dataStore.data
        .catch { e ->
            Timber.e(e, "Failed to read AnalyticsSrsKeySubmissionStorage")
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }

    suspend fun reset() {
        dataStore.edit { pres ->
            pres.clear()
        }
    }

    suspend fun saveSrsPpaData(srsPpaData: String) {
        runCatching {
            dataStore.edit { prefs -> prefs[SRS_PPA_DATA] = srsPpaData }
        }.onFailure { e ->
            Timber.e(e, "Failed to saveSrsPpaData.")
        }
    }

    suspend fun getSrsPpaData(): String? {
        return dataStoreFlow.map { prefs -> prefs[SRS_PPA_DATA] }.first()
    }

    companion object {
        internal val SRS_PPA_DATA = stringPreferencesKey("srs.ppadata.key.submission")
    }
}
