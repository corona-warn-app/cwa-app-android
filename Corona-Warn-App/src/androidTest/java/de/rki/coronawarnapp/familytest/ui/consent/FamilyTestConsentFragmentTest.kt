package de.rki.coronawarnapp.familytest.ui.consent

import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@HiltAndroidTest
class FamilyTestConsentFragmentTest : BaseUITest() {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val request = CoronaTestQRCode.PCR(qrCodeGUID = "qrCodeGUID", rawQrCode = "rawQrCode")
    @MockK lateinit var viewModel: FamilyTestConsentViewModel

    private val fragmentArgs = FamilyTestConsentFragmentArgs(
        coronaTestQrCode = request
    ).toBundle()

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.nav_graph)
            setCurrentDestination(R.id.familyTestConsentFragment)
        }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<FamilyTestConsentFragment>(
            fragmentArgs = fragmentArgs,
            testNavHostController = navController
        )
        takeScreenshot<FamilyTestConsentFragment>("no_data")

        onView(withId(R.id.name_input_edit))
            .perform(click())
            .perform(typeText("Lara"), closeSoftKeyboard())

        onView(withId(R.id.scrollview)).perform(ViewActions.swipeDown())

        takeScreenshot<FamilyTestConsentFragment>("with_data")
    }
}
