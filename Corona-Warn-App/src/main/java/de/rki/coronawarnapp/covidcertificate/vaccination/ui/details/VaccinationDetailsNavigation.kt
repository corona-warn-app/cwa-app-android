package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

sealed class VaccinationDetailsNavigation {
    object Back : VaccinationDetailsNavigation()
    data class FullQrCode(val qrCode: CoilQrCode) : VaccinationDetailsNavigation()
    data class ValidationStart(val containerId: CertificateContainerId) : VaccinationDetailsNavigation()
    object Export : VaccinationDetailsNavigation() // TODO: check what we need to pass and convert to data class
}
