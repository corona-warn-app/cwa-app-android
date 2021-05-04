package de.rki.coronawarnapp.vaccination.core.repository.storage

import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationContainerTest : BaseTest() {

    private val storedCert = VaccinationContainer.StoredCertificate(
        firstName = "François-Joan",
        firstNameStandardized = "FRANCOIS<JOAN",
        lastName = "d'Arsøns - van Halen",
        lastNameStandardized = "DARSONS<VAN<HALEN",
        dateOfBirth = LocalDate.parse("2009-02-28"),
        targetId = "840539006",
        vaccineId = "1119349007",
        medicalProductId = "EU/1/20/1528",
        marketAuthorizationHolderId = "ORG-100030215",
        doseNumber = 1,
        totalSeriesOfDoses = 2,
        vaccinatedAt = LocalDate.parse("2021-04-21"),
        certificateCountryCode = "NL",
        certificateIssuer = "Ministry of Public Health, Welfare and Sport",
        certificateId = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
        lotNumber = "lotNumber",
    )

    private val container = VaccinationContainer(
        certificate = storedCert,
        scannedAt = Instant.ofEpochMilli(1620062834471),
        certificateBase45 = "BASE45",
        certificateCBORBase64 = "BASE64"
    )

    @Test
    fun `person identifier calculation`() {
        container.personIdentifier shouldBe VaccinatedPersonIdentifier(
            firstNameStandardized = "FRANCOIS<JOAN",
            lastNameStandardized = "DARSONS<VAN<HALEN",
            dateOfBirth = LocalDate.parse("2009-02-28"),
        )

        container.personIdentifier.code shouldBe "2009-02-28#DARSONS<VAN<HALEN#FRANCOIS<JOAN"

        container.copy(
            certificate = storedCert.copy(firstNameStandardized = " ")
        ).personIdentifier.code shouldBe "2009-02-28#DARSONS<VAN<HALEN# "

        container.copy(
            certificate = storedCert.copy(firstNameStandardized = "")
        ).personIdentifier.code shouldBe "2009-02-28#DARSONS<VAN<HALEN#"
    }
}
