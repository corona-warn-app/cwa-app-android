package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.covidcertificate.common.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.QrCodeString
import org.joda.time.Instant
import org.joda.time.LocalDate

interface TestCertificate {
    val firstName: String?
    val lastName: String

    val dateOfBirth: LocalDate

    /**
     * Disease or agent targeted (required)
     */
    val targetName: String
    val testType: String
    val testResult: String

    /**
     * NAA Test Name (only for PCR tests, but not required)
     */
    val testName: String?

    /**
     * RAT Test name and manufacturer (only for RAT tests, but not required)
     */
    val testNameAndManufactor: String?
    val sampleCollectedAt: Instant
    val testResultAt: Instant?
    val testCenter: String

    val certificateIssuer: String
    val certificateCountry: String
    val certificateId: String

    val personIdentifier: CertificatePersonIdentifier

    val issuer: String
    val issuedAt: Instant
    val expiresAt: Instant

    val qrCode: QrCodeString
}
