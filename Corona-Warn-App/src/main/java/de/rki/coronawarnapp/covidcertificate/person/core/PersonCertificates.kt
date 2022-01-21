package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfoWrapper
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

data class PersonCertificates(
    @Deprecated("Please use DccWalletInfoWrapper")
    val certificates: List<CwaCovidCertificate>,
    @Deprecated("Please use DccWalletInfoWrapper")
    val isCwaUser: Boolean = false,
    @Deprecated("Please use DccWalletInfoWrapper")
    val badgeCount: Int = 0,
    val dccWalletInfoWrapper: DccWalletInfoWrapper? = null
) {
    val personIdentifier: CertificatePersonIdentifier?
        get() = certificates.firstOrNull()?.personIdentifier

    val highestPriorityCertificate: CwaCovidCertificate? by lazy {
        certificates.findHighestPriorityCertificate()
    }

    val admissionState: AdmissionState?
        get() = certificates.determineAdmissionState()

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
