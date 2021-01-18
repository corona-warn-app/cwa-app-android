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
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingFragment
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingViewModel
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
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
class SubmissionTestResultFragmentTest : BaseUITest() {

    lateinit var viewModel: SubmissionTestResultPendingViewModel
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var shareTestResultNotificationService: ShareTestResultNotificationService

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { submissionRepository.deviceUIStateFlow } returns flowOf()
        every { submissionRepository.testResultReceivedDateFlow } returns flowOf()

        viewModel = spyk(
            SubmissionTestResultPendingViewModel(
                TestDispatcherProvider,
                shareTestResultNotificationService,
                submissionRepository
            )
        )

        with(viewModel) {
            every { observeTestResultToSchedulePositiveTestResultReminder() } just Runs
        }

        setupMockViewModel(object : SubmissionTestResultPendingViewModel.Factory {
            override fun create(): SubmissionTestResultPendingViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionTestResultPendingFragment>()
    }

    @Test
    fun testEventPendingRefreshClicked() {
        val scenario = launchFragmentInContainer<SubmissionTestResultPendingFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.submission_test_result_button_pending_refresh))
            .perform(ViewActions.scrollTo())
            .perform(ViewActions.click())

        // TODO verify result
    }

    @Test
    fun testEventPendingRemoveClicked() {
        val scenario = launchFragmentInContainer<SubmissionTestResultPendingFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.submission_test_result_button_pending_remove_test))
            .perform(ViewActions.scrollTo())
            .perform(ViewActions.click())

        // TODO verify result
    }

    @Test
    fun testEventInvalidRemoveClicked() {
        val scenario = launchFragmentInContainer<SubmissionTestResultPendingFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.submission_test_result_button_invalid_remove_test))
            .perform(ViewActions.scrollTo())
            .perform(ViewActions.click())

        // TODO verify result
    }

    @Test
    fun testEventNegativeRemoveClicked() {
        val scenario = launchFragmentInContainer<SubmissionTestResultPendingFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.submission_test_result_button_negative_remove_test))
            .perform(ViewActions.scrollTo())
            .perform(ViewActions.click())

        // TODO verify result
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        every { viewModel.testState } returns MutableLiveData(
            TestResultUIState(
                NetworkRequestWrapper.RequestSuccessful(
                    DeviceUIState.PAIRED_NO_RESULT
                ), Date()
            )
        )
        captureScreenshot<SubmissionTestResultPendingFragment>()
    }
}

@Module
abstract class SubmissionTestResultTestModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultPendingFragment
}
