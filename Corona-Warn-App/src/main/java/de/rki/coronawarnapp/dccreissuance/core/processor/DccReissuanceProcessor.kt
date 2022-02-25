package de.rki.coronawarnapp.dccreissuance.core.processor

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Certificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServer
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DccReissuanceProcessor @Inject constructor(
    private val dccReissuanceServer: DccReissuanceServer,
    private val personCertificatesProvider: PersonCertificatesProvider
) {

    @Throws(DccReissuanceException::class)
    suspend fun requestDccReissuance(dccReissuanceDescriptor: CertificateReissuance) {
        Timber.tag(TAG).d("requestDccReissuance(dccReissuanceDescriptor=%s)", dccReissuanceDescriptor)

        // 1. Call API
        val dccReissuanceResponse = dccReissuanceDescriptor.callApi()

        // 2. Add DCC to Wallet
        dccReissuanceResponse.addDccToWallet()

        // 3. Move previous DCC to bin
        dccReissuanceDescriptor.certificateToReissue.recycleDcc()
    }

    private suspend fun CertificateReissuance.callApi(): DccReissuanceResponse {
        Timber.tag(TAG).d("Call DCC Reissuance API")
        return dccReissuanceServer.requestDccReissuance(
            action = Action.RENEW.raw,
            certificates = certificates
        )
    }

    private suspend fun DccReissuanceResponse.addDccToWallet() {
        Timber.tag(TAG).d("Adding DCC to Wallet")
        val firstEntry = dccReissuances.find { dccReissuance ->
            dccReissuance.relations.any { it.action == Action.REPLACE.raw && it.index == 0 }
        }

        if (firstEntry == null) {
            Timber.tag(TAG).d("")
            throw DccReissuanceException(errorCode = DccReissuanceException.ErrorCode.DCC_RI_NO_RELATION)
        }

        // TODO: Implement
    }

    private suspend fun Certificate.recycleDcc() {
        Timber.tag(TAG).d("Recycling previous DCC: %s", this)
        // TODO: Implement
    }

    private val CertificateReissuance.certificates: List<String>
        get() = accompanyingCertificates.plusElement(certificateToReissue).map { it.certificateRef.barcodeData }

    private enum class Action(val raw: String) {
        RENEW("renew"),
        REPLACE("replace")
    }

    companion object {
        private val TAG = tag<DccReissuanceProcessor>()
    }
}
