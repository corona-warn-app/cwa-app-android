package de.rki.coronawarnapp.covidcertificate.revocation.check

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import timber.log.Timber
import javax.inject.Inject

class RevocationCheck @Inject constructor(
    private val revocationRepository: RevocationRepository
) {

    suspend fun checkDccAgainstCachedRevocationList(dcc: CwaCovidCertificate): Boolean {
        // satisfy CI ¯\_(ツ)_/¯
        Timber.e("Not yet implemented")
        return false
    }
}
