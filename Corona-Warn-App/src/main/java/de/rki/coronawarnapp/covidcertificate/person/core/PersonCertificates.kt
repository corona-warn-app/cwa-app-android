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
    // null when certificates list is empty
    val admissionState: AdmissionState?
        get() = AdmissionState.ThreeGWithPCR(highestPriorityCertificate!!)

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
