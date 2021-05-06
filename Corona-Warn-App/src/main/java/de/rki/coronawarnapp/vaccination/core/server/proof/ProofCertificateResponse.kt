package de.rki.coronawarnapp.vaccination.core.server.proof

import okio.ByteString

interface ProofCertificateResponse {
    val proofCertificateData: ProofCertificateData

    // COSE representation of the Proof Certificate (as byte sequence)
    val proofCertificateCOSE: ByteString
}
