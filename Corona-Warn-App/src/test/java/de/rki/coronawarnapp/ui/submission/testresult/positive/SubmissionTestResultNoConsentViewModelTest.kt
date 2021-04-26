package de.rki.coronawarnapp.ui.submission.testresult.positive

import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class SubmissionTestResultNoConsentViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var testResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    private fun createInstance(testType: Type) = SubmissionTestResultNoConsentViewModel(
        submissionRepository,
        testResultAvailableNotificationService,
        analyticsKeySubmissionCollector,
        testType
    )

    @Test
    fun `onTestOpened() should call analyticsKeySubmissionCollector for PCR tests`() {
        createInstance(PCR).onTestOpened()
        verify(exactly = 1) { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT) }
    }

    @Test
    fun `onTestOpened() should NOT call analyticsKeySubmissionCollector for RAT tests`() {
        createInstance(RAPID_ANTIGEN).onTestOpened()
        verify(exactly = 0) { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT) }
    }
}
