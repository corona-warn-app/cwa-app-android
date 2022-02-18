package de.rki.coronawarnapp.covidcertificate.common.certificate

import androidx.annotation.VisibleForTesting
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
    @JsonProperty("givenNameStandardized") val firstNameStandardized: String? = null,
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @JsonIgnore val sanitizedFamilyName: List<String> = lastNameStandardized.sanitizeName()
    @JsonIgnore private val sanitizedGivenName: List<String> = firstNameStandardized?.sanitizeName() ?: emptyList()

    /**
    Method shall decide whether the DGCs belongs the same holder.
    Two DCCs shall be considered as belonging to the same holder, if:

    - the sanitized `dob` attributes are the same strings, and
    - one of:
    - the intersection/overlap of the name components of sanitized `a.nam.fnt` and `b.nam.fnt` has at least one element,
    and the intersection/overlap of the name components of sanitized `a.nam.gnt` and `b.nam.gnt` has at least one
    element or both are empty sets (`gnt` is an optional field)
    - the intersection/overlap of the name components of sanitized `a.nam.fnt` and `b.nam.gnt` has at least one element,
    and the intersection/overlap of the name components of sanitized `a.nam.gnt` and `b.nam.fnt` has at least one
    element
     */
    // or belongsToTheSamePerson()
    fun isTheSamePerson(other: CwaCovidCertificate): Boolean = isTheSamePerson(other.personIdentifier)
    fun isTheSamePerson(other: CertificatePersonIdentifier?): Boolean {
        if (other == null) return false
        if (dateOfBirthFormatted.trim() != other.dateOfBirthFormatted.trim()) return false

        // TODO: need refactoring
        val familyNameOverlap = sanitizedFamilyName.intersect(other.sanitizedFamilyName).isNotEmpty()
        val givenNameOverlap = sanitizedGivenName.intersect(other.sanitizedGivenName).isNotEmpty()
        val bothGivenNamesAreEmpty = sanitizedGivenName.isEmpty() && other.sanitizedGivenName.isEmpty()
        val familyNameAndGivenNameAreSwapped = sanitizedFamilyName.intersect(other.sanitizedGivenName).isNotEmpty() &&
            sanitizedGivenName.intersect(other.sanitizedFamilyName).isNotEmpty()

        if (familyNameOverlap && (givenNameOverlap || bothGivenNamesAreEmpty))
        {
            return true
        }

        return familyNameAndGivenNameAreSwapped
    }

    /**
     * Used internally to group and store the data related to this person.
     */
    @get:JsonIgnore
    internal val groupingKey: String // String representation of Person Identifier
        get() {
            val lastName = sanitizedFamilyName.joinToString(separator = "<")
            val firstName = sanitizedGivenName.joinToString(separator = "<")
            return "$dateOfBirthFormatted#$lastName#$firstName"
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

/*
Function sanitize names according to the following requirements:

- dots `.` and dashes `-` shall be replaced by `<`
- the string shall be converted to upper-case
- German umlauts `Ä/ä`, `Ö/ö`, `Ü/ü` shall be replaced by `AE`, `OE`, `UE`
- German `ß` shall be replaced by `SS`
- the string shall be trimmed for leading and training whitespace
- the string shall be trimmed for leading and trailing `<`
- any whitespace in the string shall be replaced by `<`
- any occurrence of more than one `<` shall be replaced by a single `<`

Name fields shall be split into components as follows:

- the string shall be split by `<`
- components with the value `DR` shall be filtered out
 */
private fun String.sanitizeName(): List<String> {
    val filteringList = listOf("DR")
    return uppercase()
        .trim()
        .trim('<')
        .replace("\\s+".toRegex(), "<")
        .replace(".", "<")
        .replace("-", "<")
        .replace("Ä", "AE")
        .replace("Ö", "OE")
        .replace("Ü", "UE")
        .replace("ß", "SS")
        .replace("<+".toRegex(), "<")
        .split("<")
        .filter { !filteringList.contains(it) }
        .filter { it.isNotBlank() }
}
