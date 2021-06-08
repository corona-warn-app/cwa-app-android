package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainer
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class VaccinatedPersonTest : BaseTest() {

    @Inject lateinit var testData: VaccinationTestData

    @BeforeEach
    fun setup() {
        DaggerVaccinationTestComponent.factory().create().inject(this)
    }

    @Test
    fun `test name combinations`() {
        val certificate = mockk<VaccinationCertificate>()
        val vaccinationContainer = mockk<VaccinationContainer>().apply {
            every { toVaccinationCertificate(any()) } returns certificate
        }
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(vaccinationContainer)
        }
        val vaccinatedPerson = VaccinatedPerson(
            data = personData,
            valueSet = null
        )

        certificate.apply {
            every { fullName } returns "Straw Berry"
        }
        vaccinatedPerson.fullName shouldBe "Straw Berry"

        certificate.apply {
            every { fullName } returns "Siphon"
        }
        vaccinatedPerson.fullName shouldBe "Siphon"
    }

    @Test
    fun `vaccination status - INCOMPLETE`() {
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(testData.personAVac1Container)
        }
        val vaccinatedPerson = VaccinatedPerson(
            data = personData,
            valueSet = null
        )

        vaccinatedPerson.getVaccinationStatus(Instant.EPOCH) shouldBe VaccinatedPerson.Status.INCOMPLETE
    }

    @Test
    fun `vaccination status - COMPLETE`() {
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(testData.personAVac1Container, testData.personAVac2Container)
        }
        val vaccinatedPerson = VaccinatedPerson(
            data = personData,
            valueSet = null
        )

        vaccinatedPerson.getVaccinationStatus(Instant.EPOCH) shouldBe VaccinatedPerson.Status.COMPLETE
    }

    @Test
    fun `vaccination status - IMMUNITY`() {
        // vaccinatedAt "2021-04-27"
        val immunityContainer = testData.personAVac2Container
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(testData.personAVac1Container, immunityContainer)
        }
        val vaccinatedPerson = VaccinatedPerson(
            data = personData,
            valueSet = null
        )

        vaccinatedPerson.apply {
            getVaccinationStatus(
                Instant.parse("2021-04-27T12:00:00.000Z")
            ) shouldBe VaccinatedPerson.Status.COMPLETE
            getVaccinationStatus(
                Instant.parse("2021-05-10T12:00:00.000Z")
            ) shouldBe VaccinatedPerson.Status.COMPLETE
            getVaccinationStatus(
                Instant.parse("2021-05-11T12:00:00.000Z")
            ) shouldBe VaccinatedPerson.Status.IMMUNITY
        }
    }

    @Test
    fun `time until status IMMUNITY`() {
        // vaccinatedAt "2021-04-27"
        val immunityContainer = testData.personAVac2Container
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(testData.personAVac1Container, immunityContainer)
        }
        val vaccinatedPerson = VaccinatedPerson(
            data = personData,
            valueSet = null
        )
        vaccinatedPerson.apply {
            getTimeUntilImmunity(
                Instant.parse("2021-04-27T12:00:00.000Z")
            )!!.standardDays shouldBe Duration.standardDays(14).standardDays
            getTimeUntilImmunity(
                Instant.parse("2021-05-10T12:00:00.000Z")
            )!!.standardDays shouldBe Duration.standardDays(1).standardDays
            getTimeUntilImmunity(
                Instant.parse("2021-05-11T12:00:00.000Z")
            )!!.standardDays shouldBe Duration.ZERO.standardDays
        }
    }
}
