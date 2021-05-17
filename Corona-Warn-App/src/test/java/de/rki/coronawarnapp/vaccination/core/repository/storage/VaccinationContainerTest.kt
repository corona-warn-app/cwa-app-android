package de.rki.coronawarnapp.vaccination.core.repository.storage

import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.vaccination.core.DaggerVaccinationTestComponent
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class VaccinationContainerTest : BaseTest() {

    @Inject lateinit var testData: VaccinationTestData

    @BeforeEach
    fun setup() {
        DaggerVaccinationTestComponent.factory().create().inject(this)
    }

    @Test
    fun `person identifier calculation`() {
        testData.personAVac1Container.personIdentifier shouldBe VaccinatedPersonIdentifier(
            dateOfBirth = LocalDate.parse("1966-11-11"),
            firstNameStandardized = "ANDREAS",
            lastNameStandardized = "ASTRA<EINS"
        )
    }

    @Test
    fun `header decoding`() {
        testData.personAVac1Container.header.apply {
            issuer shouldBe "DE"
            issuedAt shouldBe Instant.parse("2021-05-11T09:25:00.000Z")
            expiresAt shouldBe Instant.parse("2022-05-11T09:25:00.000Z")
        }
    }

    @Test
    fun `full property decoding - 1 of 2`() {
        testData.personAVac1Container.apply {
            certificate shouldBe testData.personAVac1Certificate
            certificateId shouldBe "01DE/00001/1119305005/7T1UG87G61Y7NRXIBQJDTYQ9#S"
        }
    }

    @Test
    fun `full property decoding - 2 of 2`() {
        testData.personAVac2Container.apply {
            certificate shouldBe testData.personAVac2Certificate
            certificateId shouldBe "01DE/00001/1119305005/6IPYBAIDWEWRWW73QEP92FQSN#S"
        }
    }

    @Test
    fun `mapping to user facing data - valueset is null`() {
        testData.personAVac1Container.toVaccinationCertificate(null).apply {
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
        val vpItem = mockk<VaccinationValueSet.ValueSet.Item> {
            every { key } returns "1119305005"
            every { displayText } returns "Vaccine-Name"
        }

        val mpItem = mockk<VaccinationValueSet.ValueSet.Item> {
            every { key } returns "EU/1/21/1529"
            every { displayText } returns "MedicalProduct-Name"
        }

        val maItem = mockk<VaccinationValueSet.ValueSet.Item> {
            every { key } returns "ORG-100001699"
            every { displayText } returns "Manufactorer-Name"
        }

        val vpMockk = mockk<VaccinationValueSet.ValueSet> {
            every { items } returns listOf(vpItem, mpItem, maItem)
        }

        val valueSet = mockk<VaccinationValueSet> {
            every { vp } returns vpMockk
            every { mp } returns vpMockk
            every { ma } returns vpMockk
        }

        testData.personAVac1Container.toVaccinationCertificate(valueSet).apply {
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
            issuer shouldBe "DE"
            issuedAt shouldBe Instant.parse("2021-05-11T09:25:00.000Z")
            expiresAt shouldBe Instant.parse("2022-05-11T09:25:00.000Z")
        }
    }
}
