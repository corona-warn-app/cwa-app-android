package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Instant
import org.joda.time.Seconds
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PersonCertificatesExtensionsTest : BaseTest() {

    private val time = Instant.parse("2021-06-24T14:00:00.000Z")

    @Test
    fun `certificate sort order`() {
        val certificateFirst = mockk<CwaCovidCertificate>().apply {
            every { issuedAt } returns time.minus(Seconds.ONE.toStandardDuration())
        }

        val certificateSecond = mockk<CwaCovidCertificate>().apply {
            every { issuedAt } returns time
        }

        val certificateThird = mockk<CwaCovidCertificate>().apply {
            every { issuedAt } returns time.plus(Seconds.ONE.toStandardDuration())
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
}
