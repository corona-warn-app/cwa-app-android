package de.rki.coronawarnapp.covidcertificate.test.ui.details

sealed class CovidCertificateDetailsNavigation {
    object Back : CovidCertificateDetailsNavigation()
    data class FullQrCode(val qrCodeText: String) : CovidCertificateDetailsNavigation()
}
