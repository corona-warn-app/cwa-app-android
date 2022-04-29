package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

sealed class VaccinationDetailsNavigation {
    object Back : VaccinationDetailsNavigation()
    data class FullQrCode(val qrCode: CoilQrCode) : VaccinationDetailsNavigation()
    data class ValidationStart(val containerId: CertificateContainerId) : VaccinationDetailsNavigation()
    data class Export(val containerId: CertificateContainerId) : VaccinationDetailsNavigation()
    object OpenCovPassInfo : VaccinationDetailsNavigation()
    object ReturnToPersonDetailsAfterRecycling : VaccinationDetailsNavigation()
}
