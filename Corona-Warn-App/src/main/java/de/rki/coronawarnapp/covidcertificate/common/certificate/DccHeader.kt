package de.rki.coronawarnapp.covidcertificate.common.certificate

import org.joda.time.Instant

data class DccHeader(
    val issuer: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
)
