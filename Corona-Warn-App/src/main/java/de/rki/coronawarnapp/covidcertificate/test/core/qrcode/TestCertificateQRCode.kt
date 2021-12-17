package de.rki.coronawarnapp.covidcertificate.test.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.util.HashExtensions.toSHA256

data class TestCertificateQRCode(
    override val qrCode: QrCodeString,
    override val data: DccData<TestDccV1>,
) : DccQrCode {
    override val uniqueCertificateIdentifier: String
        get() = qrCode.toSHA256()
}
