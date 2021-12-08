package de.rki.coronawarnapp.dccticketing.ui.consent.one

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneViewModel.UiState
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
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
import testhelpers.betterScrollTo
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class DccTicketingConsentOneFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: DccTicketingConsentOneViewModel

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.nav_graph)
            setCurrentDestination(R.id.dccTicketingConsentOneFragment)
        }
    }

    private val fragmentArgs = DccTicketingConsentOneFragmentArgs(
        transactionContextIdentifier = "transactionContextIdentifier"
    ).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : DccTicketingConsentOneViewModel.Factory {
                override fun create(
                    dccTicketingSharedViewModel: DccTicketingSharedViewModel,
                    qrcodeSharedViewModel: QrcodeSharedViewModel,
                    transactionContextIdentifier: String
                ): DccTicketingConsentOneViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Screenshot
    @Test
    fun dccTicketingConsentOneScreeshots() {
        every { viewModel.uiState } returns MutableLiveData(
            mockk<UiState>().apply {
                every { provider } returns "Anbietername"
                every { subject } returns "Buchungsbetreff"
            }
        )

        launchFragmentInContainer2<DccTicketingConsentOneFragment>(
            testNavHostController = navController,
            fragmentArgs = fragmentArgs
        )
        takeScreenshot<DccTicketingConsentOneFragment>("1")

        // Take legal part screenshot
        onView(ViewMatchers.withId(R.id.legal_second_bulletpoint_text)).perform(betterScrollTo())
        takeScreenshot<DccTicketingConsentOneFragment>("2")

        // Take description bullet point screenshot
        onView(ViewMatchers.withId(R.id.third_bulletpoint_text)).perform(betterScrollTo())
        takeScreenshot<DccTicketingConsentOneFragment>("3")

        // Take privacy information button screenshot
        onView(ViewMatchers.withId(R.id.privacy_information)).perform(betterScrollTo())
        takeScreenshot<DccTicketingConsentOneFragment>("4")
    }
}

@Module
abstract class DccTicketingConsentOneFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun dccTicketingConsentOneFragment(): DccTicketingConsentOneFragment
}
