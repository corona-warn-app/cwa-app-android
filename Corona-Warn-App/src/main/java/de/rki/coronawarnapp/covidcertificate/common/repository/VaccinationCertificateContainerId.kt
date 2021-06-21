package de.rki.coronawarnapp.covidcertificate.common.repository

import kotlinx.parcelize.Parcelize

@Parcelize
data class VaccinationCertificateContainerId(override val identifier: String) : CertificateContainerId()
