package de.rki.coronawarnapp.covidcertificate.vaccination.core.certificate

import org.joda.time.Instant

interface CoseCertificateHeader {
    val issuer: String
    val issuedAt: Instant
    val expiresAt: Instant
}
