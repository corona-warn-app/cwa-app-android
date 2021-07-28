package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId

sealed class VaccinationDetailsNavigation {
    object Back : VaccinationDetailsNavigation()
    data class FullQrCode(val qrCodeText: String) : VaccinationDetailsNavigation()
    data class ValidationStart(val containerId: CertificateContainerId) : VaccinationDetailsNavigation()
}
