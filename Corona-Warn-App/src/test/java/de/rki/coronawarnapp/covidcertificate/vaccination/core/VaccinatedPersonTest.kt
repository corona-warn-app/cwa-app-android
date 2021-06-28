package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainer
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.TimeZone
import javax.inject.Inject

class VaccinatedPersonTest : BaseTest() {

    @Inject lateinit var testData: VaccinationTestData

    lateinit var defaultTimezone: TimeZone

    @BeforeEach
    fun setup() {
        DaggerVaccinationTestComponent.factory().create().inject(this)
        defaultTimezone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
    }

    @AfterEach
    fun teardown() {
        TimeZone.setDefault(defaultTimezone)
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
            // Less than 14 days
            getVaccinationStatus(
                Instant.parse("2021-04-27T12:00:00.000Z")
            ) shouldBe VaccinatedPerson.Status.COMPLETE
            getVaccinationStatus(
                Instant.parse("2021-05-10T12:00:00.000Z")
            ) shouldBe VaccinatedPerson.Status.COMPLETE

            // 14 days exactly
            getVaccinationStatus(
                Instant.parse("2021-05-11T12:00:00.000Z")
            ) shouldBe VaccinatedPerson.Status.COMPLETE

            // More than 14 days
            getVaccinationStatus(
                Instant.parse("2021-05-12T12:00:00.000Z")
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
            getTimeUntilImmunity(Instant.parse("2021-04-27T12:00:00.000Z"))!!.apply {
                standardDays shouldBe 13
            }
            getTimeUntilImmunity(Instant.parse("2021-05-10T12:00:00.000Z"))!!.apply {
                standardDays shouldBe 0
                standardHours shouldBe 10
            }
            getTimeUntilImmunity(Instant.parse("2021-05-11T12:00:00.000Z"))!!.apply {
                standardDays shouldBe 0
            }
        }
    }

    @Test
    fun `time until immunity - case #3562`() {
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any()) } returns mockk<VaccinationCertificate>().apply {
                        every { vaccinatedAt } returns LocalDate.parse("2021-06-13")
                        every { doseNumber } returns 2
                        every { totalSeriesOfDoses } returns 2
                    }
                }
            )
        }

        VaccinatedPerson(data = personData, valueSet = null).apply {
            // User was in GMT+2 timezone, we want their MIDNIGHT
            // Last day before immunity, UI shows 0 days until immunity
            Instant.parse("2021-06-27T12:00:00.000Z").let { now ->
                getTimeUntilImmunity(now)!!.standardHours shouldBe -14
                getTimeUntilImmunity(now)!!.standardDays shouldBe 0
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.COMPLETE
            }
            // Immunity should be reached at midnight in the users timezone
            Instant.parse("2021-06-27T22:00:00.000Z").let { now ->
                getTimeUntilImmunity(now)!!.standardHours shouldBe -24
                getTimeUntilImmunity(now)!!.standardDays shouldBe -1
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.IMMUNITY
            }
        }
    }

    @Test
    fun `time until immunity - case Luka#1`() {
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any()) } returns mockk<VaccinationCertificate>().apply {
                        every { vaccinatedAt } returns LocalDate.parse("2021-01-01")
                        every { doseNumber } returns 2
                        every { totalSeriesOfDoses } returns 2
                    }
                }
            )
        }

        VaccinatedPerson(data = personData, valueSet = null).apply {
            Instant.parse("2021-01-14T0:00:00.000Z").let { now ->
                getTimeUntilImmunity(now)!!.standardHours shouldBe 23
                getTimeUntilImmunity(now)!!.standardDays shouldBe 0
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.COMPLETE
            }
            Instant.parse("2021-01-15T0:00:00.000Z").let { now ->
                getTimeUntilImmunity(now)!!.standardHours shouldBe -1
                getTimeUntilImmunity(now)!!.standardDays shouldBe 0
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.COMPLETE
            }
            Instant.parse("2021-01-16T0:00:00.000Z").let { now ->
                getTimeUntilImmunity(now)!!.standardHours shouldBe -25
                getTimeUntilImmunity(now)!!.standardDays shouldBe -1
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.IMMUNITY
            }
        }
    }
}
