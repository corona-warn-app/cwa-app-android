package de.rki.coronawarnapp.vaccination.ui.details

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class VaccinationDetailsFragmentTest : BaseUITest() {

    @MockK lateinit var vaccinationDetailsViewModel: VaccinationDetailsViewModel

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private val args = VaccinationDetailsFragmentArgs("vaccinationCertificateId").toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : VaccinationDetailsViewModel.Factory {
                override fun create(certificateId: String): VaccinationDetailsViewModel = vaccinationDetailsViewModel
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<VaccinationDetailsFragment>(fragmentArgs = args)
    }

    @Screenshot
    @Test
    fun capture_screenshot_complete() {
        every { vaccinationDetailsViewModel.vaccinationCertificate } returns vaccinationDetailsData(true)
        launchFragmentInContainer2<VaccinationDetailsFragment>(fragmentArgs = args)
        takeScreenshot<VaccinationDetailsFragment>("complete")
    }

    @Screenshot
    @Test
    fun capture_screenshot_incomplete() {
        every { vaccinationDetailsViewModel.vaccinationCertificate } returns vaccinationDetailsData(false)
        launchFragmentInContainer2<VaccinationDetailsFragment>(fragmentArgs = args)
        takeScreenshot<VaccinationDetailsFragment>("incomplete")
    }

    private fun vaccinationDetailsData(complete: Boolean): MutableLiveData<VaccinationDetails> {
        val mockCertificate = mockk<VaccinationCertificate>().apply {
            every { firstName } returns "Max"
            every { lastName } returns "Mustermann"
            every { dateOfBirth } returns LocalDate.now()
            every { vaccinatedAt } returns LocalDate.now()
            every { vaccineName } returns "Comirnaty (mRNA)"
            every { vaccineManufacturer } returns "BioNTech"
            every { certificateIssuer } returns "Landratsamt Potsdam"
            every { certificateCountry } returns Country.DE
            every { certificateId } returns "05930482748454836478695764787841"
        }

        return MutableLiveData(
            VaccinationDetails(mockCertificate, complete)
        )
    }

    @After
    fun tearDown() {
        clearAllViewModels()
    }
}

@Module
abstract class VaccinationDetailsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun vaccinationDetailsFragment(): VaccinationDetailsFragment
}
