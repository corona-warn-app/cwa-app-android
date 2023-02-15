package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingValidationResultFragment
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@HiltAndroidTest
class DccTicketingValidationResultFragmentTest : BaseUITest() {

    @MockK lateinit var mockSharedViewModel: DccTicketingSharedViewModel

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.nav_graph)
            setCurrentDestination(R.id.dccTicketingValidationResultFragment)
        }
    }

    @Test
    @Screenshot
    fun doPassScreenshot() {
        every { mockSharedViewModel.transactionContext } returns flowOf(dccTicketingTransactionContextPassed)
        launchFragmentInContainer2<DccTicketingValidationResultFragment>(testNavHostController = navController)
        takeScreenshot<DccTicketingValidationResultFragment>("pass")
    }

    @Test
    @Screenshot
    fun doOpenScreenshot() {
        every { mockSharedViewModel.transactionContext } returns flowOf(dccTicketingTransactionContextOpen)
        launchFragmentInContainer2<DccTicketingValidationResultFragment>(testNavHostController = navController)
        takeScreenshot<DccTicketingValidationResultFragment>("open")
    }

    @Test
    @Screenshot
    fun doFailScreenshot() {
        every { mockSharedViewModel.transactionContext } returns flowOf(dccTicketingTransactionContextFailed)
        launchFragmentInContainer2<DccTicketingValidationResultFragment>(testNavHostController = navController)
        takeScreenshot<DccTicketingValidationResultFragment>("fail")
    }
}
