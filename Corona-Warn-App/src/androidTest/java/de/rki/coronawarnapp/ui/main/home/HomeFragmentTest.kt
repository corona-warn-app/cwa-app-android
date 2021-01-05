package de.rki.coronawarnapp.ui.main.home

import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.submission.ui.homecards.NoTest
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.ui.homecards.LowRiskCard
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState
import de.rki.coronawarnapp.ui.main.home.items.DiaryCard
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.joda.time.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: HomeFragmentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { viewModel.tracingHeaderState } returns MutableLiveData()
        every { viewModel.homeItems } returns MutableLiveData(emptyList())
        every { viewModel.refreshRequiredData() } just Runs

        setupMockViewModel(object : HomeFragmentViewModel.Factory {
            override fun create(): HomeFragmentViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun onResumeCallsRefresh() {
        // AppTheme is required here to prevent xml inflation crash
        launchFragment<HomeFragment>(themeResId = R.style.AppTheme)
        verify(exactly = 1) { viewModel.refreshRequiredData() }
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        val headerState = MutableLiveData<TracingHeaderState>(TracingHeaderState.TracingActive)
        val lowRiskItem = LowRiskCard.Item(
            state = LowRisk(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = true,
                lastExposureDetectionTime = Instant.now(),
                allowManualUpdate = true,
                daysWithEncounters = 1,
                activeTracingDays = 1
            ),
            onCardClick = {},
            onUpdateClick = {}
        )

        val homeItems = MutableLiveData(
            listOf(
                lowRiskItem,
                DiaryCard.Item(onClickAction = { }),
                FAQCard.Item(onClickAction = { })
            )
        )

        every { viewModel.tracingHeaderState } returns headerState
        every { viewModel.homeItems } returns homeItems
        launchFragmentInContainer2<HomeFragment>()

        Thread.sleep(5000)
    }
}

@Module
abstract class HomeFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun homeScreen(): HomeFragment
}
