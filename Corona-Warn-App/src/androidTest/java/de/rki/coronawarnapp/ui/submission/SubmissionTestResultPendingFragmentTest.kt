package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.assisted.Assisted
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingFragment
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingFragmentArgs
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultPendingFragmentTest : BaseUITest() {

    private val submissionTestResultPendingFragmentArgs = SubmissionTestResultPendingFragmentArgs(testIdentifier = "").toBundle()

    @MockK lateinit var viewModel: SubmissionTestResultPendingViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : SubmissionTestResultPendingViewModel.Factory {
                override fun create(
                    testIdentifier: TestIdentifier,
                    @Assisted("initialUpdate") initialUpdate: Boolean,
                    @Assisted("comesFromDispatcherFragment") comesFromDispatcherFragment: Boolean
                ): SubmissionTestResultPendingViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        every { viewModel.testState } returns MutableLiveData(
            TestResultUIState(
                coronaTest = mockk<PersonalCoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.RAT_PENDING
                    every { registeredAt } returns Instant.now()
                    every { identifier } returns ""
                    every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
                    every { isProcessing } returns false
                }
            )
        )

        launchFragmentInContainer2<SubmissionTestResultPendingFragment>(
            fragmentArgs = submissionTestResultPendingFragmentArgs
        )
        takeScreenshot<SubmissionTestResultPendingFragment>()
    }
}

@Module
abstract class SubmissionTestResultPendingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultPendingScreen(): SubmissionTestResultPendingFragment
}
