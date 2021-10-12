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

    internal val groupingKey: String
        get() {
            val lastName = lastNameStandardized.trim()
            val firstName = firstNameStandardized?.trim()
            return "$dateOfBirthFormatted#$lastName#$firstName".condense()
        }

    /**
     * Used internally to identify and store the data related to this person.
     */
    internal val code: String
        get() = groupingKey

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is CertificatePersonIdentifier) return false
        return groupingKey == other.groupingKey
    }

    override fun hashCode(): Int {
        return groupingKey.hashCode()
    }

    /**
     * Can be used as external identifier for the data set representing this person.
     * e.g. pass this identifier as uri argument.
     */
    val codeSHA256: String
        get() = groupingKey.toSHA256()

    internal fun requireMatch(other: CertificatePersonIdentifier) {
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

internal fun String.condense() = this.replace("\\s+".toRegex(), " ").replace("<+".toRegex(), "<")
