package de.rki.coronawarnapp.vaccination.core

import org.joda.time.Instant

interface ProofCertificate {
    val expiresAt: Instant
    val updatedAt: Instant
}
