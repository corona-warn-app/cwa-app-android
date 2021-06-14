package de.rki.coronawarnapp.covidcertificate.common.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.Dcc
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData

interface DccQrCode<DccT : Dcc<*>> {
    val qrCode: QrCodeString
    val data: DccData<DccT>

    val personIdentifier: CertificatePersonIdentifier
        get() = data.certificate.personIdentifier

    val uniqueCertificateIdentifier: String
        get() = data.certificate.payload.uniqueCertificateIdentifier
}
