package de.rki.coronawarnapp.covidcertificate.revocation.check

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import javax.inject.Inject

class RevocationCheck @Inject constructor(
    private val revocationRepository: RevocationRepository
){

    suspend fun checkDccAgainstCachedRevocationList(dcc: CwaCovidCertificate): Boolean {
        TODO("Not yet implemented")
    }
}
