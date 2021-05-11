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

    @Test
    fun `person identifier calculation`() {
        VaccinationContainer(
            vaccinationCertificateCOSE = VaccinationTestData.PERSON_A_VAC_1_COSE,
            scannedAt = Instant.ofEpochSecond(123456789)
        ).personIdentifier shouldBe VaccinatedPersonIdentifier(
            dateOfBirth = LocalDate.parse("1966-11-11"),
            firstNameStandardized = "ANDREAS",
            lastNameStandardized = "ASTRA<EINS"
        )
    }

    @Test
    fun `full property decoding - 1 of 2`() {
        VaccinationContainer(
            vaccinationCertificateCOSE = VaccinationTestData.PERSON_A_VAC_1_COSE,
            scannedAt = Instant.ofEpochSecond(123456789)
        ).apply {
            certificate shouldBe VaccinationTestData.PERSON_A_VAC_1_CERTIFICATE
            certificateId shouldBe "01DE/00001/1119305005/7T1UG87G61Y7NRXIBQJDTYQ9#S"
            isEligbleForProofCertificate shouldBe false
        }
    }

    @Test
    fun `full property decoding - 2 of 2`() {
        VaccinationContainer(
            vaccinationCertificateCOSE = VaccinationTestData.PERSON_A_VAC_2_COSE,
            scannedAt = Instant.ofEpochSecond(123456789)
        ).apply {
            certificate shouldBe VaccinationTestData.PERSON_A_VAC_2_CERTIFICATE
            certificateId shouldBe "01DE/00001/1119305005/6IPYBAIDWEWRWW73QEP92FQSN#S"
            isEligbleForProofCertificate shouldBe true
        }
    }

    @Test
    fun `mapping to user facing data - valueset is null`() {
        VaccinationContainer(
            vaccinationCertificateCOSE = VaccinationTestData.PERSON_A_VAC_1_COSE,
            scannedAt = Instant.ofEpochSecond(123456789)
        ).toVaccinationCertificate(null).apply {
            firstName shouldBe "Andreas"
            lastName shouldBe "Astrá Eins"
            dateOfBirth shouldBe LocalDate.parse("1966-11-11")
            vaccinatedAt shouldBe LocalDate.parse("2021-03-01")
            vaccineName shouldBe "1119305005"
            vaccineManufacturer shouldBe "ORG-100001699"
            medicalProductName shouldBe "EU/1/21/1529"
            doseNumber shouldBe 1
            totalSeriesOfDoses shouldBe 2
            certificateIssuer shouldBe "Bundesministerium für Gesundheit - Test01"
            certificateCountry shouldBe Country.DE
            certificateId shouldBe "01DE/00001/1119305005/7T1UG87G61Y7NRXIBQJDTYQ9#S"
            personIdentifier shouldBe VaccinatedPersonIdentifier(
                dateOfBirth = LocalDate.parse("1966-11-11"),
                firstNameStandardized = "ANDREAS",
                lastNameStandardized = "ASTRA<EINS"
            )
        }
    }

    @Test
    fun `mapping to user facing data - with valueset`() {
        val valueSet = mockk<VaccinationValueSet> {
            every { getDisplayText("ORG-100001699") } returns "Manufactorer-Name"
            every { getDisplayText("EU/1/21/1529") } returns "MedicalProduct-Name"
            every { getDisplayText("1119305005") } returns "Vaccine-Name"
        }
        VaccinationContainer(
            vaccinationCertificateCOSE = VaccinationTestData.PERSON_A_VAC_1_COSE,
            scannedAt = Instant.ofEpochSecond(123456789)
        ).toVaccinationCertificate(valueSet).apply {
            firstName shouldBe "Andreas"
            lastName shouldBe "Astrá Eins"
            dateOfBirth shouldBe LocalDate.parse("1966-11-11")
            vaccinatedAt shouldBe LocalDate.parse("2021-03-01")
            vaccineName shouldBe "Vaccine-Name"
            vaccineManufacturer shouldBe "Manufactorer-Name"
            medicalProductName shouldBe "MedicalProduct-Name"
            doseNumber shouldBe 1
            totalSeriesOfDoses shouldBe 2
            certificateIssuer shouldBe "Bundesministerium für Gesundheit - Test01"
            certificateCountry shouldBe Country.DE
            certificateId shouldBe "01DE/00001/1119305005/7T1UG87G61Y7NRXIBQJDTYQ9#S"
            personIdentifier shouldBe VaccinatedPersonIdentifier(
                dateOfBirth = LocalDate.parse("1966-11-11"),
                firstNameStandardized = "ANDREAS",
                lastNameStandardized = "ASTRA<EINS"
            )
        }
    }
}
