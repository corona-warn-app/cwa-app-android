package de.rki.coronawarnapp.covidcertificate.test.ui.details

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId

sealed class TestCertificateDetailsNavigation {
    object Back : TestCertificateDetailsNavigation()
    data class FullQrCode(val qrCodeText: String) : TestCertificateDetailsNavigation()
    data class ValidationStart(val containerId: CertificateContainerId) : TestCertificateDetailsNavigation()
}
