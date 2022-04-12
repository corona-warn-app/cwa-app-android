package de.rki.coronawarnapp.covidcertificate.revocation.check

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.calculateRevocationEntryForType
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.kidHash
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import timber.log.Timber
import javax.inject.Inject

class DccRevocationChecker @Inject constructor() {

    fun isRevoked(
        dccData: DccData<*>,
        revocationList: List<CachedRevocationChunk>,
    ): Boolean {
        if (dccData.kid.isEmpty()) return false // early return

        val coordinatesHashes = revocationList.associateBy { chunk -> chunk.coordinates }
        RevocationHashType.values().forEach { type ->
            val hash = dccData.calculateRevocationEntryForType(type)
            val coordinates = RevocationEntryCoordinates(
                kid = dccData.kidHash(),
                type = type,
                x = hash.substring(0, 0),
                y = hash.substring(1, 1),
            )

            val cachedRevocationChunk = coordinatesHashes[coordinates]
            if (cachedRevocationChunk != null &&
                cachedRevocationChunk.revocationChunk.hashes.contains(hash)
            ) {
                return true
            }
        }

        return false
    }
}
