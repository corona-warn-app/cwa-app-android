package de.rki.coronawarnapp.ui.submission.covidcertificate

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class RequestCovidCertificateFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: RequestCovidCertificateViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { viewModel.birthDate } returns MutableLiveData(null)
        every { viewModel.registrationState } returns MutableLiveData(
            TestRegistrationStateProcessor.State.Idle
        )

        setupMockViewModel(
            object : RequestCovidCertificateViewModel.Factory {
                override fun create(
                    testRegistrationRequest: TestRegistrationRequest,
                    coronaTestConsent: Boolean,
                    allowTestReplacement: Boolean,
                    personName: String?
                ): RequestCovidCertificateViewModel = viewModel
            }
        )
    }

    @Test
    fun launch_fragment_pcr() {
        val args = RequestCovidCertificateFragmentArgs(
            CoronaTestQRCode.PCR(qrCodeGUID = "GUID", rawQrCode = "rawQrCode")
        ).toBundle()
        launchFragmentInContainer2<RequestCovidCertificateFragment>(fragmentArgs = args)
    }

    @Test
    fun launch_fragment_rat() {
        val args = RequestCovidCertificateFragmentArgs(
            CoronaTestQRCode.RapidAntigen(hash = "hash", createdAt = Instant.EPOCH, rawQrCode = "rawQrCode")
        ).toBundle()
        launchFragmentInContainer2<RequestCovidCertificateFragment>(fragmentArgs = args)
    }

    @Test
    @Screenshot
    fun capture_fragment_pcr() {
        val args = RequestCovidCertificateFragmentArgs(
            CoronaTestQRCode.PCR(qrCodeGUID = "GUID", rawQrCode = "rawQrCode")
        ).toBundle()
        launchFragmentInContainer2<RequestCovidCertificateFragment>(fragmentArgs = args)
        takeScreenshot<RequestCovidCertificateFragment>("pcr")

        onView(withId(R.id.scrollview)).perform(swipeUp())
        takeScreenshot<RequestCovidCertificateFragment>("pcr_2")
    }

    @Test
    @Screenshot
    fun capture_fragment_pcr_birthdate_dialog() {
        val args = RequestCovidCertificateFragmentArgs(
            CoronaTestQRCode.PCR(qrCodeGUID = "GUID", rawQrCode = "rawQrCode")
        ).toBundle()
        launchFragmentInContainer2<RequestCovidCertificateFragment>(fragmentArgs = args)
        onView(withId(R.id.date_input_edit)).perform(click())
        takeScreenshot<RequestCovidCertificateFragment>("date_picker")
    }

    @Test
    @Screenshot
    fun capture_fragment_rat() {
        val args = RequestCovidCertificateFragmentArgs(
            CoronaTestQRCode.RapidAntigen(hash = "hash", createdAt = Instant.EPOCH, rawQrCode = "rawQrCode")
        ).toBundle()
        launchFragmentInContainer2<RequestCovidCertificateFragment>(fragmentArgs = args)
        takeScreenshot<RequestCovidCertificateFragment>("rat")

        onView(withId(R.id.scrollview)).perform(swipeUp())
        takeScreenshot<RequestCovidCertificateFragment>("rat_2")
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }
}

@Module
abstract class RequestCovidCertificateFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun requestCovidCertificateFragment(): RequestCovidCertificateFragment
}
