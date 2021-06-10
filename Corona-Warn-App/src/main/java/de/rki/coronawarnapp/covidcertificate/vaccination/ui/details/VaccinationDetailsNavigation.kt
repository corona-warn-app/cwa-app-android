package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

sealed class VaccinationDetailsNavigation {
    object Back : VaccinationDetailsNavigation()
    data class FullQrCode(val qrCodeText: String) : VaccinationDetailsNavigation()
}
