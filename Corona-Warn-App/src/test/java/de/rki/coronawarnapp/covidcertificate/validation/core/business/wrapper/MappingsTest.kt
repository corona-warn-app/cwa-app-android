package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDateTime
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.RuleCertificateType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class MappingsTest : BaseTest() {

    @Test
    fun `Instant toZonedDateTime works`() {
        val original = Instant.parse("2021-05-27T07:46:40.000Z")
        val zoned = original.toZonedDateTime(UTC_ZONE_ID)
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
        zoned.toInstant().toEpochMilli() shouldBe original.toEpochMilli()
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
        val validationDate = OffsetDateTime.ofInstant(validationClock, ZoneOffset.UTC)

        val vacA1 = createDccRule(
            certificateType = RuleCertificateType.VACCINATION,
            identifier = "VR-DE-1",
            version = "1.0.0",
            validFrom = validationClock.minusMillis(100).toString(),
            validTo = validationClock.plusMillis(100).toString(),
        ) // Has newer version
        val vacA2 = createDccRule(
            certificateType = RuleCertificateType.VACCINATION,
            identifier = "VR-DE-1",
            version = "1.0.1",
            validFrom = validationClock.minusMillis(100).toString(),
            validTo = validationClock.plusMillis(100).toString(),
        ) // :)
        val vacA3 = createDccRule(
            certificateType = RuleCertificateType.VACCINATION,
            identifier = "VR-DE-1",
            version = "1.0.2",
            validFrom = validationClock.minusMillis(100).toString(),
            validTo = validationClock.plusMillis(100).toString(),
            country = "NL"
        ) // Wrong arrival country
        val vacB1 = createDccRule(
            certificateType = RuleCertificateType.TEST,
            identifier = "TR-DE-2",
            version = "1.0.0",
            validFrom = validationClock.minusMillis(100).toString(),
            validTo = validationClock.plusMillis(100).toString(),
        ) // Wrong type
        val genA1 = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            identifier = "GR-DE-1",
            version = "1.0.0",
            validFrom = validationClock.minusMillis(100).toString(),
            validTo = validationClock.plusMillis(100).toString(),
        ) // Has newer version
        val genA2 = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            identifier = "GR-DE-1",
            version = "1.0.1",
            validFrom = validationClock.minusMillis(100).toString(),
            validTo = validationClock.plusMillis(100).toString(),
        ) // :)
        val genA3 = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            identifier = "GR-DE-1",
            version = "2.0.1",
            validFrom = validationClock.plusMillis(1).toString(),
            validTo = validationClock.plusMillis(100).toString(),
        ) // validFrom is in the future
        val genA4 = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            identifier = "GR-DE-1",
            version = "2.0.1",
            validFrom = validationClock.plusMillis(1).toString(),
            validTo = validationClock.minusMillis(1).toString(),
        ) // validTo is in the past

        val genA5 = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            identifier = "GR-DE-5",
            version = "1.0.1",
            validFrom = validationClock.toString(),
            validTo = validationClock.toString(),
        ) // validFrom and validTo is equal to validationClock

        val rules = listOf(
            vacA1, vacA2, vacA3, vacB1, genA1, genA2, genA3, genA4, genA5
        )

        rules.filterRelevantRules(
            validationDateTime = validationDate,
            certificateType = VACCINATION,
            country = DccCountry("de")
        ) shouldBe listOf(vacA2, genA2, genA5)
    }
}
