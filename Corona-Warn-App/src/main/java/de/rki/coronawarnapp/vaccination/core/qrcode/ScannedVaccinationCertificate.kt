package de.rki.coronawarnapp.vaccination.core.qrcode

import org.joda.time.LocalDate

interface ScannedVaccinationCertificate {
    // Given name
    val firstName: String

    // Standardized given name
    val firstNameStandardized: String

    // Family name
    val lastName: String

    // Standardized family name
    val lastNameStandardized: String
    val dateOfBirth: LocalDate

    val vaccinatedAt: LocalDate
    val vaccinationLocation: String

    // Disease or agent targeted
    val targetId: String

    // Vaccine or prophylaxis
    val vaccineId: String
    val medicalProductId: String
    val marketAuthorizationHolderId: String

    val doseNumber: Int
    val totalSeriesOfDoses: Int

    val chargeId: String

    val certificateIssuer: String
    val certificateCountryCode: String
    val certificateId: String
}
