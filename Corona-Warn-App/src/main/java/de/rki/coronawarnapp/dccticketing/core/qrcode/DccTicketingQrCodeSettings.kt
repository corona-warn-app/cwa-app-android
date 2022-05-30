package de.rki.coronawarnapp.dccticketing.core.qrcode

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import de.rki.coronawarnapp.dccticketing.core.DccTicketingDataStore
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class DccTicketingQrCodeSettings @Inject constructor(
    @DccTicketingDataStore private val dataStore: DataStore<Preferences>
) : Resettable {


    val checkServiceIdentity: Flow<Boolean> = dataStore.dataRecovering.distinctUntilChanged(
        key = PREFS_KEY_CHECK_SERVICE_IDENTITY,
        defaultValue = true
    )

    suspend fun updateCheckServiceIdentity(checkService: Boolean) {
        dataStore.trySetValue(PREFS_KEY_CHECK_SERVICE_IDENTITY, checkService)
    }

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }
}

private val PREFS_KEY_CHECK_SERVICE_IDENTITY = booleanPreferencesKey("dccTicketing_checkServiceIdentity")
