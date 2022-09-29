package de.rki.coronawarnapp.risk.storage.internal

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.minusDaysAtStartOfDayUtc
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.LocalDate

class RiskCombinatorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(1234567890)
    }

    private fun createInstance() = RiskCombinator(
        timeStamper = timeStamper
    )

    @Test
    fun `Initial results`() {
        createInstance().initialCombinedResult.apply {
            riskState shouldBe LOW_RISK
        }
    }

    @Test
    fun `Fallback results on empty data`() {
        createInstance().latestCombinedResult.apply {
            riskState shouldBe LOW_RISK
        }
    }

    @Test
    fun `combineRisk works`() {
        val ptRisk0 = PresenceTracingDayRisk(
            localDateUtc = LocalDate.of(2021, 3, 19),
            riskState = LOW_RISK
        )
        val ptRisk1 = PresenceTracingDayRisk(
            localDateUtc = LocalDate.of(2021, 3, 20),
            riskState = INCREASED_RISK
        )
        val ptRisk2 = PresenceTracingDayRisk(
            localDateUtc = LocalDate.of(2021, 3, 21),
            riskState = LOW_RISK
        )
        val ptRisk3 = PresenceTracingDayRisk(
            localDateUtc = LocalDate.of(2021, 3, 22),
            riskState = CALCULATION_FAILED
        )
        val ptRisk4 = PresenceTracingDayRisk(
            localDateUtc = LocalDate.of(2021, 3, 23),
            riskState = LOW_RISK
        )
        val ptRisk5 = PresenceTracingDayRisk(
            localDateUtc = LocalDate.of(2021, 3, 24),
            riskState = INCREASED_RISK
        )

        val ewRisk0 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = Instant.parse("2021-03-24T14:00:00.000Z").toEpochMilli(),
            riskLevel = RiskLevel.HIGH,
            0,
            0
        )
        val ewRisk1 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = Instant.parse("2021-03-23T14:00:00.000Z").toEpochMilli(),
            riskLevel = RiskLevel.HIGH,
            0,
            0
        )
        val ewRisk2 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = Instant.parse("2021-03-22T14:00:00.000Z").toEpochMilli(),
            riskLevel = RiskLevel.HIGH,
            0,
            0
        )
        val ewRisk3 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = Instant.parse("2021-03-19T14:00:00.000Z").toEpochMilli(),
            riskLevel = RiskLevel.LOW,
            0,
            0
        )
        val ewRisk4 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = Instant.parse("2021-03-20T14:00:00.000Z").toEpochMilli(),
            riskLevel = RiskLevel.UNSPECIFIED,
            0,
            0
        )
        val ewRisk5 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = Instant.parse("2021-03-15T14:00:00.000Z").toEpochMilli(),
            riskLevel = RiskLevel.UNSPECIFIED,
            0,
            0
        )

        val ptDayRiskList: List<PresenceTracingDayRisk> = listOf(ptRisk0, ptRisk1, ptRisk2, ptRisk3, ptRisk4, ptRisk5)
        val ewDayRiskList: List<ExposureWindowDayRisk> = listOf(ewRisk0, ewRisk1, ewRisk2, ewRisk3, ewRisk4, ewRisk5)
        val result = createInstance().combineRisk(ptDayRiskList, ewDayRiskList)
        result.size shouldBe 7

        result.single {
            it.localDate == LocalDate.of(2021, 3, 15)
        }.riskState shouldBe CALCULATION_FAILED
        result.single {
            it.localDate == LocalDate.of(2021, 3, 19)
        }.riskState shouldBe LOW_RISK
        result.single {
            it.localDate == LocalDate.of(2021, 3, 20)
        }.riskState shouldBe CALCULATION_FAILED
        result.single {
            it.localDate == LocalDate.of(2021, 3, 21)
        }.riskState shouldBe LOW_RISK
        result.single {
            it.localDate == LocalDate.of(2021, 3, 22)
        }.riskState shouldBe CALCULATION_FAILED
        result.single {
            it.localDate == LocalDate.of(2021, 3, 22)
        }.riskState shouldBe CALCULATION_FAILED
        result.single {
            it.localDate == LocalDate.of(2021, 3, 23)
        }.riskState shouldBe INCREASED_RISK
        result.single {
            it.localDate == LocalDate.of(2021, 3, 24)
        }.riskState shouldBe INCREASED_RISK
    }

    @Test
    fun `combineEwPtRiskLevelResults works`() {
        val maxCheckInAge = 10
        val startInstant = Instant.ofEpochMilli(10000)

        val ptResult = PtRiskLevelResult(
            calculatedAt = startInstant.plusMillis(1000L),
            riskState = LOW_RISK,
            calculatedFrom = startInstant.plusMillis(1000L).minusDaysAtStartOfDayUtc(maxCheckInAge)
                .toInstant()
        )
        val ptResult2 = PtRiskLevelResult(
            calculatedAt = startInstant.plusMillis(3000L),
            riskState = LOW_RISK,
            calculatedFrom = startInstant.plusMillis(3000L).minusDaysAtStartOfDayUtc(maxCheckInAge)
                .toInstant()
        )
        val ptResult3 = PtRiskLevelResult(
            calculatedAt = startInstant.plusMillis(6000L),
            riskState = CALCULATION_FAILED,
            calculatedFrom = startInstant.plusMillis(6000L).minusDaysAtStartOfDayUtc(maxCheckInAge)
                .toInstant()
        )
        val ptResult4 = PtRiskLevelResult(
            calculatedAt = startInstant.plusMillis(7000L),
            riskState = CALCULATION_FAILED,
            calculatedFrom = startInstant.plusMillis(7000L).minusDaysAtStartOfDayUtc(maxCheckInAge)
                .toInstant()
        )

        val ptResults = listOf(ptResult, ptResult2, ptResult4, ptResult3)
        val ewResult = createEwRiskLevelResult(
            calculatedAt = startInstant.plusMillis(2000L),
            riskState = LOW_RISK
        )
        val ewResult2 = createEwRiskLevelResult(
            calculatedAt = startInstant.plusMillis(4000L),
            riskState = INCREASED_RISK
        )
        val ewResult3 = createEwRiskLevelResult(
            calculatedAt = startInstant.plusMillis(5000L),
            riskState = CALCULATION_FAILED
        )
        val ewResult4 = createEwRiskLevelResult(
            calculatedAt = startInstant.plusMillis(8000L),
            riskState = CALCULATION_FAILED
        )
        val ewResults = listOf(ewResult, ewResult4, ewResult2, ewResult3)
        val result = createInstance().combineEwPtRiskLevelResults(
            ptRiskResults = ptResults,
            ewRiskResults = ewResults
        ).sortedByDescending { it.calculatedAt }

        result.size shouldBe 8
        result[0].apply {
            riskState shouldBe CALCULATION_FAILED
            calculatedAt shouldBe startInstant.plusMillis(8000L)
        }

        result[1].apply {
            riskState shouldBe CALCULATION_FAILED
            calculatedAt shouldBe startInstant.plusMillis(5000L)
        }

        result[2].apply {
            riskState shouldBe CALCULATION_FAILED
            calculatedAt shouldBe startInstant.plusMillis(5000L)
        }

        result[3].apply {
            riskState shouldBe CALCULATION_FAILED
            calculatedAt shouldBe startInstant.plusMillis(5000L)
        }

        result[4].apply {
            riskState shouldBe INCREASED_RISK
            calculatedAt shouldBe startInstant.plusMillis(4000L)
        }

        result[5].apply {
            riskState shouldBe LOW_RISK
            calculatedAt shouldBe startInstant.plusMillis(3000L)
        }

        result[6].apply {
            riskState shouldBe LOW_RISK
            calculatedAt shouldBe startInstant.plusMillis(2000L)
        }

        result[7].apply {
            riskState shouldBe LOW_RISK
            calculatedAt shouldBe startInstant.plusMillis(1000L)
        }
    }

    @Test
    fun `combine RiskState works`() {
        RiskCombinator.combine(INCREASED_RISK, INCREASED_RISK) shouldBe INCREASED_RISK
        RiskCombinator.combine(INCREASED_RISK, LOW_RISK) shouldBe INCREASED_RISK
        RiskCombinator.combine(INCREASED_RISK, CALCULATION_FAILED) shouldBe CALCULATION_FAILED
        RiskCombinator.combine(LOW_RISK, INCREASED_RISK) shouldBe INCREASED_RISK
        RiskCombinator.combine(CALCULATION_FAILED, INCREASED_RISK) shouldBe CALCULATION_FAILED
        RiskCombinator.combine(LOW_RISK, LOW_RISK) shouldBe LOW_RISK
        RiskCombinator.combine(CALCULATION_FAILED, LOW_RISK) shouldBe CALCULATION_FAILED
        RiskCombinator.combine(CALCULATION_FAILED, CALCULATION_FAILED) shouldBe CALCULATION_FAILED
    }
}

private fun createEwRiskLevelResult(
    calculatedAt: Instant,
    riskState: RiskState
): EwRiskLevelResult = object : EwRiskLevelResult {
    override val calculatedAt: Instant = calculatedAt
    override val riskState: RiskState = riskState
    override val failureReason: EwRiskLevelResult.FailureReason? = null
    override val ewAggregatedRiskResult: EwAggregatedRiskResult? = null
    override val exposureWindows: List<ExposureWindow>? = null
    override val matchedKeyCount: Int = 0
}
