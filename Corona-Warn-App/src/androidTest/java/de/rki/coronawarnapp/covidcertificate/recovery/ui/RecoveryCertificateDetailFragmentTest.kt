package de.rki.coronawarnapp.covidcertificate.recovery.ui

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragmentArgs
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsViewModel
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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

@HiltAndroidTest
class RecoveryCertificateDetailFragmentTest : BaseUITest() {

    @MockK lateinit var recoveryDetailsViewModel: RecoveryCertificateDetailsViewModel

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val args = RecoveryCertificateDetailsFragmentArgs(certIdentifier = "recoveryCertificateId").toBundle()

    private val testDateFormatted = "2021-05-24"
    private val expirationDate: Instant = LocalDateTime.parse(
        "24.05.2022 15:00",
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
            every { isNotScreened } returns true
            every { state } returns CwaCovidCertificate.State.Valid(expirationDate)
        }
    )

    private fun invalidMockCertificate() = MutableLiveData(
        mockCertificate().apply {
            every { isDisplayValid } returns false
            every { isNotScreened } returns true
            every { state } returns CwaCovidCertificate.State.Invalid()
        }
    )

    private fun expiredMockCertificate() = MutableLiveData(
        mockCertificate().apply {
            every { isDisplayValid } returns false
            every { isNotScreened } returns true
            every { state } returns CwaCovidCertificate.State.Expired(expirationDate)
        }
    )

    private fun mockCertificate() = mockk<RecoveryCertificate>().apply {
        every { fullName } returns "Max Mustermann"
        every { fullNameStandardizedFormatted } returns "MUSTERMANN<<MAX"
        every { dateOfBirthFormatted } returns "1969-01-08"
        every { targetDisease } returns "COVID-19"
        every { testedPositiveOnFormatted } returns testDateFormatted
        every { certificateCountry } returns "Deutschland"
        every { certificateIssuer } returns "Robert Koch-Institut"
        every { hasNotificationBadge } returns false
        every { uniqueCertificateIdentifier } returns "URN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
        every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.recoveryCertificate)
        every { validUntil } returns
            LocalDate.parse("2021-11-10", DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        every { fullNameFormatted } returns "Mustermann, Max"
        every { headerExpiresAt } returns expirationDate
        every { isNotScreened } returns true
    }
}
