package de.rki.coronawarnapp.dccreissuance.core.reissuer

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException.ErrorCode
import de.rki.coronawarnapp.dccreissuance.core.processor.DccReissuanceProcessor
import javax.inject.Inject

class DccReissuer @Inject constructor(
    private val dccSwapper: DccSwapper,
    private val dccReissuanceProcessor: DccReissuanceProcessor,
) {

    /**
     * Requests new certificate from the server and replaces the old one in the holder's wallet
     */
    @Throws(
        DccReissuanceException::class,
        InvalidHealthCertificateException::class
    )
    suspend fun startReissuance(dccReissuanceDescriptor: CertificateReissuance) {
        val response = dccReissuanceProcessor.requestDccReissuance(dccReissuanceDescriptor)
        val dccReissuance = response.dccReissuances.find { issuance ->
            issuance.relations.any { r -> r.action == "replace" && r.index == 0 }
        } ?: throw DccReissuanceException(ErrorCode.DCC_RI_NO_RELATION)

        dccSwapper.swap(
            dccReissuance = dccReissuance,
            certificateToRecycle = dccReissuanceDescriptor.certificateToReissue
        )
    }
}
