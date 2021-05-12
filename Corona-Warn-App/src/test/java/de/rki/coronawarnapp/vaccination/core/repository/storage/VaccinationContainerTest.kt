package de.rki.coronawarnapp.vaccination.core.repository.storage

import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationContainerTest : BaseTest() {

    private fun createInstance() = VaccinationContainer(
        vaccinationCertificateCOSE = VaccinationTestData.PERSON_C_VAC_1_COSE,
        scannedAt = Instant.ofEpochSecond(123456789)
    )

    @Test
    fun `person identifier calculation`() {
        createInstance().personIdentifier shouldBe VaccinatedPersonIdentifier(
            dateOfBirth = LocalDate.parse("1964-08-12"),
            firstNameStandardized = "ERIKA<DOERTE",
            lastNameStandardized = "SCHMITT<MUSTERMANN"
        )
    }

    @Test
    fun `full property decoding`() {
        createInstance().apply {
            certificate shouldBe VaccinationTestData.PERSON_C_VAC_1_CERTIFICATE
            vaccination shouldBe VaccinationTestData.PERSON_C_VAC_1_CERTIFICATE.vaccinationDatas.single()
            certificateId shouldBe "01DE/84503/1119349007/DXSGWLWL40SU8ZFKIYIBK39A3#S"
            isEligbleForProofCertificate shouldBe true
        }
    }

    @Test
    fun `mapping to user facing data - valueset is null`() {
        createInstance().toVaccinationCertificate(null).apply {
            firstName shouldBe "Erika Dörte"
            lastName shouldBe "Schmitt Mustermann"
            dateOfBirth shouldBe LocalDate.parse("1964-08-12")
            vaccinatedAt shouldBe LocalDate.parse("2021-02-02")
            vaccineName shouldBe "1119349007"
            vaccineManufacturer shouldBe "ORG-100030215"
            medicalProductName shouldBe "EU/1/20/1528"
            doseNumber shouldBe 2
            totalSeriesOfDoses shouldBe 2
            certificateIssuer shouldBe "Bundesministerium für Gesundheit"
            certificateCountry shouldBe Country.DE
            certificateId shouldBe "01DE/84503/1119349007/DXSGWLWL40SU8ZFKIYIBK39A3#S"
            personIdentifier shouldBe VaccinatedPersonIdentifier(
                dateOfBirth = LocalDate.parse("1964-08-12"),
                firstNameStandardized = "ERIKA<DOERTE",
                lastNameStandardized = "SCHMITT<MUSTERMANN"
            )
        }
    }

    @Test
    fun `mapping to user facing data - with valueset`() {
        val valueSet = mockk<VaccinationValueSet> {
            every { getDisplayText("ORG-100030215") } returns "Manufactorer-Name"
            every { getDisplayText("EU/1/20/1528") } returns "MedicalProduct-Name"
            every { getDisplayText("1119349007") } returns "Vaccine-Name"
        }
        createInstance().toVaccinationCertificate(valueSet).apply {
            firstName shouldBe "Erika Dörte"
            lastName shouldBe "Schmitt Mustermann"
            dateOfBirth shouldBe LocalDate.parse("1964-08-12")
            vaccinatedAt shouldBe LocalDate.parse("2021-02-02")
            vaccineName shouldBe "Vaccine-Name"
            vaccineManufacturer shouldBe "Manufactorer-Name"
            medicalProductName shouldBe "MedicalProduct-Name"
            doseNumber shouldBe 2
            totalSeriesOfDoses shouldBe 2
            certificateIssuer shouldBe "Bundesministerium für Gesundheit"
            certificateCountry shouldBe Country.DE
            certificateId shouldBe "01DE/84503/1119349007/DXSGWLWL40SU8ZFKIYIBK39A3#S"
            personIdentifier shouldBe VaccinatedPersonIdentifier(
                dateOfBirth = LocalDate.parse("1964-08-12"),
                firstNameStandardized = "ERIKA<DOERTE",
                lastNameStandardized = "SCHMITT<MUSTERMANN"
            )
        }
    }
}
