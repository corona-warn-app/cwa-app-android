package de.rki.coronawarnapp.vaccination.core.qrcode

import org.joda.time.Instant

data class VaccinationCertificateHeader(
    val issuer: String,
    val issuedAt: Instant,
    val expiresAt: Instant
)
