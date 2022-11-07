package de.rki.coronawarnapp.srs.ui.consent

import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragment
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class SrsSubmissionConsentFragmentTest : BaseUITest() {

    private val srsSubmissionType: SrsSubmissionType = SrsSubmissionType.SRS_UNREGISTERED_PCR
    private lateinit var viewModel: SrsSubmissionConsentFragmentViewModel

    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory
    @MockK lateinit var enfClient: ENFClient

    private val fragmentArgs = SrsSubmissionConsentFragmentArgs(
        srsSubmissionType = srsSubmissionType,
        unregisteredTest = true
    ).toBundle()

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

        viewModel = SrsSubmissionConsentFragmentViewModel(
            srsSubmissionType,
            true,
            checkInRepository,
            enfClient,
            TestDispatcherProvider(),
            tekHistoryUpdaterFactory
        )
        setupMockViewModel(
            object : SrsSubmissionConsentFragmentViewModel.Factory {
                override fun create(
                    srsSubmissionType: SrsSubmissionType,
                    inAppResult: Boolean
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

        onView(withId(R.id.content_scrollcontainer)).perform(swipeUp())
        takeScreenshot<ContactDiaryOverviewFragment>("2")
    }
}

@Module
abstract class SrsSubmissionConsentFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun srsSubmissionConsentScreen(): SrsSubmissionConsentFragment
}
