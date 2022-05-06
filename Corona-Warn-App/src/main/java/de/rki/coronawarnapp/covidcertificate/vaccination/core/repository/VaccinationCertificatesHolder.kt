package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificatesHolder
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate

data class VaccinationCertificatesHolder(
    val certificates: Set<VaccinationCertificateWrapper>,
    val recycledCertificates: Set<VaccinationCertificate>
) : CertificatesHolder {
    override val allCertificates: Set<VaccinationCertificate> by lazy {
        certificates.map { it.vaccinationCertificate }.toSet() + recycledCertificates
    }
}
