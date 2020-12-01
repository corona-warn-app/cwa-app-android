package de.rki.coronawarnapp.ui.submission.testavailable

import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableEvents
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableViewModel
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
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

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { submissionRepository.hasGivenConsentToSubmission } returns flowOf(true)
    }

    private fun createViewModel(): SubmissionTestResultAvailableViewModel = SubmissionTestResultAvailableViewModel(
        submissionRepository = submissionRepository,
        dispatcherProvider = TestDispatcherProvider
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

        viewModel.goBack()
        viewModel.clickEvent.value shouldBe SubmissionTestResultAvailableEvents.GoBack
    }

    @Test
    fun `go to your consent page`() {
        val viewModel = createViewModel()

        viewModel.goConsent()
        viewModel.clickEvent.value shouldBe SubmissionTestResultAvailableEvents.GoConsent
    }

    @Test
    fun `go to next page`() {
        val viewModel = createViewModel()

        viewModel.proceed()
        viewModel.clickEvent.value shouldBe SubmissionTestResultAvailableEvents.Proceed
    }
}
