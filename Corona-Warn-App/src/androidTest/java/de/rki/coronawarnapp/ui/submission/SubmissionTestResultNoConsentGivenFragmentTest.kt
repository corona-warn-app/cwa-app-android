package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenFragment
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultNoConsentFragment
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultNoConsentViewModel
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.util.Date

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultNoConsentGivenFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var dispatcherProvider: DispatcherProvider

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private lateinit var viewModel: SubmissionTestResultNoConsentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { dispatcherProvider.Default } returns Dispatchers.Default
        viewModel =
            spyk(SubmissionTestResultNoConsentViewModel(submissionRepository))
        setupMockViewModel(object : SubmissionTestResultNoConsentViewModel.Factory {
            override fun create(): SubmissionTestResultNoConsentViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionTestResultNoConsentFragment>()
    }

    @Test
    fun testEventConsentGivenContinueWithSymptomsClicked() {

        val mockNavController = mockk<NavController>()
        val scenario = launchFragmentInContainer<SubmissionTestResultConsentGivenFragment>()

        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }
        // Verify that performing a click prompts the correct Navigation action
        onView(ViewMatchers.withId(R.id.submission_test_result_button_consent_given_continue)).perform(ViewActions.click())
        verify {
            mockNavController.navigate(R.id.action_submissionTestResultConsentGivenFragment_to_submissionSymptomIntroductionFragment)
        }
    }

    @Test
    @Screenshot
    fun capture_fragment_paired_positive() {
        every { viewModel.uiState } returns MutableLiveData(
            TestResultUIState(
                NetworkRequestWrapper.RequestSuccessful(
                    DeviceUIState.PAIRED_POSITIVE
                ), Date()
            )
        )

        captureScreenshot<SubmissionTestResultNoConsentFragment>()
    }
}

@Module
abstract class SubmissionTestResultNoConsentModel {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultNoConsentFragment
}
