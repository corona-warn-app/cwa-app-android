package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

data class PersonCertificates(
    val certificates: List<CwaCovidCertificate>,
    val isCwaUser: Boolean = false,
    val badgeCount: Int = 0
) {
    val personIdentifier: CertificatePersonIdentifier?
        get() = certificates.firstOrNull()?.personIdentifier

    val highestPriorityCertificate: CwaCovidCertificate? by lazy {
        certificates.findHighestPriorityCertificate()
    }

    // TODO: would be implemented in admission state logic task
    val admissionState: AdmissionState = AdmissionState.ThreeGWithPCR(highestPriorityCertificate!!)

    sealed class AdmissionState(val primaryCertificate: CwaCovidCertificate) {
        class TwoGPlusPCR(twoGCertificate: CwaCovidCertificate, certificateTest: CwaCovidCertificate) :
            AdmissionState(twoGCertificate)

        class TwoGPlusRAT(twoGCertificate: CwaCovidCertificate, certificateTest: CwaCovidCertificate) :
            AdmissionState(twoGCertificate)

        class TwoG(twoGCertificate: CwaCovidCertificate) : AdmissionState(twoGCertificate)

        class ThreeGWithPCR(testCertificate: CwaCovidCertificate) : AdmissionState(testCertificate)
        class ThreeGWithRAT(testCertificate: CwaCovidCertificate) : AdmissionState(testCertificate)

        class Other(certificate: CwaCovidCertificate) : AdmissionState(certificate)
    }
}
