package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import io.kotest.matchers.shouldBe
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber

class AggregatedRiskPerDateResultTest: BaseTest() {

    @Test
    fun `day is correct`() {
        val riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW
        val minimumDistinctEncountersWithLowRisk = 0
        val minimumDistinctEncountersWithHighRisk = 0

        val oneDay = Days.ONE.toStandardDuration()
        val todayInstant = Instant.now()
        val todayLocalDate = LocalDate.now()
        val yesterdayInstant = Instant.now().minus(oneDay)
        val yesterdayLocalDate = LocalDate.now().minusDays(1)
        val tomorrowInstant = Instant.now().plus(oneDay)
        val tomorrowLocalDate = LocalDate.now().plusDays(1)

        Timber.i("Today as LocalDate $todayLocalDate and as Instant $todayInstant")
        Timber.i("Yesterday as LocalDate $yesterdayLocalDate and as Instant $yesterdayInstant")
        Timber.i("Tomorrow as LocalDate $tomorrowLocalDate and as Instant $tomorrowInstant")

        val todayAggregatedRiskPerDateResult = AggregatedRiskPerDateResult(
            dateMillisSinceEpoch = todayInstant.millis,
            riskLevel = riskLevel,
            minimumDistinctEncountersWithLowRisk = minimumDistinctEncountersWithLowRisk,
            minimumDistinctEncountersWithHighRisk = minimumDistinctEncountersWithHighRisk
        )

        val yesterdayAggregatedRiskPerDateResult = AggregatedRiskPerDateResult(
            dateMillisSinceEpoch = yesterdayInstant.millis,
            riskLevel = riskLevel,
            minimumDistinctEncountersWithLowRisk = minimumDistinctEncountersWithLowRisk,
            minimumDistinctEncountersWithHighRisk = minimumDistinctEncountersWithHighRisk
        )

        val tomorrowAggregatedRiskPerDateResult = AggregatedRiskPerDateResult(
            dateMillisSinceEpoch = tomorrowInstant.millis,
            riskLevel = riskLevel,
            minimumDistinctEncountersWithLowRisk = minimumDistinctEncountersWithLowRisk,
            minimumDistinctEncountersWithHighRisk = minimumDistinctEncountersWithHighRisk
        )

        todayAggregatedRiskPerDateResult.day shouldBe todayLocalDate
        yesterdayAggregatedRiskPerDateResult.day shouldBe yesterdayLocalDate
        tomorrowAggregatedRiskPerDateResult.day shouldBe tomorrowLocalDate
    }
}
