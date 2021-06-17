package de.rki.coronawarnapp.covidcertificate.common.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1

interface DccQrCode {
    val qrCode: QrCodeString
    val data: DccData<out DccV1.MetaData>

    val personIdentifier: CertificatePersonIdentifier
        get() = data.certificate.personIdentifier

    val uniqueCertificateIdentifier: String
}
