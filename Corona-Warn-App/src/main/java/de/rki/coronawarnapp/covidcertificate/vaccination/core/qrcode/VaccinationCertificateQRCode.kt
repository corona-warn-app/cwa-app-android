package de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.vaccination.core.certificate.VaccinationDccV1

data class VaccinationCertificateQRCode(
    override val qrCode: QrCodeString,
    override val data: DccData<VaccinationDccV1>
) : DccQrCode<VaccinationDccV1>
