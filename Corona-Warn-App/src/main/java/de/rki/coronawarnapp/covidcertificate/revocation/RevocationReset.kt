package de.rki.coronawarnapp.covidcertificate.revocation

import de.rki.coronawarnapp.covidcertificate.revocation.server.RevocationServer
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

class RevocationReset @Inject constructor(
    private val revocationServer: RevocationServer,
    private val revocationRepository: RevocationRepository
) {

    suspend fun clear() {
        Timber.tag(TAG).d("clear()")
        revocationServer.clearCache()
        revocationRepository.clear()
    }
}

private val TAG = tag<RevocationReset>()
