package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.DOB_MISMATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.NAME_MISMATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import timber.log.Timber

data class CertificatePersonIdentifier(
    @SerializedName("dateOfBirth") val dateOfBirthFormatted: String,
    @SerializedName("familyNameStandardized") val lastNameStandardized: String,
    @SerializedName("givenNameStandardized") val firstNameStandardized: String?
) {

    /**
     * Used internally to idenitify and store the data related to this person.
     */
    internal val code: String
        get() {
            val lastName = lastNameStandardized
            val firstName = firstNameStandardized
            return "$dateOfBirthFormatted#$lastName#$firstName"
        }

    /**
     * Can be used as external identifier for the data set representing this person.
     * e.g. pass this identifier as uri argument.
     */
    val codeSHA256: String
        get() = code.toSHA256()

    fun requireMatch(other: CertificatePersonIdentifier) {
        if (lastNameStandardized != other.lastNameStandardized) {
            Timber.d("Family name does not match, got ${other.lastNameStandardized}, expected $lastNameStandardized")
            throw InvalidVaccinationCertificateException(NAME_MISMATCH)
        }
        if (firstNameStandardized != other.firstNameStandardized) {
            Timber.d("Given name does not match, got ${other.firstNameStandardized}, expected $firstNameStandardized")
            throw InvalidVaccinationCertificateException(NAME_MISMATCH)
        }
        if (dateOfBirthFormatted != other.dateOfBirthFormatted) {
            Timber.d("Date of birth does not match, got ${other.dateOfBirthFormatted}, expected $dateOfBirthFormatted")
            throw InvalidVaccinationCertificateException(DOB_MISMATCH)
        }
    }
}
