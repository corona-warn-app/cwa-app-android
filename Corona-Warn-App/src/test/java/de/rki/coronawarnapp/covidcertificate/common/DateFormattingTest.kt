package de.rki.coronawarnapp.covidcertificate.common

import de.rki.coronawarnapp.covidcertificate.common.certificate.formatDate
import de.rki.coronawarnapp.covidcertificate.common.certificate.formatDateTime
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.ZoneOffset

class DateFormattingTest : BaseTest() {

    @Test
    fun `format date of birth`() {
        "1978-01-26T00:00:00".formatDate() shouldBe "1978-01-26"
        "1964-08-12".formatDate() shouldBe "1964-08-12"
        "1964-08".formatDate() shouldBe "1964-08"
        "1964".formatDate() shouldBe "1964"
        "".formatDate() shouldBe ""
        "lorem-ipsum".formatDate() shouldBe "lorem-ipsum"
    }

    @Test
    fun `format date`() {
        "1964-08-12".formatDate() shouldBe "1964-08-12"
        "1978-01-26T00:00:00".formatDate() shouldBe "1978-01-26"
        "2021-03-18T15:31:00+02:00".formatDate() shouldBe "2021-03-18"
        "lorem-ipsum".formatDate() shouldBe "lorem-ipsum"
    }

    @Test
    fun `format date time`() {
        val tz = ZoneOffset.ofHours(timeZoneOffsetBerlin)
        "2021-08-20T10:03:12Z".formatDateTime(tz) shouldBe "2021-08-20 12:03 UTC +02"
        "2021-08-20T12:03:12+02".formatDateTime(tz) shouldBe "2021-08-20 12:03 UTC +02"
        "2021-08-20T12:03:12+0200".formatDateTime(tz) shouldBe "2021-08-20 12:03 UTC +02"
        "2021-08-20T12:03:12+02:00".formatDateTime(tz) shouldBe "2021-08-20 12:03 UTC +02"
        "2021-08-20T09:03:12Z".formatDateTime(
            ZoneOffset.ofHours(timeZoneOffsetHawaii)
        ) shouldBe "2021-08-19 23:03 UTC -10"
        "lorem-ipsum".formatDateTime() shouldBe "lorem-ipsum"
    }

    private val timeZoneOffsetBerlin = 2 // Berlin
    private val timeZoneOffsetHawaii = -10 // Hawaii
}
