package de.rki.coronawarnapp.familytest.ui.selection

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
class TestRegistrationSelectionFragmentTest : BaseUITest() {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val request = CoronaTestQRCode.PCR(qrCodeGUID = "qrCodeGUID", rawQrCode = "rawQrCode")
    @MockK lateinit var viewModel: TestRegistrationSelectionViewModel

    private val fragmentArgs = TestRegistrationSelectionFragmentArgs(
        coronaTestQrCode = request
    ).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<TestRegistrationSelectionFragment>(fragmentArgs = fragmentArgs)
        takeScreenshot<TestRegistrationSelectionFragment>()
    }
}
