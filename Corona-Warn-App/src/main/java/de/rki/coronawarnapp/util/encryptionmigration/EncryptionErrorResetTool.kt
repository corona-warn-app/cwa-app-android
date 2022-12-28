package de.rki.coronawarnapp.util.encryptionmigration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionErrorResetTool @Inject constructor(
    @EncryptionErrorResetToolDataStore private val dataStore: DataStore<Preferences>
) {

    val isResetNoticeToBeShown = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_EA2850_SHOW_RESET_NOTICE, defaultValue = false
    )

    suspend fun updateIsResetNoticeToBeShown(value: Boolean) = dataStore.trySetValue(
        preferencesKey = PKEY_EA2850_SHOW_RESET_NOTICE, value = value
    )

    companion object {
        private val PKEY_EA2850_SHOW_RESET_NOTICE = booleanPreferencesKey("ea2850.reset.shownotice")
    }
}
