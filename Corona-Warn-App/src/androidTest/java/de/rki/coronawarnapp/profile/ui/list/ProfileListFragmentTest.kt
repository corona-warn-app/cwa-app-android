package de.rki.coronawarnapp.profile.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.ui.list.items.ProfileCard
import de.rki.coronawarnapp.profile.ui.list.items.ProfileListItem
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class ProfileListFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: ProfileListViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { viewModel.profiles } returns profileCards()

        setupMockViewModel(
            object : ProfileListViewModel.Factory {
                override fun create(): ProfileListViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<ProfileListFragment>()
        takeScreenshot<ProfileListFragment>("1")
    }

    private fun profileCards(): LiveData<List<ProfileListItem>> =
        MutableLiveData(
            listOf<ProfileListItem>(
                ProfileCard.Item(
                    profile = Profile(
                        id = 1,
                        firstName = "Andrea",
                        lastName = "Schneider",
                        birthDate = LocalDate.of(1990, 11, 17),
                        city = "Potsdam",
                        zipCode = "14471",
                        street = "Lange Straße 5",
                        phone = "0151123456789",
                        email = "andreaschneider@web.de"
                    ),
                    qrCode = "BEGIN:VCARD\n" +
                        "VERSION:4.0\n" +
                        "N:Schneider;Andrea;;;\n" +
                        "FN:Andrea Schneider\n" +
                        "BDAY:19690420\n" +
                        "EMAIL;TYPE=home:andrea.schneider@gmail.com\n" +
                        "TEL;TYPE=\"cell,home\":(359) 260-345\n" +
                        "ADR;TYPE=home:;;Long Str\\, No. 4;Liverpool;;12345\n" +
                        "REV:20220418T091325Z\n" +
                        "END:VCARD",
                    onClickAction = { _, _ -> }
                ),

                ProfileCard.Item(
                    profile = Profile(
                        id = 2,
                        firstName = "Max",
                        lastName = "Mustermann",
                        birthDate = LocalDate.of(1997, 7, 24),
                        city = "Potsdam",
                        zipCode = "14471",
                        street = "Lange Straße 5",
                        phone = "0234523454545",
                        email = "maxmustermann@web.de"
                    ),
                    qrCode = "BEGIN:VCARD\n" +
                        "VERSION:4.0\n" +
                        "N:Musterman;Max;;;\n" +
                        "FN:Max Musterman\n" +
                        "BDAY:19690420\n" +
                        "EMAIL;TYPE=home:max.musterman@gmail.com\n" +
                        "TEL;TYPE=\"cell,home\":(359) 260-345\n" +
                        "ADR;TYPE=home:;;Long Str\\, No. 4;Liverpool;;12345\n" +
                        "REV:20220418T091325Z\n" +
                        "END:VCARD",
                    onClickAction = { _, _ -> }
                )
            )
        )
}

@Module
abstract class ProfileListFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun profileListFragment(): ProfileListFragment
}
