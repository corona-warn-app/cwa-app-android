package de.rki.coronawarnapp.covidcertificate.validation.core

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DCC_BLOCKED
import javax.inject.Inject

class DccBlocklistValidator @Inject constructor() {

    fun validate(dccData: DccData<*>, blocklist: List<CovidCertificateConfig.BlockedChunk>) {
        val chunks = parseIdentifierToChunks(dccData.certificate.payload.uniqueCertificateIdentifier)
        blocklist.forEach {
            val hash = it.indices.mapNotNull { index ->
                if (index >= 0 && index < chunks.size)
                    chunks[index]
                else
                    null
            }
                .joinToString("/")
                .toSHA256()
            if (hash == it.hash.hex()) throw InvalidHealthCertificateException(HC_DCC_BLOCKED)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun parseIdentifierToChunks(uvci: String): List<String> {
        return uvci.removePrefix(UVCI_PREFIX).split('/', '#', ':')
    }

    companion object {
        const val UVCI_PREFIX = "URN:UVCI:"
    }
}
