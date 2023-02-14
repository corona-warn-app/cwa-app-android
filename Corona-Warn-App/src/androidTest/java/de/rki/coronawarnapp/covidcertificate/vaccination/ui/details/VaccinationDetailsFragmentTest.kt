package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class VaccinationDetailsFragmentTest : BaseUITest() {

    @MockK lateinit var vaccinationDetailsViewModel: VaccinationDetailsViewModel

    private val args = VaccinationDetailsFragmentArgs(
        certIdentifier = "vaccinationCertificateId",
        colorShade = PersonColorShade.COLOR_1
    ).toBundle()

    private val vaccinationDateFormatted = "2021-02-18"
    private val vaccinationDate: LocalDate = LocalDate.parse(
        "18.02.2021",
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    )
    private val expirationDate: Instant = LocalDateTime.parse(
        "18.02.2022 15:00",
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    ).atZone(ZoneId.systemDefault()).toInstant()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        setupFakeImageLoader(
            createFakeImageLoaderForQrCodes()
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
            every { state } returns CwaCovidCertificate.State.Valid(expirationDate)
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
            every { state } returns CwaCovidCertificate.State.Expired(expirationDate)
        }
        return MutableLiveData(vaccinationCertificate)
    }

    private fun vaccinationCertificate(): VaccinationCertificate {

        return mockk<VaccinationCertificate>().apply {
            every { fullName } returns "Max Mustermann"
            every { fullNameStandardizedFormatted } returns "MUSTERMANN<<MAX"
            every { dateOfBirthFormatted } returns "1976-02-01"
            every { vaccinatedOnFormatted } returns vaccinationDateFormatted
            every { vaccinatedOn } returns vaccinationDate
            every { targetDisease } returns "COVID-19"
            every { medicalProductName } returns "Comirnaty"
            every { vaccineTypeName } returns "mRNA"
            every { vaccineManufacturer } returns "BioNTech"
            every { certificateIssuer } returns "Landratsamt Musterstadt"
            every { certificateCountry } returns "Deutschland"
            every { uniqueCertificateIdentifier } returns "URN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
            every { headerExpiresAt } returns expirationDate
            every { totalSeriesOfDoses } returns 2
            every { hasNotificationBadge } returns false
            every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.vaccinationCertificate)
            every { fullNameFormatted } returns "Mustermann, Max"
            every { isSeriesCompletingShot } returns false
            every { isNotScreened } returns true
        }
    }


}
