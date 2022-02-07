package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import io.kotest.matchers.shouldBe
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CclDateTimeTest : BaseTest() {

    @Test
    fun `example from tech spec`() {
        val dateTime = DateTime.parse("2021-12-30T10:00:00.897+01:00")
        val cclDateTime = CclDateTime(dateTime)
        cclDateTime.timestamp shouldBe dateTime.millis / 1000
        cclDateTime.localDate shouldBe "2021-12-30"
        cclDateTime.localDateTime shouldBe "2021-12-30T10:00:00+01:00"
        cclDateTime.localDateTimeMidnight shouldBe "2021-12-30T00:00:00+01:00"
        cclDateTime.utcDate shouldBe "2021-12-30"
        cclDateTime.utcDateTime shouldBe "2021-12-30T09:00:00Z"
        cclDateTime.utcDateTimeMidnight shouldBe "2021-12-30T00:00:00Z"
    }

    @Test
    fun `example with date change`() {
        val dateTime = DateTime.parse("2021-12-30T01:00:00.897+02:00")
        val cclDateTime = CclDateTime(dateTime)
        cclDateTime.timestamp shouldBe dateTime.millis / 1000
        cclDateTime.localDate shouldBe "2021-12-30"
        cclDateTime.localDateTime shouldBe "2021-12-30T01:00:00+02:00"
        cclDateTime.localDateTimeMidnight shouldBe "2021-12-30T00:00:00+02:00"
        cclDateTime.utcDate shouldBe "2021-12-29"
        cclDateTime.utcDateTime shouldBe "2021-12-29T23:00:00Z"
        cclDateTime.utcDateTimeMidnight shouldBe "2021-12-29T00:00:00Z"
    }
}
