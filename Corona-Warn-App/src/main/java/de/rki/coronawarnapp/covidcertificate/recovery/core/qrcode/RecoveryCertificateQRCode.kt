package de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Recovery
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString

data class RecoveryCertificateQRCode(
    override val qrCode: QrCodeString,
    override val data: DccData<DccV1Recovery>,
) : DccQrCode {
    override val uniqueCertificateIdentifier: String
        get() = data.certificate.recovery.uniqueCertificateIdentifier
}
