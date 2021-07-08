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
    fun `String toZonedDateTime and reverse works`() {
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

    @Test
    fun `filter rules works`() {
        val validationClock = Instant.parse("2021-05-27T07:46:40Z")
        val rules = listOf(
            createVaccinationRule("2021-01-01T07:46:40Z", "2021-05-01T07:46:40Z"),
            createVaccinationRule("2021-05-27T07:46:40Z", "2022-08-01T07:46:40Z"),
            createVaccinationRule("2021-05-01T07:46:40Z", "2021-05-27T07:46:40Z"),
            createVaccinationRule("2021-05-01T07:46:40Z", "2021-05-30T07:46:40Z"),
            createVaccinationRule("2021-05-28T07:46:40Z", "2022-08-01T07:46:40Z"),
            createGeneralRule("2021-05-01T07:46:40Z", "2021-05-30T07:46:40Z"),
        )

        rules.filterRelevantRules(
            validationClock,
            VACCINATION
        ).size shouldBe 4
    }

    @Test
    fun `highest version works`() {
        val rules = listOf(
            createVaccinationRule("2021-01-01T07:46:40Z", "2021-05-01T07:46:40Z", "0.1.21"),
            createVaccinationRule("2021-05-27T07:46:40Z", "2022-08-01T07:46:40Z", "12.1.21"),
            createVaccinationRule("2021-05-27T07:46:40Z", "2022-08-01T07:46:40Z", "12.1.21.1"),
            createVaccinationRule("2021-05-01T07:46:40Z", "2021-05-27T07:46:40Z", "2.5.33"),
            createVaccinationRule("2021-05-01T07:46:40Z", "2021-05-30T07:46:40Z", "1.0.21"),
            createVaccinationRule("2021-05-28T07:46:40Z", "2022-08-01T07:46:40Z", "1.1.111"),
            createGeneralRule("2021-05-01T07:46:40Z", "2021-05-30T07:46:40Z"),
        )
        rules.takeHighestVersion()!!.version shouldBe "12.1.21.1"
    }
}
