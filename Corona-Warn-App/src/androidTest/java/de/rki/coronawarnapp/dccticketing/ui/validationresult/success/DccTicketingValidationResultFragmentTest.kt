package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingValidationResultFragment
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingValidationResultFragmentArgs
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingValidationResultViewModel
import de.rki.coronawarnapp.dccticketing.ui.validationresult.ValidationResultItemCreator
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

    @Before
    fun setUp() {
        setupMockViewModel(
            object : DccTicketingValidationResultViewModel.Factory {
                override fun create(transactionContext: DccTicketingTransactionContext) =
                    DccTicketingValidationResultViewModel(
                        transactionContext,
                        ValidationResultItemCreator(),
                        TestDispatcherProvider()
                    )
            }
        )
    }

    @Test
    @Screenshot
    fun doPassScreenshot() {
        launchFragmentInContainer2<DccTicketingValidationResultFragment>(
            DccTicketingValidationResultFragmentArgs(
                dccTicketingTransactionContextPassed
            ).toBundle()
        )
        takeScreenshot<DccTicketingValidationResultFragment>("pass")
    }

    @Test
    @Screenshot
    fun doOpenScreenshot() {
        launchFragmentInContainer2<DccTicketingValidationResultFragment>(
            DccTicketingValidationResultFragmentArgs(
                dccTicketingTransactionContextOpen
            ).toBundle()
        )
        takeScreenshot<DccTicketingValidationResultFragment>("open")
    }

    @Test
    @Screenshot
    fun doFailScreenshot() {
        launchFragmentInContainer2<DccTicketingValidationResultFragment>(
            DccTicketingValidationResultFragmentArgs(
                dccTicketingTransactionContextFailed
            ).toBundle()
        )
        takeScreenshot<DccTicketingValidationResultFragment>("fail")
    }
}

@Module
abstract class DccTicketingValidationSuccessFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun dccTicketingValidationSuccessFragment(): DccTicketingValidationResultFragment
}
