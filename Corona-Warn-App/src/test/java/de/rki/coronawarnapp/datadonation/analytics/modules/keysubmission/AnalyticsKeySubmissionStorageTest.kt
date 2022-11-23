package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.preferences.FakeDataStore

class AnalyticsKeySubmissionStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    lateinit var pcrStorage: AnalyticsPCRKeySubmissionStorage
    lateinit var raStorage: AnalyticsRAKeySubmissionStorage

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        pcrStorage = AnalyticsPCRKeySubmissionStorage(FakeDataStore())
        raStorage = AnalyticsRAKeySubmissionStorage(FakeDataStore())
    }

    @AfterEach
    fun tearDown() = runTest2 {
        pcrStorage.clear()
        raStorage.clear()
    }

    @Test
    fun dataIsNotMixedPcr() = runTest2 {
        pcrStorage.updateSubmitted(true)
        pcrStorage.submitted.first() shouldBe true
        raStorage.submitted.first() shouldBe false

        pcrStorage.updateSubmittedWithCheckIns(true)
        pcrStorage.submittedWithCheckIns.first() shouldBe true
        raStorage.submittedWithCheckIns.first() shouldBe false

        pcrStorage.updateSubmittedAfterCancel(true)
        pcrStorage.submittedAfterCancel.first() shouldBe true
        raStorage.submittedAfterCancel.first() shouldBe false

        pcrStorage.updateSubmittedAfterSymptomFlow(true)
        pcrStorage.submittedAfterSymptomFlow.first() shouldBe true
        raStorage.submittedAfterSymptomFlow.first() shouldBe false

        pcrStorage.updateSubmittedInBackground(true)
        pcrStorage.submittedInBackground.first() shouldBe true
        raStorage.submittedInBackground.first() shouldBe false

        pcrStorage.updateSubmittedAt(2000)
        pcrStorage.submittedAt.first() shouldBe 2000L
        raStorage.submittedAt.first() shouldBe -1L

        pcrStorage.updateRegisteredWithTeleTAN(true)
        pcrStorage.registeredWithTeleTAN.first() shouldBe true
        raStorage.registeredWithTeleTAN.first() shouldBe false

        pcrStorage.updateAdvancedConsentGiven(true)
        pcrStorage.advancedConsentGiven.first() shouldBe true
        raStorage.advancedConsentGiven.first() shouldBe false

        pcrStorage.updateLastSubmissionFlowScreen(3)
        pcrStorage.lastSubmissionFlowScreen.first() shouldBe 3
        raStorage.lastSubmissionFlowScreen.first() shouldBe 0

        pcrStorage.updateTestRegisteredAt(1000)
        pcrStorage.testRegisteredAt.first() shouldBe 1000L
        raStorage.testRegisteredAt.first() shouldBe -1L

        pcrStorage.updateTestResultReceivedAt(3000)
        pcrStorage.testResultReceivedAt.first() shouldBe 3000L
        raStorage.testResultReceivedAt.first() shouldBe -1L

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
    }

    @Test
    fun dataIsNotMixedRa() = runTest2 {
        raStorage.updateSubmitted(true)
        raStorage.submitted.first() shouldBe true
        pcrStorage.submitted.first() shouldBe false

        raStorage.updateSubmittedWithCheckIns(true)
        raStorage.submittedWithCheckIns.first() shouldBe true
        pcrStorage.submittedWithCheckIns.first() shouldBe false

        raStorage.updateSubmittedAfterCancel(true)
        raStorage.submittedAfterCancel.first() shouldBe true
        pcrStorage.submittedAfterCancel.first() shouldBe false

        raStorage.updateSubmittedAfterSymptomFlow(true)
        raStorage.submittedAfterSymptomFlow.first() shouldBe true
        pcrStorage.submittedAfterSymptomFlow.first() shouldBe false

        raStorage.updateSubmittedInBackground(true)
        raStorage.submittedInBackground.first() shouldBe true
        pcrStorage.submittedInBackground.first() shouldBe false

        raStorage.updateSubmittedAt(2000)
        raStorage.submittedAt.first() shouldBe 2000L
        pcrStorage.submittedAt.first() shouldBe -1L

        raStorage.updateRegisteredWithTeleTAN(true)
        raStorage.registeredWithTeleTAN.first() shouldBe true
        pcrStorage.registeredWithTeleTAN.first() shouldBe false

        raStorage.updateAdvancedConsentGiven(true)
        raStorage.advancedConsentGiven.first() shouldBe true
        pcrStorage.advancedConsentGiven.first() shouldBe false

        raStorage.updateTestRegisteredAt(1000)
        raStorage.testRegisteredAt.first() shouldBe 1000L
        pcrStorage.testRegisteredAt.first() shouldBe -1L

        raStorage.updateTestResultReceivedAt(3000)
        raStorage.testResultReceivedAt.first() shouldBe 3000L
        pcrStorage.testResultReceivedAt.first() shouldBe -1L

        raStorage.updateLastSubmissionFlowScreen(3)
        raStorage.lastSubmissionFlowScreen.first() shouldBe 3
        pcrStorage.lastSubmissionFlowScreen.first() shouldBe 0

        raStorage.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(3)
        raStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 3
        pcrStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe -1

        raStorage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(2)
        raStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe 2
        pcrStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first() shouldBe -1

        raStorage.updateEwHoursSinceHighRiskWarningAtTestRegistration(10)
        raStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        pcrStorage.ewHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe -1

        raStorage.updatePtHoursSinceHighRiskWarningAtTestRegistration(10)
        raStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe 10
        pcrStorage.ptHoursSinceHighRiskWarningAtTestRegistration.first() shouldBe -1
    }
}
