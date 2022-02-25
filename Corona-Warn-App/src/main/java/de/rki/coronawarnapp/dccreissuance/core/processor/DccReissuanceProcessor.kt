package de.rki.coronawarnapp.dccreissuance.core.processor

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServer
import javax.inject.Inject

class DccReissuanceProcessor @Inject constructor(
    private val dccReissuanceServer: DccReissuanceServer
) {

    suspend fun requestDccReissuance(dccReissuanceDescriptor: CertificateReissuance) {
        val dccReissuanceResponse = dccReissuanceServer.requestDccReissuance(
            action = "",
            certificates = dccReissuanceDescriptor.certificates
        )

        // TODO: Implement other step 2 and 3
    }

    private val CertificateReissuance.certificates: List<String>
        get() = accompanyingCertificates.plusElement(certificateToReissue).map { it.certificateRef.barcodeData }
}
