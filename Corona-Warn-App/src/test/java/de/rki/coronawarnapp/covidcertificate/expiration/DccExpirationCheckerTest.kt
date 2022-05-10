package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.test.TestData
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class DccExpirationCheckerTest : BaseTest() {
    @Inject lateinit var extractor: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    private fun createInstance() = DccExpirationChecker()

    @Test
    fun `getExpirationState works`() = runTest {
        // certificate expires at 2021-06-03T10:12:48.000Z
        val instance = createInstance()
        val exp = Instant.parse("2021-06-03T10:12:48.000Z")
        val dccData = extractor.extract(TestData.qrCodeTestCertificate).data

        instance.getExpirationState(
            dccData = dccData,
            expirationThreshold = Duration.standardDays(10),
            now = Instant.parse("2021-06-03T10:12:48.000+02:00"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe CwaCovidCertificate.State.ExpiringSoon(exp)

        instance.getExpirationState(
            dccData = dccData,
            expirationThreshold = Duration.standardDays(10),
            now = Instant.parse("2021-06-04T00:12:48.000+02:00"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe CwaCovidCertificate.State.Expired(exp)

        instance.getExpirationState(
            dccData = dccData,
            expirationThreshold = Duration.standardDays(10),
            now = Instant.parse("2021-05-24T10:12:48.000Z"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe CwaCovidCertificate.State.ExpiringSoon(exp)

        instance.getExpirationState(
            dccData = dccData,
            expirationThreshold = Duration.standardDays(10),
            now = Instant.parse("2021-05-23T23:59:59.000+02:00"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe CwaCovidCertificate.State.Valid(exp)

        instance.getExpirationState(
            dccData = dccData,
            expirationThreshold = Duration.standardDays(10),
            now = Instant.parse("2021-05-03T10:12:48.000Z"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe CwaCovidCertificate.State.Valid(exp)
    }
}
