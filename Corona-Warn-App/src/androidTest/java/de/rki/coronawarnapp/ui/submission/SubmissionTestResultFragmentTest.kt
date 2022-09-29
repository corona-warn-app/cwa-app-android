package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingFragment
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingFragmentArgs
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
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
class SubmissionTestResultFragmentTest : BaseUITest() {

    lateinit var viewModel: SubmissionTestResultPendingViewModel
    @MockK lateinit var recycledTestProvider: RecycledCoronaTestsProvider
    @MockK lateinit var coronaTestProvider: CoronaTestProvider

    private val pendingFragmentArgs =
        SubmissionTestResultPendingFragmentArgs(testIdentifier = "").toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { coronaTestProvider.getTestForIdentifier(any()) } returns flowOf()

        viewModel = spyk(
            SubmissionTestResultPendingViewModel(
                TestDispatcherProvider(),
                recycledTestProvider = recycledTestProvider,
                initialUpdate = false,
                testIdentifier = "",
                comesFromDispatcherFragment = false,
                coronaTestProvider = coronaTestProvider
            )
        )

        with(viewModel) {
            every { consentGiven } returns MutableLiveData(true)
            every { testState } returns MutableLiveData(
                TestResultUIState(
                    coronaTest = mockk<PersonalCoronaTest>().apply {
                        every { testResult } returns CoronaTestResult.PCR_POSITIVE
                        every { registeredAt } returns Instant.now()
                        every { isProcessing } returns false
                        every { type } returns BaseCoronaTest.Type.PCR
                    }
                )
            )
        }

        setupMockViewModel(
            object : SubmissionTestResultPendingViewModel.Factory {
                override fun create(
                    testIdentifier: TestIdentifier,
                    initialUpdate: Boolean,
                    comesFromDispatcherFragment: Boolean
                ): SubmissionTestResultPendingViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<SubmissionTestResultPendingFragment>(pendingFragmentArgs)
    }

    @Test
    fun testEventPendingRefreshClicked() {
        launchFragmentInContainer2<SubmissionTestResultPendingFragment>(pendingFragmentArgs)
        onView(withId(R.id.submission_test_result_button_pending_refresh))
            .perform(click())
    }

    @Test
    fun testEventPendingRemoveClicked() {
        launchFragmentInContainer2<SubmissionTestResultPendingFragment>(pendingFragmentArgs)
        onView(withId(R.id.submission_test_result_button_pending_remove_test))
            .perform(click())
    }

    @Test
    @Screenshot
    fun capture_fragment_for_personal_test() {
        every { viewModel.testState } returns MutableLiveData(
            TestResultUIState(
                coronaTest = mockk<PersonalCoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.PCR_OR_RAT_PENDING
                    every { registeredAt } returns Instant.now()
                    every { isProcessing } returns false
                    every { type } returns BaseCoronaTest.Type.PCR
                }
            )
        )
        launchFragmentInContainer2<SubmissionTestResultPendingFragment>(fragmentArgs = pendingFragmentArgs)
        takeScreenshot<SubmissionTestResultPendingFragment>()
    }

    @Test
    @Screenshot
    fun capture_fragment_for_family_test() {
        every { viewModel.testState } returns MutableLiveData(
            TestResultUIState(
                coronaTest = mockk<FamilyCoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.PCR_OR_RAT_PENDING
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
        launchFragmentInContainer2<SubmissionTestResultPendingFragment>(fragmentArgs = pendingFragmentArgs)
        takeScreenshot<SubmissionTestResultPendingFragment>()
    }
}

@Module
abstract class SubmissionTestResultTestModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultPendingFragment
}
