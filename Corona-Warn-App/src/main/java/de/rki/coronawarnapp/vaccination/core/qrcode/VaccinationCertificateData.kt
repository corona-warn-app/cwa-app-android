package de.rki.coronawarnapp.vaccination.core.qrcode

/**
 * Represents the information gained from data in COSE representation
 */
data class VaccinationCertificateData(
    val header: VaccinationCertificateHeader,
    val vaccinationCertificate: VaccinationCertificateV1,
)
