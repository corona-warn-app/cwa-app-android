package de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.util.HashExtensions.toSHA256

data class VaccinationCertificateQRCode(
    override val qrCode: QrCodeString,
    override val data: DccData<VaccinationDccV1>
) : DccQrCode {
    override val uniqueCertificateIdentifier: String
        get() = qrCode.toSHA256()
}
