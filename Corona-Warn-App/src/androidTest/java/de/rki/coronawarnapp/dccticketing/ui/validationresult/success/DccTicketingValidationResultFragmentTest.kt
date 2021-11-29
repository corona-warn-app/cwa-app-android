package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingValidationResultFragment
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingValidationResultFragmentArgs
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingValidationResultViewModel
import de.rki.coronawarnapp.dccticketing.ui.validationresult.ValidationResultItemCreator
import io.mockk.MockKAnnotations
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
class DccTicketingValidationResultFragmentTest : BaseUITest() {

    @MockK lateinit var resultViewModel: DccTicketingValidationResultViewModel
    private val fragmentArgs = DccTicketingValidationResultFragmentArgs(transactionContextSampleData).toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : DccTicketingValidationResultViewModel.Factory {
                override fun create(transactionContext: DccTicketingTransactionContext) = DccTicketingValidationResultViewModel(
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
        launchFragmentInContainer2<DccTicketingValidationResultFragment>(fragmentArgs)
        takeScreenshot<DccTicketingValidationResultFragment>("success")
    }

    @Test
    @Screenshot
    fun doScreenshotScroll() {
        launchFragmentInContainer2<DccTicketingValidationResultFragment>(fragmentArgs)
        Espresso.onView(ViewMatchers.withId(R.id.list)).perform(recyclerScrollTo(4))
        takeScreenshot<DccTicketingValidationResultFragment>("success_scroll")
    }
}

@Module
abstract class DccTicketingValidationSuccessFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun dccTicketingValidationSuccessFragment(): DccTicketingValidationResultFragment
}

