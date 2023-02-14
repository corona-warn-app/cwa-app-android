package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingValidationResultFragment
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingValidationResultViewModel
import de.rki.coronawarnapp.dccticketing.ui.validationresult.ValidationResultItemCreator
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
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
class DccTicketingValidationResultFragmentTest : BaseUITest() {

    @MockK lateinit var mockSharedViewModel: DccTicketingSharedViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        setupMockViewModel(
            object : DccTicketingValidationResultViewModel.Factory {
                override fun create(dccTicketingSharedViewModel: DccTicketingSharedViewModel) =
                    DccTicketingValidationResultViewModel(
                        mockSharedViewModel,
                        ValidationResultItemCreator(),
                        TestDispatcherProvider()
                    )
            }
        )
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
