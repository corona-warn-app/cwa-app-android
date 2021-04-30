package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.ui.Country
import org.joda.time.Instant
import org.joda.time.LocalDate

data class VaccinationCertificate(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val vaccinatedAt: Instant,
    val vaccineName: String,
    val vaccineManufacturer: String,
    val chargeId: String,
    val certificateIssuer: String,
    val certificateCountry: Country,
    val certificateId: String,
) {
    val identifier: VaccinatedPersonIdentifier get() = ""
}
