package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

data class PersonCertificates(
    val certificates: List<CwaCovidCertificate>
) {

    val personIdentifier: CertificatePersonIdentifier
        get() = certificates.first().personIdentifier

    val highestPriorityCertificate: CwaCovidCertificate
        get() = certificates.first()
}
