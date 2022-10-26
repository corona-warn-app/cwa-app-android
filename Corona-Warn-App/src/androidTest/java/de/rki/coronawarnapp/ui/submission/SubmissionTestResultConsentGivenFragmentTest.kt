package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.testing.launchFragmentInContainer
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
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenFragment
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenFragmentArgs
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultConsentGivenFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var testResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var testType: BaseCoronaTest.Type
    private val consentGivenFragmentArgs =
        SubmissionTestResultConsentGivenFragmentArgs(testType = BaseCoronaTest.Type.PCR).toBundle()

    private lateinit var viewModel: SubmissionTestResultConsentGivenViewModel

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        runOnUiThread {
            setGraph(R.navigation.nav_graph)
            setCurrentDestination(R.id.submissionTestResultConsentGivenFragment)
        }
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
                    testType,
                    TestDispatcherProvider()
                )
            )
        setupMockViewModel(
            object : SubmissionTestResultConsentGivenViewModel.Factory {
                override fun create(testType: BaseCoronaTest.Type): SubmissionTestResultConsentGivenViewModel =
                    viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<SubmissionTestResultConsentGivenFragment>(consentGivenFragmentArgs)
    }

    @Test
    fun testEventConsentGivenContinueWithSymptomsClicked() {
        launchFragmentInContainer<SubmissionTestResultConsentGivenFragment>(
            themeResId = R.style.AppTheme_Main,
            fragmentArgs = consentGivenFragmentArgs
        ).onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        // Verify that performing a click prompts the correct Navigation action
        onView(withId(R.id.submission_test_result_button_consent_given_continue)).perform(click())
    }

    @Test
    @Screenshot
    fun capture_fragment_for_personal_test() {
        every { viewModel.uiState } returns MutableLiveData(
            TestResultUIState(
                coronaTest = mockk<PersonalCoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.PCR_POSITIVE
                    every { registeredAt } returns Instant.now()
                    every { type } returns BaseCoronaTest.Type.PCR
                }
            )
        )

        launchFragmentInContainer2<SubmissionTestResultConsentGivenFragment>(fragmentArgs = consentGivenFragmentArgs)
        takeScreenshot<SubmissionTestResultConsentGivenFragment>()
    }

    @Test
    @Screenshot
    fun capture_fragment_for_family_test() {
        every { viewModel.uiState } returns MutableLiveData(
            TestResultUIState(
                coronaTest = mockk<FamilyCoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.PCR_POSITIVE
                    every { registeredAt } returns Instant.now()
                    every { type } returns BaseCoronaTest.Type.PCR
                    every { identifier } returns ""
                    every { personName } returns "Lara"
                    every { coronaTest } returns CoronaTest(
                        identifier = identifier,
                        type = type,
                        registeredAt = registeredAt,
                        registrationToken = ""
                    )
                }
            )
        )

        launchFragmentInContainer2<SubmissionTestResultConsentGivenFragment>(fragmentArgs = consentGivenFragmentArgs)
        takeScreenshot<SubmissionTestResultConsentGivenFragment>()
    }
}

@Module
abstract class SubmissionTestResultConsentGivenTestModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultConsentGivenFragment
}
