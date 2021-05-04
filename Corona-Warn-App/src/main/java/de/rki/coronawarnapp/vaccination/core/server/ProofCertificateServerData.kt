package de.rki.coronawarnapp.vaccination.core.server

import okio.ByteString
import org.joda.time.Instant
import org.joda.time.LocalDate

interface ProofCertificateServerData {
    val firstName: String
    val firstNameStandardized: String
    val lastName: String
    val lastNameStandardized: String
    val dateOfBirth: LocalDate
    val targetId: String
    val vaccineId: String
    val medicalProductId: String
    val marketAuthorizationHolderId: String
    val doseNumber: Int
    val totalSeriesOfDoses: Int
    val vaccinatedAt: LocalDate
    val certificateIssuer: String
    val certificateId: String

    // From CBOR Web Token
    // Issuer (2-letter country code)
    val issuerCountryCode: String

    // Issued at (server data returns UNIX timestamp in seconds)
    val issuedAt: Instant

    // Expiration time (server data returns UNIX timestamp in seconds)
    val expiresAt: Instant

    // COSE representation of the Proof Certificate (as byte sequence)
    val proofCertificateCBOR: ByteString
}
