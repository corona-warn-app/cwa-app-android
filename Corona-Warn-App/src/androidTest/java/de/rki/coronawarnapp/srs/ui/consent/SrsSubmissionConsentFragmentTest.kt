package de.rki.coronawarnapp.srs.ui.consent

import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.srs.ui.vm.TeksSharedViewModel
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class SrsSubmissionConsentFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SrsSubmissionConsentFragmentViewModel

    private val fragmentArgs = SrsSubmissionConsentFragmentArgs().toBundle()

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.srs_nav_graph)
            setCurrentDestination(R.id.srsSubmissionConsentFragment)
        }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel.apply {
            every { showKeysRetrievalProgress } returns SingleLiveEvent()
            every { showTracingConsentDialog } returns SingleLiveEvent()
            every { showPermissionRequest } returns SingleLiveEvent()
            every { event } returns SingleLiveEvent()
        }

        setupMockViewModel(
            object : SrsSubmissionConsentFragmentViewModel.Factory {
                override fun create(
                    openTypeSelection: Boolean,
                    teksSharedViewModel: TeksSharedViewModel
                ): SrsSubmissionConsentFragmentViewModel = viewModel
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
        launchFragmentInContainer2<SrsSubmissionConsentFragment>(
            fragmentArgs = fragmentArgs,
            testNavHostController = navController
        )
        takeScreenshot<SrsSubmissionConsentFragment>("1")

        onView(withId(R.id.first_bulletpoint)).perform(scrollTo())
        takeScreenshot<SrsSubmissionConsentFragment>("2")

        onView(withId(R.id.third_bulletpoint)).perform(scrollTo())
        takeScreenshot<SrsSubmissionConsentFragment>("3")

        onView(withId(R.id.srs_submission_consent_more_info)).perform(scrollTo())
        takeScreenshot<SrsSubmissionConsentFragment>("4")
    }
}

@Module
abstract class SrsSubmissionConsentFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun srsSubmissionConsentScreen(): SrsSubmissionConsentFragment
}
