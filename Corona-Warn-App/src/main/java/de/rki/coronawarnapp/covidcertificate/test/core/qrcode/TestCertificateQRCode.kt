package de.rki.coronawarnapp.covidcertificate.test.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString

data class TestCertificateQRCode(
    override val qrCode: QrCodeString,
    override val data: DccData<TestDccV1>,
) : DccQrCode {
    override val uniqueCertificateIdentifier: String
        get() = data.certificate.test.uniqueCertificateIdentifier
}
