package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import org.joda.time.Instant
import org.joda.time.LocalDate

/**
 * For use with the UI
 */
interface CwaCovidCertificate {
    // Header
    val issuer: String
    val issuedAt: Instant
    val expiresAt: Instant

    val qrCode: QrCodeString

    val firstName: String?

    val lastName: String
    val fullName: String
    val dateOfBirth: LocalDate

    val personIdentifier: CertificatePersonIdentifier

    val certificateIssuer: String
    val certificateCountry: String
    val certificateId: String
}
