package de.rki.coronawarnapp.covidcertificate.common.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.qrcode.scanner.QrCode
import de.rki.coronawarnapp.util.HashExtensions.toSHA256

interface DccQrCode : QrCode {
    val qrCode: QrCodeString
    val data: DccData<out DccV1.MetaData>

    val personIdentifier: CertificatePersonIdentifier
        get() = data.certificate.personIdentifier

    val hash: String
        get() = qrCode.toSHA256()
}
