package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
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
import io.mockk.spyk
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
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.util.Date

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultConsentGivenFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var testResultAvailableNotificationService: TestResultAvailableNotificationService
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private lateinit var viewModel: SubmissionTestResultConsentGivenViewModel

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        runOnUiThread { setGraph(R.navigation.nav_graph) }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel =
            spyk(
                SubmissionTestResultConsentGivenViewModel(
                    submissionRepository,
                    autoSubmission,
                    testResultAvailableNotificationService,
                    analyticsKeySubmissionCollector,
                    TestDispatcherProvider()
                )
            )
        setupMockViewModel(
            object : SubmissionTestResultConsentGivenViewModel.Factory {
                override fun create(): SubmissionTestResultConsentGivenViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<SubmissionTestResultConsentGivenFragment>()
    }

    @Test
    fun testEventConsentGivenContinueWithSymptomsClicked() {
        launchFragmentInContainer2<SubmissionTestResultConsentGivenFragment>().onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        // Verify that performing a click prompts the correct Navigation action
        onView(withId(R.id.submission_test_result_button_consent_given_continue)).perform(click())
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        every { viewModel.uiState } returns MutableLiveData(
            TestResultUIState(
                NetworkRequestWrapper.RequestSuccessful(
                    DeviceUIState.PAIRED_POSITIVE
                ),
                Date()
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
