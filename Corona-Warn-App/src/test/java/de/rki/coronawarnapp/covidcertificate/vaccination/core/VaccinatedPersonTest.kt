package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainer
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.DateTimeZone
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
    lateinit var defaultTimezone: DateTimeZone

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
        defaultTimezone = DateTimeZone.getDefault()
        DateTimeZone.setDefault(DateTimeZone.UTC)
    }

    @AfterEach
    fun teardown() {
        DateTimeZone.setDefault(defaultTimezone)
    }

    @Test
    fun `test name combinations`() {
        val conId = VaccinationCertificateContainerId("VaccinationCertificateContainerId")
        val certificate = mockk<VaccinationCertificate>()
        val vaccinationContainer = mockk<VaccinationContainer>().apply {
            every { toVaccinationCertificate(any(), any()) } returns certificate
            every { containerId } returns conId
            every { isNotRecycled } returns true
        }
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(vaccinationContainer)
        }
        val vaccinatedPerson = VaccinatedPerson(
            data = personData,
            certificateStates = mapOf(conId to CwaCovidCertificate.State.Invalid()),
            valueSet = null
        )

        certificate.apply { every { fullName } returns "Straw Berry" }
        vaccinatedPerson.fullName shouldBe "Straw Berry"
        certificate.apply { every { fullName } returns "Siphon" }
        vaccinatedPerson.fullName shouldBe "Siphon"
    }

    @Test
    fun `vaccination status - INCOMPLETE`() {
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(testData.personAVac1Container)
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
        }
        val vaccinatedPerson = VaccinatedPerson(
            data = personData,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() },
            valueSet = null
        )

        vaccinatedPerson.getVaccinationStatus(Instant.EPOCH) shouldBe VaccinatedPerson.Status.INCOMPLETE
    }

    @Test
    fun `vaccination status - COMPLETE`() {
        val personData = mockk<VaccinatedPersonData>().apply {
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { vaccinatedOn } returns LocalDate.parse("2021-04-28")
                            every { doseNumber } returns 2
                            every { totalSeriesOfDoses } returns 2
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
                            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-04-27")
                            every { isSeriesCompletingShot } returns true
                            every { isBooster } returns false
                        }
                    every { containerId } returns VaccinationCertificateContainerId("VaccinationCertificateContainerId")
                    every { isNotRecycled } returns true
                }
            )
        }
        val vaccinatedPerson = VaccinatedPerson(
            data = personData,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() },
            valueSet = null
        )

        vaccinatedPerson.getVaccinationStatus(Instant.EPOCH) shouldBe VaccinatedPerson.Status.COMPLETE
    }

    @Test
    fun `vaccination status - IMMUNITY`() {
        // vaccinatedAt "2021-04-27"
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { vaccinatedOn } returns LocalDate.parse("2021-04-28")
                            every { doseNumber } returns 2
                            every { totalSeriesOfDoses } returns 2
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
                            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-04-27")
                            every { isSeriesCompletingShot } returns true
                            every { isBooster } returns  false
                        }
                    every { containerId } returns VaccinationCertificateContainerId("VaccinationCertificateContainerId")
                    every { isNotRecycled } returns true
                }
            )
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
        }
        val vaccinatedPerson = VaccinatedPerson(
            data = personData,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() },
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
        val personData = mockk<VaccinatedPersonData>().apply {
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { vaccinatedOn } returns LocalDate.parse("2021-04-27")
                            every { doseNumber } returns 2
                            every { totalSeriesOfDoses } returns 2
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
                            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-04-27")
                            every { isSeriesCompletingShot } returns true
                            every { isBooster } returns false
                        }
                    every { containerId } returns VaccinationCertificateContainerId("VaccinationCertificateContainerId")
                    every { isNotRecycled } returns true
                }
            )
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
        }
        VaccinatedPerson(
            data = personData, valueSet = null,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() }
        ).apply {

            Instant.parse("2021-04-27T12:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!!.apply {
                    this shouldBe 15
                }
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.COMPLETE
            }
            Instant.parse("2021-05-10T12:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!!.apply { this shouldBe 2 }
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.COMPLETE
            }
            Instant.parse("2021-05-11T12:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!!.apply { this shouldBe 1 }
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.COMPLETE
            }
            Instant.parse("2021-05-12T0:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!!.apply { this shouldBe 0 }
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.IMMUNITY
            }
        }
    }

    @Test
    fun `time until immunity - case #3562`() {
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Berlin")))

        val personData = mockk<VaccinatedPersonData>().apply {
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { vaccinatedOn } returns LocalDate.parse("2021-06-13")
                            every { doseNumber } returns 2
                            every { totalSeriesOfDoses } returns 2
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
                            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-06-13")
                            every { isSeriesCompletingShot } returns true
                            every { isBooster } returns false
                        }
                    every { containerId } returns VaccinationCertificateContainerId("VaccinationCertificateContainerId")
                    every { isNotRecycled } returns true
                }
            )
        }

        VaccinatedPerson(
            data = personData,
            valueSet = null,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() }
        ).apply {
            // User was in GMT+2 timezone (UTC+2) , we want their MIDNIGHT
            // Last day before immunity, UI shows 1 day until immunity
            Instant.parse("2021-06-27T12:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!!.apply { this shouldBe 1 }
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.COMPLETE
            }
            // Immunity should be reached at midnight in the users timezone
            Instant.parse("2021-06-27T22:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!!.apply { this shouldBe 0 }
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.IMMUNITY
            }
        }
    }

    @Test
    fun `time until immunity - case Luka#1`() {
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Berlin")))

        val personData = mockk<VaccinatedPersonData>().apply {
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { doseNumber } returns 2
                            every { totalSeriesOfDoses } returns 2
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
                            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { isSeriesCompletingShot } returns true
                            every { isBooster } returns false
                        }

                    every { containerId } returns VaccinationCertificateContainerId("VaccinationCertificateContainerId")
                    every { isNotRecycled } returns true
                }
            )
        }

        VaccinatedPerson(
            data = personData,
            valueSet = null,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() }
        ).apply {
            Instant.parse("2021-01-14T0:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!! shouldBe 2
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.COMPLETE
            }
            Instant.parse("2021-01-15T0:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!! shouldBe 1
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.COMPLETE
            }
            // Case Luka#1 happens on 15.01.21, this mean it's winter time!
            // The users timezone is GMT+1 (winter-time) (UTC+1), not GMT+2 (summer-time) (UTC+2)
            Instant.parse("2021-01-15T22:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!! shouldBe 1
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.COMPLETE
            }
            Instant.parse("2021-01-16T0:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!! shouldBe 0
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.IMMUNITY
            }
            Instant.parse("2021-01-15T23:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!! shouldBe 0
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.IMMUNITY
            }
        }
    }

    @Test
    fun `vaccination with 1 of 1 dose after recovery returns immunity - case BIONTECH`() {
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Berlin")))

        val personData = mockk<VaccinatedPersonData>().apply {
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { doseNumber } returns 1
                            every { totalSeriesOfDoses } returns 1
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528" // BIONTECH
                            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { isSeriesCompletingShot } returns true
                            every { isBooster } returns false
                        }

                    every { containerId } returns VaccinationCertificateContainerId("VaccinationCertificateContainerId")
                    every { isNotRecycled } returns true
                }
            )
        }

        VaccinatedPerson(
            data = personData,
            valueSet = null,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() }
        ).apply {
            Instant.parse("2021-01-14T0:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!! shouldBe 2
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.IMMUNITY
            }
        }
    }

    @Test
    fun `vaccination with 1 of 1 dose after recovery returns immunity - case MODERNA`() {
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Berlin")))

        val personData = mockk<VaccinatedPersonData>().apply {
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { doseNumber } returns 1
                            every { totalSeriesOfDoses } returns 1
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1507" // MODERNA
                            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { isSeriesCompletingShot } returns true
                            every { isBooster } returns false
                        }

                    every { containerId } returns VaccinationCertificateContainerId("VaccinationCertificateContainerId")
                    every { isNotRecycled } returns true
                }
            )
        }

        VaccinatedPerson(
            data = personData,
            valueSet = null,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() }
        ).apply {
            Instant.parse("2021-01-14T0:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!! shouldBe 2
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.IMMUNITY
            }
        }
    }

    @Test
    fun `vaccination with 1 of 1 dose after recovery returns immunity - case ASTRA`() {
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Berlin")))

        val personData = mockk<VaccinatedPersonData>().apply {
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { doseNumber } returns 1
                            every { totalSeriesOfDoses } returns 1
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/21/1529" // ASTRA
                            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { isSeriesCompletingShot } returns true
                            every { isBooster } returns false
                        }

                    every { containerId } returns VaccinationCertificateContainerId("VaccinationCertificateContainerId")
                    every { isNotRecycled } returns true
                }
            )
        }

        VaccinatedPerson(
            data = personData,
            valueSet = null,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() }
        ).apply {
            Instant.parse("2021-01-14T0:00:00.000Z").let { now ->
                getDaysUntilImmunity(now)!! shouldBe 2
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.IMMUNITY
            }
        }
    }

    @Test
    fun `vaccination with 1 of 2 dose returns incomplete`() {
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Berlin")))

        val personData = mockk<VaccinatedPersonData>().apply {
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { doseNumber } returns 1
                            every { totalSeriesOfDoses } returns 2
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528" // BIONTECH
                        }

                    every { containerId } returns VaccinationCertificateContainerId("VaccinationCertificateContainerId")
                    every { isNotRecycled } returns true
                }
            )
        }

        VaccinatedPerson(
            data = personData,
            valueSet = null,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() }
        ).apply {
            Instant.parse("2021-01-14T0:00:00.000Z").let { now ->
                getVaccinationStatus(now) shouldBe VaccinatedPerson.Status.INCOMPLETE
            }
        }
    }

    @Test
    fun `vaccination filtered by recycle`() {
        val recycledID = VaccinationCertificateContainerId("Recycled")
        val notRecycledID = VaccinationCertificateContainerId("NotRecycled")

        val personData = mockk<VaccinatedPersonData>().apply {
            every { boosterRule } returns null
            every { lastSeenBoosterRuleIdentifier } returns null
            every { lastBoosterNotifiedAt } returns null
            every { vaccinations } returns setOf(
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { containerId } returns notRecycledID
                            every { vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { doseNumber } returns 1
                            every { totalSeriesOfDoses } returns 1
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/21/1529" // ASTRA
                            every { isNotRecycled } returns true
                        }

                    every { containerId } returns notRecycledID
                    every { isNotRecycled } returns true
                    every { isRecycled } returns false
                },
                mockk<VaccinationContainer>().apply {
                    every { toVaccinationCertificate(any(), any()) } returns
                        mockk<VaccinationCertificate>().apply {
                            every { containerId } returns recycledID
                            every { vaccinatedOn } returns LocalDate.parse("2021-01-01")
                            every { doseNumber } returns 1
                            every { totalSeriesOfDoses } returns 1
                            every { rawCertificate.vaccination.doseNumber } returns doseNumber
                            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/21/1529" // ASTRA
                            every { isRecycled } returns true
                        }

                    every { containerId } returns recycledID
                    every { isNotRecycled } returns false
                    every { isRecycled } returns true
                }
            )
        }

        VaccinatedPerson(
            data = personData,
            valueSet = null,
            certificateStates = personData.vaccinations
                .associate { it.containerId to CwaCovidCertificate.State.Invalid() }
        ).run {
            recycledVaccinationCertificates.also {
                it.size shouldBe 1

                val cert = it.first()
                cert.containerId.qrCodeHash shouldBe "Recycled"
                cert.isRecycled shouldBe true
            }

            vaccinationCertificates.also {
                it.size shouldBe 1

                val cert = it.first()
                cert.containerId.qrCodeHash shouldBe "NotRecycled"
                cert.isNotRecycled shouldBe true
            }
        }
    }
}
