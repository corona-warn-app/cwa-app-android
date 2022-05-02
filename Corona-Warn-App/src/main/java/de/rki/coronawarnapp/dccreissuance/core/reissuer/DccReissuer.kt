package de.rki.coronawarnapp.dccreissuance.core.reissuer

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuanceItem
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.common.qrcode.hash
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServer
import javax.inject.Inject

class DccReissuer @Inject constructor(
    private val dccReissuanceServer: DccReissuanceServer,
    private val dccQrCodeExtractor: DccQrCodeExtractor,
    private val vcRepo: VaccinationCertificateRepository,
    private val tcRepo: TestCertificateRepository,
    private val rcRepo: RecoveryCertificateRepository,
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
            addNew(issuance.certificate)
            issuance.relations.filter { relation ->
                relation.action == "replace"
            }.forEach {
                moveToBin(
                    allQrCodes[it.index].certificateRef.barcodeData,
                )
            }
        }
    }

    private suspend fun moveToBin(qrCodeString: QrCodeString) {
        val hash = qrCodeString.hash()
        when (qrCodeString.extract()) {
            is RecoveryCertificateQRCode -> rcRepo.recycleCertificate(
                RecoveryCertificateContainerId(hash)
            )
            is VaccinationCertificateQRCode -> vcRepo.recycleCertificate(
                VaccinationCertificateContainerId(hash)
            )
            is TestCertificateQRCode -> tcRepo.recycleCertificate(
                TestCertificateContainerId(hash)
            )
        }
    }

    private suspend fun addNew(qrCodeString: QrCodeString) {
        when (val qrCode = qrCodeString.extract()) {
            is RecoveryCertificateQRCode -> rcRepo.registerCertificate(
                qrCode
            )
            is VaccinationCertificateQRCode -> vcRepo.registerCertificate(
                qrCode
            )
            is TestCertificateQRCode -> tcRepo.registerCertificate(
                qrCode
            )
        }
    }

    private suspend fun QrCodeString.extract() = dccQrCodeExtractor.extract(this)
}
