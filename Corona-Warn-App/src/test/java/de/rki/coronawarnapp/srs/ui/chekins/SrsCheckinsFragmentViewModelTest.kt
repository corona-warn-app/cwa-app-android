package de.rki.coronawarnapp.srs.ui.chekins

import androidx.lifecycle.SavedStateHandle
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.ui.checkins.SrsCheckinsFragmentViewModel
import de.rki.coronawarnapp.srs.ui.checkins.SrsCheckinsNavigation
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent.HeaderCheckInsVH
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent.SelectableCheckInVH
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import java.time.Instant

@ExtendWith(InstantExecutorExtension::class)
open class SrsCheckinsFragmentViewModelTest {

    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var savedState: SavedStateHandle
    @MockK lateinit var submissionType: SrsSubmissionType

    private val selectedFlow = MutableStateFlow<Set<Long>>(emptySet())

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
        MockKAnnotations.init(this)

        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf(checkIn1, checkIn2, checkIn3))
        coEvery { checkInRepository.updateSubmissionConsents(any(), true) } just Runs
        coEvery { checkInRepository.updateSubmissionConsents(any(), false) } just Runs
        every { savedState[any()] = any<Set<Long>>() } just Runs
        every { savedState.get<Set<Long>>(any()) } returns emptySet()
    }

    @Test
    fun `Nothing is selected initially`() {
        every { savedState.get<Set<Long>>(any()) } returns emptySet()

        val viewModel = createViewModel()
        viewModel.checkIns.getOrAwaitValue().apply {
            size shouldBe 3
            get(0).apply {
                this is HeaderCheckInsVH.Item
            }

            get(1).apply {
                this as SelectableCheckInVH.Item
                this.checkIn.hasSubmissionConsent shouldBe false
            }

            get(2).apply {
                this as SelectableCheckInVH.Item
                this.checkIn.hasSubmissionConsent shouldBe false
            }
        }
    }

    @Test
    fun `Saved state is restored`() {
        every { savedState.get<Set<Long>>(any()) } returns setOf(1L)
        val viewModel = createViewModel()
        viewModel.checkIns.getOrAwaitValue().apply {
            size shouldBe 3
            get(0).apply {
                this is HeaderCheckInsVH.Item
            }

            get(1).apply {
                this as SelectableCheckInVH.Item
                this.checkIn.hasSubmissionConsent shouldBe true
            }

            get(2).apply {
                this as SelectableCheckInVH.Item
                this.checkIn.hasSubmissionConsent shouldBe false
            }
        }
    }

    @Test
    fun `Select all`() {
        every { savedState.get<Set<Long>>(any()) } returns emptySet()
        val viewModel = createViewModel()
        viewModel.checkIns.getOrAwaitValue().apply {
            size shouldBe 3
            get(0).apply {
                this as HeaderCheckInsVH.Item
                (get(1) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe false
                (get(2) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe false

                this.selectAll()

                viewModel.checkIns.getOrAwaitValue().apply {
                    (get(1) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe true
                    (get(2) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe true
                }
            }
        }
    }

    @Test
    fun `Select all does not un-select all`() {
        every { savedState.get<Set<Long>>(any()) } returns emptySet()
        val viewModel = createViewModel()
        viewModel.checkIns.getOrAwaitValue().apply {
            size shouldBe 3
            get(0).apply {
                this as HeaderCheckInsVH.Item
                (get(1) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe false
                (get(2) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe false

                this.selectAll()

                viewModel.checkIns.getOrAwaitValue().apply {
                    (get(1) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe true
                    (get(2) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe true
                }

                this.selectAll()

                viewModel.checkIns.getOrAwaitValue().apply {
                    (get(1) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe true
                    (get(2) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe true
                }
            }
        }
    }

    @Test
    fun `Single selection`() {
        every { savedState.get<Set<Long>>(any()) } returns emptySet()
        val viewModel = createViewModel()
        viewModel.checkIns.getOrAwaitValue().apply {

            (get(1) as SelectableCheckInVH.Item).apply {
                checkIn.hasSubmissionConsent shouldBe false
                onItemSelected(checkIn)
            }

            viewModel.checkIns.getOrAwaitValue().apply {
                (get(1) as SelectableCheckInVH.Item).checkIn.hasSubmissionConsent shouldBe true
            }
        }
    }

    @Test
    fun `Single deselection`() {
        every { savedState.get<Set<Long>>(any()) } returns emptySet()
        val viewModel = createViewModel()
        viewModel.checkIns.getOrAwaitValue().apply {
            (get(1) as SelectableCheckInVH.Item).apply {
                checkIn.hasSubmissionConsent shouldBe false
                onItemSelected(checkIn)
            }

            viewModel.checkIns.getOrAwaitValue().apply {
                (get(1) as SelectableCheckInVH.Item).apply {
                    checkIn.hasSubmissionConsent shouldBe true
                    onItemSelected(checkIn)
                }
            }

            viewModel.checkIns.getOrAwaitValue().apply {
                (get(1) as SelectableCheckInVH.Item).apply {
                    checkIn.hasSubmissionConsent shouldBe false
                }
            }
        }
    }

    @Test
    fun `Confirming cancel goes to home screen`() {
        createViewModel().apply {
            goHome()
            events.getOrAwaitValue() shouldBe SrsCheckinsNavigation.GoToHome
        }
    }

    @Test
    fun `Skip opens skipDialog`() {
        createViewModel().apply {
            onSkipClick()
            events.getOrAwaitValue() shouldBe SrsCheckinsNavigation.ShowSkipDialog
        }
    }

    @Test
    fun `Close opens closeDialog `() {
        createViewModel().apply {
            onCloseClick()
            events.getOrAwaitValue() shouldBe SrsCheckinsNavigation.ShowCloseDialog
        }
    }

    @Test
    fun `Pass selected checkins to symptom intro screen when submit button is clicked`() {
        createViewModel().apply {
            onNextClick()
            events.getOrAwaitValue() shouldBe SrsCheckinsNavigation.GoToSymptomSubmission(
                submissionType,
                selectedFlow.value.toLongArray()
            )
        }
    }

    private fun createViewModel() = SrsCheckinsFragmentViewModel(
        savedState = savedState,
        dispatcherProvider = TestDispatcherProvider(),
        checkInRepository = checkInRepository,
        submissionType = submissionType
    )
}
