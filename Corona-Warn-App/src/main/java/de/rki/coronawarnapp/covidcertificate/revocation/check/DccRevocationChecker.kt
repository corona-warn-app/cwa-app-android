package de.rki.coronawarnapp.covidcertificate.revocation.check

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.calculateRevocationEntryForType
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.kidHash
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedKidTypeXYChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import javax.inject.Inject

class DccRevocationChecker @Inject constructor() {

    fun isRevoked(
        dccData: DccData<*>,
        revocationList: List<CachedKidTypeXYChunk>,
    ): Boolean {
        if (dccData.kid.isEmpty()) return false

        val revocationEntries = RevocationHashType.values().map { type ->
            val hash = dccData.calculateRevocationEntryForType(type)
            RevocationEntryCoordinates(
                kid = dccData.kidHash(),
                type = type,
                x = hash.substring(0, 0),
                y = hash.substring(1, 1),
            )
        }

        return revocationList.forEach {
            it.kid =
        }
    }
}
