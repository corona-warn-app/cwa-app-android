package de.rki.coronawarnapp.covidcertificate.common.repository

import kotlinx.parcelize.Parcelize

@Parcelize
class VaccinationCertificateContainerId(override val qrCodeHash: String) : CertificateContainerId()
