package de.rki.coronawarnapp.ui.contactdiary

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditLocationsFragment
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditLocationsViewModel
import de.rki.coronawarnapp.ui.contactdiary.DiaryData.LOCATIONS_EDIT_LIST
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.takeScreenshot
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class ContactDiaryEditLocationsFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    private lateinit var viewModel: ContactDiaryEditLocationsViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            ContactDiaryEditLocationsViewModel(
                contactDiaryRepository,
                TestDispatcherProvider()
            )
        )
        setupMockViewModel(
            object : ContactDiaryEditLocationsViewModel.Factory {
                override fun create(): ContactDiaryEditLocationsViewModel = viewModel
            }
        )
    }

    @After
    fun tearDown() {
        clearAllViewModels()
        unmockkAll()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<ContactDiaryEditLocationsFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        every { viewModel.locationsLiveData } returns MutableLiveData(LOCATIONS_EDIT_LIST)
        launchFragmentInContainer2<ContactDiaryEditLocationsFragment>()
        takeScreenshot<ContactDiaryEditLocationsFragment>()
    }
}

@Module
abstract class ContactDiaryEditLocationsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun contactDiaryEditLocationsFragment(): ContactDiaryEditLocationsFragment
}
