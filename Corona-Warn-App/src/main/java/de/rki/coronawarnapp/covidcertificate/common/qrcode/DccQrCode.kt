package de.rki.coronawarnapp.covidcertificate.common.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData

interface DccQrCode {
    val qrCode: QrCodeString
    val data: DccData

    val personIdentifier: CertificatePersonIdentifier
        get() = data.certificate.personIdentifier

    val uniqueCertificateIdentifier: String
}
