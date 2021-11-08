package de.rki.coronawarnapp.covidcertificate.validation.core

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.util.HashExtensions
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import javax.inject.Inject

class BlocklistValidator @Inject constructor() {

    fun isValid(dccData: DccData<*>, blocklist: List<CovidCertificateConfig.BlockedChunk>): Boolean {
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
            if (hash == it.hash.hex()) return false
        }
        return true
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun parseIdentifierToChunks(uvci: String): List<String> {
        return uvci.removePrefix(UVCI_PREFIX).split('/', '#', ':')
    }

    companion object {
        const val UVCI_PREFIX = "URN:UVCI:"
    }
}
