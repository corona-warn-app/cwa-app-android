package de.rki.coronawarnapp.covidcertificate.test.core.qrcode

import de.rki.coronawarnapp.covidcertificate.test.core.certificate.TestCertificateData

data class TestCertificateQRCode(
    val qrCode: String,
    val testCertificateData: TestCertificateData,
)
