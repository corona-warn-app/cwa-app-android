package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.HashExtensions.toSHA256

data class PersonCertificates(
    val certificates: List<CwaCovidCertificate>,
    val isCwaUser: Boolean = false,
    val badgeCount: Int = 0,
    val dccWalletInfo: DccWalletInfo? = null
) {
    val personIdentifier: CertificatePersonIdentifier?
        get() = certificates.firstOrNull()?.personIdentifier

    // PersonDetails
    val highestPriorityCertificate: CwaCovidCertificate? by lazy {
        certificates.firstOrNull { certificate ->
            certificate.qrCodeHash == dccWalletInfo?.mostRelevantCertificate
                ?.certificateRef?.barcodeData?.toSHA256()
        } ?: certificates.findFallbackDcc()
    }

    // PersonOverview
    val overviewCertificates: List<VerificationCertificate> by lazy {
        // TODO .take(2)
        dccWalletInfo?.verification?.certificates?.mapNotNull { certRef ->
            certRef.buttonText
            certificates.firstOrNull { it.qrCodeHash == certRef.certificateRef.barcodeData.toSHA256() }
        }

        listOf(VerificationCertificate(certificate = certificates.findFallbackDcc()))
    }

    val admissionState: AdmissionState? get() = certificates.determineAdmissionState()

    sealed class AdmissionState(val primaryCertificate: CwaCovidCertificate) {
        data class TwoGPlusPCR(val twoGCertificate: CwaCovidCertificate, val testCertificate: CwaCovidCertificate) :
            AdmissionState(twoGCertificate)

        data class TwoGPlusRAT(val twoGCertificate: CwaCovidCertificate, val testCertificate: CwaCovidCertificate) :
            AdmissionState(twoGCertificate)

        data class TwoG(val twoGCertificate: CwaCovidCertificate) : AdmissionState(twoGCertificate)

        data class ThreeGWithPCR(val testCertificate: CwaCovidCertificate) : AdmissionState(testCertificate)
        data class ThreeGWithRAT(val testCertificate: CwaCovidCertificate) : AdmissionState(testCertificate)

        data class Other(val otherCertificate: CwaCovidCertificate) : AdmissionState(otherCertificate)
    }
}

data class VerificationCertificate(
    val certificate: CwaCovidCertificate?,
    val buttonText: String = ""
)
