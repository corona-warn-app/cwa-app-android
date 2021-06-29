package de.rki.coronawarnapp.covidcertificate.test.ui.details

sealed class TestCertificateDetailsNavigation {
    object Back : TestCertificateDetailsNavigation()
    data class FullQrCode(val qrCodeText: String) : TestCertificateDetailsNavigation()
    object ValidationStart : TestCertificateDetailsNavigation()
}
