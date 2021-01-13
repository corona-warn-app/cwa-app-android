package de.rki.coronawarnapp.util.database

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File
import java.util.UUID

class CommonConvertersTest : BaseTest() {
    private val converters = CommonConverters()

    @Test
    fun `int list conversion`() {
        converters.apply {
            val orig = listOf(1, 2, 3)
            val raw = "[1,2,3]"
            fromIntList(orig) shouldBe raw
            toIntList(raw) shouldBe orig
        }
    }

    @Test
    fun `UUID conversion`() {
        converters.apply {
            val orig = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
            val raw = "123e4567-e89b-12d3-a456-426614174000"
            fromUUID(orig) shouldBe raw
            toUUID(raw) shouldBe orig
        }
    }

    @Test
    fun `path conversion`() {
        converters.apply {
            val orig = File("/row/row/row/your/boat")
            val raw = "/row/row/row/your/boat"
            fromPath(orig) shouldBe raw
            toPath(raw) shouldBe orig
        }
    }

    @Test
    fun `local date conversion`() {
        converters.apply {
            val orig = LocalDate.parse("2222-12-31")
            val raw = "2222-12-31"
            fromLocalDate(orig) shouldBe raw
            toLocalDate(raw) shouldBe orig
        }
    }

    @Test
    fun `local time conversion`() {
        converters.apply {
            val orig = LocalTime.parse("23:59")
            val raw = "23:59:00.000"
            fromLocalTime(orig) shouldBe raw
            toLocalTime(raw) shouldBe orig
        }
    }

    @Test
    fun `instant conversion`() {
        converters.apply {
            val orig = Instant.EPOCH
            val raw = "1970-01-01T00:00:00.000Z"
            fromInstant(orig) shouldBe raw
            toInstant(raw) shouldBe orig
        }
    }

    @Test
    fun `LocationCode conversion`() {
        converters.apply {
            val orig = LocationCode("DE")
            val raw = "DE"
            fromLocationCode(orig) shouldBe raw
            toLocationCode(raw) shouldBe orig
        }
    }
}
