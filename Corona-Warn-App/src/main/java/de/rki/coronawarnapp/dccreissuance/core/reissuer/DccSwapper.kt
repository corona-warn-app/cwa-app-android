package de.rki.coronawarnapp.dccreissuance.core.reissuer

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Certificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

class DccSwapper @Inject constructor(
    private val dccQrCodeExtractor: DccQrCodeExtractor,
    private val vcRepo: VaccinationCertificateRepository,
    private val tcRepo: TestCertificateRepository,
    private val rcRepo: RecoveryCertificateRepository,
) {

    suspend fun swap(
        dccReissuance: DccReissuanceResponse.DccReissuance,
        certificateToRecycle: Certificate
    ) {
        Timber.tag(TAG).d("swap()")
        val qrCodeHash = certificateToRecycle.certificateRef.qrCodeHash()
        when (val qrcode = dccQrCodeExtractor.extract(dccReissuance.certificate)) {
            is RecoveryCertificateQRCode -> rcRepo.replaceCertificate(
                certificateToReplace = RecoveryCertificateContainerId(qrCodeHash),
                newCertificateQrCode = qrcode
            )

            is VaccinationCertificateQRCode -> vcRepo.replaceCertificate(
                certificateToReplace = VaccinationCertificateContainerId(qrCodeHash),
                newCertificateQrCode = qrcode
            )

            is TestCertificateQRCode -> tcRepo.replaceCertificate(
                certificateToReplace = TestCertificateContainerId(qrCodeHash),
                newCertificateQrCode = qrcode
            )
        }
        Timber.tag(TAG).d("swap() finished")
    }

    companion object {
        private val TAG = tag<DccSwapper>()
    }
}
