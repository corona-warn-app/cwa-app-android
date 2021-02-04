package de.rki.coronawarnapp.ui.contactdiary

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragment
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewViewModel
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.TaskController
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2

@RunWith(AndroidJUnit4::class)
class ContactDiaryOverviewFragmentTest : BaseUITest() {

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
        unmockkAll()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<ContactDiaryOverviewFragment>()
    }
}

@Module
abstract class ContactDiaryOverviewFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun contactContactDiaryOverviewFragment(): ContactDiaryOverviewFragment
}
