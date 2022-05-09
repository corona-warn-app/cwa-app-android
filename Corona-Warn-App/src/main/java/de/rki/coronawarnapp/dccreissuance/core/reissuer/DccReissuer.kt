package de.rki.coronawarnapp.dccreissuance.core.reissuer

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuanceItem
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServer
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import timber.log.Timber
import javax.inject.Inject

class DccReissuer @Inject constructor(
    private val dccReissuanceServer: DccReissuanceServer,
    private val dccQrCodeHandler: DccQrCodeHandler,
    private val dccQrCodeExtractor: DccQrCodeExtractor,
) {

    /**
     * Requests a new certificate, registers it and moves certificates into the recycle bin as indicated
     */
    @Throws(
        DccReissuanceException::class,
        InvalidHealthCertificateException::class
    )
    suspend fun startReissuance(certificateReissuance: CertificateReissuance) {
        val updates = certificateReissuance.asCertificateReissuanceCompat().certificates?.map {
            reissue(it)
        }
        updates?.map {
            it.recycleBin
        }?.flatten()?.toSet()?.forEach {
            moveToBin(it)
        }

        updates?.map {
            it.register
        }?.flatten()?.toSet()?.forEach {
            register(it)
        }
    }

    suspend fun reissue(item: CertificateReissuanceItem): CertificateUpdate {
        val recycleBin = mutableSetOf<QrCodeString>()
        val register = mutableSetOf<QrCodeString>()
        val allQrCodes = mutableListOf(item.certificateToReissue) + item.accompanyingCertificates

        val response = dccReissuanceServer.requestDccReissuance(
            action = item.action,
            certificates = allQrCodes.map { it.certificateRef.barcodeData }
        )

        response.dccReissuances.forEach { issuance ->
            register.add(issuance.certificate)
            issuance.relations.filter { relation ->
                relation.action == ACTION_REPLACE
            }.forEach {
                try {
                    recycleBin.add(allQrCodes[it.index].certificateRef.barcodeData)
                } catch (e: IndexOutOfBoundsException) {
                    Timber.d(e, "No certificate at index ${it.index}. Size is ${allQrCodes.size}")
                    throw DccReissuanceException(DccReissuanceException.ErrorCode.DCC_RI_SERVER_ERR)
                }
            }
        }

        return CertificateUpdate(
            register = register,
            recycleBin = recycleBin
        )
    }

    private suspend fun moveToBin(qrCodeString: QrCodeString) {
        val qrCode = qrCodeString.extract()
        dccQrCodeHandler.moveToRecycleBin(qrCode)
    }

    private suspend fun register(qrCodeString: QrCodeString) {
        val qrCode = qrCodeString.extract()
        dccQrCodeHandler.register(qrCode)
    }

    private suspend fun QrCodeString.extract() = dccQrCodeExtractor.extract(this)

    data class CertificateUpdate(
        val register: Set<QrCodeString>,
        val recycleBin: Set<QrCodeString>,
    )
}

internal const val ACTION_RENEW = "renew"
internal const val ACTION_REPLACE = "replace"
