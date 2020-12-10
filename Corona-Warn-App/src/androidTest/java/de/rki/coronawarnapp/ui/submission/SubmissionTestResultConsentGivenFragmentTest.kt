package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenFragment
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultConsentGivenFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionTestResultConsentGivenViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
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

        val mockNavController = mock(NavController::class.java)
        val scenario = launchFragmentInContainer<SubmissionTestResultConsentGivenFragment>()

        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }
        // Verify that performing a click prompts the correct Navigation action
        onView(ViewMatchers.withId(R.id.submission_test_result_button_consent_given_continue)).perform(ViewActions.click())
        verify(mockNavController).navigate(R.id.action_submissionTestResultConsentGivenFragment_to_submissionSymptomIntroductionFragment)
    }
}

@Module
abstract class SubmissionTestResultConsentGivenTestModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultConsentGivenViewModel
}
