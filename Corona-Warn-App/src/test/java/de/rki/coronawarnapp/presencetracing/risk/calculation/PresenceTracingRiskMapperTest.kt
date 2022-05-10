package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingRiskCalculationParamContainer
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PresenceTracingRiskMapperTest : BaseTest() {
    @MockK lateinit var configProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData

    private val transmissionRiskValueMapping =
        RiskCalculationParametersOuterClass.TransmissionRiskValueMapping.newBuilder()
            .setTransmissionRiskLevel(4)
            .setTransmissionRiskValue(0.8)
            .build()

    private val normalizedTimeMappingLow =
        RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.newBuilder()
            .setNormalizedTimeRange(
                RiskCalculationParametersOuterClass.Range.newBuilder()
                    .setMin(0.0)
                    .setMinExclusive(false)
                    .setMax(30.0)
                    .setMaxExclusive(false)
            )
            .setRiskLevel(RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW)
            .build()

    private val normalizedTimeMappingHigh =
        RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.newBuilder()
            .setNormalizedTimeRange(
                RiskCalculationParametersOuterClass.Range.newBuilder()
                    .setMin(30.0)
                    .setMinExclusive(true)
                    .setMax(9999.0)
                    .setMaxExclusive(true)
            )
            .setRiskLevel(RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH)
            .build()

    private val container = PresenceTracingRiskCalculationParamContainer(
        transmissionRiskValueMapping = listOf(transmissionRiskValueMapping),
        normalizedTimePerCheckInToRiskLevelMapping = listOf(normalizedTimeMappingLow, normalizedTimeMappingHigh),
        normalizedTimePerDayToRiskLevelMapping = listOf(normalizedTimeMappingLow, normalizedTimeMappingHigh)
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { configProvider.currentConfig } returns flowOf(configData)
        every { configData.presenceTracing.riskCalculationParameters } returns container
    }

    @Test
    fun `lookupTransmissionRiskValue returns correct value `() {
        runTest {
            createInstance().lookupTransmissionRiskValue(4) shouldBe 0.8
        }
    }

    @Test
    fun `lookupRiskStatePerDay returns correct value `() {
        runTest {
            createInstance().lookupRiskStatePerDay(30.0) shouldBe RiskState.LOW_RISK
            createInstance().lookupRiskStatePerDay(30.1) shouldBe RiskState.INCREASED_RISK
            createInstance().lookupRiskStatePerDay(0.0) shouldBe RiskState.LOW_RISK
            createInstance().lookupRiskStatePerDay(60.0) shouldBe RiskState.INCREASED_RISK
        }
    }

    @Test
    fun `lookupRiskStatePerCheckIn returns correct value `() {
        runTest {
            createInstance().lookupRiskStatePerCheckIn(30.0) shouldBe RiskState.LOW_RISK
            createInstance().lookupRiskStatePerCheckIn(30.1) shouldBe RiskState.INCREASED_RISK
            createInstance().lookupRiskStatePerCheckIn(0.0) shouldBe RiskState.LOW_RISK
            createInstance().lookupRiskStatePerCheckIn(100.1) shouldBe RiskState.INCREASED_RISK
        }
    }

    @Test
    fun `out of range returns failed calculation `() {
        runTest {
            createInstance().lookupRiskStatePerDay(10000.0) shouldBe RiskState.CALCULATION_FAILED
            createInstance().lookupRiskStatePerCheckIn(100000.1) shouldBe RiskState.CALCULATION_FAILED
            createInstance().lookupRiskStatePerDay(-1.0) shouldBe RiskState.CALCULATION_FAILED
            createInstance().lookupRiskStatePerCheckIn(-1.0) shouldBe RiskState.CALCULATION_FAILED
        }
    }

    @Test
    fun `config is requested only once`() {
        runTest {
            val mapper = createInstance()
            mapper.lookupRiskStatePerDay(30.0)
            mapper.lookupRiskStatePerDay(60.0)
            coVerify(exactly = 1) { configProvider.currentConfig }
        }
    }

    @Test
    fun `config is requested again after reset`() {
        runTest {
            val mapper = createInstance()
            mapper.lookupRiskStatePerDay(30.0)
            mapper.clearConfig()
            mapper.lookupRiskStatePerDay(60.0)
            coVerify(exactly = 2) { configProvider.currentConfig }
        }
    }

    private fun createInstance() = PresenceTracingRiskMapper(configProvider)
}
