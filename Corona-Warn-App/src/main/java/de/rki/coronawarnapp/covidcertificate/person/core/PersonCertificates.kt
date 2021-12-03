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

    sealed class AdmissionState(val primaryCertificate: CwaCovidCertificate) {
        class Is2GPlusPCR(certificate2G: CwaCovidCertificate, certificateTest: CwaCovidCertificate) :
            AdmissionState(certificate2G)

        class Is2GPlusRAT(certificate2G: CwaCovidCertificate, certificateTest: CwaCovidCertificate) :
            AdmissionState(certificate2G)

        class Is2G(certificate2G: CwaCovidCertificate) : AdmissionState(certificate2G)
        class Is3GWithPCR(certificateTest: CwaCovidCertificate) : AdmissionState(certificateTest)
        class Is3GWithRAT(certificateTest: CwaCovidCertificate) : AdmissionState(certificateTest)
    }
}
