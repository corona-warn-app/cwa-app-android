package de.rki.coronawarnapp.ui.contactdiary

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayFragment
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayFragmentArgs
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayViewModel
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListViewModel
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListViewModel
import de.rki.coronawarnapp.contactdiary.util.toFormattedDay
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import tools.fastlane.screengrab.locale.LocaleUtil
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ContactDiaryDayFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @MockK lateinit var viewModel: ContactDiaryDayViewModel
    @MockK lateinit var personListViewModel: ContactDiaryPersonListViewModel
    @MockK lateinit var locationListViewModel: ContactDiaryLocationListViewModel

    private val selectedDay = LocalDate.now()
        .toFormattedDay(LocaleUtil.getTestLocale() ?: Locale.ENGLISH)

    private val fragmentArgs = ContactDiaryDayFragmentArgs(selectedDay = selectedDay).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { viewModel.uiState } returns MutableLiveData(
            ContactDiaryDayViewModel.UIState(
                dayText = { selectedDay },
                dayTextContentDescription = { "Description" }
            )
        )
        setupViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<ContactDiaryDayFragment>(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.AppTheme_ContactDiary
        )
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<ContactDiaryDayFragment>(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.AppTheme_ContactDiary
        )
        Thread.sleep(10000)
        Screengrab.screenshot(ContactDiaryDayFragment::class.simpleName)
    }

    private fun setupViewModels() {
        setupMockViewModel(
            object : ContactDiaryDayViewModel.Factory {
                override fun create(selectedDay: String): ContactDiaryDayViewModel {
                    return viewModel
                }
            }
        )

        setupMockViewModel(
            object : ContactDiaryPersonListViewModel.Factory {
                override fun create(selectedDay: String): ContactDiaryPersonListViewModel {
                    return personListViewModel
                }
            }
        )

        setupMockViewModel(
            object : ContactDiaryLocationListViewModel.Factory {
                override fun create(selectedDay: String): ContactDiaryLocationListViewModel {
                    return locationListViewModel
                }
            }
        )
    }
}

@Module
abstract class ContactDiaryDayFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun contactDiaryDayFragment(): ContactDiaryDayFragment
}

@Module
abstract class ContactDiaryPersonListFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun contactDiaryPersonListFragment(): ContactDiaryPersonListFragment
}

@Module
abstract class ContactDiaryLocationListFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun contactDiaryLocationListFragment(): ContactDiaryLocationListFragment
}