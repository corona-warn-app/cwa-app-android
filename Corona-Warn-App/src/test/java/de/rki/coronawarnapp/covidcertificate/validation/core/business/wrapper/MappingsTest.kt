package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import dgca.verifier.app.engine.UTC_ZONE_ID
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.ZonedDateTime

class MappingsTest : BaseTest() {

    @Test
    fun `Instant toZonedDateTime works`() {
        val original = Instant.parse("2021-05-27T07:46:40.000Z")
        val zoned = original.toZonedDateTime()
        zoned shouldBe ZonedDateTime.of(
            2021,
            5,
            27,
            7,
            46,
            40,
            0,
            UTC_ZONE_ID
        )
        zoned.toInstant().toEpochMilli() shouldBe original.millis
    }

    @Test
    fun ` String toZonedDateTime and reverse works`() {
        val original = "2021-05-27T07:46:40Z"
        val zoned = original.toZonedDateTime()
        zoned shouldBe ZonedDateTime.of(
            2021,
            5,
            27,
            7,
            46,
            40,
            0,
            UTC_ZONE_ID
        )
        zoned.asExternalString shouldBe original
    }
}
