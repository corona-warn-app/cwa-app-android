package de.rki.coronawarnapp.vaccination.core.server

import org.joda.time.Instant

/**
 * Represents the information gained from data in COSE representation
 */
data class ProofCertificateData constructor(
    // Parsed json
    val proofCertificate: ProofCertificateV1,
    // Issuer (2-letter country code)
    val issuerCountryCode: String,
    // Issued at (server data returns UNIX timestamp in seconds)
    val issuedAt: Instant,
    // Expiration time (server data returns UNIX timestamp in seconds)
    val expiresAt: Instant,
)
