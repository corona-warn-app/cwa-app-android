package de.rki.coronawarnapp.vaccination.core

import org.joda.time.Instant
import org.joda.time.LocalDate

interface ProofCertificate {
    val personIdentifier: VaccinatedPersonIdentifier

    val expiresAt: Instant

    val firstName: String?
    val lastName: String

    val dateOfBirth: LocalDate

    val vaccineName: String
    val medicalProductName: String
    val vaccineManufacturer: String

    val doseNumber: Int
    val totalSeriesOfDoses: Int

    val vaccinatedAt: LocalDate

    val certificateIssuer: String
    val certificateId: String
}
