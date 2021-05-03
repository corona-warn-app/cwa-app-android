package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.ui.Country
import org.joda.time.Instant
import org.joda.time.LocalDate

interface VaccinationCertificate {
    val firstName: String
    val lastName: String
    val dateOfBirth: LocalDate
    val vaccinatedAt: LocalDate

    val vaccineName: String
    val vaccineManufacturer: String
    val medicalProductName: String

    val chargeId: String
    val certificateIssuer: String
    val certificateCountry: Country
    val certificateId: String
    val scannedAt: Instant

    val personIdentifier: VaccinatedPersonIdentifier
}
