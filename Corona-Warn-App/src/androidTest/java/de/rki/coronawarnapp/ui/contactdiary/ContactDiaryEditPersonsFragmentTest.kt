package de.rki.coronawarnapp.ui.contactdiary

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditPersonsFragment
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditPersonsViewModel
import de.rki.coronawarnapp.ui.contactdiary.DiaryData.PERSONS_EDIT_LIST
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchInEmptyActivity
import testhelpers.takeScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class ContactDiaryEditPersonsFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    private lateinit var viewModel: ContactDiaryEditPersonsViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            ContactDiaryEditPersonsViewModel(
                TestCoroutineScope(),
                contactDiaryRepository,
                TestDispatcherProvider()
            )
        )
        setupMockViewModel(
            object : ContactDiaryEditPersonsViewModel.Factory {
                override fun create(): ContactDiaryEditPersonsViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<ContactDiaryEditPersonsFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        every { viewModel.personsLiveData } returns MutableLiveData(PERSONS_EDIT_LIST)
        launchInEmptyActivity<ContactDiaryEditPersonsFragment>()
        takeScreenshot<ContactDiaryEditPersonsFragment>()
    }
}

@Module
abstract class ContactDiaryEditPersonsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun contactDiaryEditPersonsFragment(): ContactDiaryEditPersonsFragment
}
