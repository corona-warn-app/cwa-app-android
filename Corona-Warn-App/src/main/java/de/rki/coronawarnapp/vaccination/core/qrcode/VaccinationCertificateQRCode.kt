package de.rki.coronawarnapp.vaccination.core.qrcode

data class VaccinationCertificateQRCode(
    val parsedData: VaccinationCertificateData,
    val certificateCOSE: RawCOSEObject,
) {
    val uniqueCertificateIdentifier: String
        get() = parsedData.vaccinationCertificate.vaccinationDatas.single().uniqueCertificateIdentifier
}

typealias RawCOSEObject = ByteArray

val EmptyRawCOSEObject = ByteArray(0)
