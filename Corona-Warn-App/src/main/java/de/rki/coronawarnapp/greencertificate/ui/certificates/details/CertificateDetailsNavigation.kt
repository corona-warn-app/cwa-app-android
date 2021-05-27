package de.rki.coronawarnapp.greencertificate.ui.certificates.details

sealed class CertificateDetailsNavigation {
    object Back : CertificateDetailsNavigation()
    data class FullQrCode(val qrCodeText: String) : CertificateDetailsNavigation()
}
