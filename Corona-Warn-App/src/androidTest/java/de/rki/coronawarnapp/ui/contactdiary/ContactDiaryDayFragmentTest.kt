package de.rki.coronawarnapp.ui.contactdiary

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayFragment
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayFragmentArgs
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayViewModel
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListViewModel
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.DiaryLocationListItem
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListViewModel
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.DiaryPersonListItem
import de.rki.coronawarnapp.contactdiary.util.toFormattedDay
import de.rki.coronawarnapp.ui.contactdiary.DiaryData.LOCATIONS
import de.rki.coronawarnapp.ui.contactdiary.DiaryData.PERSONS
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.selectTabAtPosition
import testhelpers.takeScreenshot
import java.time.LocalDate
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ContactDiaryDayFragmentTest : BaseUITest() {

    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository

    private lateinit var viewModel: ContactDiaryDayViewModel
    private lateinit var personListViewModel: ContactDiaryPersonListViewModel
    private lateinit var locationListViewModel: ContactDiaryLocationListViewModel

    private val selectedDay = LocalDate.now().toString()
    private val fragmentArgs = ContactDiaryDayFragmentArgs(selectedDay = selectedDay).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupViewModels()

        every { viewModel.uiState } returns MutableLiveData(
            ContactDiaryDayViewModel.UIState(
                dayText = { LocalDate.now().toFormattedDay(Locale.getDefault()) },
                dayTextContentDescription = { "Description" }
            )
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<ContactDiaryDayFragment>(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.AppTheme_Main
        )
    }

    @Screenshot
    @Test
    fun capture_screenshot_no_data() {
        captureScreen(listOf(), listOf(), "no_data")
    }

    @Screenshot
    @Test
    fun capture_screenshot_data() {
        captureScreen(PERSONS, LOCATIONS, "data")
    }

    private fun captureScreen(
        persons: List<DiaryPersonListItem>,
        locations: List<DiaryLocationListItem>,
        suffix: String
    ) {
        every { personListViewModel.uiList } returns MutableLiveData(persons)
        every { locationListViewModel.uiList } returns MutableLiveData(locations)

        launchFragmentInContainer2<ContactDiaryDayFragment>(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.AppTheme_Main
        )
        onView(withId(R.id.contact_diary_day_tab_layout))
            .perform(selectTabAtPosition(0))
        takeScreenshot<ContactDiaryDayFragment>("persons_$suffix")
        onView(withId(R.id.contact_diary_day_tab_layout))
            .perform(selectTabAtPosition(1))
        takeScreenshot<ContactDiaryDayFragment>("locations_$suffix")
    }

    private fun setupViewModels() {
        viewModel = spyk(
            ContactDiaryDayViewModel(
                TestDispatcherProvider(),
                selectedDay
            )
        )

        personListViewModel = spyk(
            ContactDiaryPersonListViewModel(
                TestDispatcherProvider(),
                TestScope(),
                selectedDay,
                contactDiaryRepository,
            )
        )

        locationListViewModel = spyk(
            ContactDiaryLocationListViewModel(
                TestDispatcherProvider(),
                TestScope(),
                selectedDay,
                contactDiaryRepository
            )
        )

        setupMockViewModel(
            object : ContactDiaryDayViewModel.Factory {
                override fun create(selectedDay: String): ContactDiaryDayViewModel = viewModel
            }
        )

        setupMockViewModel(
            object : ContactDiaryPersonListViewModel.Factory {
                override fun create(selectedDay: String): ContactDiaryPersonListViewModel = personListViewModel
            }
        )

        setupMockViewModel(
            object : ContactDiaryLocationListViewModel.Factory {
                override fun create(selectedDay: String): ContactDiaryLocationListViewModel = locationListViewModel
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
