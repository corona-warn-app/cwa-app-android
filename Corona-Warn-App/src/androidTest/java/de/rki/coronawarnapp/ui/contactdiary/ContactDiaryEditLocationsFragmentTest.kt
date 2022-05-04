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
import kotlinx.coroutines.test.TestCoroutineScope
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
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class ContactDiaryEditLocationsFragmentTest : BaseUITest() {

    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    private lateinit var viewModel: ContactDiaryEditLocationsViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            ContactDiaryEditLocationsViewModel(
                TestScope(),
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
    fun teardown() {
        clearAllViewModels()
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
