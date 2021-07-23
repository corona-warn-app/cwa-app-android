package de.rki.coronawarnapp.covidcertificate.signature.core

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.test.TestData
import io.kotest.matchers.shouldBe
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class DccStateCheckerTest : BaseTest() {

    @Inject lateinit var extractor: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    @Test
    fun `getExpirationState works`() {
        // certificate expires at 2021-06-03T10:12:48.000Z
        val exp = Instant.parse("2021-06-03T10:12:48.000Z")
        val dccData = extractor.extract(TestData.qrCodeTestCertificate).data
        dccData.getExpirationState(
            expirationThresholdInDays = 10,
            now = Instant.parse("2021-06-03T10:12:48.000+02:00"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe CwaCovidCertificate.State.Expired(exp)

        dccData.getExpirationState(
            expirationThresholdInDays = 10,
            now = Instant.parse("2021-06-04T00:12:48.000+02:00"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe CwaCovidCertificate.State.Expired(exp)

        dccData.getExpirationState(
            expirationThresholdInDays = 10,
            now = Instant.parse("2021-05-24T10:12:48.000Z"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe CwaCovidCertificate.State.ExpiringSoon(exp)

        dccData.getExpirationState(
            expirationThresholdInDays = 10,
            now = Instant.parse("2021-05-23T23:59:59.000+02:00"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe CwaCovidCertificate.State.Valid(exp)

        dccData.getExpirationState(
            expirationThresholdInDays = 10,
            now = Instant.parse("2021-05-03T10:12:48.000Z"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe CwaCovidCertificate.State.Valid(exp)
    }
}
