package de.rki.coronawarnapp.ui.submission.testresult

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenViewModel
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionTestResultConsentGivenViewModelTest : BaseTest() {
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var testResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    lateinit var viewModel: SubmissionTestResultConsentGivenViewModel
    @MockK lateinit var testType: BaseCoronaTest.Type

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    private fun createViewModel() = SubmissionTestResultConsentGivenViewModel(
        submissionRepository = submissionRepository,
        dispatcherProvider = TestDispatcherProvider(),
        autoSubmission = autoSubmission,
        testResultAvailableNotificationService = testResultAvailableNotificationService,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        testType = testType
    )

    @Test
    fun testOnConsentProvideSymptomsButtonClick() {
        viewModel = createViewModel()
        viewModel.onContinuePressed()
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToSymptomIntroduction
    }

    @Test
    fun testOnCancelled() {
        viewModel = createViewModel()
        viewModel.onCancelConfirmed()
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToMainActivity
    }

    @Test
    fun `onNewUserActivity should call analyticsSubmissionCollector for PCR tests`() {
        testType = PCR
        createViewModel().onNewUserActivity()
        coVerify(exactly = 1) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT, PCR)
        }
        coVerify(exactly = 0) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT, RAPID_ANTIGEN)
        }
    }

    @Test
    fun `onNewUserActivity should NOT call analyticsSubmissionCollector for RAT tests`() {
        testType = RAPID_ANTIGEN
        createViewModel().onNewUserActivity()
        coVerify(exactly = 0) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT, PCR)
        }
        coVerify(exactly = 1) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.TEST_RESULT, RAPID_ANTIGEN)
        }
    }
}
