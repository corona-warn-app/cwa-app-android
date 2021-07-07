package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
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
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class VaccinationDetailsFragmentTest : BaseUITest() {

    @MockK lateinit var vaccinationDetailsViewModel: VaccinationDetailsViewModel

    private val args = VaccinationDetailsFragmentArgs(
        containerId = VaccinationCertificateContainerId("vaccinationCertificateId")
    ).toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { vaccinationDetailsViewModel.qrCode } returns bitmapLiveDate()

        setupMockViewModel(
            object : VaccinationDetailsViewModel.Factory {
                override fun create(
                    containerId: VaccinationCertificateContainerId
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
        every { vaccinationDetailsViewModel.vaccinationCertificate } returns vaccinationDetailsData(true)
        launchFragmentInContainer2<VaccinationDetailsFragment>(fragmentArgs = args)
        takeScreenshot<VaccinationDetailsFragment>("immune")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<VaccinationDetailsFragment>("immune_2")
    }

    @Screenshot
    @Test
    fun capture_screenshot_incomplete() {
        every { vaccinationDetailsViewModel.vaccinationCertificate } returns vaccinationDetailsData(false)
        launchFragmentInContainer2<VaccinationDetailsFragment>(fragmentArgs = args)
        takeScreenshot<VaccinationDetailsFragment>("incomplete")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<VaccinationDetailsFragment>("incomplete_2")
    }

    private fun bitmapLiveDate(): LiveData<Bitmap?> {
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        return MutableLiveData(
            BitmapFactory.decodeResource(applicationContext.resources, R.drawable.test_qr_code)
        )
    }

    private fun vaccinationDetailsData(complete: Boolean): MutableLiveData<VaccinationDetails> {
        val formatter = DateTimeFormat.forPattern("dd.MM.yyyy")
        val mockCertificate = mockk<VaccinationCertificate>().apply {
            every { fullName } returns "Max Mustermann"
            every { dateOfBirthFormatted } returns "01.02.1976"
            every { vaccinatedOnFormatted } returns "18.02.2021"
            every { vaccinatedOn } returns LocalDate.parse("18.02.2021", formatter)
            every { targetDisease } returns "COVID-19"
            every { medicalProductName } returns "Comirnaty"
            every { vaccineTypeName } returns "mRNA"
            every { vaccineManufacturer } returns "BioNTech"
            every { certificateIssuer } returns "Landratsamt Musterstadt"
            every { certificateCountry } returns "Deutschland"
            every { certificateId } returns "05930482748454836478695764787841"
            every { headerExpiresAt } returns Instant.parse("2021-05-16T00:00:00.000Z")
            every { totalSeriesOfDoses } returns 2
            if (complete) every { doseNumber } returns 2 else every { doseNumber } returns 1
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
