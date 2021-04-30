package de.rki.coronawarnapp.vaccination.core

import org.joda.time.Instant

data class ProofCertificate(
    val expiresAt: Instant
)
