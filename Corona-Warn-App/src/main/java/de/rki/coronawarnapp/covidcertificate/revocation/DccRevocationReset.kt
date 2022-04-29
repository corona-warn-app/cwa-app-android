package de.rki.coronawarnapp.covidcertificate.revocation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import de.rki.coronawarnapp.tag
import okhttp3.Cache
import timber.log.Timber
import javax.inject.Inject

class DccRevocationReset @Inject constructor(
    @DccRevocationCache private val cache: Cache,
    @DccRevocationDataStore private val dataStore: DataStore<Preferences>
) {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun clear() = try {
        Timber.tag(TAG).d("clear()")
        cache.evictAll()
        dataStore.edit { prefs -> prefs.clear() }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to clear revocation data")
    }
}

private val TAG = tag<DccRevocationReset>()
