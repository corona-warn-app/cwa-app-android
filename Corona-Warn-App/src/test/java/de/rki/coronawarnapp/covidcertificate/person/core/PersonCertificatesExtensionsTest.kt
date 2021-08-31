package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PersonCertificatesExtensionsTest : BaseTest() {

    private val time = Instant.parse("2021-06-24T14:00:00.000Z")
    private val oneDayDuration = Duration.standardDays(1)

    @Test
    fun `certificate sort order`() {

        val certificateFirst = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns time.plus(oneDayDuration).toLocalDateUtc()
        }

        val certificateSecond = mockk<TestCertificate>().apply {
            every { sampleCollectedAt } returns time
        }

        val certificateThird = mockk<RecoveryCertificate>().apply {
            every { validFrom } returns time.minus(oneDayDuration).toLocalDateUtc()
        }

        val expectedOrder = listOf(certificateFirst, certificateSecond, certificateThird)
        val wrongOrder = listOf(certificateSecond, certificateFirst, certificateThird)
        val wrongOrder2 = listOf(certificateThird, certificateSecond, certificateFirst)

        expectedOrder.toCertificateSortOrder() shouldBe expectedOrder
        wrongOrder.toCertificateSortOrder() shouldBe expectedOrder
        wrongOrder2.toCertificateSortOrder() shouldBe expectedOrder

        wrongOrder shouldNotBe expectedOrder
        wrongOrder2 shouldNotBe expectedOrder
    }

    @Test
    fun `find Highest Priority Certificate`() {
        val first = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time
            every { getState() } returns mockk<State.ExpiringSoon>()
        }

        val firstButExpired = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time
            every { getState() } returns mockk<State.Expired>()
        }

        val second = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time
            every { getState() } returns mockk<State.Valid>()
        }

        val secondButInvalid = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time
            every { getState() } returns mockk<State.Invalid>()
        }

        val third = mockk<VaccinationCertificate>().apply {
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(15)).toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val fourth = mockk<RecoveryCertificate>().apply {
            every { rawCertificate.recovery.validFrom } returns time.toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val fifth = mockk<VaccinationCertificate>().apply {
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns time.toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val sixth = mockk<VaccinationCertificate>().apply {
            every { rawCertificate.vaccination.doseNumber } returns 1
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns time.toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val seventh = mockk<RecoveryCertificate>().apply {
            every { rawCertificate.recovery.validFrom } returns time.minus(Duration.standardDays(181)).toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val eighth = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(149))
            every { getState() } returns mockk<State.Valid>()
        }

        val ninth = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(25))
            every { getState() } returns mockk<State.Valid>()
        }

        val fallback = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "Not-LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time
            every { getState() } returns mockk<State.Invalid>()
        }

        val certificates = mutableListOf(
            firstButExpired,
            secondButInvalid,
            fallback,
            ninth,
            eighth,
            seventh,
            sixth,
            fifth,
            fourth,
            third,
            second,
            first,
        )

        // Valid
        certificates.findHighestPriorityCertificate(time) shouldBe first
        certificates.remove(first)
        certificates.findHighestPriorityCertificate(time) shouldBe second
        certificates.remove(second)
        certificates.findHighestPriorityCertificate(time) shouldBe fourth
        certificates.remove(fourth)
        certificates.findHighestPriorityCertificate(time) shouldBe third
        certificates.remove(third)
        certificates.findHighestPriorityCertificate(time) shouldBe sixth
        certificates.remove(sixth)
        certificates.findHighestPriorityCertificate(time) shouldBe fifth
        certificates.remove(fifth)
        certificates.findHighestPriorityCertificate(time) shouldBe seventh
        certificates.remove(seventh)
        certificates.findHighestPriorityCertificate(time) shouldBe eighth
        certificates.remove(eighth)
        certificates.findHighestPriorityCertificate(time) shouldBe ninth
        certificates.remove(ninth)

        // Expired
        certificates.findHighestPriorityCertificate(time) shouldBe firstButExpired
        certificates.remove(firstButExpired)

        // Invalid
        certificates.findHighestPriorityCertificate(time) shouldBe secondButInvalid
        certificates.remove(secondButInvalid)

        certificates.findHighestPriorityCertificate(time) shouldBe fallback
        certificates.remove(fallback)

        shouldThrow<NoSuchElementException> {
            certificates.findHighestPriorityCertificate(time) shouldBe null
        }
    }

    /**
     * Bad: listOf(null,null,cert).map {...}.firstOrNull() ?: fallback
     *
     * findHighestPriorityCertificate(nowUtc=2021-06-24T14:00:00.000Z): [VaccinationCertificate(#1), VaccinationCertificate(#7)]
     * No certs with state Valid/ExpiringSoon
     * Checking 2 certs with for Expired
     * Rule 3 match (Series-completing Vaccination Certificate > 14 days): VaccinationCertificate(#7)
     * No certs with state Invalid
     * No priority match, this should not happen: [VaccinationCertificate(#1), VaccinationCertificate(#7)]
     *
     *
     * Good: listOf(null,null,cert).mapNotNull {...}.firstOrNull() ?: fallback
     *
     * findHighestPriorityCertificate(nowUtc=2021-06-24T14:00:00.000Z): [VaccinationCertificate(#1), VaccinationCertificate(#7)]
     * No certs with state Valid/ExpiringSoon
     * Checking 2 certs with for Expired
     * Rule 3 match (Series-completing Vaccination Certificate > 14 days): VaccinationCertificate(#7)
     * No certs with state Invalid
     */
    @Test
    fun `fallback behavior when there are no valid certificates`() {
        val first = mockk<VaccinationCertificate>().apply {
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-01-01")
            every { getState() } returns mockk<State.Expired>()
        }

        val second = mockk<VaccinationCertificate>().apply {
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-01-02")
            every { getState() } returns mockk<State.Expired>()
        }

        listOf(first, second).findHighestPriorityCertificate(time) shouldBe second
    }
}
