package de.rki.coronawarnapp.ui.submission.testavailable

import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableFragmentDirections
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableViewModel
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class SubmissionTestResultAvailableViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var tekHistoryUpdater: TEKHistoryUpdater
    @MockK lateinit var tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { submissionRepository.hasGivenConsentToSubmission } returns flowOf(true)

        every { tekHistoryUpdaterFactory.create(any()) } returns tekHistoryUpdater
        every { tekHistoryUpdater.updateTEKHistoryOrRequestPermission() } just Runs

        // TODO Check specific behavior
        every { submissionRepository.refreshDeviceUIState(any()) } just Runs
    }

    private fun createViewModel(): SubmissionTestResultAvailableViewModel = SubmissionTestResultAvailableViewModel(
        submissionRepository = submissionRepository,
        dispatcherProvider = TestDispatcherProvider,
        tekHistoryUpdaterFactory = tekHistoryUpdaterFactory,
        autoSubmission = autoSubmission
    )

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `consent repository changed`() {
        val consentMutable = MutableStateFlow(false)
        every { submissionRepository.hasGivenConsentToSubmission } returns consentMutable

        val viewModel = createViewModel()

        viewModel.consent.observeForever { }
        viewModel.consent.value shouldBe false

        consentMutable.value = true
        viewModel.consent.value shouldBe true
    }

    @Test
    fun `go back`() {
        val viewModel = createViewModel()

        viewModel.showCloseDialog.value shouldBe null
        viewModel.goBack()
        viewModel.showCloseDialog.value shouldBe Unit
    }

    @Test
    fun `go to your consent page`() {
        val viewModel = createViewModel()

        viewModel.goConsent()
        viewModel.routeToScreen.value shouldBe SubmissionTestResultAvailableFragmentDirections
            .actionSubmissionTestResultAvailableFragmentToSubmissionYourConsentFragment(true)
    }

    @Test
    fun `update TEK history if consent is given`() {
        val viewModel = createViewModel()

        viewModel.proceed()
        verify {
            tekHistoryUpdater.updateTEKHistoryOrRequestPermission()
        }
    }

    @Test
    fun `go to test result without updating TEK history if NO consent is given`() {
        every { submissionRepository.hasGivenConsentToSubmission } returns flowOf(false)
        val viewModel = createViewModel()

        viewModel.proceed()
        viewModel.routeToScreen.value shouldBe SubmissionTestResultAvailableFragmentDirections
            .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultNoConsentFragment()
    }
}
