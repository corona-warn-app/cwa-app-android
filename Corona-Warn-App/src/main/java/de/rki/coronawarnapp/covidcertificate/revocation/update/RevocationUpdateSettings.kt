package de.rki.coronawarnapp.covidcertificate.revocation.update

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import de.rki.coronawarnapp.covidcertificate.revocation.RevocationDataStore
import javax.inject.Inject

class RevocationUpdateSettings @Inject constructor(
    @RevocationDataStore private val revocationDataStore: DataStore<Preferences>
) {
    // TO DO(Save last successful execution time)
}
