package de.rki.coronawarnapp.vaccination.core

import android.os.Parcelable
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateV1
import de.rki.coronawarnapp.vaccination.core.repository.errors.VaccinationDateOfBirthMissmatchException
import de.rki.coronawarnapp.vaccination.core.repository.errors.VaccinationNameMissmatchException
import de.rki.coronawarnapp.vaccination.core.server.ProofCertificateV1
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.joda.time.LocalDate

@Parcelize
data class VaccinatedPersonIdentifier(
    val dateOfBirth: LocalDate,
    val lastNameStandardized: String,
    val firstNameStandardized: String?
): Parcelable {

    @IgnoredOnParcel
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

val VaccinationCertificateV1.personIdentifier: VaccinatedPersonIdentifier
    get() = VaccinatedPersonIdentifier(
        dateOfBirth = dateOfBirth,
        lastNameStandardized = nameData.familyNameStandardized,
        firstNameStandardized = nameData.givenNameStandardized
    )

val ProofCertificateV1.personIdentifier: VaccinatedPersonIdentifier
    get() = VaccinatedPersonIdentifier(
        dateOfBirth = dateOfBirth,
        lastNameStandardized = nameData.familyNameStandardized,
        firstNameStandardized = nameData.givenNameStandardized
    )

val VaccinationCertificateQRCode.personIdentifier: VaccinatedPersonIdentifier
    get() = parsedData.vaccinationCertificate.personIdentifier
