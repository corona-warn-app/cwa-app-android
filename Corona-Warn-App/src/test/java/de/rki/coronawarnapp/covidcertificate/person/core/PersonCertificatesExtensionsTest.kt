package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import io.kotest.assertions.throwables.shouldNotThrowAny
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
            every { headerIssuedAt } returns Instant.parse("2021-09-20T10:00:00.000Z")
            every { vaccinatedOn } returns time.plus(oneDayDuration).toLocalDateUtc()
        }

        val certificateFirstIssuedAtAnotherDate = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-07-20T10:00:00.000Z")
            every { vaccinatedOn } returns time.plus(oneDayDuration).toLocalDateUtc()
        }

        val certificateSecond = mockk<TestCertificate>().apply {
            every { sampleCollectedAt } returns time
        }

        val certificateThird = mockk<RecoveryCertificate>().apply {
            every { validFrom } returns time.minus(oneDayDuration).toLocalDateUtc()
        }

        val expectedOrder =
            listOf(certificateFirst, certificateFirstIssuedAtAnotherDate, certificateSecond, certificateThird)
        val wrongOrder =
            listOf(certificateSecond, certificateFirst, certificateThird, certificateFirstIssuedAtAnotherDate)
        val wrongOrder2 =
            listOf(certificateThird, certificateSecond, certificateFirstIssuedAtAnotherDate, certificateFirst)

        expectedOrder.toCertificateSortOrder() shouldBe expectedOrder
        wrongOrder.toCertificateSortOrder() shouldBe expectedOrder
        wrongOrder2.toCertificateSortOrder() shouldBe expectedOrder

        wrongOrder shouldNotBe expectedOrder
        wrongOrder2 shouldNotBe expectedOrder
    }

    @Test
    fun `certificate sort order -  crash EXPOSUREAPP-9880`() {
        val vc1 = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-09-20T10:00:00.000Z")
            every { vaccinatedOn } returns Instant.parse("2021-06-24T14:00:00.000Z").toLocalDateUtc()
        }

        val vc2 = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-09-20T10:00:00.000Z")
            every { vaccinatedOn } returns Instant.parse("2021-04-25T14:00:00.000Z").toLocalDateUtc()
        }

        val rc1 = mockk<RecoveryCertificate>().apply {
            every { validFrom } returns Instant.parse("2021-04-25T14:00:00.000Z").toLocalDateUtc()
        }

        val tc2 = mockk<TestCertificate>().apply {
            every { sampleCollectedAt } returns Instant.parse("2021-08-23T14:00:00.000Z")
        }

        shouldNotThrowAny {
            listOf(vc1, vc2, rc1, tc2).toCertificateSortOrder()
        }
    }

    @Suppress("LongMethod", "ComplexMethod")
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
            every { headerIssuedAt } returns Instant.parse("2021-06-24T14:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(25)).toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val thirdIssuedAtAnotherDate = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-07-20T10:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(25)).toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val thirdIsBooster = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-11-05T14:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 3
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 3
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(15)).toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val thirdIsBoosterIssuedAtAnotherDate = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-12-02T14:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 3
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 3
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
            every { headerIssuedAt } returns Instant.parse("2021-06-24T14:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns time.toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val fifthIssuedAtAnotherDate = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-07-24T10:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns time.toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val fifthIsBooster = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-06-24T14:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 3
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 3
            every { rawCertificate.vaccination.vaccinatedOn } returns time.plus(oneDayDuration).toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val fifthIsBoosterIssuedAtAnotherDate = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-07-24T10:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 3
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 3
            every { rawCertificate.vaccination.vaccinatedOn } returns time.plus(oneDayDuration).toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val sixth = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-06-24T10:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 1
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns time.toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }

        val sixthIssuedAtAnotherDate = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-06-24T14:00:00.000Z")
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

            // First shot 1/2, same shot that has two different certificates issued on different dates
            sixth,
            sixthIssuedAtAnotherDate,

            // Complete shot 2/2, same shot that has two different certificates issued on different dates  <= 14
            fifth,
            fifthIssuedAtAnotherDate,

            // Complete shot 3/3, same shot that has two different certificates issued on different dates  <= 14
            fifthIsBooster,
            fifthIsBoosterIssuedAtAnotherDate,

            fourth,

            // Complete shot 2/2, same shot that has two different certificates issued on different dates > 14
            third,
            thirdIssuedAtAnotherDate,

            // Booster shot 3/3, same shot that has two different certificates issued on different dates  > 14
            thirdIsBooster,
            thirdIsBoosterIssuedAtAnotherDate,

            second,
            first,
        )

        // Valid
        certificates.findHighestPriorityCertificate(time) shouldBe first
        certificates.remove(first)

        certificates.findHighestPriorityCertificate(time) shouldBe second
        certificates.remove(second)

        certificates.findHighestPriorityCertificate(time) shouldBe thirdIsBoosterIssuedAtAnotherDate
        certificates.remove(thirdIsBoosterIssuedAtAnotherDate)

        certificates.findHighestPriorityCertificate(time) shouldBe thirdIsBooster
        certificates.remove(thirdIsBooster)

        certificates.findHighestPriorityCertificate(time) shouldBe thirdIssuedAtAnotherDate
        certificates.remove(thirdIssuedAtAnotherDate)

        certificates.findHighestPriorityCertificate(time) shouldBe third
        certificates.remove(third)

        certificates.findHighestPriorityCertificate(time) shouldBe fourth
        certificates.remove(fourth)

        certificates.findHighestPriorityCertificate(time) shouldBe fifthIsBoosterIssuedAtAnotherDate
        certificates.remove(fifthIsBoosterIssuedAtAnotherDate)

        certificates.findHighestPriorityCertificate(time) shouldBe fifthIsBooster
        certificates.remove(fifthIsBooster)

        certificates.findHighestPriorityCertificate(time) shouldBe fifthIssuedAtAnotherDate
        certificates.remove(fifthIssuedAtAnotherDate)

        certificates.findHighestPriorityCertificate(time) shouldBe fifth
        certificates.remove(fifth)

        certificates.findHighestPriorityCertificate(time) shouldBe sixthIssuedAtAnotherDate
        certificates.remove(sixthIssuedAtAnotherDate)

        certificates.findHighestPriorityCertificate(time) shouldBe sixth
        certificates.remove(sixth)

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
            every { headerIssuedAt } returns Instant.parse("2021-01-20T14:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-01-01")
            every { getState() } returns mockk<State.Expired>()
        }

        val second = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-02-20T14:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-01-02")
            every { getState() } returns mockk<State.Expired>()
        }

        listOf(first, second).findHighestPriorityCertificate(time) shouldBe second
    }
}
