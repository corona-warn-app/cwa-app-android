package de.rki.coronawarnapp.covidcertificate.revocation.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.covidcertificate.revocation.RevocationDataStore
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class RevocationStorage @Inject constructor(
    @RevocationDataStore private val dataStore: DataStore<Preferences>
) {

    val data = dataStore.data
        .catch {
            Timber.tag(TAG).e(it, "Failed to read DataStore")
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { prefs -> prefs[CACHED_REVOCATION_CHUNKS_KEY] }

    suspend fun save(data: String) {
        Timber.tag(TAG).v("save(data=%s)", data)
        dataStore.edit { prefs -> prefs[CACHED_REVOCATION_CHUNKS_KEY] = data }
    }

    suspend fun clear() {
        Timber.tag(TAG).d("clear()")
        try {
            dataStore.edit { prefs -> prefs.clear() }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to clear data")
        }
    }
}

private val TAG = tag<RevocationStorage>()
private val CACHED_REVOCATION_CHUNKS_KEY by lazy {
    stringPreferencesKey("RevocationStorage.cached_revocation_chunks")
}
