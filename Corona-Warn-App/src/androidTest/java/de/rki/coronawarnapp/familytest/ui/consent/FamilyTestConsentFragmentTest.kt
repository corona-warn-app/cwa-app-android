package de.rki.coronawarnapp.familytest.ui.consent

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.censors.family.FamilyTestCensor
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
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
class FamilyTestConsentFragmentTest : BaseUITest() {

    private val request = CoronaTestQRCode.PCR(qrCodeGUID = "qrCodeGUID", rawQrCode = "rawQrCode")
    private lateinit var viewModel: FamilyTestConsentViewModel
    @MockK lateinit var familyTestCensor: FamilyTestCensor

    private val fragmentArgs = FamilyTestConsentFragmentArgs(
        coronaTestQrCode = request
    ).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = FamilyTestConsentViewModel(
            TestDispatcherProvider(),
            request,
            familyTestCensor
        )
        setupMockViewModel(
            object : FamilyTestConsentViewModel.Factory {
                override fun create(
                    coronaTestQRCode: CoronaTestQRCode
                ): FamilyTestConsentViewModel = viewModel
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
        launchFragmentInContainer2<FamilyTestConsentFragment>(fragmentArgs = fragmentArgs)
        takeScreenshot<FamilyTestConsentFragment>("no_data")

        onView(withId(R.id.name_input_edit))
            .perform(click())
            .perform(typeText("Lara"), closeSoftKeyboard())

        onView(withId(R.id.scrollview)).perform(ViewActions.swipeDown())

        takeScreenshot<FamilyTestConsentFragment>("with_data")
    }
}

@Module
abstract class FamilyTestConsentFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun familyTestConsentScreen(): FamilyTestConsentFragment
}
