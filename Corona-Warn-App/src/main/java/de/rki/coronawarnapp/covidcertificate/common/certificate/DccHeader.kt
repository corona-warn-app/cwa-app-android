package de.rki.coronawarnapp.covidcertificate.common.certificate

import java.time.Instant

data class DccHeader(
    val issuer: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
)
