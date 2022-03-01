package de.rki.coronawarnapp.dccreissuance.core.processor

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServer
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

class DccReissuanceProcessor @Inject constructor(
    private val dccReissuanceServer: DccReissuanceServer
) {

    @Throws(DccReissuanceException::class)
    suspend fun requestDccReissuance(dccReissuanceDescriptor: CertificateReissuance): DccReissuanceResponse {
        Timber.tag(TAG).d("Requesting Dcc Reissuance")
        return dccReissuanceDescriptor.callApi().also { Timber.tag(TAG).d("Returning response") }
    }

    private suspend fun CertificateReissuance.callApi(): DccReissuanceResponse {
        Timber.tag(TAG).d("Call DCC Reissuance API")
        return dccReissuanceServer.requestDccReissuance(
            action = ACTION_RENEW,
            certificates = certificates
        )
    }

    private val CertificateReissuance.certificates: List<String>
        get() = accompanyingCertificates.plusElement(certificateToReissue).map { it.certificateRef.barcodeData }

    companion object {
        private val TAG = tag<DccReissuanceProcessor>()
        private const val ACTION_RENEW = "renew"
    }
}
