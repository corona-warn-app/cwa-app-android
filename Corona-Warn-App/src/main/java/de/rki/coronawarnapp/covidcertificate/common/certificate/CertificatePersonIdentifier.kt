package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.DOB_MISMATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.NAME_MISMATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import timber.log.Timber

data class CertificatePersonIdentifier(
    @JsonProperty("dateOfBirth") val dateOfBirthFormatted: String,
    @JsonProperty("familyNameStandardized") val lastNameStandardized: String,
    @JsonProperty("givenNameStandardized") val firstNameStandardized: String?
) {

    /**
     * Used internally to group and store the data related to this person.
     */
    @get:JsonIgnore
    internal val groupingKey: String
        get() {
            val lastName = lastNameStandardized.trim()
            val firstName = firstNameStandardized?.trim()
            return "$dateOfBirthFormatted#$lastName#$firstName".condense()
        }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is CertificatePersonIdentifier) return false
        return this.groupingKey == other.groupingKey
    }

    override fun hashCode(): Int {
        return this.groupingKey.hashCode()
    }

    /**
     * Can be used as external identifier for the data set representing this person.
     * e.g. pass this identifier as uri argument.
     */
    @get:JsonIgnore
    val codeSHA256: String
        get() = this.groupingKey.toSHA256()

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
