package de.rki.coronawarnapp.ui.submission.testresult.positive

import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.submission.SubmissionRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

internal class SubmissionTestResultNoConsentViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var testResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var coronaTestProvider: CoronaTestProvider

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    private fun createInstance(testType: Type) = SubmissionTestResultNoConsentViewModel(
        TestDispatcherProvider(),
        testResultAvailableNotificationService = testResultAvailableNotificationService,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        coronaTestProvider = coronaTestProvider,
        testIdentifier = ""
    )

    @Test
    fun `onTestOpened() should call analyticsKeySubmissionCollector for PCR tests`() {
        createInstance(PCR).onTestOpened()
        verify(exactly = 1) { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT, PCR) }
        verify(exactly = 0) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT, RAPID_ANTIGEN)
        }
    }

    @Test
    fun `onTestOpened() should call analyticsKeySubmissionCollector for RAT tests`() {
        createInstance(RAPID_ANTIGEN).onTestOpened()
        verify(exactly = 0) { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT, PCR) }
        verify(exactly = 1) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT, RAPID_ANTIGEN)
        }
    }
}
