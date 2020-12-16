package de.rki.coronawarnapp.ui.submission.testresult

import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenViewModel
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionTestResultConsentGivenViewModelTest : BaseTest() {
    @MockK
    lateinit var submissionRepository: SubmissionRepository
    lateinit var viewModel: SubmissionTestResultConsentGivenViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    private fun createViewModel() = SubmissionTestResultConsentGivenViewModel(
        submissionRepository = submissionRepository,
        dispatcherProvider = TestDispatcherProvider
    )

    @Test
    fun testOnConsentProvideSymptomsButtonClick() {
        viewModel = createViewModel()
        viewModel.onContinuePressed()
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToSymptomIntroduction
    }

    @Test
    fun testOnCancelled() {
        viewModel = createViewModel()
        viewModel.cancelTestSubmission()
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToMainActivity
    }
}
