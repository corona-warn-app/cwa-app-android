package de.rki.coronawarnapp.srs.ui.symptoms.calnedar

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.core.repository.SrsSubmissionRepository
import de.rki.coronawarnapp.srs.ui.symptoms.calendar.SrsSymptomsCalendarNavigation
import de.rki.coronawarnapp.srs.ui.symptoms.calendar.SrsSymptomsCalendarViewModel
import de.rki.coronawarnapp.srs.ui.vm.TeksSharedViewModel
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.util.preferences.FlowPreference
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
import testhelpers.preferences.mockFlowPreference
import java.time.Instant

@ExtendWith(InstantExecutorExtension::class)
class SrsSymptomsCalendarViewModelTest : BaseTest() {
    @MockK lateinit var srsSubmissionRepository: SrsSubmissionRepository
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var submissionType: SrsSubmissionType
    @MockK lateinit var symptomsIndication: Symptoms.Indication
    @MockK lateinit var currentSymptoms: FlowPreference<Symptoms?>
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

        currentSymptoms = mockFlowPreference(null)

        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf(checkIn1, checkIn2, checkIn3))
        coEvery { checkInRepository.updateSubmissionConsents(any(), true) } just Runs
        coEvery { checkInRepository.updateSubmissionConsents(any(), false) } just Runs
        coEvery { teksSharedViewModel.osTeks() } returns emptyList()
    }

    private fun createViewModel() = SrsSymptomsCalendarViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        srsSubmissionRepository = srsSubmissionRepository,
        submissionType = submissionType,
        selectedCheckIns = selectedCheckIns,
        checkInRepository = checkInRepository,
        symptomsIndication = symptomsIndication,
        teksSharedViewModel = teksSharedViewModel
    )

    @Test
    fun `Submitting symptoms opens submission warning dialog`() {
        createViewModel().apply {
            onLastSevenDaysStart()
            onDone()
            events.getOrAwaitValue() shouldBe SrsSymptomsCalendarNavigation.ShowSubmissionWarning
        }
    }

    @Test
    fun `Accepting to submit leads to thank you screen`() = runTest {
        createViewModel().apply {
            onLastSevenDaysStart()
            startSubmission()
            events.getOrAwaitValue() shouldBe SrsSymptomsCalendarNavigation.GoToThankYouScreen(submissionType)

            coVerify {
                checkInRepository.updateSubmissionConsents(any(), true)
                srsSubmissionRepository.submit(
                    submissionType,
                    Symptoms(Symptoms.StartOf.LastSevenDays, symptomsIndication),
                    emptyList()
                )
            }
        }
    }

    @Test
    fun `Closing the screen will show the close dialog`() {
        createViewModel().apply {
            onCancelConfirmed()
            events.getOrAwaitValue() shouldBe SrsSymptomsCalendarNavigation.ShowCloseDialog
        }
    }

    @Test
    fun `Accepting to close the flow will lead to home fragment`() {
        createViewModel().apply {
            goHome()
            events.getOrAwaitValue() shouldBe SrsSymptomsCalendarNavigation.GoToHome
        }
    }
}
