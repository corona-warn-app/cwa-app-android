package de.rki.coronawarnapp.covidcertificate.common.repository

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

interface CertificatesHolder {
    val allCertificates: Set<CwaCovidCertificate>
}
