package de.rki.coronawarnapp.ui.submission.submissiondone

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import io.mockk.MockKAnnotations
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class SubmissionDoneFragmentTest : BaseUITest() {
    private val testType = BaseCoronaTest.Type.RAPID_ANTIGEN

    private lateinit var viewModel: SubmissionDoneViewModel
    private val fragmentArgs = SubmissionDoneFragmentArgs(
        testType
    ).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = SubmissionDoneViewModel(
            testType
        )
        setupMockViewModel(
            object : SubmissionDoneViewModel.Factory {
                override fun create(
                    testType: BaseCoronaTest.Type
                ): SubmissionDoneViewModel = viewModel
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
        launchFragmentInContainer2<SubmissionDoneFragment>(fragmentArgs = fragmentArgs)
        takeScreenshot<SubmissionDoneFragment>()
    }
}

@Module
abstract class SubmissionDoneFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun submissionDoneFragmentTestScreen(): SubmissionDoneFragment
}
