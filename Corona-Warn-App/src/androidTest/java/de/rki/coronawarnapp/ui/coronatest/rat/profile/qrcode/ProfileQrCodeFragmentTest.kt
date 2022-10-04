package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.ui.create.ProfileCreateFragmentArgs
import de.rki.coronawarnapp.profile.ui.qrcode.PersonProfile
import de.rki.coronawarnapp.profile.ui.qrcode.ProfileQrCodeFragment
import de.rki.coronawarnapp.profile.ui.qrcode.ProfileQrCodeFragmentViewModel
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

class ProfileQrCodeFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: ProfileQrCodeFragmentViewModel

    private val args = ProfileCreateFragmentArgs(id = 1).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : ProfileQrCodeFragmentViewModel.Factory {
                override fun create(profileId: Int): ProfileQrCodeFragmentViewModel = viewModel
            }
        )

        viewModel.apply {
            every { events } returns SingleLiveEvent()
            every { personProfile } returns MutableLiveData(
                PersonProfile(
                    profile = Profile(
                        firstName = "Max",
                        lastName = "Mustermann",
                        birthDate = LocalDate.of(1990, 11, 17),
                        city = "Potsdam",
                        zipCode = "14471",
                        street = "Lange Straße 5",
                        phone = "0151123456789",
                        email = "maxmustermann@web.de"
                    ),
                    qrCode = """
                        BEGIN:VCARD
                        VERSION:4.0
                        N:Mustermann;Max;;;
                        FN:Max Mustermann
                        BDAY:19800625
                        EMAIL;TYPE=home:max@mustermann.de
                        TEL;TYPE="cell,home":0190 1234567
                        ADR;TYPE=home:;;Musterstrasse 14;Musterstadt;;51466
                        REV:19951031T222710Z
                        END:VCARD
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun launch_fragment() {
        launchFragment2<ProfileQrCodeFragment>(
            fragmentArgs = args
        )
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<ProfileQrCodeFragment>(
            fragmentArgs = args
        )
        takeScreenshot<ProfileQrCodeFragment>()
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }
}

@Module
abstract class ProfileQrCodeFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun profileQrCodeFragment(): ProfileQrCodeFragment
}
