package de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString

data class VaccinationCertificateQRCode(
    override val qrCode: QrCodeString,
    override val data: DccData<VaccinationDccV1>
) : DccQrCode {
    override val uniqueCertificateIdentifier: String
        get() = data.certificate.vaccination.uniqueCertificateIdentifier
}
