package de.rki.coronawarnapp.vaccination.core.certificate

import org.joda.time.Instant

data class HealthCertificateHeader(
    override val issuer: String,
    override val issuedAt: Instant,
    override val expiresAt: Instant,
) : CoseCertificateHeader

