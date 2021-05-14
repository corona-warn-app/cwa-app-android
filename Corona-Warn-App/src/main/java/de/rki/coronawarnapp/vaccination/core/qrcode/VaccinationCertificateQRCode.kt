package de.rki.coronawarnapp.vaccination.core.qrcode

data class VaccinationCertificateQRCode(
    val qrCodeString: QrCodeString,
    val parsedData: VaccinationCertificateData,
) {
    val uniqueCertificateIdentifier: String
        get() = parsedData.certificate.vaccinationDatas.single().uniqueCertificateIdentifier
}

typealias QrCodeString = String
