package de.rki.coronawarnapp.covidcertificate.test.ui.details

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

sealed class TestCertificateDetailsNavigation {
    object Back : TestCertificateDetailsNavigation()
    data class FullQrCode(val qrCode: CoilQrCode) : TestCertificateDetailsNavigation()
    data class ValidationStart(val containerId: CertificateContainerId) : TestCertificateDetailsNavigation()
    data class Export(val containerId: CertificateContainerId) : TestCertificateDetailsNavigation()
    object OpenCovPassInfo : TestCertificateDetailsNavigation()
    object ReturnToPersonDetailsAfterRecycling : TestCertificateDetailsNavigation()
}
