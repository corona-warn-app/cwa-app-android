package de.rki.coronawarnapp.ui.presencetracing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import de.rki.coronawarnapp.presencetracing.LocationPreferencesDataStore
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceLocationPreferences @Inject constructor(
    @LocationPreferencesDataStore private val dataStore: DataStore<Preferences>
) : Resettable {

    val qrInfoAcknowledged = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_ACKNOWLEDGED, defaultValue = false
    )

    suspend fun updateQrInfoAcknowledged(value: Boolean) =
        dataStore.trySetValue(preferencesKey = PKEY_ACKNOWLEDGED, value = value)

    val createJournalEntryCheckedState = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_CREATE_JOURNAL_ENTRY, defaultValue = true
    )

    suspend fun updateCreateJournalEntryCheckedState(value: Boolean) =
        dataStore.trySetValue(preferencesKey = PKEY_CREATE_JOURNAL_ENTRY, value = value)

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }

    companion object {
        private val PKEY_ACKNOWLEDGED = booleanPreferencesKey("trace_location_qr_info_acknowledged")
        private val PKEY_CREATE_JOURNAL_ENTRY =
            booleanPreferencesKey("trace_location_create_journal_entry_checked_state")
    }
}
