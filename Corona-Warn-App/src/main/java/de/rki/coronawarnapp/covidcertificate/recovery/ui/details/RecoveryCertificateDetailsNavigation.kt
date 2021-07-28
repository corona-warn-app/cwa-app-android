package de.rki.coronawarnapp.covidcertificate.recovery.ui.details

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId

sealed class RecoveryCertificateDetailsNavigation {
    object Back : RecoveryCertificateDetailsNavigation()
    data class FullQrCode(val qrCodeText: String) : RecoveryCertificateDetailsNavigation()
    data class ValidationStart(val containerId: CertificateContainerId) : RecoveryCertificateDetailsNavigation()
}
