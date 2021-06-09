package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.VC_DOB_MISMATCH
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.VC_NAME_MISMATCH
import de.rki.coronawarnapp.covidcertificate.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.test.certificate.TestCertificateDccV1
import de.rki.coronawarnapp.covidcertificate.test.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.certificate.VaccinationDGCV1
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import org.joda.time.LocalDate
import timber.log.Timber

data class CertificatePersonIdentifier(
    val dateOfBirth: LocalDate,
    val lastNameStandardized: String,
    val firstNameStandardized: String?
) {

    /**
     * Used internally to idenitify and store the data related to this person.
     */
    internal val code: String by lazy {
        val dob = dateOfBirth.toString()
        val lastName = lastNameStandardized
        val firstName = firstNameStandardized
        "$dob#$lastName#$firstName"
    }

    /**
     * Can be used as external identifier for the data set representing this person.
     * e.g. pass this identifier as uri argument.
     */
    val codeSHA256: String by lazy {
        code.toSHA256()
    }

    fun requireMatch(other: CertificatePersonIdentifier) {
        if (lastNameStandardized != other.lastNameStandardized) {
            Timber.d("Family name does not match, got ${other.lastNameStandardized}, expected $lastNameStandardized")
            throw InvalidVaccinationCertificateException(VC_NAME_MISMATCH)
        }
        if (firstNameStandardized != other.firstNameStandardized) {
            Timber.d("Given name does not match, got ${other.firstNameStandardized}, expected $firstNameStandardized")
            throw InvalidVaccinationCertificateException(VC_NAME_MISMATCH)
        }
        if (dateOfBirth != other.dateOfBirth) {
            Timber.d("Date of birth does not match, got ${other.dateOfBirth}, expected $dateOfBirth")
            throw InvalidVaccinationCertificateException(VC_DOB_MISMATCH)
        }
    }
}

val VaccinationDGCV1.personIdentifier: CertificatePersonIdentifier
    get() = CertificatePersonIdentifier(
        dateOfBirth = dateOfBirth,
        lastNameStandardized = nameData.familyNameStandardized,
        firstNameStandardized = nameData.givenNameStandardized
    )

val VaccinationCertificateQRCode.personIdentifier: CertificatePersonIdentifier
    get() = parsedData.certificate.personIdentifier

val TestCertificateDccV1.personIdentifier: CertificatePersonIdentifier
    get() = CertificatePersonIdentifier(
        dateOfBirth = dateOfBirth,
        lastNameStandardized = nameData.familyNameStandardized,
        firstNameStandardized = nameData.givenNameStandardized
    )

val TestCertificateQRCode.personIdentifier: CertificatePersonIdentifier
    get() = testCertificateData.certificate.personIdentifier
