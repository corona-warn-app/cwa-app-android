package de.rki.coronawarnapp.covidcertificate.test.qrcode

import de.rki.coronawarnapp.covidcertificate.test.certificate.TestCertificateData

data class TestCertificateQRCode(
    val qrCode: String,
    val testCertificateData: TestCertificateData,
)
