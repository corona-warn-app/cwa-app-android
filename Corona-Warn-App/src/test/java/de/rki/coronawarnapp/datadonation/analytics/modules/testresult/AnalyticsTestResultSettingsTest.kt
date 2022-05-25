package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindow
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsScanInstance
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.serialization.BaseGson
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences
import javax.inject.Inject

class AnalyticsTestResultSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    lateinit var preferences: MockSharedPreferences
    lateinit var pcrStorage: AnalyticsPCRTestResultSettings
    lateinit var raStorage: AnalyticsRATestResultSettings
    @MockK lateinit var analyticsExposureWindow: AnalyticsExposureWindow
    @MockK lateinit var analyticsScanInstance: AnalyticsScanInstance

    @Inject @BaseGson lateinit var gson: Gson
    private val sharedPrefKey = "analytics_testResultDonor"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
        preferences = MockSharedPreferences()
        every {
            context.getSharedPreferences(
                sharedPrefKey,
                Context.MODE_PRIVATE
            )
        } returns preferences
        every {
            context.getSharedPreferences(
                sharedPrefKey + "_RAT",
                Context.MODE_PRIVATE
            )
        } returns preferences
        pcrStorage = AnalyticsPCRTestResultSettings(
            context = context,
            gson
        )
        raStorage = AnalyticsRATestResultSettings(
            context = context,
            gson
        )

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
    fun tearDown() {
        pcrStorage.clear()
        raStorage.clear()
    }

    @Test
    fun dataIsNotMixedPcr() {
        pcrStorage.testRegisteredAt.update { Instant.ofEpochMilli(1000) }
        pcrStorage.testRegisteredAt.value shouldBe Instant.ofEpochMilli(1000)
        raStorage.testRegisteredAt.value shouldBe null

        pcrStorage.finalTestResultReceivedAt.update { Instant.ofEpochMilli(3000) }
        pcrStorage.finalTestResultReceivedAt.value shouldBe Instant.ofEpochMilli(3000)
        raStorage.finalTestResultReceivedAt.value shouldBe null

        pcrStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.update { 3 }
        pcrStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value shouldBe 3
        raStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value shouldBe -1

        pcrStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.update { 2 }
        pcrStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value shouldBe 2
        raStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value shouldBe -1

        pcrStorage.ewHoursSinceHighRiskWarningAtTestRegistration.update { 10 }
        pcrStorage.ewHoursSinceHighRiskWarningAtTestRegistration.value shouldBe 10
        raStorage.ewHoursSinceHighRiskWarningAtTestRegistration.value shouldBe -1

        pcrStorage.ptHoursSinceHighRiskWarningAtTestRegistration.update { 10 }
        pcrStorage.ptHoursSinceHighRiskWarningAtTestRegistration.value shouldBe 10
        raStorage.ptHoursSinceHighRiskWarningAtTestRegistration.value shouldBe -1

        pcrStorage.ewRiskLevelAtTestRegistration.update { PpaData.PPARiskLevel.RISK_LEVEL_HIGH }
        pcrStorage.ewRiskLevelAtTestRegistration.value shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        raStorage.ewRiskLevelAtTestRegistration.value shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW

        pcrStorage.ptRiskLevelAtTestRegistration.update { PpaData.PPARiskLevel.RISK_LEVEL_HIGH }
        pcrStorage.ptRiskLevelAtTestRegistration.value shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        raStorage.ptRiskLevelAtTestRegistration.value shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW

        pcrStorage.exposureWindowsAtTestRegistration.update { listOf(analyticsExposureWindow) }
        pcrStorage.exposureWindowsAtTestRegistration.value shouldBe listOf(analyticsExposureWindow)
        raStorage.exposureWindowsAtTestRegistration.value shouldBe null

        pcrStorage.exposureWindowsUntilTestResult.update { listOf(analyticsExposureWindow) }
        pcrStorage.exposureWindowsUntilTestResult.value shouldBe listOf(analyticsExposureWindow)
        raStorage.exposureWindowsUntilTestResult.value shouldBe null
    }
}
