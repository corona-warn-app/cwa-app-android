package de.rki.coronawarnapp.vaccination.core.qrcode

import de.rki.coronawarnapp.vaccination.core.RawCOSEObject

data class VaccinationCertificateQRCode(
    val parsedData: VaccinationCertificateData,
    val certificateCOSE: RawCOSEObject,
) {
    val uniqueCertificateIdentifier: String
        get() = parsedData.vaccinationCertificate.vaccinationDatas.single().uniqueCertificateIdentifier
}

