package de.rki.coronawarnapp.covidcertificate.recovery.ui

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
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragmentArgs
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsViewModel
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragment
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
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
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

        every { recoveryDetailsViewModel.qrCode } returns bitmapLiveDate()

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

    private fun bitmapLiveDate(): LiveData<Bitmap> {
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        return MutableLiveData(
            BitmapFactory.decodeResource(applicationContext.resources, R.drawable.test_qr_code)
        )
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
