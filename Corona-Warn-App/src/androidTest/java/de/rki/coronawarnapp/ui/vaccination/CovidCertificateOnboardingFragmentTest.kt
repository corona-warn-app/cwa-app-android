package de.rki.coronawarnapp.ui.vaccination

import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.ui.onboarding.CovidCertificateOnboardingFragment
import de.rki.coronawarnapp.covidcertificate.ui.onboarding.CovidCertificateOnboardingFragmentArgs
import de.rki.coronawarnapp.covidcertificate.ui.onboarding.CovidCertificateOnboardingViewModel
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class CovidCertificateOnboardingFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: CovidCertificateOnboardingViewModel

    private val fragmentArgs = CovidCertificateOnboardingFragmentArgs(showBottomNav = false).toBundle()

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
    fun launch_fragment() {
        launchFragmentInContainer2<CovidCertificateOnboardingFragment>(
            fragmentArgs = fragmentArgs,
            testNavHostController = navController
        )
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<CovidCertificateOnboardingFragment>(
            fragmentArgs = fragmentArgs,
            testNavHostController = navController
        )
        takeScreenshot<CovidCertificateOnboardingFragment>()
    }
}
