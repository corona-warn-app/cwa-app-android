package de.rki.coronawarnapp.covidcertificate.revocation.update

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import de.rki.coronawarnapp.covidcertificate.revocation.DccRevocationDataStore
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class DccRevocationUpdateSettings @Inject constructor(
    @DccRevocationDataStore private val revocationDataStore: DataStore<Preferences>
) {
    private val dataStoreFlow = revocationDataStore.data
        .catch { e ->
            Timber.tag(TAG).e(e, "Failed to read RevocationList Settings")
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }

    suspend fun getLastUpdateTime(): Instant? =
        dataStoreFlow.map { prefs ->
            prefs[LAST_UPDATE_TIME_KEY]
        }.map { time ->
            if (time != null) {
                Timber.tag(TAG).d("Last RevocationList update time found in data store: %s", time)
                Instant.ofEpochSecond(time)
            } else {
                Timber.tag(TAG).d("No RevocationList update time yet")
                null
            }
        }.first()

    /**
     * Stores the update time of the Dcc revocation list
     */
    suspend fun setUpdateTimeToNow(time: Instant = Instant.now()) {
        runCatching {
            revocationDataStore.edit { prefs -> prefs[LAST_UPDATE_TIME_KEY] = time.seconds }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "Failed to set RevocationList update time.")
        }
    }

    companion object {
        private val TAG = tag<DccRevocationUpdateSettings>()
        internal val LAST_UPDATE_TIME_KEY = longPreferencesKey("dccRevocationList.lastUpdateTime")
    }
}
