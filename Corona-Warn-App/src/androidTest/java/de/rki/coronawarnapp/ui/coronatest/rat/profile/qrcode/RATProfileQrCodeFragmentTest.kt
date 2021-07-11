package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

class RATProfileQrCodeFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: RATProfileQrCodeFragmentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : RATProfileQrCodeFragmentViewModel.Factory {
                override fun create(): RATProfileQrCodeFragmentViewModel = viewModel
            }
        )

        viewModel.apply {
            every { events } returns SingleLiveEvent()
            every { profile } returns MutableLiveData(
                PersonProfile(
                    profile = RATProfile(
                        firstName = "Max",
                        lastName = "Mustermann",
                        birthDate = LocalDate(1990, 11, 17),
                        city = "Potsdam",
                        zipCode = "14471",
                        street = "Lange Straße 5",
                        phone = "0151123456789",
                        email = "maxmustermann@web.de"
                    ),
                    bitmap = BitmapFactory.decodeResource(
                        ApplicationProvider.getApplicationContext<Context>().resources,
                        R.drawable.test_qr_code
                    )
                )
            )
        }
    }

    @Test
    fun launch_fragment() {
        launchFragment2<RATProfileQrCodeFragment>()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<RATProfileQrCodeFragment>()
        takeScreenshot<RATProfileQrCodeFragment>()
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }
}

@Module
abstract class RATProfileQrCodeFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun ratProfileQrCodeFragment(): RATProfileQrCodeFragment
}
