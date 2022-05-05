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
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class SubmissionSymptomIntroductionViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var testType: BaseCoronaTest.Type
    private val currentSymptoms = mockFlowPreference<Symptoms?>(null)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { autoSubmission.isSubmissionRunning } returns flowOf(false)
        coEvery { autoSubmission.runSubmissionNow(any()) } just Runs
        every { submissionRepository.currentSymptoms } returns currentSymptoms
    }

    private fun createViewModel() = SubmissionSymptomIntroductionViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        submissionRepository = submissionRepository,
        autoSubmission = autoSubmission,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        testType = testType
    )

    @Test
    fun `positive symptom indication is forwarded using navigation arguments`() {
        createViewModel().apply {
            onPositiveSymptomIndication()
            onNextClicked()
            navigation.value shouldBe SubmissionSymptomIntroductionFragmentDirections
                .actionSubmissionSymptomIntroductionFragmentToSubmissionSymptomCalendarFragment(
                    symptomIndication = Symptoms.Indication.POSITIVE,
                    testType = testType
                )
        }

        verify(exactly = 0) { submissionRepository.currentSymptoms }
    }

    @Test
    fun `negative symptom indication leads to submission`() {
        createViewModel().apply {
            onNegativeSymptomIndication()
            onNextClicked()
            navigation.value shouldBe SubmissionSymptomIntroductionFragmentDirections
                .actionSubmissionSymptomIntroductionFragmentToMainFragment()
            currentSymptoms.value shouldBe Symptoms(
                startOfSymptoms = null,
                symptomIndication = Symptoms.Indication.NEGATIVE
            )
        }

        coVerify { autoSubmission.runSubmissionNow(any()) }
    }

    @Test
    fun `no information symptom indication leads to submission`() {
        createViewModel().apply {
            onNoInformationSymptomIndication()
            onNextClicked()
            navigation.value shouldBe SubmissionSymptomIntroductionFragmentDirections
                .actionSubmissionSymptomIntroductionFragmentToMainFragment()
            currentSymptoms.value shouldBe Symptoms(
                startOfSymptoms = null,
                symptomIndication = Symptoms.Indication.NO_INFORMATION
            )
        }

        coVerify { autoSubmission.runSubmissionNow(any()) }
    }

    @Test
    fun `submission by abort does not write any symptom data`() {
        createViewModel().onCancelConfirmed()

        currentSymptoms.value shouldBe null

        coVerifySequence {
            autoSubmission.isSubmissionRunning
            autoSubmission.runSubmissionNow(any())
        }
    }

    @Test
    fun `submission shows upload dialog`() {
        val uploadStatus = MutableStateFlow(false)
        every { autoSubmission.isSubmissionRunning } returns uploadStatus
        createViewModel().apply {
            showUploadDialog.observeForever { }
            showUploadDialog.value shouldBe false

            uploadStatus.value = true
            showUploadDialog.value shouldBe true

            uploadStatus.value = false
            showUploadDialog.value shouldBe false
        }
    }

    @Test
    fun `onNewUserActivity() should call analyticsKeySubmissionCollector for PCR tests`() {
        testType = PCR

        createViewModel().onNewUserActivity()

        verify(exactly = 1) { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS, PCR) }
        verify(exactly = 0) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS, RAPID_ANTIGEN)
        }
    }

    @Test
    fun `onNewUserActivity() should NOT call analyticsKeySubmissionCollector for RAT tests`() {
        testType = RAPID_ANTIGEN

        createViewModel().onNewUserActivity()

        verify(exactly = 0) { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS, PCR) }
        verify(exactly = 1) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS, RAPID_ANTIGEN)
        }
    }
}
