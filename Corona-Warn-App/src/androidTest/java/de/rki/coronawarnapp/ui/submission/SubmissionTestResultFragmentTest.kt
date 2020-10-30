package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.testresult.SubmissionTestResultFragment
import de.rki.coronawarnapp.ui.submission.testresult.SubmissionTestResultViewModel
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionTestResultViewModel
    @MockK lateinit var uiState: TestResultUIState

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { viewModel.uiState } returns MutableLiveData()

        setupMockViewModel(object : SubmissionTestResultViewModel.Factory {
            override fun create(): SubmissionTestResultViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionTestResultFragment>()
    }

    @Test
    fun testEventPendingRefreshClicked() {
        val scenario = launchFragmentInContainer<SubmissionTestResultFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.submission_test_result_button_pending_refresh))
            .perform(ViewActions.scrollTo())
            .perform(ViewActions.click())

        // TODO verify result
    }

    @Test
    fun testEventPendingRemoveClicked() {
        val scenario = launchFragmentInContainer<SubmissionTestResultFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.submission_test_result_button_pending_remove_test))
            .perform(ViewActions.scrollTo())
            .perform(ViewActions.click())

        // TODO verify result
    }

    @Test
    fun testEventInvalidRemoveClicked() {
        val scenario = launchFragmentInContainer<SubmissionTestResultFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.submission_test_result_button_invalid_remove_test))
            .perform(ViewActions.scrollTo())
            .perform(ViewActions.click())

        // TODO verify result
    }

    @Test
    fun testEventPositiveContinueWithSymptomsClicked() {
        val scenario = launchFragmentInContainer<SubmissionTestResultFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.submission_test_result_button_positive_continue))
            .perform(ViewActions.scrollTo())
            .perform(ViewActions.click())

        // TODO verify result
    }

    @Test
    fun testEventPositiveContinueWithoutSymptomsClicked() {
        val scenario = launchFragmentInContainer<SubmissionTestResultFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.submission_test_result_button_positive_continue_without_symptoms))
            .perform(ViewActions.scrollTo())
            .perform(ViewActions.click())

        // TODO verify result
    }

    @Test
    fun testEventNegativeRemoveClicked() {
        val scenario = launchFragmentInContainer<SubmissionTestResultFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.submission_test_result_button_negative_remove_test))
            .perform(ViewActions.scrollTo())
            .perform(ViewActions.click())

        // TODO verify result
    }
}

@Module
abstract class SubmissionTestResultTestModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultFragment
}
