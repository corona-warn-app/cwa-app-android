package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionSymptomIntroductionViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var testType: BaseCoronaTest.Type

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { autoSubmission.isSubmissionRunning } returns flowOf(false)
        coEvery { autoSubmission.runSubmissionNow(any()) } just Runs
        coEvery { submissionRepository.updateCurrentSymptoms(any()) } just Runs
    }

    private fun createViewModel(scope: CoroutineScope) = SubmissionSymptomIntroductionViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        submissionRepository = submissionRepository,
        autoSubmission = autoSubmission,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        testType = testType,
        appScope = scope,
        comesFromDispatcherFragment = true
    )

    @Test
    fun `positive symptom indication is forwarded using navigation arguments`() = runTest {
        createViewModel(this).apply {
            onPositiveSymptomIndication()
            onNextClicked()
            navigation.value shouldBe SubmissionSymptomIntroductionFragmentDirections
                .actionSubmissionSymptomIntroductionFragmentToSubmissionSymptomCalendarFragment(
                    symptomIndication = Symptoms.Indication.POSITIVE,
                    testType = testType,
                    comesFromDispatcherFragment = true
                )
        }

        coVerify(exactly = 0) { submissionRepository.updateCurrentSymptoms(any()) }
    }

    @Test
    fun `negative symptom indication leads to submission`() {
        runTest {
            createViewModel(this).apply {
                onNegativeSymptomIndication()
                onNextClicked()
                navigation.value shouldBe SubmissionSymptomIntroductionFragmentDirections
                    .actionSubmissionSymptomIntroductionFragmentToSubmissionDoneFragment(testType)
            }
        }

        coVerifySequence {
            submissionRepository.updateCurrentSymptoms(
                Symptoms(
                    startOfSymptoms = null,
                    symptomIndication = Symptoms.Indication.NEGATIVE
                )
            )
            autoSubmission.runSubmissionNow(any())
        }
    }

    @Test
    fun `no information symptom indication leads to submission`() {
        runTest {
            val viewModel = createViewModel(this).apply {
                onNoInformationSymptomIndication()
                onNextClicked()
            }
            viewModel.navigation.value shouldBe SubmissionSymptomIntroductionFragmentDirections
                .actionSubmissionSymptomIntroductionFragmentToSubmissionDoneFragment(testType)
        }

        coVerifySequence {
            submissionRepository.updateCurrentSymptoms(
                Symptoms(
                    startOfSymptoms = null,
                    symptomIndication = Symptoms.Indication.NO_INFORMATION
                )
            )
            autoSubmission.runSubmissionNow(any())
        }
    }

    @Test
    fun `submission by abort does not write any symptom data`() {
        runTest {
            createViewModel(this).apply {
                onCancelConfirmed()
                navigation.value shouldBe SubmissionSymptomIntroductionFragmentDirections
                    .actionSubmissionSymptomIntroductionFragmentToMainFragment()
            }
        }

        coVerify(exactly = 0) {
            submissionRepository.updateCurrentSymptoms(any())
        }

        coVerifySequence {
            autoSubmission.runSubmissionNow(any())
        }
    }

    @Test
    fun `onNewUserActivity() should call analyticsKeySubmissionCollector for PCR tests`() = runTest {
        testType = PCR

        createViewModel(this).onNewUserActivity()

        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS, PCR) }
        coVerify(exactly = 0) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS, RAPID_ANTIGEN)
        }
    }

    @Test
    fun `onNewUserActivity() should NOT call analyticsKeySubmissionCollector for RAT tests`() = runTest {
        testType = RAPID_ANTIGEN

        createViewModel(this).onNewUserActivity()

        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS, PCR) }
        coVerify(exactly = 1) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS, RAPID_ANTIGEN)
        }
    }
}
