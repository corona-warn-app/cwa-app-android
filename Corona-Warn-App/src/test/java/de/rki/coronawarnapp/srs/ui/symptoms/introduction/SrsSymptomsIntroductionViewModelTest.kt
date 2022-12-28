package de.rki.coronawarnapp.srs.ui.symptoms.introduction

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.core.repository.SrsSubmissionRepository
import de.rki.coronawarnapp.srs.ui.symptoms.intro.SrsSymptomsIntroductionNavigation
import de.rki.coronawarnapp.srs.ui.symptoms.intro.SrsSymptomsIntroductionViewModel
import de.rki.coronawarnapp.srs.ui.vm.TeksSharedViewModel
import de.rki.coronawarnapp.submission.Symptoms
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import java.time.Instant

@ExtendWith(InstantExecutorExtension::class)
class SrsSymptomsIntroductionViewModelTest : BaseTest() {
    @MockK lateinit var srsSubmissionRepository: SrsSubmissionRepository
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var submissionType: SrsSubmissionType
    @MockK lateinit var teksSharedViewModel: TeksSharedViewModel
    private val selectedCheckIns = longArrayOf()

    private val checkIn1 = CheckIn(
        id = 1L,
        traceLocationId = "41da2115-eba2-49bd-bf17-adb3d635ddaf".decodeBase64()!!,
        version = 1,
        type = 2,
        description = "brothers birthday",
        address = "Malibu",
        traceLocationStart = Instant.EPOCH,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.EPOCH,
        checkInEnd = Instant.EPOCH,
        completed = true,
        createJournalEntry = false
    )

    private val checkIn2 = CheckIn(
        id = 2L,
        traceLocationId = "41da2115-eba2-49bd-bf17-adb3d635ddaf".decodeBase64()!!,
        version = 1,
        type = 2,
        description = "brothers birthday",
        address = "Malibu",
        traceLocationStart = Instant.EPOCH,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.EPOCH,
        checkInEnd = Instant.EPOCH,
        completed = true,
        createJournalEntry = false
    )

    private val checkIn3 = CheckIn(
        id = 3L,
        traceLocationId = "41da2115-eba2-49bd-bf17-adb3d635ddaf".decodeBase64()!!,
        version = 1,
        type = 2,
        description = "brothers birthday",
        address = "Malibu",
        traceLocationStart = Instant.EPOCH,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.EPOCH,
        checkInEnd = Instant.EPOCH,
        completed = false,
        createJournalEntry = false
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf(checkIn1, checkIn2, checkIn3))
        coEvery { checkInRepository.updateSubmissionConsents(any(), true) } just Runs
        coEvery { checkInRepository.updateSubmissionConsents(any(), false) } just Runs
        coEvery { teksSharedViewModel.osTeks() } returns emptyList()
    }

    private fun createViewModel() = SrsSymptomsIntroductionViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        srsSubmissionRepository = srsSubmissionRepository,
        submissionType = submissionType,
        selectedCheckIns = selectedCheckIns,
        checkInRepository = checkInRepository,
        teksSharedViewModel = teksSharedViewModel
    )

    @Test
    fun `Choosing Yes goes to Symptoms calendar fragment`() {
        createViewModel().apply {
            onPositiveSymptomIndication()
            onNextClick()
            events.getOrAwaitValue() shouldBe SrsSymptomsIntroductionNavigation.GoToSymptomCalendar(
                submissionType,
                selectedCheckIns,
                Symptoms.Indication.POSITIVE
            )
        }
    }

    @Test
    fun `Choosing No opens the submission warning dialog, accepting leads to submission`() = runTest {
        createViewModel().apply {
            onNegativeSymptomIndication()
            onNextClick()
            events.getOrAwaitValue() shouldBe SrsSymptomsIntroductionNavigation.ShowSubmissionWarning
        }
    }

    @Test
    fun `Choosing No Answer opens the submission warning dialog`() {
        createViewModel().apply {
            onNoInformationSymptomIndication()
            onNextClick()
            events.value shouldBe SrsSymptomsIntroductionNavigation.ShowSubmissionWarning
        }
    }

    @Test
    fun `Accepting to submit leads to thank you screen`() = runTest {
        createViewModel().apply {
            onNegativeSymptomIndication()
            onWarningClicked()
            events.getOrAwaitValue() shouldBe SrsSymptomsIntroductionNavigation.GoToThankYouScreen(submissionType)

            coVerify {
                checkInRepository.updateSubmissionConsents(any(), true)
                srsSubmissionRepository.submit(
                    submissionType,
                    Symptoms(null, Symptoms.Indication.NEGATIVE),
                    emptyList()
                )
            }
        }
    }

    @Test
    fun `Closing the screen will show the close dialog`() {
        createViewModel().apply {
            onCancelConfirmed()
            events.getOrAwaitValue() shouldBe SrsSymptomsIntroductionNavigation.ShowCloseDialog
        }
    }

    @Test
    fun `Accepting to close the flow will lead to the home screen`() {
        createViewModel().apply {
            goHome()
            events.getOrAwaitValue() shouldBe SrsSymptomsIntroductionNavigation.GoToHome
        }
    }
}
