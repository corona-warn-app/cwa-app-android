package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import de.rki.coronawarnapp.datadonation.analytics.common.AnalyticsExposureWindow
import de.rki.coronawarnapp.datadonation.analytics.common.AnalyticsScanInstance
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.preferences.FakeDataStore

class AnalyticsTestResultSettingsTest : BaseTest() {
    private lateinit var pcrStorage: AnalyticsPCRTestResultSettings
    private lateinit var raStorage: AnalyticsRATestResultSettings
    @MockK lateinit var analyticsExposureWindow: AnalyticsExposureWindow
    @MockK lateinit var analyticsScanInstance: AnalyticsScanInstance

    private val dataStore = FakeDataStore()
    private val mapper = SerializationModule().jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        pcrStorage = AnalyticsPCRTestResultSettings(mapper, dataStore)
        raStorage = AnalyticsRATestResultSettings(mapper, dataStore)

        with(analyticsScanInstance) {
            every { minAttenuation } returns 1
            every { typicalAttenuation } returns 2
            every { secondsSinceLastScan } returns 3
        }

        with(analyticsExposureWindow) {
            every { analyticsScanInstances } returns listOf(analyticsScanInstance)
            every { calibrationConfidence } returns 4
            every { dateMillis } returns 1000L
            every { infectiousness } returns 5
            every { reportType } returns 6
            every { normalizedTime } returns 1.1
            every { transmissionRiskLevel } returns 7
        }
    }

    @AfterEach
    fun tearDown() = runTest2 {
        pcrStorage.clear()
        raStorage.clear()
    }

    @Test
    fun dataIsNotMixedPcr() = runTest2 {
        pcrStorage.updateTestRegisteredAt(Instant.ofEpochMilli(1000))
        pcrStorage.testRegisteredAt.first() shouldBe Instant.ofEpochMilli(1000)
        raStorage.testRegisteredAt.first() shouldBe null

        pcrStorage.updateFinalTestResultReceivedAt(Instant.ofEpochMilli(3000))
        pcrStorage.finalTestResultReceivedAt.first() shouldBe Instant.ofEpochMilli(3000)
        raStorage.finalTestResultReceivedAt.first() shouldBe null

        pcrStorage.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(3)
        pcrStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 3
        raStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe -1

        pcrStorage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(2)
        pcrStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 2
        raStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe -1

        pcrStorage.updateEwHoursSinceHighRiskWarningAtTestRegistration(10)
        pcrStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        raStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe -1

        pcrStorage.updatePtHoursSinceHighRiskWarningAtTestRegistration(10)
        pcrStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        raStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe -1

        pcrStorage.updateEwRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        pcrStorage.ewRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        raStorage.ewRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW

        pcrStorage.updatePtRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        pcrStorage.ptRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        raStorage.ptRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW

        pcrStorage.updateExposureWindowsAtTestRegistration(listOf(analyticsExposureWindow))
        pcrStorage.exposureWindowsAtTestRegistration.first() shouldBe listOf(analyticsExposureWindow)
        raStorage.exposureWindowsAtTestRegistration.first() shouldBe null

        pcrStorage.updateExposureWindowsUntilTestResult(listOf(analyticsExposureWindow))
        pcrStorage.exposureWindowsUntilTestResult.first() shouldBe listOf(analyticsExposureWindow)
        raStorage.exposureWindowsUntilTestResult.first() shouldBe null
    }

    @Test
    fun `dataMixedRatPcr - clear PCR does not clear RAT`() = runTest2 {
        pcrStorage.updateTestRegisteredAt(Instant.ofEpochMilli(1000))
        pcrStorage.testRegisteredAt.first() shouldBe Instant.ofEpochMilli(1000)
        raStorage.updateTestRegisteredAt(Instant.ofEpochMilli(1000))
        raStorage.testRegisteredAt.first() shouldBe Instant.ofEpochMilli(1000)

        pcrStorage.updateFinalTestResultReceivedAt(Instant.ofEpochMilli(3000))
        pcrStorage.finalTestResultReceivedAt.first() shouldBe Instant.ofEpochMilli(3000)
        raStorage.updateFinalTestResultReceivedAt(Instant.ofEpochMilli(3000))
        raStorage.finalTestResultReceivedAt.first() shouldBe Instant.ofEpochMilli(3000)

        pcrStorage.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(3)
        pcrStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 3
        raStorage.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(3)
        raStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 3

        pcrStorage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(2)
        pcrStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 2
        raStorage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(2)
        raStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 2

        pcrStorage.updateEwHoursSinceHighRiskWarningAtTestRegistration(10)
        pcrStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        raStorage.updateEwHoursSinceHighRiskWarningAtTestRegistration(10)
        raStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10

        pcrStorage.updatePtHoursSinceHighRiskWarningAtTestRegistration(10)
        pcrStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        raStorage.updatePtHoursSinceHighRiskWarningAtTestRegistration(10)
        raStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10

        pcrStorage.updateEwRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        pcrStorage.ewRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        raStorage.updateEwRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        raStorage.ewRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH

        pcrStorage.updatePtRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        pcrStorage.ptRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        raStorage.updatePtRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        raStorage.ptRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH

        pcrStorage.updateExposureWindowsAtTestRegistration(listOf(analyticsExposureWindow))
        pcrStorage.exposureWindowsAtTestRegistration.first() shouldBe listOf(analyticsExposureWindow)
        raStorage.updateExposureWindowsAtTestRegistration(listOf(analyticsExposureWindow))
        raStorage.exposureWindowsAtTestRegistration.first() shouldBe listOf(analyticsExposureWindow)

        pcrStorage.updateExposureWindowsUntilTestResult(listOf(analyticsExposureWindow))
        pcrStorage.exposureWindowsUntilTestResult.first() shouldBe listOf(analyticsExposureWindow)
        raStorage.updateExposureWindowsUntilTestResult(listOf(analyticsExposureWindow))
        raStorage.exposureWindowsUntilTestResult.first() shouldBe listOf(analyticsExposureWindow)

        pcrStorage.clear()

        raStorage.testRegisteredAt.first() shouldBe Instant.ofEpochMilli(1000)
        pcrStorage.testRegisteredAt.first() shouldBe null

        raStorage.finalTestResultReceivedAt.first() shouldBe Instant.ofEpochMilli(3000)
        pcrStorage.finalTestResultReceivedAt.first() shouldBe null

        raStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 3
        pcrStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe -1

        raStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 2
        pcrStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe -1

        raStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        pcrStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe -1

        raStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        pcrStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe -1

        raStorage.ewRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        pcrStorage.ewRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW

        raStorage.ptRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        pcrStorage.ptRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW

        raStorage.exposureWindowsAtTestRegistration.first() shouldBe listOf(analyticsExposureWindow)
        pcrStorage.exposureWindowsAtTestRegistration.first() shouldBe null

        raStorage.exposureWindowsUntilTestResult.first() shouldBe listOf(analyticsExposureWindow)
        pcrStorage.exposureWindowsUntilTestResult.first() shouldBe null
    }

    @Test
    fun `dataMixedRatPcr - clear RAT does clear PCR`() = runTest2 {
        pcrStorage.updateTestRegisteredAt(Instant.ofEpochMilli(1000))
        pcrStorage.testRegisteredAt.first() shouldBe Instant.ofEpochMilli(1000)
        raStorage.updateTestRegisteredAt(Instant.ofEpochMilli(1000))
        raStorage.testRegisteredAt.first() shouldBe Instant.ofEpochMilli(1000)

        pcrStorage.updateFinalTestResultReceivedAt(Instant.ofEpochMilli(3000))
        pcrStorage.finalTestResultReceivedAt.first() shouldBe Instant.ofEpochMilli(3000)
        raStorage.updateFinalTestResultReceivedAt(Instant.ofEpochMilli(3000))
        raStorage.finalTestResultReceivedAt.first() shouldBe Instant.ofEpochMilli(3000)

        pcrStorage.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(3)
        pcrStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 3
        raStorage.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(3)
        raStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 3

        pcrStorage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(2)
        pcrStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 2
        raStorage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(2)
        raStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 2

        pcrStorage.updateEwHoursSinceHighRiskWarningAtTestRegistration(10)
        pcrStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        raStorage.updateEwHoursSinceHighRiskWarningAtTestRegistration(10)
        raStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10

        pcrStorage.updatePtHoursSinceHighRiskWarningAtTestRegistration(10)
        pcrStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        raStorage.updatePtHoursSinceHighRiskWarningAtTestRegistration(10)
        raStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10

        pcrStorage.updateEwRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        pcrStorage.ewRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        raStorage.updateEwRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        raStorage.ewRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH

        pcrStorage.updatePtRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        pcrStorage.ptRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        raStorage.updatePtRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        raStorage.ptRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH

        pcrStorage.updateExposureWindowsAtTestRegistration(listOf(analyticsExposureWindow))
        pcrStorage.exposureWindowsAtTestRegistration.first() shouldBe listOf(analyticsExposureWindow)
        raStorage.updateExposureWindowsAtTestRegistration(listOf(analyticsExposureWindow))
        raStorage.exposureWindowsAtTestRegistration.first() shouldBe listOf(analyticsExposureWindow)

        pcrStorage.updateExposureWindowsUntilTestResult(listOf(analyticsExposureWindow))
        pcrStorage.exposureWindowsUntilTestResult.first() shouldBe listOf(analyticsExposureWindow)
        raStorage.updateExposureWindowsUntilTestResult(listOf(analyticsExposureWindow))
        raStorage.exposureWindowsUntilTestResult.first() shouldBe listOf(analyticsExposureWindow)

        raStorage.clear()

        pcrStorage.testRegisteredAt.first() shouldBe Instant.ofEpochMilli(1000)
        raStorage.testRegisteredAt.first() shouldBe null

        pcrStorage.finalTestResultReceivedAt.first() shouldBe Instant.ofEpochMilli(3000)
        raStorage.finalTestResultReceivedAt.first() shouldBe null

        pcrStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 3
        raStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe -1

        pcrStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 2
        raStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe -1

        pcrStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        raStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe -1

        pcrStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        raStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe -1

        pcrStorage.ewRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        raStorage.ewRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW

        pcrStorage.ptRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        raStorage.ptRiskLevelAtTestRegistration.first() shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW

        pcrStorage.exposureWindowsAtTestRegistration.first() shouldBe listOf(analyticsExposureWindow)
        raStorage.exposureWindowsAtTestRegistration.first() shouldBe null

        pcrStorage.exposureWindowsUntilTestResult.first() shouldBe listOf(analyticsExposureWindow)
        raStorage.exposureWindowsUntilTestResult.first() shouldBe null
    }
}
