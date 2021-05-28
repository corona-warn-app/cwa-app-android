package de.rki.coronawarnapp.ui.onboarding

import androidx.lifecycle.asLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flow
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
import testhelpers.launchFragment2
import testhelpers.launchInEmptyActivity
import testhelpers.takeScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class OnboardingAnalyticsFragmentTest : BaseUITest() {

    @MockK lateinit var settings: AnalyticsSettings
    @MockK lateinit var districts: Districts
    @MockK lateinit var analytics: Analytics

    private lateinit var viewModel: OnboardingAnalyticsViewModel

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { districts.loadDistricts() } returns listOf(
            Districts.District(
                districtId = 11011004,
                districtName = "SK Berlin Charlottenburg-Wilmersdorf"
            )
        )

        viewModel = onboardingAnalyticsViewModelSpy()
        with(viewModel) {
            every { ageGroup } returns flowOf(PpaData.PPAAgeGroup.AGE_GROUP_0_TO_29).asLiveData()
            every { federalState } returns flowOf(PpaData.PPAFederalState.FEDERAL_STATE_BE).asLiveData()
            every { district } returns flow { emit(districts.loadDistricts().first()) }.asLiveData()
        }

        setupMockViewModel(
            object : OnboardingAnalyticsViewModel.Factory {
                override fun create(): OnboardingAnalyticsViewModel = viewModel
            }
        )
    }

    private fun onboardingAnalyticsViewModelSpy() = spyk(
        OnboardingAnalyticsViewModel(
            appScope = GlobalScope,
            settings = settings,
            districts = districts,
            dispatcherProvider = TestDispatcherProvider(),
            analytics = analytics
        )
    )

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<OnboardingAnalyticsFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchInEmptyActivity<OnboardingAnalyticsFragment>()
        takeScreenshot<OnboardingAnalyticsFragment>()
    }
}

@Module
abstract class OnboardingAnalyticsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingAnalyticsFragment(): OnboardingAnalyticsFragment
}
