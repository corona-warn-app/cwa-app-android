package de.rki.coronawarnapp.nearby

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ENFClientLocalData @Inject constructor(
    @ENFClientDataStore private val dataStore: DataStore<Preferences>
) {
    val lastQuotaResetAt =
        dataStore.dataRecovering.distinctUntilChanged(key = PKEY_QUOTA_LAST_RESET, defaultValue = 0L).map {
            Instant.ofEpochMilli(it)
        }

    suspend fun updateLastQuotaResetAt(value: Instant) = dataStore.trySetValue(
        preferencesKey = PKEY_QUOTA_LAST_RESET, value = value.toEpochMilli()
    )

    val currentQuota =
        dataStore.dataRecovering.distinctUntilChanged(key = PKEY_QUOTA_CURRENT, defaultValue = 0)

    suspend fun updateCurrentQuota(value: Int) = dataStore.trySetValue(
        preferencesKey = PKEY_QUOTA_CURRENT, value = value
    )

    companion object {
        private val PKEY_QUOTA_LAST_RESET = longPreferencesKey("enfclient.quota.lastreset")
        private val PKEY_QUOTA_CURRENT = intPreferencesKey("enfclient.quota.current")
    }
}
