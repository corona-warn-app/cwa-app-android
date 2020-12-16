package de.rki.coronawarnapp.ui.submission.symptoms.calendar

import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.util.preferences.FlowPreference
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.joda.time.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
class SubmissionSymptomCalendarViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    private lateinit var currentSymptoms: FlowPreference<Symptoms?>

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        currentSymptoms = mockFlowPreference(null)

        every { submissionRepository.isSubmissionRunning } returns flowOf(false)
        coEvery { submissionRepository.startSubmission() } just Runs
        every { submissionRepository.currentSymptoms } returns currentSymptoms
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createViewModel(indication: Symptoms.Indication = Symptoms.Indication.POSITIVE) =
        SubmissionSymptomCalendarViewModel(
            symptomIndication = indication,
            dispatcherProvider = TestDispatcherProvider,
            submissionRepository = submissionRepository
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
            submissionRepository.isSubmissionRunning
            submissionRepository.currentSymptoms
            submissionRepository.startSubmission()
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
            submissionRepository.isSubmissionRunning
            submissionRepository.startSubmission()
        }
    }

    @Test
    fun `submission shows upload dialog`() {
        val uploadStatus = MutableStateFlow(false)
        every { submissionRepository.isSubmissionRunning } returns uploadStatus
        createViewModel().apply {
            showUploadDialog.observeForever { }
            showUploadDialog.value shouldBe false

            uploadStatus.value = true
            showUploadDialog.value shouldBe true

            uploadStatus.value = false
            showUploadDialog.value shouldBe false
        }
    }
}
