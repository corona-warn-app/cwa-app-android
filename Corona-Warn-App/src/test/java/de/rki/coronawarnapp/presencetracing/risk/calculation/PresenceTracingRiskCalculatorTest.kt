package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.util.toLocalDateUtc
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

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

        coEvery { presenceTracingRiskMapper.lookupRiskStatePerCheckIn(10000.0) } returns RiskState.CALCULATION_FAILED
        coEvery { presenceTracingRiskMapper.lookupRiskStatePerDay(10000.0) } returns RiskState.CALCULATION_FAILED
    }

    @Test
    fun `calculateNormalizedTime works correctly`() {
        val overlap = CheckInWarningOverlap(
            checkInId = 1L,
            transmissionRiskLevel = 4,
            traceWarningPackageId = "id",
            startTime = Instant.parse("2021-03-15T05:00:00.000Z"),
            endTime = Instant.parse("2021-03-15T05:30:00.000Z")
        )

        val overlap2 = CheckInWarningOverlap(
            checkInId = 2L,
            transmissionRiskLevel = 6,
            traceWarningPackageId = "id",
            startTime = Instant.parse("2021-03-15T05:00:00.000Z"),
            endTime = Instant.parse("2021-03-15T05:45:00.000Z")
        )

        runTest {
            val result = createInstance().calculateNormalizedTime(listOf(overlap, overlap2))
            result.size shouldBe 2
            result.find { it.checkInId == 1L }!!.normalizedTime shouldBe (0.8 * 30)
            result.find { it.checkInId == 2L }!!.normalizedTime shouldBe (1.2 * 45)
        }
    }

    @Test
    fun `calculateCheckInRiskPerDay works correctly`() {
        val normTime = CheckInNormalizedTime(
            checkInId = 1L,
            localDateUtc = Instant.parse("2021-03-15T05:00:00.000Z").toLocalDateUtc(),
            normalizedTime = 31.0
        )

        val normTime2 = CheckInNormalizedTime(
            checkInId = 2L,
            localDateUtc = Instant.parse("2021-03-15T05:00:00.000Z").toLocalDateUtc(),
            normalizedTime = 15.0
        )
        runTest {
            val result = createInstance().calculateCheckInRiskPerDay(listOf(normTime, normTime2))
            result.size shouldBe 2
            result.find { it.checkInId == 1L }!!.riskState shouldBe RiskState.INCREASED_RISK
            result.find { it.checkInId == 2L }!!.riskState shouldBe RiskState.LOW_RISK
        }
    }

    @Test
    fun `calculateCheckInRiskPerDay returns calculation failed for value out of range`() {
        val normTime = CheckInNormalizedTime(
            checkInId = 1L,
            localDateUtc = Instant.parse("2021-03-15T05:00:00.000Z").toLocalDateUtc(),
            normalizedTime = 10000.0
        )

        runTest {
            val result = createInstance().calculateCheckInRiskPerDay(listOf(normTime))
            result.size shouldBe 1
            result.find { it.checkInId == 1L }!!.riskState shouldBe RiskState.CALCULATION_FAILED
        }
    }

    @Test
    fun `calculateAggregatedRiskPerDay returns calculation failed for value out of range`() {
        val normTime = CheckInNormalizedTime(
            checkInId = 1L,
            localDateUtc = Instant.parse("2021-03-15T05:00:00.000Z").toLocalDateUtc(),
            normalizedTime = 10000.0
        )

        runTest {
            val result = createInstance().calculateDayRisk(listOf(normTime))
            result.size shouldBe 1
            result[0].riskState shouldBe RiskState.CALCULATION_FAILED
        }
    }

    @Test
    fun `calculateAggregatedRiskPerDay works correctly`() {
        val localDate = Instant.parse("2021-03-15T00:00:00.000Z").toLocalDateUtc()
        val localDate2 = Instant.parse("2021-03-16T00:00:00.000Z").toLocalDateUtc()

        val normTime = CheckInNormalizedTime(
            checkInId = 1L,
            localDateUtc = localDate,
            normalizedTime = 20.0
        )

        val normTime2 = CheckInNormalizedTime(
            checkInId = 2L,
            localDateUtc = localDate,
            normalizedTime = 26.0
        )

        val normTime3 = CheckInNormalizedTime(
            checkInId = 3L,
            localDateUtc = localDate2,
            normalizedTime = 15.0
        )

        runTest {
            val result = createInstance().calculateDayRisk(listOf(normTime, normTime2, normTime3))
            result.size shouldBe 2
            result.find { it.localDateUtc == localDate }!!.riskState shouldBe RiskState.INCREASED_RISK
            result.find { it.localDateUtc == localDate2 }!!.riskState shouldBe RiskState.LOW_RISK
        }
    }

    private fun createInstance() = PresenceTracingRiskCalculator(
        presenceTracingRiskMapper
    )
}
