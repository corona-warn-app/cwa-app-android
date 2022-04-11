package de.rki.coronawarnapp.covidcertificate.revocation.update

import androidx.datastore.core.DataStore
import de.rki.coronawarnapp.covidcertificate.revocation.RevocationDataStore
import de.rki.coronawarnapp.server.protocols.internal.dgc.RevocationChunkOuterClass.RevocationChunk
import javax.inject.Inject

class RevocationUpdateSettings @Inject constructor(
    @RevocationDataStore private val revocationDataStore: DataStore<RevocationChunk>
) {
    // TO DO(Save last successful execution time)
}
