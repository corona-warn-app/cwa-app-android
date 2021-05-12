package de.rki.coronawarnapp.vaccination.core.server.proof

import de.rki.coronawarnapp.vaccination.core.certificate.CoseCertificateHeader
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1

/**
 * Represents the information gained from data in COSE representation
 */
data class ProofCertificateData constructor(
    val header: CoseCertificateHeader,
    val certificate: VaccinationDGCV1,
)
