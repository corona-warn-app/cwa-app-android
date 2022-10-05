package de.rki.coronawarnapp.risk

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.map
import de.rki.coronawarnapp.util.datastore.trySetValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskLevelSettings @Inject constructor(
    @RiskLevelSettingsDataStore private val dataStore: DataStore<Preferences>
) {
    /**
     * The identifier of the config used during the last risklevel calculation
     */

    val lastUsedConfigIdentifier: Flow<String?> = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_RISKLEVEL_CALC_LAST_CONFIG_ID
    )

    suspend fun updateLastUsedConfigIdentifier(identifier: String) = dataStore.trySetValue(
        preferencesKey = PKEY_RISKLEVEL_CALC_LAST_CONFIG_ID,
        value = identifier
    )

    val ewLastChangeCheckedRiskLevelTimestamp: Flow<Instant?> =
        dataStore.dataRecovering.map(PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_EW)
            .map { if (it != null && it != 0L) Instant.ofEpochMilli(it) else null }.distinctUntilChanged()

    suspend fun updateEwLastChangeCheckedRiskLevelTimestamp(value: Instant?) = dataStore.trySetValue(
        preferencesKey = PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_EW,
        value = value?.toEpochMilli() ?: 0L
    )

    val lastChangeCheckedRiskLevelCombinedTimestamp: Flow<Instant?> =
        dataStore.dataRecovering.map(PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED)
            .map { if (it != null && it != 0L) Instant.ofEpochMilli(it) else null }.distinctUntilChanged()

    suspend fun updateLastChangeCheckedRiskLevelCombinedTimestamp(value: Instant?) = dataStore.trySetValue(
        preferencesKey = PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED,
        value = value?.toEpochMilli() ?: 0L
    )

    companion object {
        @VisibleForTesting
        val PKEY_RISKLEVEL_CALC_LAST_CONFIG_ID = stringPreferencesKey("risklevel.config.identifier.last")

        @VisibleForTesting
        val PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_EW =
            longPreferencesKey("PKEY_RISKLEVEL_CALC_LAST_CONFIG_ID") // A copy/paste mistake that lives on...

        @VisibleForTesting
        val PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED =
            longPreferencesKey("PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED")
    }
}
