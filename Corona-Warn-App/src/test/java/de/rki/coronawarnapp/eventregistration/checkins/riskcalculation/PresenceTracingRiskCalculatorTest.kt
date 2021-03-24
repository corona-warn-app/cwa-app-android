package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PresenceTracingRiskCalculatorTest : BaseTest() {

    @MockK lateinit var presenceTracingRiskMapper: PresenceTracingRiskMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { presenceTracingRiskMapper.lookupTransmissionRiskValue(4) } returns 0.8
        coEvery { presenceTracingRiskMapper.lookupTransmissionRiskValue(6) } returns 1.2

        coEvery { presenceTracingRiskMapper.lookupRiskStatePerDay(15.0) } returns RiskState.LOW_RISK
        coEvery { presenceTracingRiskMapper.lookupRiskStatePerDay(46.0) } returns RiskState.INCREASED_RISK

        coEvery { presenceTracingRiskMapper.lookupRiskStatePerCheckIn(15.0) } returns RiskState.LOW_RISK
        coEvery { presenceTracingRiskMapper.lookupRiskStatePerCheckIn(31.0) } returns RiskState.INCREASED_RISK
    }

    @Test
    fun `calculateNormalizedTime works correctly`() {
        val overlap = CheckInOverlap(
            checkInId = 1L,
            transmissionRiskLevel = 4,
            traceWarningPackageId = 1L,
            startTime = Instant.parse("2021-03-15T05:00:00.000Z"),
            endTime = Instant.parse("2021-03-15T05:30:00.000Z")
        )

        val overlap2 = CheckInOverlap(
            checkInId = 2L,
            transmissionRiskLevel = 6,
            traceWarningPackageId = 1L,
            startTime = Instant.parse("2021-03-15T05:00:00.000Z"),
            endTime = Instant.parse("2021-03-15T05:45:00.000Z")
        )

        runBlockingTest {
            val result = createInstance().calculateNormalizedTime(listOf(overlap, overlap2))
            result.size shouldBe 2
            result.find { it.checkInId == 1L }!!.normalizedTime shouldBe (0.8 * 30)
            result.find { it.checkInId == 2L }!!.normalizedTime shouldBe (1.2 * 45)
        }
    }

    @Test
    fun `calculateRisk works correctly`() {
        val normTime = TraceLocationCheckInNormalizedTime(
            checkInId = 1L,
            localDate = Instant.parse("2021-03-15T05:00:00.000Z").toLocalDate(),
            normalizedTime = 31.0
        )

        val normTime2 = TraceLocationCheckInNormalizedTime(
            checkInId = 2L,
            localDate = Instant.parse("2021-03-15T05:00:00.000Z").toLocalDate(),
            normalizedTime = 15.0
        )
        runBlockingTest {
            val result = createInstance().calculateRisk(listOf(normTime, normTime2))
            result.size shouldBe 2
            result.find { it.checkInId == 1L }!!.riskState shouldBe RiskState.INCREASED_RISK
            result.find { it.checkInId == 2L }!!.riskState shouldBe RiskState.LOW_RISK
        }
    }

    @Test
    fun `calculateAggregatedRiskPerDay works correctly`() {
        val localDate = Instant.parse("2021-03-15T00:00:00.000Z").toLocalDate()
        val localDate2 = Instant.parse("2021-03-16T00:00:00.000Z").toLocalDate()

        val normTime = TraceLocationCheckInNormalizedTime(
            checkInId = 1L,
            localDate = localDate,
            normalizedTime = 31.0
        )

        val normTime2 = TraceLocationCheckInNormalizedTime(
            checkInId = 2L,
            localDate = localDate,
            normalizedTime = 15.0
        )

        val normTime3 = TraceLocationCheckInNormalizedTime(
            checkInId = 2L,
            localDate = localDate2,
            normalizedTime = 15.0
        )

        runBlockingTest {
            val result = createInstance().calculateAggregatedRiskPerDay(listOf(normTime, normTime2, normTime3))
            result.size shouldBe 2
            result.find { it.localDate == localDate }!!.riskState shouldBe RiskState.INCREASED_RISK
            result.find { it.localDate == localDate2 }!!.riskState shouldBe RiskState.LOW_RISK
        }
    }

    private fun createInstance() = PresenceTracingRiskCalculator(
        presenceTracingRiskMapper
    )
}
