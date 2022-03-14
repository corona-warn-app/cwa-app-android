package de.rki.coronawarnapp.covidcertificate.recovery.ui

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
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragmentArgs
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsViewModel
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
class RecoveryCertificateDetailFragmentTest : BaseUITest() {

    @MockK lateinit var recoveryDetailsViewModel: RecoveryCertificateDetailsViewModel

    private val args = RecoveryCertificateDetailsFragmentArgs(certIdentifier = "recoveryCertificateId").toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        setupFakeImageLoader(
            createFakeImageLoaderForQrCodes()
        )
        setupMockViewModel(
            object : RecoveryCertificateDetailsViewModel.Factory {
                override fun create(
                    containerId: RecoveryCertificateContainerId,
                    fromScanner: Boolean
                ): RecoveryCertificateDetailsViewModel = recoveryDetailsViewModel
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<RecoveryCertificateDetailsFragment>(fragmentArgs = args)
    }

    @Screenshot
    @Test
    fun capture_screenshot_recovered() {
        every { recoveryDetailsViewModel.recoveryCertificate } returns validMockCertificate()
        launchFragmentInContainer2<RecoveryCertificateDetailsFragment>(fragmentArgs = args)
        takeScreenshot<RecoveryCertificateDetailsFragment>("recovered")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<RecoveryCertificateDetailsFragment>("recovered_2")
    }

    @Screenshot
    @Test
    fun capture_screenshot_recovered_expired() {
        every { recoveryDetailsViewModel.recoveryCertificate } returns expiredMockCertificate()
        launchFragmentInContainer2<RecoveryCertificateDetailsFragment>(fragmentArgs = args)
        takeScreenshot<RecoveryCertificateDetailsFragment>("recovered_expired")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<RecoveryCertificateDetailsFragment>("recovered_expired_2")
    }

    @Screenshot
    @Test
    fun capture_screenshot_recovered_invalid() {
        every { recoveryDetailsViewModel.recoveryCertificate } returns invalidMockCertificate()
        launchFragmentInContainer2<RecoveryCertificateDetailsFragment>(fragmentArgs = args)
        takeScreenshot<RecoveryCertificateDetailsFragment>("recovered_invalid")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<RecoveryCertificateDetailsFragment>("recovered_invalid_2")
    }

    private fun validMockCertificate() = MutableLiveData(
        mockCertificate().apply {
            every { isDisplayValid } returns true
            every { isNotBlocked } returns true
            every { getState() } returns CwaCovidCertificate.State.Valid(Instant.now().plus(21))
        }
    )

    private fun invalidMockCertificate() = MutableLiveData(
        mockCertificate().apply {
            every { isDisplayValid } returns false
            every { isNotBlocked } returns true
            every { getState() } returns CwaCovidCertificate.State.Invalid()
        }
    )

    private fun expiredMockCertificate() = MutableLiveData(
        mockCertificate().apply {
            every { isDisplayValid } returns false
            every { isNotBlocked } returns true
            every { getState() } returns CwaCovidCertificate.State.Expired(Instant.now())
        }
    )

    private fun mockCertificate() = mockk<RecoveryCertificate>().apply {
        every { fullName } returns "Max Mustermann"
        every { fullNameStandardizedFormatted } returns "MUSTERMANN<<MAX"
        every { dateOfBirthFormatted } returns "1969-01-08"
        every { targetDisease } returns "COVID-19"
        every { testedPositiveOnFormatted } returns "2021-05-24"
        every { certificateCountry } returns "Deutschland"
        every { certificateIssuer } returns "Robert Koch-Institut"
        every { hasNotificationBadge } returns false
        every { uniqueCertificateIdentifier } returns "URN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
        every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.recoveryCertificate)
        every { validUntil } returns
            LocalDate.parse("2021-11-10", DateTimeFormat.forPattern("yyyy-MM-dd"))

        every { fullNameFormatted } returns "Mustermann, Max"
        every { headerExpiresAt } returns Instant.now().plus(21)
        every { isNotBlocked } returns true
    }

    @After
    fun tearDown() {
        clearAllViewModels()
    }
}

@Module
abstract class RecoveryCertificateDetailsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun recoveryCertificateDetailsFragment(): RecoveryCertificateDetailsFragment
}
