package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PersonCertificatesExtensionsTest : BaseTest() {

    private val time = Instant.now()
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
            every { testedPositiveOn } returns time.minus(oneDayDuration).toLocalDateUtc()
        }

        val certificateWithoutDate = mockk<TestCertificate>().apply {
            every { sampleCollectedAt } returns null
        }

        val expectedOrder =
            listOf(certificateFirst, certificateFirstIssuedAtAnotherDate, certificateSecond, certificateThird)
        val expectedOrderWithoutDate = listOf(certificateSecond, certificateWithoutDate)

        val wrongOrder =
            listOf(certificateSecond, certificateFirst, certificateThird, certificateFirstIssuedAtAnotherDate)
        val wrongOrder2 =
            listOf(certificateThird, certificateSecond, certificateFirstIssuedAtAnotherDate, certificateFirst)
        val wrongOrderWithoutDate = listOf(
            certificateWithoutDate, certificateSecond
        )

        expectedOrder.toCertificateSortOrder() shouldBe expectedOrder
        wrongOrder.toCertificateSortOrder() shouldBe expectedOrder
        wrongOrder2.toCertificateSortOrder() shouldBe expectedOrder
        wrongOrderWithoutDate.toCertificateSortOrder() shouldBe expectedOrderWithoutDate

        wrongOrder shouldNotBe expectedOrder
        wrongOrder2 shouldNotBe expectedOrder
    }

    @Test
    fun `certificate sort order - crash EXPOSUREAPP-9880`() {
        val vc1 = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-09-20T10:00:00.000Z")
            every { vaccinatedOn } returns Instant.parse("2021-06-24T14:00:00.000Z").toLocalDateUtc()
        }

        val vc2 = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-09-20T10:00:00.000Z")
            every { vaccinatedOn } returns Instant.parse("2021-04-25T14:00:00.000Z").toLocalDateUtc()
        }

        val rc1 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns Instant.parse("2021-04-25T14:00:00.000Z").toLocalDateUtc()
        }

        val tc2 = mockk<TestCertificate>().apply {
            every { sampleCollectedAt } returns Instant.parse("2021-08-23T14:00:00.000Z")
        }

        shouldNotThrowAny {
            listOf(vc1, vc2, rc1, tc2).toCertificateSortOrder()
        }
    }
}
