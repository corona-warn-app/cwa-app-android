package de.rki.coronawarnapp.statistics

import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test

class StatisticsDataTest {

    @Test
    fun `infection mapping`() {
        InfectionStats(updatedAt = Instant.EPOCH, keyFigures = emptyList()).apply {
            cardType shouldBe StatsItem.Type.INFECTION
            cardType.id shouldBe 1
        }
    }

    @Test
    fun `incidence mapping`() {
        IncidenceStats(updatedAt = Instant.EPOCH, keyFigures = emptyList()).apply {
            cardType shouldBe StatsItem.Type.INCIDENCE
            cardType.id shouldBe 2
        }
    }

    @Test
    fun `keysubmission mapping`() {
        KeySubmissionsStats(updatedAt = Instant.EPOCH, keyFigures = emptyList()).apply {
            cardType shouldBe StatsItem.Type.KEYSUBMISSION
            cardType.id shouldBe 3
        }
    }

    @Test
    fun `7 day R value mapping`() {
        SevenDayRValue(updatedAt = Instant.EPOCH, keyFigures = emptyList()).apply {
            cardType shouldBe StatsItem.Type.SEVEN_DAY_RVALUE
            cardType.id shouldBe 4
        }
    }
}
