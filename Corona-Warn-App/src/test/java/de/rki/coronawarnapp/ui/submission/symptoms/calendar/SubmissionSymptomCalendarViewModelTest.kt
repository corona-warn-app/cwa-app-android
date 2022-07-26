package de.rki.coronawarnapp.ui.submission.symptoms.calendar

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.util.preferences.FlowPreference
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.preferences.mockFlowPreference
import java.time.LocalDate

@ExtendWith(InstantExecutorExtension::class)
class SubmissionSymptomCalendarViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var testType: BaseCoronaTest.Type
    private lateinit var currentSymptoms: FlowPreference<Symptoms?>

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        currentSymptoms = mockFlowPreference(null)

        every { autoSubmission.isSubmissionRunning } returns flowOf(false)
        every { autoSubmission.updateMode(any()) } just Runs
        coEvery { autoSubmission.runSubmissionNow(any()) } just Runs
        every { submissionRepository.currentSymptoms } returns currentSymptoms
    }

    private fun createViewModel(indication: Symptoms.Indication = Symptoms.Indication.POSITIVE) =
        SubmissionSymptomCalendarViewModel(
            symptomIndication = indication,
            dispatcherProvider = TestDispatcherProvider(),
            submissionRepository = submissionRepository,
            autoSubmission = autoSubmission,
            analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
            testType = testType
        )

    @Test
    fun `symptom indication is not written to settings`() {
        createViewModel().apply {
            onLastSevenDaysStart()
            onOneToTwoWeeksAgoStart()
            onMoreThanTwoWeeksStart()
            onNoInformationStart()
            onDateSelected(LocalDate.now())
        }

        verify(exactly = 0) { submissionRepository.currentSymptoms }
    }

    @Test
    fun `submission by symptom completion updates symptom data`() {
        createViewModel().apply {
            onLastSevenDaysStart()
            onDone()
        }

        coVerifySequence {
            autoSubmission.isSubmissionRunning
            submissionRepository.currentSymptoms
            autoSubmission.runSubmissionNow(any())
        }

        currentSymptoms.value shouldBe Symptoms(
            startOfSymptoms = Symptoms.StartOf.LastSevenDays,
            symptomIndication = Symptoms.Indication.POSITIVE
        )
    }

    @Test
    fun `submission by abort does not write any symptom data`() {
        createViewModel().onCancelConfirmed()

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

        verify(exactly = 1) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOM_ONSET, PCR)
        }
        verify(exactly = 0) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOM_ONSET, RAPID_ANTIGEN)
        }
    }

    @Test
    fun `onNewUserActivity() should NOT call analyticsKeySubmissionCollector for RAT tests`() {
        testType = RAPID_ANTIGEN

        createViewModel().onNewUserActivity()

        verify(exactly = 0) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOM_ONSET, PCR)
        }
        verify(exactly = 1) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOM_ONSET, RAPID_ANTIGEN)
        }
    }

    @Test
    fun `onCancelConfirmed() should call analyticsKeySubmissionCollector for PCR tests`() {
        testType = PCR

        createViewModel().onCancelConfirmed()

        verify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedAfterCancel(PCR) }
        verify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedAfterCancel(RAPID_ANTIGEN) }
    }

    @Test
    fun `onCancelConfirmed() should NOT call analyticsKeySubmissionCollector for RAT tests`() {
        testType = RAPID_ANTIGEN

        createViewModel().onCancelConfirmed()

        verify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedAfterCancel(PCR) }
        verify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedAfterCancel(RAPID_ANTIGEN) }
    }

    @Test
    fun `onDone() should call analyticsKeySubmissionCollector for PCR tests`() = runTest {
        testType = PCR

        createViewModel().apply {
            onLastSevenDaysStart()
            onDone()
        }

        verify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedAfterSymptomFlow(PCR) }
        verify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedAfterSymptomFlow(RAPID_ANTIGEN) }
    }

    @Test
    fun `onDone() should NOT call analyticsKeySubmissionCollector for RAT tests`() {
        testType = RAPID_ANTIGEN

        createViewModel().apply {
            onLastSevenDaysStart()
            onDone()
        }

        verify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedAfterSymptomFlow(PCR) }
        verify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedAfterSymptomFlow(RAPID_ANTIGEN) }
    }
}
