package de.rki.coronawarnapp.covidcertificate.recovery.ui.details

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

sealed class RecoveryCertificateDetailsNavigation {
    object Back : RecoveryCertificateDetailsNavigation()
    data class FullQrCode(val qrCode: CoilQrCode) : RecoveryCertificateDetailsNavigation()
    data class ValidationStart(val containerId: CertificateContainerId) : RecoveryCertificateDetailsNavigation()
    object Export : RecoveryCertificateDetailsNavigation() // TODO: check what we need to pass and convert to data class
}
