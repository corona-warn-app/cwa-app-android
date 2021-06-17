package de.rki.coronawarnapp.covidcertificate.test.ui

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
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.test.ui.details.CovidCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.details.CovidCertificateDetailsFragmentArgs
import de.rki.coronawarnapp.covidcertificate.test.ui.details.CovidCertificateDetailsViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.DateTime
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

    @MockK lateinit var vaccinationDetailsViewModel: CovidCertificateDetailsViewModel
    @MockK lateinit var certificatePersonIdentifier: CertificatePersonIdentifier

    private val args = CovidCertificateDetailsFragmentArgs("testCertificateIdentifier").toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { vaccinationDetailsViewModel.qrCode } returns bitmapLiveDate()

        setupMockViewModel(
            object : CovidCertificateDetailsViewModel.Factory {
                override fun create(testCertificateIdentifier: TestCertificateIdentifier):
                    CovidCertificateDetailsViewModel = vaccinationDetailsViewModel
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<CovidCertificateDetailsFragment>(fragmentArgs = args)
    }

    @Screenshot
    @Test
    fun capture_screenshot_incomplete() {
        every { vaccinationDetailsViewModel.covidCertificate } returns vaccinationDetailsData()
        launchFragmentInContainer2<CovidCertificateDetailsFragment>(fragmentArgs = args)
        takeScreenshot<CovidCertificateDetailsFragment>()
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<CovidCertificateDetailsFragment>("_2")
    }

    private fun bitmapLiveDate(): LiveData<Bitmap> {
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        return MutableLiveData(
            BitmapFactory.decodeResource(applicationContext.resources, R.drawable.test_qr_code)
        )
    }

    private fun vaccinationDetailsData(): MutableLiveData<TestCertificate> {
        val formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm")
        val testDate = DateTime.parse("12.05.2021 19:00", formatter).toInstant()
        return MutableLiveData(
            object : TestCertificate {
                override val targetName: String
                    get() = "Schneider, Andrea"
                override val testType: String
                    get() = "SARS-CoV-2-Test"
                override val testResult: String
                    get() = "negative"
                override val testName: String
                    get() = "Xep"
                override val testNameAndManufacturer: String
                    get() = "Xup"
                override val sampleCollectedAt: Instant
                    get() = testDate
                override val testCenter: String
                    get() = "AB123"
                override val registeredAt: Instant
                    get() = testDate
                override val isUpdatingData: Boolean
                    get() = false
                override val isCertificateRetrievalPending: Boolean
                    get() = false
                override val issuer: String
                    get() = "G0593048274845483647869576478784"
                override val issuedAt: Instant
                    get() = testDate
                override val expiresAt: Instant
                    get() = testDate
                override val qrCode: QrCodeString
                    get() = ""
                override val firstName: String
                    get() = "Andrea"
                override val lastName: String
                    get() = "Schneider"
                override val fullName: String
                    get() = "Schneider, Andrea"
                override val dateOfBirth: LocalDate
                    get() = LocalDate.parse("18.04.1943 00:00", formatter)
                override val personIdentifier: CertificatePersonIdentifier
                    get() = certificatePersonIdentifier
                override val certificateIssuer: String
                    get() = "G0593048274845483647869576478784"
                override val certificateCountry: String
                    get() = "Germany"
                override val certificateId: String
                    get() = "05930482748454836478695764787840"
            }
        )
    }

    @After
    fun tearDown() {
        clearAllViewModels()
    }
}

@Module
abstract class CovidCertificateDetailsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun covidCertificateDetailsFragment(): CovidCertificateDetailsFragment
}
