package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.createFakeImageLoaderForQrCodes
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.setupFakeImageLoader
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class VaccinationDetailsFragmentTest : BaseUITest() {

    @MockK lateinit var vaccinationDetailsViewModel: VaccinationDetailsViewModel

    private val args = VaccinationDetailsFragmentArgs(certIdentifier = "vaccinationCertificateId").toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        setupFakeImageLoader(
            createFakeImageLoaderForQrCodes()
        )
        setupMockViewModel(
            object : VaccinationDetailsViewModel.Factory {
                override fun create(
                    containerId: VaccinationCertificateContainerId,
                    fromScanner: Boolean
                ): VaccinationDetailsViewModel = vaccinationDetailsViewModel
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<VaccinationDetailsFragment>(fragmentArgs = args)
    }

    @Screenshot
    @Test
    fun capture_screenshot_immune() {
        every { vaccinationDetailsViewModel.vaccinationCertificate } returns validVaccinationDetailsData(true)
        launchFragmentInContainer2<VaccinationDetailsFragment>(fragmentArgs = args)
        takeScreenshot<VaccinationDetailsFragment>("immune")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<VaccinationDetailsFragment>("immune_2")
    }

    @Screenshot
    @Test
    fun capture_screenshot_incomplete() {
        every { vaccinationDetailsViewModel.vaccinationCertificate } returns validVaccinationDetailsData(false)
        launchFragmentInContainer2<VaccinationDetailsFragment>(fragmentArgs = args)
        takeScreenshot<VaccinationDetailsFragment>("incomplete")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<VaccinationDetailsFragment>("incomplete_2")
    }

    @Screenshot
    @Test
    fun capture_screenshot_invalid() {
        every { vaccinationDetailsViewModel.vaccinationCertificate } returns invalidVaccinationDetailsData()
        launchFragmentInContainer2<VaccinationDetailsFragment>(fragmentArgs = args)
        takeScreenshot<VaccinationDetailsFragment>("invalid")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<VaccinationDetailsFragment>("invalid_2")
    }

    @Screenshot
    @Test
    fun capture_screenshot_expired() {
        every { vaccinationDetailsViewModel.vaccinationCertificate } returns expiredVaccinationDetailsData()
        launchFragmentInContainer2<VaccinationDetailsFragment>(fragmentArgs = args)
        takeScreenshot<VaccinationDetailsFragment>("expired")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<VaccinationDetailsFragment>("expired_2")
    }

    private fun validVaccinationDetailsData(complete: Boolean): LiveData<VaccinationCertificate?> {
        val vaccinationCertificate = vaccinationCertificate().apply {
            if (complete) every { doseNumber } returns 2 else every { doseNumber } returns 1
            every { isDisplayValid } returns true
            every { state } returns CwaCovidCertificate.State.Valid(Instant.now().plus(21))
        }
        return MutableLiveData(vaccinationCertificate)
    }

    private fun invalidVaccinationDetailsData(): LiveData<VaccinationCertificate?> {
        val vaccinationCertificate = vaccinationCertificate().apply {
            every { doseNumber } returns 2
            every { isDisplayValid } returns false
            every { isNotScreened } returns true
            every { state } returns CwaCovidCertificate.State.Invalid()
        }
        return MutableLiveData(vaccinationCertificate)
    }

    private fun expiredVaccinationDetailsData(): LiveData<VaccinationCertificate?> {
        val vaccinationCertificate = vaccinationCertificate().apply {
            every { doseNumber } returns 2
            every { isDisplayValid } returns false
            every { isNotScreened } returns true
            every { state } returns CwaCovidCertificate.State.Expired(Instant.now())
        }
        return MutableLiveData(vaccinationCertificate)
    }

    private fun vaccinationCertificate(): VaccinationCertificate {
        val formatter = DateTimeFormat.forPattern("dd.MM.yyyy")
        return mockk<VaccinationCertificate>().apply {
            every { fullName } returns "Max Mustermann"
            every { fullNameStandardizedFormatted } returns "MUSTERMANN<<MAX"
            every { dateOfBirthFormatted } returns "1976-02-01"
            every { vaccinatedOnFormatted } returns "2021-02-18"
            every { vaccinatedOn } returns LocalDate.parse("18.02.2021", formatter)
            every { targetDisease } returns "COVID-19"
            every { medicalProductName } returns "Comirnaty"
            every { vaccineTypeName } returns "mRNA"
            every { vaccineManufacturer } returns "BioNTech"
            every { certificateIssuer } returns "Landratsamt Musterstadt"
            every { certificateCountry } returns "Deutschland"
            every { uniqueCertificateIdentifier } returns "URN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
            every { headerExpiresAt } returns Instant.parse("2021-05-16T00:00:00.000Z")
            every { totalSeriesOfDoses } returns 2
            every { hasNotificationBadge } returns false
            every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.vaccinationCertificate)
            every { fullNameFormatted } returns "Mustermann, Max"
            every { isSeriesCompletingShot } returns false
            every { isNotScreened } returns true
        }
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
