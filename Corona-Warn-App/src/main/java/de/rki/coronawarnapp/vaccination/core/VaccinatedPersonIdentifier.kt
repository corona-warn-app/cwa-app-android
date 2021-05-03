package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.qrcode.ScannedVaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationDateOfBirthMissmatchException
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationNameMissmatchException
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainer
import org.joda.time.LocalDate

data class VaccinatedPersonIdentifier(
    val dateOfBirth: LocalDate,
    val lastNameStandardized: String,
    val firstNameStandardized: String
) {
    val code: String by lazy {
        val dob = dateOfBirth.toString()
        val lastName = lastNameStandardized
        val firstName = firstNameStandardized
        "$dob#$lastName#$firstName"
    }

    fun requireMatch(other: VaccinatedPersonIdentifier) {
        if (lastNameStandardized != other.lastNameStandardized) {
            throw VaccinationNameMissmatchException(
                "Family name does not match, got ${other.lastNameStandardized}, expected $lastNameStandardized"
            )
        }
        if (firstNameStandardized != other.firstNameStandardized) {
            throw VaccinationNameMissmatchException(
                "Given name does not match, got ${other.firstNameStandardized}, expected $firstNameStandardized"
            )
        }
        if (dateOfBirth != other.dateOfBirth) {
            throw VaccinationDateOfBirthMissmatchException(
                "Date of birth does not match, got ${other.dateOfBirth}, expected $dateOfBirth"
            )
        }
    }
}

val VaccinationContainer.StoredCertificate.personIdentifier: VaccinatedPersonIdentifier
    get() = VaccinatedPersonIdentifier(
        dateOfBirth = dateOfBirth,
        lastNameStandardized = lastNameStandardized,
        firstNameStandardized = firstNameStandardized
    )

val ScannedVaccinationCertificate.personIdentifier: VaccinatedPersonIdentifier
    get() = VaccinatedPersonIdentifier(
        dateOfBirth = dateOfBirth,
        lastNameStandardized = lastNameStandardized,
        firstNameStandardized = firstNameStandardized
    )
