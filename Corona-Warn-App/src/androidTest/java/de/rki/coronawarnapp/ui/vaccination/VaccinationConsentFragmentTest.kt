package de.rki.coronawarnapp.ui.vaccination

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.vaccination.ui.consent.VaccinationConsentFragment
import de.rki.coronawarnapp.vaccination.ui.consent.VaccinationConsentViewModel
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
class VaccinationConsentFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: VaccinationConsentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : VaccinationConsentViewModel.Factory {
                override fun create(): VaccinationConsentViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<VaccinationConsentFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<VaccinationConsentFragment>()
        takeScreenshot<VaccinationConsentFragment>()
    }
}

@Module
abstract class VaccinationConsentFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun vaccinationConsentFragment(): VaccinationConsentFragment
}
