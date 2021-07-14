package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalTimeUtc
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.RuleCertificateType
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
        val validationDate = validationClock.toLocalDateUtc()
        val validationTime = validationClock.toLocalTimeUtc()

        val vacA1 = createDccRule(
            certificateType = RuleCertificateType.VACCINATION,
            identifier = "VR-DE-1",
            version = "1.0.0",
            validFrom = validationClock.minus(100).toString(),
            validTo = validationClock.plus(100).toString(),
        ) // Has newer version
        val vacA2 = createDccRule(
            certificateType = RuleCertificateType.VACCINATION,
            identifier = "VR-DE-1",
            version = "1.0.1",
            validFrom = validationClock.minus(100).toString(),
            validTo = validationClock.plus(100).toString(),
        ) // :)
        val vacA3 = createDccRule(
            certificateType = RuleCertificateType.VACCINATION,
            identifier = "VR-DE-1",
            version = "1.0.2",
            validFrom = validationClock.minus(100).toString(),
            validTo = validationClock.plus(100).toString(),
            country = "NL"
        ) // Wrong arrival country
        val vacB1 = createDccRule(
            certificateType = RuleCertificateType.TEST,
            identifier = "TR-DE-2",
            version = "1.0.0",
            validFrom = validationClock.minus(100).toString(),
            validTo = validationClock.plus(100).toString(),
        ) // Wrong type
        val genA1 = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            identifier = "GR-DE-1",
            version = "1.0.0",
            validFrom = validationClock.minus(100).toString(),
            validTo = validationClock.plus(100).toString(),
        ) // Has newer version
        val genA2 = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            identifier = "GR-DE-1",
            version = "1.0.1",
            validFrom = validationClock.minus(100).toString(),
            validTo = validationClock.plus(100).toString(),
        ) // :)
        val genA3 = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            identifier = "GR-DE-1",
            version = "2.0.1",
            validFrom = validationClock.plus(1).toString(),
            validTo = validationClock.plus(100).toString(),
        ) // validFrom is in the future
        val genA4 = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            identifier = "GR-DE-1",
            version = "2.0.1",
            validFrom = validationClock.plus(1).toString(),
            validTo = validationClock.minus(1).toString(),
        ) // validTo is in the past

        val rules = listOf(
            vacA1, vacA2, vacA3, vacB1, genA1, genA2, genA3, genA4
        )

        rules.filterRelevantRules(
            validationTime = validationTime,
            validationDate = validationDate,
            certificateType = VACCINATION,
            country = DccCountry("de")
        ) shouldBe listOf(vacA2, genA2)
    }
}
