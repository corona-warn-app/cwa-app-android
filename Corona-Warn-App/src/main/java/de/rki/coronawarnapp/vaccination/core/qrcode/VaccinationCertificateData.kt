package de.rki.coronawarnapp.vaccination.core.qrcode

import de.rki.coronawarnapp.vaccination.core.certificate.CoseCertificateHeader
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1

/**
 * Represents the parsed data from the QR code
 */
data class VaccinationCertificateData(
    val header: CoseCertificateHeader,
    val certificate: VaccinationDGCV1,
)
