package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingRiskCalculationParamContainer
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
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

    val container = PresenceTracingRiskCalculationParamContainer(
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
        runBlockingTest {
            createInstance().lookupTransmissionRiskValue(4) shouldBe 0.8
        }
    }

    @Test
    fun `lookupRiskStatePerDay returns correct value `() {
        runBlockingTest {
            createInstance().lookupRiskStatePerDay(15.0) shouldBe RiskState.LOW_RISK
            createInstance().lookupRiskStatePerDay(60.0) shouldBe RiskState.INCREASED_RISK
        }
    }

    @Test
    fun `lookupRiskStatePerCheckIn returns correct value `() {
        runBlockingTest {
            createInstance().lookupRiskStatePerCheckIn(30.0) shouldBe RiskState.LOW_RISK
            createInstance().lookupRiskStatePerCheckIn(30.1) shouldBe RiskState.INCREASED_RISK
        }
    }

    private fun createInstance() = PresenceTracingRiskMapper(configProvider)
}
