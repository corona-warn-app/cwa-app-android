package de.rki.coronawarnapp.vaccination.ui.list

import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import org.joda.time.LocalDate

/**
 * Mock Data needed for UI development while backend connection is not yet available
 * Can be removed later
 */
internal fun getMockVaccinationCertificate() = MockVaccinationCertificate(
    firstName = "François-Joan",
    lastName = "FRANCOIS<JOAN",
    dateOfBirth = LocalDate.parse("2009-02-28"),
    vaccinatedAt = LocalDate.parse("2021-04-22"),
    vaccineName = "vaccineName",
    vaccineManufacturer = "vaccineManufactorer",
    medicalProductName = "medicalProductName",
    doseNumber = 1,
    totalSeriesOfDoses = 2,
    certificateIssuer = "certificateIssuer",
    certificateCountry = Country.AT,
    certificateId = "certificate Id",
    personIdentifier = getPersonIdentifier(),
    issuer = "BMG",
    issuedAt = Instant.ofEpochMilli(1620835549458).minus(Duration.standardDays(1)),
    expiresAt = Instant.ofEpochMilli(1620835549458).plus(Duration.standardDays(60))
)

fun getPersonIdentifier() = VaccinatedPersonIdentifier(
    dateOfBirth = LocalDate.parse("2009-02-28"),
    lastNameStandardized = "DARSONS<VAN<HALEN",
    firstNameStandardized = "FRANCOIS<JOAN"
)

internal data class MockVaccinationCertificate(
    override val firstName: String?,
    override val lastName: String,
    override val dateOfBirth: LocalDate,
    override val vaccinatedAt: LocalDate,
    override val vaccineName: String,
    override val vaccineManufacturer: String,
    override val medicalProductName: String,
    override val doseNumber: Int,
    override val totalSeriesOfDoses: Int,
    override val certificateIssuer: String,
    override val certificateCountry: Country,
    override val certificateId: String,
    override val personIdentifier: VaccinatedPersonIdentifier,
    override val issuer: String,
    override val issuedAt: Instant,
    override val expiresAt: Instant,
) : VaccinationCertificate
