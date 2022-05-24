package de.rki.coronawarnapp.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.reset.Resettable
import timber.log.Timber
import javax.inject.Inject

class StorageReset @Inject constructor(
    @StorageDataStore private val dataStore: DataStore<Preferences>
) : Resettable {

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }
}
