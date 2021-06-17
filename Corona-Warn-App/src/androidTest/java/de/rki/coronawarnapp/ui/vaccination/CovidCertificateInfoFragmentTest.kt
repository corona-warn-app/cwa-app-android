package de.rki.coronawarnapp.ui.vaccination

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.ui.info.CovidCertificateInfoFragment
import de.rki.coronawarnapp.covidcertificate.ui.info.CovidCertificateInfoFragmentArgs
import de.rki.coronawarnapp.covidcertificate.ui.info.CovidCertificateInfoViewModel
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
class CovidCertificateInfoFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: CovidCertificateInfoViewModel

    private val fragmentArgs = CovidCertificateInfoFragmentArgs(
        showBottomNav = false
    ).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : CovidCertificateInfoViewModel.Factory {
                override fun create(): CovidCertificateInfoViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<CovidCertificateInfoFragment>(fragmentArgs)
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<CovidCertificateInfoFragment>(fragmentArgs)
        takeScreenshot<CovidCertificateInfoFragment>()
    }
}

@Module
abstract class CovidCertificateInfoFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun covidCertificateInfoFragment(): CovidCertificateInfoFragment
}
