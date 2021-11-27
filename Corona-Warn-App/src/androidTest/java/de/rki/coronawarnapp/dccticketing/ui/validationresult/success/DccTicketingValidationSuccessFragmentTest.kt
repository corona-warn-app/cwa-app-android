package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.ValidationResultItemCreator
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.recyclerScrollTo
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class DccTicketingValidationSuccessFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: DccTicketingValidationViewModel
    private val fragmentArgs = DccTicketingValidationSuccessFragmentArgs(transactionContextSampleData).toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : DccTicketingValidationViewModel.Factory {
                override fun create(transactionContext: DccTicketingTransactionContext) = DccTicketingValidationViewModel(
                    transactionContext,
                    ValidationResultItemCreator(),
                    TestDispatcherProvider()
                )
            }
        )
    }

    @Test
    @Screenshot
    fun doScreenshot() {
        launchFragmentInContainer2<DccTicketingValidationSuccessFragment>(fragmentArgs)
        takeScreenshot<DccTicketingValidationSuccessFragment>("success")
    }

    @Test
    @Screenshot
    fun doScreenshotScroll() {
        launchFragmentInContainer2<DccTicketingValidationSuccessFragment>(fragmentArgs)
        Espresso.onView(ViewMatchers.withId(R.id.list)).perform(recyclerScrollTo(4))
        takeScreenshot<DccTicketingValidationSuccessFragment>("success_scroll")
    }
}

@Module
abstract class DccTicketingValidationSuccessFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun dccTicketingValidationSuccessFragment(): DccTicketingValidationSuccessFragment
}

