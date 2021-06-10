package de.rki.coronawarnapp.covidcertificate.test.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.test.core.certificate.TestDccV1

data class RecoveryCertificateQRCode(
    override val qrCode: QrCodeString,
    override val data: DccData<TestDccV1>,
) : DccQrCode<TestDccV1>
