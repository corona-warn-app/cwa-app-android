package de.rki.coronawarnapp.vaccination.core.server.proof

import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject

data class ProofCertificateResponse(
    val proofData: ProofCertificateData,
    // COSE representation of the Proof Certificate (as byte sequence)
    val rawCose: RawCOSEObject
)
