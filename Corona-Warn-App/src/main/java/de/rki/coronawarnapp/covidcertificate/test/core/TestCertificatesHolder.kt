package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificatesHolder

data class TestCertificatesHolder(
    val certificates: Set<TestCertificateWrapper>,
    val recycledCertificates: Set<TestCertificate>
) : CertificatesHolder {
    override val allCertificates: Set<TestCertificate> by lazy {
        certificates.mapNotNull { it.testCertificate }.toSet() + recycledCertificates
    }
}
