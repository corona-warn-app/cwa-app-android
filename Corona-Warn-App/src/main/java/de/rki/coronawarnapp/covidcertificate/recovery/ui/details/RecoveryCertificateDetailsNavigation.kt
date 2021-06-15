package de.rki.coronawarnapp.covidcertificate.recovery.ui.details

sealed class RecoveryCertificateDetailsNavigation {
    object Back : RecoveryCertificateDetailsNavigation()
    data class FullQrCode(val qrCodeText: String) : RecoveryCertificateDetailsNavigation()
}
