package de.rki.coronawarnapp.greencertificate.ui.certificates.details

sealed class GreenCertificateDetailsNavigation {
    object Back : GreenCertificateDetailsNavigation()
    data class FullQrCode(val qrCodeText: String) : GreenCertificateDetailsNavigation()
}
