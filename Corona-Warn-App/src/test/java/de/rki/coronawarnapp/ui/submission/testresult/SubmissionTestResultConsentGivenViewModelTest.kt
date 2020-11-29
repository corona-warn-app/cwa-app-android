package de.rki.coronawarnapp.ui.submission.testresult

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.extensions.InstantExecutorExtension

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultConsentGivenViewModelTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionTestResultConsentGivenViewModel
    @MockK lateinit var uiState: TestResultUIState

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { viewModel.uiState } returns MutableLiveData()

        setupMockViewModel(object : SubmissionTestResultConsentGivenViewModel.Factory {
            override fun create(): SubmissionTestResultConsentGivenViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun testOnConsentProvideSymptomsButtonClick() {
        viewModel.onContinuePressed()
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToSymptomIntroduction

    }

    @Test
    fun testOnCancelled() {
        viewModel.cancelTestSubmission()
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToMainActivity
    }

}
