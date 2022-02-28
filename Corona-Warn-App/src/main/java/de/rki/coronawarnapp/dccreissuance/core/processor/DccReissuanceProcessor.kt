package de.rki.coronawarnapp.dccreissuance.core.processor

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Certificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServer
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import de.rki.coronawarnapp.qrcode.scanner.QrCodeValidator
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DccReissuanceProcessor @Inject constructor(
    private val dccReissuanceServer: DccReissuanceServer,
    private val certificateProvider: CertificateProvider,
    private val recycledCertificatesProvider: RecycledCertificatesProvider,
    private val qeCodeValidator: QrCodeValidator,
    private val dccQrCodeHandler: DccQrCodeHandler
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

        Timber.tag(TAG).d("Successfully request the Reissuance of a DCC")
    }

    private suspend fun CertificateReissuance.callApi(): DccReissuanceResponse {
        Timber.tag(TAG).d("Call DCC Reissuance API")
        return dccReissuanceServer.requestDccReissuance(
            action = Action.RENEW.raw,
            certificates = certificates
        )
    }

    private suspend fun DccReissuanceResponse.addDccToWallet() = try {
        Timber.tag(TAG).d("Adding DCC to Wallet")
        val dcc = dccReissuances.find { dccReissuance ->
            dccReissuance.relations.any { it.action == Action.REPLACE.raw && it.index == 0 }
        }

        checkNotNull(dcc) { "Found no entry where relations has an entry with action == replace and index == 0" }

        when (val dccQrCode = qeCodeValidator.validate(rawString = dcc.certificate)) {
            is DccQrCode -> dccQrCodeHandler.handleQrCode(dccQrCode = dccQrCode)
            else -> throw IllegalStateException("Expected an ${tag<DccQrCode>()} but got $dccQrCode")
        }
    } catch (e: Exception) {
        throw DccReissuanceException(errorCode = DccReissuanceException.ErrorCode.DCC_RI_NO_RELATION, cause = e)
    }

    private suspend fun Certificate.recycleDcc() {
        Timber.tag(TAG).d("Recycling previous DCC: %s", this)
        val barcodeData = certificateRef.barcodeData
        val certContainerId = findContainerIdForBarcode(barcodeData = barcodeData)

        if (certContainerId == null) {
            Timber.tag(TAG).w("Didn't find a cert for barcode data %s", barcodeData)
            return
        }

        recycledCertificatesProvider.recycleCertificate(containerId = certContainerId)
    }

    private val CertificateReissuance.certificates: List<String>
        get() = accompanyingCertificates.plusElement(certificateToReissue).map { it.certificateRef.barcodeData }

    private suspend fun findContainerIdForBarcode(barcodeData: String) = certificateProvider.certificateContainer
        .first().allCwaCertificates.find { it.qrCodeToDisplay.content == barcodeData }?.containerId

    private enum class Action(val raw: String) {
        RENEW("renew"),
        REPLACE("replace")
    }

    companion object {
        private val TAG = tag<DccReissuanceProcessor>()
    }
}
