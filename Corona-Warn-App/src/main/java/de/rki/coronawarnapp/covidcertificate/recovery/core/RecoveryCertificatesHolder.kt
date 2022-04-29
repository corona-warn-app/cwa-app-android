package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificatesHolder

data class RecoveryCertificatesHolder(
    val certificates: Set<RecoveryCertificateWrapper>,
    val recycledCertificates: Set<RecoveryCertificate>
) : CertificatesHolder {
    override val allCertificates: Set<RecoveryCertificate> by lazy {
        certificates.map { it.recoveryCertificate }.toSet() + recycledCertificates
    }
}
