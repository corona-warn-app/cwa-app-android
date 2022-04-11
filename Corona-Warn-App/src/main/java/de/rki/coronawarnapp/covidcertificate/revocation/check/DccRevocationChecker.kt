package de.rki.coronawarnapp.covidcertificate.revocation.check

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import javax.inject.Inject

class DccRevocationChecker @Inject constructor() {

    suspend fun isRevoked(
        dccData: DccData<*>,
        revocationList: List<CachedRevocationChunk>,
    ): Boolean {

        return false
    }
}
