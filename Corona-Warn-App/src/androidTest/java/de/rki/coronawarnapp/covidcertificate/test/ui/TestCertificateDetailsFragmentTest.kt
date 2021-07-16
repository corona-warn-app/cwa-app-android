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
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragmentArgs
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.DateTime
import org.joda.time.Instant
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
class TestCertificateDetailsFragmentTest : BaseUITest() {

    @MockK lateinit var vaccinationDetailsViewModel: TestCertificateDetailsViewModel
    @MockK lateinit var certificatePersonIdentifier: CertificatePersonIdentifier

    private val args = TestCertificateDetailsFragmentArgs(
        containerId = TestCertificateContainerId("testCertificateIdentifier")
    ).toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { vaccinationDetailsViewModel.qrCode } returns bitmapLiveDate()

        setupMockViewModel(
            object : TestCertificateDetailsViewModel.Factory {
                override fun create(containerId: TestCertificateContainerId):
                    TestCertificateDetailsViewModel = vaccinationDetailsViewModel
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<TestCertificateDetailsFragment>(fragmentArgs = args)
    }

    @Screenshot
    @Test
    fun capture_screenshot_incomplete() {
        every { vaccinationDetailsViewModel.covidCertificate } returns vaccinationDetailsData()
        launchFragmentInContainer2<TestCertificateDetailsFragment>(fragmentArgs = args)
        takeScreenshot<TestCertificateDetailsFragment>()
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<TestCertificateDetailsFragment>("_2")
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
                override val rawCertificate: TestDccV1
                    get() = mockk()

                override val containerId: TestCertificateContainerId
                    get() = TestCertificateContainerId("identifier")
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
                override val sampleCollectedAtFormatted: String
                    get() = "12.05.2021 19:00"
                override val testCenter: String
                    get() = "AB123"
                override val registeredAt: Instant
                    get() = testDate
                override val isUpdatingData: Boolean
                    get() = false
                override val isCertificateRetrievalPending: Boolean
                    get() = false
                override val headerIssuer: String
                    get() = "G0593048274845483647869576478784"
                override val headerIssuedAt: Instant
                    get() = testDate
                override val headerExpiresAt: Instant
                    get() = testDate
                override val qrCode: QrCodeString
                    get() = ""
                override val firstName: String
                    get() = "Andrea"
                override val lastName: String
                    get() = "Schneider"
                override val fullName: String
                    get() = "Schneider, Andrea"
                override val dateOfBirthFormatted: String
                    get() = "1943-04-18"
                override val personIdentifier: CertificatePersonIdentifier
                    get() = certificatePersonIdentifier
                override val certificateIssuer: String
                    get() = "G0593048274845483647869576478784"
                override val certificateCountry: String
                    get() = "Germany"
                override val certificateId: String
                    get() = "05930482748454836478695764787840"
                override val dccData: DccData<*>
                    get() = mockk()

                override fun getState(): CwaCovidCertificate.State = CwaCovidCertificate.State.Valid(Instant.now())
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
    abstract fun covidCertificateDetailsFragment(): TestCertificateDetailsFragment
}
