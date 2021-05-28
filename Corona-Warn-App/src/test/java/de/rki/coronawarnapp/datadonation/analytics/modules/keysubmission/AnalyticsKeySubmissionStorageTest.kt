package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class AnalyticsKeySubmissionStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    lateinit var preferences: MockSharedPreferences
    lateinit var pcrStorage: AnalyticsPCRKeySubmissionStorage
    lateinit var raStorage: AnalyticsRAKeySubmissionStorage
    private val sharedPrefKey = "analytics_key_submission_localdata"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        preferences = MockSharedPreferences()
        every {
            context.getSharedPreferences(
                "$sharedPrefKey${AnalyticsPCRKeySubmissionStorage.sharedPrefKeySuffix}", Context.MODE_PRIVATE
            )
        } returns preferences
        every {
            context.getSharedPreferences(
                "$sharedPrefKey${AnalyticsRAKeySubmissionStorage.sharedPrefKeySuffix}", Context.MODE_PRIVATE
            )
        } returns preferences
        pcrStorage = AnalyticsPCRKeySubmissionStorage(
            context = context
        )
        raStorage = AnalyticsRAKeySubmissionStorage(
            context = context
        )
    }

    @AfterEach
    fun tearDown() {
        pcrStorage.clear()
        raStorage.clear()
    }

    @Test
    fun dataIsNotMixedPcr() {
        pcrStorage.submitted.update { true }
        pcrStorage.submitted.value shouldBe true
        raStorage.submitted.value shouldBe false

        pcrStorage.submittedWithCheckIns.update { true }
        pcrStorage.submittedWithCheckIns.value shouldBe true
        raStorage.submittedWithCheckIns.value shouldBe false

        pcrStorage.submittedAfterCancel.update { true }
        pcrStorage.submittedAfterCancel.value shouldBe true
        raStorage.submittedAfterCancel.value shouldBe false

        pcrStorage.submittedAfterSymptomFlow.update { true }
        pcrStorage.submittedAfterSymptomFlow.value shouldBe true
        raStorage.submittedAfterSymptomFlow.value shouldBe false

        pcrStorage.submittedInBackground.update { true }
        pcrStorage.submittedInBackground.value shouldBe true
        raStorage.submittedInBackground.value shouldBe false

        pcrStorage.submittedAt.update { 2000 }
        pcrStorage.submittedAt.value shouldBe 2000L
        raStorage.submittedAt.value shouldBe -1L

        pcrStorage.registeredWithTeleTAN.update { true }
        pcrStorage.registeredWithTeleTAN.value shouldBe true
        raStorage.registeredWithTeleTAN.value shouldBe false

        pcrStorage.advancedConsentGiven.update { true }
        pcrStorage.advancedConsentGiven.value shouldBe true
        raStorage.advancedConsentGiven.value shouldBe false

        pcrStorage.lastSubmissionFlowScreen.update { 3 }
        pcrStorage.lastSubmissionFlowScreen.value shouldBe 3
        raStorage.lastSubmissionFlowScreen.value shouldBe 0

        pcrStorage.testRegisteredAt.update { 1000 }
        pcrStorage.testRegisteredAt.value shouldBe 1000L
        raStorage.testRegisteredAt.value shouldBe -1L

        pcrStorage.testResultReceivedAt.update { 3000 }
        pcrStorage.testResultReceivedAt.value shouldBe 3000L
        raStorage.testResultReceivedAt.value shouldBe -1L

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
    }

    @Test
    fun dataIsNotMixedRa() {
        raStorage.submitted.update { true }
        raStorage.submitted.value shouldBe true
        pcrStorage.submitted.value shouldBe false

        raStorage.submittedWithCheckIns.update { true }
        raStorage.submittedWithCheckIns.value shouldBe true
        pcrStorage.submittedWithCheckIns.value shouldBe false

        raStorage.submittedAfterCancel.update { true }
        raStorage.submittedAfterCancel.value shouldBe true
        pcrStorage.submittedAfterCancel.value shouldBe false

        raStorage.submittedAfterSymptomFlow.update { true }
        raStorage.submittedAfterSymptomFlow.value shouldBe true
        pcrStorage.submittedAfterSymptomFlow.value shouldBe false

        raStorage.submittedInBackground.update { true }
        raStorage.submittedInBackground.value shouldBe true
        pcrStorage.submittedInBackground.value shouldBe false

        raStorage.submittedAt.update { 2000 }
        raStorage.submittedAt.value shouldBe 2000L
        pcrStorage.submittedAt.value shouldBe -1L

        raStorage.registeredWithTeleTAN.update { true }
        raStorage.registeredWithTeleTAN.value shouldBe true
        pcrStorage.registeredWithTeleTAN.value shouldBe false

        raStorage.advancedConsentGiven.update { true }
        raStorage.advancedConsentGiven.value shouldBe true
        pcrStorage.advancedConsentGiven.value shouldBe false

        raStorage.testRegisteredAt.update { 1000 }
        raStorage.testRegisteredAt.value shouldBe 1000L
        pcrStorage.testRegisteredAt.value shouldBe -1L

        raStorage.testResultReceivedAt.update { 3000 }
        raStorage.testResultReceivedAt.value shouldBe 3000L
        pcrStorage.testResultReceivedAt.value shouldBe -1L

        raStorage.lastSubmissionFlowScreen.update { 3 }
        raStorage.lastSubmissionFlowScreen.value shouldBe 3
        pcrStorage.lastSubmissionFlowScreen.value shouldBe 0

        raStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.update { 3 }
        raStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value shouldBe 3
        pcrStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value shouldBe -1

        raStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.update { 2 }
        raStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value shouldBe 2
        pcrStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value shouldBe -1

        raStorage.ewHoursSinceHighRiskWarningAtTestRegistration.update { 10 }
        raStorage.ewHoursSinceHighRiskWarningAtTestRegistration.value shouldBe 10
        pcrStorage.ewHoursSinceHighRiskWarningAtTestRegistration.value shouldBe -1

        raStorage.ptHoursSinceHighRiskWarningAtTestRegistration.update { 10 }
        raStorage.ptHoursSinceHighRiskWarningAtTestRegistration.value shouldBe 10
        pcrStorage.ptHoursSinceHighRiskWarningAtTestRegistration.value shouldBe -1
    }
}
