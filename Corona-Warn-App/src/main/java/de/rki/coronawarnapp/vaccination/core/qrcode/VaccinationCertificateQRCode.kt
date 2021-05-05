package de.rki.coronawarnapp.vaccination.core.qrcode

import okio.ByteString

// TODO
data class VaccinationCertificateQRCode(
    val parsedData: VaccinationCertificateData,
    // COSE representation of the vaccination certificate (as byte sequence)
    val certificateCOSE: ByteString,
) {
    val uniqueCertificateIdentifier: String
        get() = parsedData.vaccinationCertificate.vaccinationDatas.single().uniqueCertificateIdentifier
}
