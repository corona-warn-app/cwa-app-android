package de.rki.coronawarnapp.vaccination.core.qrcode

/**
 * Represents the information gained from data in COSE representation
 */
data class VaccinationCertificateData constructor(
    // Parsed json
    val vaccinationCertificate: VaccinationCertificateV1
)
