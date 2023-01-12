package de.rki.coronawarnapp.ui.vaccination

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.ui.onboarding.CovidCertificateOnboardingFragment
import de.rki.coronawarnapp.covidcertificate.ui.onboarding.CovidCertificateOnboardingFragmentArgs
import de.rki.coronawarnapp.covidcertificate.ui.onboarding.CovidCertificateOnboardingViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class CovidCertificateOnboardingFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: CovidCertificateOnboardingViewModel

    private val fragmentArgs = CovidCertificateOnboardingFragmentArgs(showBottomNav = false).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : CovidCertificateOnboardingViewModel.Factory {
                override fun create(
                    certIdentifier: String?
                ): CovidCertificateOnboardingViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<CovidCertificateOnboardingFragment>(fragmentArgs)
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<CovidCertificateOnboardingFragment>(fragmentArgs)
        takeScreenshot<CovidCertificateOnboardingFragment>()
    }
}

@Module
abstract class CovidCertificateInfoFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun covidCertificateInfoFragment(): CovidCertificateOnboardingFragment
}
