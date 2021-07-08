package de.rki.coronawarnapp.covidcertificate.person.core

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
        }

        val second = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time
        }

        val third = mockk<VaccinationCertificate>().apply {
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(15)).toLocalDateUtc()
        }

        val fourth = mockk<RecoveryCertificate>().apply {
            every { rawCertificate.recovery.validFrom } returns time.toLocalDateUtc()
        }

        val fifth = mockk<VaccinationCertificate>().apply {
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns time.toLocalDateUtc()
        }

        val sixth = mockk<VaccinationCertificate>().apply {
            every { rawCertificate.vaccination.doseNumber } returns 1
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns time.toLocalDateUtc()
        }

        val seventh = mockk<RecoveryCertificate>().apply {
            every { rawCertificate.recovery.validFrom } returns time.minus(Duration.standardDays(181)).toLocalDateUtc()
        }

        val eighth = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(149))
        }

        val ninth = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(25))
        }

        val fallback = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "Not-LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time
        }

        val certificates = mutableListOf(fallback, ninth, eighth, seventh, sixth, fifth, fourth, third, second, first)

        certificates.findHighestPriorityCertificate(time) shouldBe first
        certificates.remove(first)
        certificates.findHighestPriorityCertificate(time) shouldBe second
        certificates.remove(second)
        certificates.findHighestPriorityCertificate(time) shouldBe third
        certificates.remove(third)
        certificates.findHighestPriorityCertificate(time) shouldBe fourth
        certificates.remove(fourth)
        certificates.findHighestPriorityCertificate(time) shouldBe fifth
        certificates.remove(fifth)
        certificates.findHighestPriorityCertificate(time) shouldBe sixth
        certificates.remove(sixth)
        certificates.findHighestPriorityCertificate(time) shouldBe seventh
        certificates.remove(seventh)
        certificates.findHighestPriorityCertificate(time) shouldBe eighth
        certificates.remove(eighth)
        certificates.findHighestPriorityCertificate(time) shouldBe ninth
        certificates.remove(ninth)
        certificates.findHighestPriorityCertificate(time) shouldBe fallback
        certificates.remove(fallback)

        shouldThrow<NoSuchElementException> {
            certificates.findHighestPriorityCertificate(time) shouldBe null
        }
    }
}
