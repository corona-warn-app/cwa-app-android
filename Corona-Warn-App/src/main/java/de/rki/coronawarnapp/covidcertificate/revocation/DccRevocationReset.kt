package de.rki.coronawarnapp.covidcertificate.revocation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.reset.Resettable
import okhttp3.Cache
import timber.log.Timber
import javax.inject.Inject

class DccRevocationReset @Inject constructor(
    @DccRevocationCache private val cache: Cache,
    @DccRevocationDataStore private val dataStore: DataStore<Preferences>
) : Resettable {

    override suspend fun reset() {
        runCatching {
            Timber.tag(TAG).d("clear()")
            cache.evictAll()
            dataStore.edit { prefs -> prefs.clear() }
        }.onFailure { Timber.tag(TAG).e(it, "Failed to clear revocation data") }
    }
}

private val TAG = tag<DccRevocationReset>()
