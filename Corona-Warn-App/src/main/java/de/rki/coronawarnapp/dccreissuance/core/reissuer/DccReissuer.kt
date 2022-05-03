package de.rki.coronawarnapp.dccreissuance.core.reissuer

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuanceItem
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServer
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import javax.inject.Inject

class DccReissuer @Inject constructor(
    private val dccReissuanceServer: DccReissuanceServer,
    private val dccQrCodeHandler: DccQrCodeHandler,
    private val dccQrCodeExtractor: DccQrCodeExtractor,
) {

    /**
     * Requests new certificate from the server and replaces the old one in the holder's wallet
     */
    @Throws(
        DccReissuanceException::class,
        InvalidHealthCertificateException::class
    )
    suspend fun startReissuance(dccReissuanceDescriptor: CertificateReissuance) {
        dccReissuanceDescriptor.certificates?.forEach {
            reissue(it)
        }
    }

    suspend fun reissue(item: CertificateReissuanceItem) {
        val allQrCodes = mutableListOf(item.certificateToReissue) + item.accompanyingCertificates
        val response = dccReissuanceServer.requestDccReissuance(
            action = item.action,
            certificates = allQrCodes.map { it.certificateRef.barcodeData }
        )

        response.dccReissuances.forEach { issuance ->
            register(issuance.certificate)
            issuance.relations.filter { relation ->
                relation.action == "replace"
            }.ifEmpty {
                throw DccReissuanceException(DccReissuanceException.ErrorCode.DCC_RI_NO_RELATION)
            }.forEach {
                moveToBin(
                    allQrCodes[it.index].certificateRef.barcodeData,
                )
            }
        }
    }

    private suspend fun moveToBin(qrCodeString: QrCodeString) {
        val qrCode = qrCodeString.extract()
        dccQrCodeHandler.moveToBin(qrCode)
    }

    private suspend fun register(qrCodeString: QrCodeString) {
        val qrCode = qrCodeString.extract()
        dccQrCodeHandler.register(qrCode)
    }

    private suspend fun QrCodeString.extract() = dccQrCodeExtractor.extract(this)
}
