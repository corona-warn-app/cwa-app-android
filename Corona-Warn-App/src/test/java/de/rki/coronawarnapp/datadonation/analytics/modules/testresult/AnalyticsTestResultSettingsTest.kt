package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import android.content.Context
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class AnalyticsTestResultSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    @MockK lateinit var timeStamper: TimeStamper
    lateinit var preferences: MockSharedPreferences
    lateinit var pcrStorage: AnalyticsPCRTestResultSettings
    lateinit var raStorage: AnalyticsRATestResultSettings
    private val sharedPrefKey = "analytics_testResultDonor"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
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
            timeStamper = timeStamper
        )
        raStorage = AnalyticsRATestResultSettings(
            context = context,
            timeStamper = timeStamper
        )
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
    }
}
