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
import org.joda.time.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
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
    fun `vaccination filtered by recycle`() {
        val recycledID = VaccinationCertificateContainerId("Recycled")
        val notRecycledID = VaccinationCertificateContainerId("NotRecycled")

        val personData = mockk<VaccinatedPersonData>().apply {
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
