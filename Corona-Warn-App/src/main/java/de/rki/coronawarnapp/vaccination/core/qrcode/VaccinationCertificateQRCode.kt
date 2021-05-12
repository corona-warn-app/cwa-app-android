package de.rki.coronawarnapp.vaccination.core.qrcode

import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject

data class VaccinationCertificateQRCode(
    val parsedData: VaccinationCertificateData,
    val certificateCOSE: RawCOSEObject,
) {
    val uniqueCertificateIdentifier: String
        get() = parsedData.certificate.vaccinationDatas.single().uniqueCertificateIdentifier
}
