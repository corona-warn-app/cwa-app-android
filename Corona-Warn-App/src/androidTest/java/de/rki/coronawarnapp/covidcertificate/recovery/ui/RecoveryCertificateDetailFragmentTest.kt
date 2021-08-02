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

    private val args = RecoveryCertificateDetailsFragmentArgs(
        containerId = RecoveryCertificateContainerId("recoveryCertificateId")
    ).toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        setupFakeImageLoader(
            createFakeImageLoaderForQrCodes()
        )
        setupMockViewModel(
            object : RecoveryCertificateDetailsViewModel.Factory {
                override fun create(
                    containerId: RecoveryCertificateContainerId
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
        every { recoveryDetailsViewModel.recoveryCertificate } returns mockCertificate()
        launchFragmentInContainer2<RecoveryCertificateDetailsFragment>(fragmentArgs = args)
        takeScreenshot<RecoveryCertificateDetailsFragment>("recovered")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<RecoveryCertificateDetailsFragment>("recovered_2")
    }

    private fun mockCertificate(): MutableLiveData<RecoveryCertificate> {
        val mockCertificate = mockk<RecoveryCertificate>().apply {
            every { fullName } returns "Max Mustermann"
            every { dateOfBirthFormatted } returns "1969-01-08"
            every { targetDisease } returns "COVID-19"
            every { testedPositiveOnFormatted } returns "2021-05-24"
            every { certificateCountry } returns "Deutschland"
            every { certificateIssuer } returns "Robert-Koch-Institut"
            every { validFromFormatted } returns "2021-06-07"
            every { validUntilFormatted } returns "2021-11-10"
            every { certificateId } returns "05930482748454836478695764787841"
            every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.recoveryCertificate)
            every { isValid } returns true
            every { validUntil } returns
                LocalDate.parse("2021-11-10", DateTimeFormat.forPattern("yyyy-MM-dd"))

            every { getState() } returns CwaCovidCertificate.State.Valid(Instant.now().plus(21))

            every { fullNameFormatted } returns "Max, Mustermann"
            every { headerExpiresAt } returns Instant.now().plus(21)
        }

        return MutableLiveData(mockCertificate)
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
