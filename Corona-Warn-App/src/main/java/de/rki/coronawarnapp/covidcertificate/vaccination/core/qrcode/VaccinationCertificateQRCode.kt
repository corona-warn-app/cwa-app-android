package de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Vaccination
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString

data class VaccinationCertificateQRCode(
    override val qrCode: QrCodeString,
    override val data: DccData<DccV1Vaccination>
) : DccQrCode {
    override val uniqueCertificateIdentifier: String
        get() = data.certificate.vaccination.uniqueCertificateIdentifier
}
