package de.rki.coronawarnapp.ui.submission.symptoms.calendar

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
import java.time.LocalDate

@ExtendWith(InstantExecutorExtension::class)
class SubmissionSymptomCalendarViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var testType: BaseCoronaTest.Type

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { autoSubmission.isSubmissionRunning } returns flowOf(false)
        coEvery { autoSubmission.updateMode(any()) } just Runs
        coEvery { autoSubmission.runSubmissionNow(any()) } just Runs
        coEvery { submissionRepository.updateCurrentSymptoms(any()) } just Runs
    }

    private fun createViewModel(
        scope: CoroutineScope,
        indication: Symptoms.Indication = Symptoms.Indication.POSITIVE,
    ) =
        SubmissionSymptomCalendarViewModel(
            symptomIndication = indication,
            dispatcherProvider = TestDispatcherProvider(),
            submissionRepository = submissionRepository,
            autoSubmission = autoSubmission,
            analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
            testType = testType,
            appScope = scope,
            comesFromDispatcherFragment = true
        )

    @Test
    fun `symptom indication is not written to settings`() {
        runTest {
            createViewModel(this).apply {
                onLastSevenDaysStart()
                onOneToTwoWeeksAgoStart()
                onMoreThanTwoWeeksStart()
                onNoInformationStart()
                onDateSelected(LocalDate.now())
            }
        }

        coVerify(exactly = 0) { submissionRepository.updateCurrentSymptoms(any()) }
    }

    @Test
    fun `submission by symptom completion updates symptom data`() {
        runTest {
            createViewModel(this).apply {
                onLastSevenDaysStart()
                onDone()
                routeToScreen.value shouldBe SubmissionSymptomCalendarFragmentDirections
                    .actionSubmissionSymptomCalendarFragmentToSubmissionDoneFragment(testType, true)
            }
        }

        coVerifySequence {
            submissionRepository.updateCurrentSymptoms(
                Symptoms(
                    startOfSymptoms = Symptoms.StartOf.LastSevenDays,
                    symptomIndication = Symptoms.Indication.POSITIVE
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
                routeToScreen.value shouldBe SubmissionSymptomCalendarFragmentDirections
                    .actionSubmissionSymptomCalendarFragmentToMainFragment()
            }
        }

        coVerifySequence {
            autoSubmission.runSubmissionNow(any())
        }
    }

    @Test
    fun `onNewUserActivity() should call analyticsKeySubmissionCollector for PCR tests`() {
        testType = PCR
        runTest {
            createViewModel(this).onNewUserActivity()
        }

        coVerify(exactly = 1) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOM_ONSET, PCR)
        }
        coVerify(exactly = 0) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOM_ONSET, RAPID_ANTIGEN)
        }
    }

    @Test
    fun `onNewUserActivity() should NOT call analyticsKeySubmissionCollector for RAT tests`() {
        testType = RAPID_ANTIGEN

        runTest {
            createViewModel(this).onNewUserActivity()
        }

        coVerify(exactly = 0) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOM_ONSET, PCR)
        }
        coVerify(exactly = 1) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOM_ONSET, RAPID_ANTIGEN)
        }
    }

    @Test
    fun `onCancelConfirmed() should call analyticsKeySubmissionCollector for PCR tests`() {
        testType = PCR
        runTest {
            createViewModel(this).apply {
                onCancelConfirmed()
                routeToScreen.value shouldBe SubmissionSymptomCalendarFragmentDirections
                    .actionSubmissionSymptomCalendarFragmentToMainFragment()
            }
        }

        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedAfterCancel(PCR) }
        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedAfterCancel(RAPID_ANTIGEN) }
    }

    @Test
    fun `onCancelConfirmed() should NOT call analyticsKeySubmissionCollector for RAT tests`() {
        testType = RAPID_ANTIGEN

        runTest {
            createViewModel(this).apply {
                onCancelConfirmed()
                routeToScreen.value shouldBe SubmissionSymptomCalendarFragmentDirections
                    .actionSubmissionSymptomCalendarFragmentToMainFragment()
            }
        }

        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedAfterCancel(PCR) }
        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedAfterCancel(RAPID_ANTIGEN) }
    }

    @Test
    fun `onDone() should call analyticsKeySubmissionCollector for PCR tests`() {
        testType = PCR

        runTest {
            createViewModel(this).apply {
                onLastSevenDaysStart()
                onDone()
                routeToScreen.value shouldBe SubmissionSymptomCalendarFragmentDirections
                    .actionSubmissionSymptomCalendarFragmentToSubmissionDoneFragment(testType, true)
            }
        }

        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedAfterSymptomFlow(PCR) }
        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedAfterSymptomFlow(RAPID_ANTIGEN) }
    }

    @Test
    fun `onDone() should NOT call analyticsKeySubmissionCollector for RAT tests`() {
        testType = RAPID_ANTIGEN

        runTest {
            createViewModel(this).apply {
                onLastSevenDaysStart()
                onDone()
                routeToScreen.value shouldBe SubmissionSymptomCalendarFragmentDirections
                    .actionSubmissionSymptomCalendarFragmentToSubmissionDoneFragment(testType, true)
            }
        }

        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedAfterSymptomFlow(PCR) }
        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedAfterSymptomFlow(RAPID_ANTIGEN) }
    }
}
