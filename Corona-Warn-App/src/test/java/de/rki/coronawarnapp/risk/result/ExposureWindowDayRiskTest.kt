package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

class ExposureWindowDayRiskTest : BaseTest() {

    @Test
    fun `day is correct`() {
        val oneDay = Duration.ofDays(1)
        val todayInstant = Instant.parse("2020-08-20T23:00:00.000Z")
        val todayLocalDate = LocalDate.parse("2020-08-20")
        val yesterdayInstant = todayInstant.minus(oneDay)
        val yesterdayLocalDate = todayLocalDate.minusDays(1)
        val tomorrowInstant = todayInstant.plus(oneDay)
        val tomorrowLocalDate = todayLocalDate.plusDays(1)

        Timber.i("Today as LocalDate $todayLocalDate and as Instant $todayInstant")
        Timber.i("Yesterday as LocalDate $yesterdayLocalDate and as Instant $yesterdayInstant")
        Timber.i("Tomorrow as LocalDate $tomorrowLocalDate and as Instant $tomorrowInstant")

        val todayAggregatedRiskPerDateResult = createAggregatedRiskPerDateResult(todayInstant)
        val yesterdayAggregatedRiskPerDateResult = createAggregatedRiskPerDateResult(yesterdayInstant)
        val tomorrowAggregatedRiskPerDateResult = createAggregatedRiskPerDateResult(tomorrowInstant)

        todayAggregatedRiskPerDateResult.localDateUtc shouldBe todayLocalDate
        yesterdayAggregatedRiskPerDateResult.localDateUtc shouldBe yesterdayLocalDate
        tomorrowAggregatedRiskPerDateResult.localDateUtc shouldBe tomorrowLocalDate
    }

    private fun createAggregatedRiskPerDateResult(date: Instant) = ExposureWindowDayRisk(
        dateMillisSinceEpoch = date.toEpochMilli(),
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 0,
        minimumDistinctEncountersWithHighRisk = 0
    )
}
