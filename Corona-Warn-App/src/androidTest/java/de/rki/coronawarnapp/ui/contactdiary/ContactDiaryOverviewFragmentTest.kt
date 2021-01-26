package de.rki.coronawarnapp.ui.contactdiary

import android.Manifest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragment
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewViewModel
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.ListItem
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.ui.contactdiary.DiaryData.DATA_ITEMS
import de.rki.coronawarnapp.ui.contactdiary.DiaryData.HIGH_RISK
import de.rki.coronawarnapp.ui.contactdiary.DiaryData.LOW_RISK
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.SCREENSHOT_DELAY_TIME
import testhelpers.ScreenShotter
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class ContactDiaryOverviewFragmentTest : BaseUITest() {
    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @MockK lateinit var taskController: TaskController
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    @MockK lateinit var riskLevelStorage: RiskLevelStorage

    private lateinit var viewModel: ContactDiaryOverviewViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            ContactDiaryOverviewViewModel(
                taskController = taskController,
                dispatcherProvider = TestDispatcherProvider(),
                contactDiaryRepository = contactDiaryRepository,
                riskLevelStorage = riskLevelStorage
            )
        )

        setupMockViewModel(
            object : ContactDiaryOverviewViewModel.Factory {
                override fun create(): ContactDiaryOverviewViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<ContactDiaryOverviewFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        every { viewModel.listItems } returns itemLiveData()
        launchFragmentInContainer2<ContactDiaryOverviewFragment>()
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        ScreenShotter.capture<ContactDiaryOverviewFragment>()
    }

    private fun itemLiveData(): LiveData<List<ListItem>> =
        MutableLiveData(
            (0 until ContactDiaryOverviewViewModel.DAY_COUNT)
                .map { LocalDate.now().minusDays(it) }
                .map {
                    ListItem(it).apply {
                        data.addAll(DATA_ITEMS)
                        risk = if (it.dayOfYear % 2 == 0) HIGH_RISK else LOW_RISK
                    }
                }
        )
}

@Module
abstract class ContactDiaryOverviewFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun contactContactDiaryOverviewFragment(): ContactDiaryOverviewFragment
}
