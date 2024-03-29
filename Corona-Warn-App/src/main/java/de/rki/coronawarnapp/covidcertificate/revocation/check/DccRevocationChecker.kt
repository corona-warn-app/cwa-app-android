package de.rki.coronawarnapp.covidcertificate.revocation.check

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.calculateCoordinatesToHash
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

class DccRevocationChecker @Inject constructor() {

    fun isRevoked(
        dccData: DccData<*>,
        revocationList: List<CachedRevocationChunk>,
    ): Boolean {
        if (dccData.kid.isEmpty()) {
            Timber.tag(TAG).d("Certificate kid is missing -> not Revoked")
            return false // early return
        }

        val cachedChunks = revocationList.associateBy { chunk -> chunk.coordinates }
        return RevocationHashType.values().any { type ->
            val (coordinates, hash) = dccData.calculateCoordinatesToHash(type)
            val chunk = cachedChunks[coordinates]
            chunk != null && chunk.revocationChunk.hashes.contains(hash)
        }.also {
            Timber.tag(TAG).d(
                "Certificate[ci=%s isRevoked=%s]", dccData.certificate.payload.uniqueCertificateIdentifier, it
            )
        }
    }

    companion object {
        private val TAG = tag<DccRevocationChecker>()
    }
}
