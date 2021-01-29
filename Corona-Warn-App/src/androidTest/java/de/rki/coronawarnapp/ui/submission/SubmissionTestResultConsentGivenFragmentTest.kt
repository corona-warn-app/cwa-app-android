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
import de.rki.coronawarnapp.notification.TestResultAvailableNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenFragment
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenViewModel
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.captureScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.util.Date

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultConsentGivenFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var testResultAvailableNotificationService: TestResultAvailableNotificationService

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private lateinit var viewModel: SubmissionTestResultConsentGivenViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel =
            spyk(
                SubmissionTestResultConsentGivenViewModel(
                    submissionRepository,
                    autoSubmission,
                    testResultAvailableNotificationService,
                    TestDispatcherProvider
                )
            )
        setupMockViewModel(object : SubmissionTestResultConsentGivenViewModel.Factory {
            override fun create(): SubmissionTestResultConsentGivenViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionTestResultConsentGivenFragment>()
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
    fun capture_fragment() {
        every { viewModel.uiState } returns MutableLiveData(
            TestResultUIState(
                NetworkRequestWrapper.RequestSuccessful(
                    DeviceUIState.PAIRED_POSITIVE
                ), Date()
            )
        )

        captureScreenshot<SubmissionTestResultConsentGivenFragment>()
    }
}

@Module
abstract class SubmissionTestResultConsentGivenTestModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultConsentGivenFragment
}
