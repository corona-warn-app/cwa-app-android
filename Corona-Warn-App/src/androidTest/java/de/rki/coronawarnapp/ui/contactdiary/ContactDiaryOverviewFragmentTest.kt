package de.rki.coronawarnapp.ui.contactdiary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragment
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewViewModel
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.subheader.OverviewSubHeaderItem
import de.rki.coronawarnapp.ui.contactdiary.DiaryData.SUBMISSION_ITEM
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchInMainActivity
import testhelpers.recyclerScrollTo
import testhelpers.selectBottomNavTab
import testhelpers.takeScreenshot
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ContactDiaryOverviewFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: ContactDiaryOverviewViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
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
    fun captureContactDiaryOverviewFragment() {
        every { viewModel.listItems } returns contactDiaryOverviewItemLiveData()
        launchInMainActivity<ContactDiaryOverviewFragment>()
        onView(withId(R.id.fake_bottom_navigation))
            .perform(selectBottomNavTab(R.id.contact_diary_nav_graph))
        takeScreenshot<ContactDiaryOverviewFragment>()

        onView(withId(R.id.contact_diary_overview_recyclerview))
            .perform(recyclerScrollTo(4))
        takeScreenshot<ContactDiaryOverviewFragment>("2")

        onView(withId(R.id.contact_diary_overview_recyclerview))
            .perform(recyclerScrollTo(10))
        takeScreenshot<ContactDiaryOverviewFragment>("3")
    }

    private fun contactDiaryOverviewItemLiveData(): LiveData<List<DiaryOverviewItem>> {
        val data = mutableListOf<DiaryOverviewItem>()
        data.add(OverviewSubHeaderItem)

        val dayData = (0 until ContactDiaryOverviewViewModel.DAY_COUNT)
            .map { LocalDate.now().minusDays(it) }
            .mapIndexed { index, localDate ->
                val dayData = mutableListOf<ContactItem.Data>().apply {
                    if (index == 1) {
                        add(DiaryData.DATA_ITEMS[0])
                        add(DiaryData.DATA_ITEMS[1])
                    } else if (index == 3) {
                        add(DiaryData.DATA_ITEMS[2])
                    }
                }

                val riskEnf = when (index % 5) {
                    3 -> DiaryData.HIGH_RISK_DUE_LOW_RISK_ENCOUNTERS
                    else -> null // DiaryData.LOW_RISK OR DiaryData.HIGH_RISK POSSIBLE
                }

                val riskEvent = when (index) {
                    6 -> {
                        dayData.add(DiaryData.LOW_RISK_EVENT_LOCATION)
                        DiaryData.LOW_RISK_EVENT_ITEM
                    }

                    7 -> {
                        dayData.apply {
                            add(DiaryData.LOW_RISK_EVENT_LOCATION)
                            add(DiaryData.HIGH_RISK_EVENT_LOCATION)
                        }
                        DiaryData.HIGH_RISK_EVENT_ITEM
                    }

                    else -> null
                }

                val coronaTestEvent = when (index) {
                    8 -> {
                        DiaryData.TEST_ITEM
                    }

                    else -> null
                }

                DayOverviewItem(
                    date = localDate,
                    contactItem = ContactItem(dayData),
                    riskEnfItem = riskEnf,
                    riskEventItem = riskEvent,
                    coronaTestItem = coronaTestEvent,
                    submissionItem = SUBMISSION_ITEM
                ) {
                    // onClick
                }
            }
        data.addAll(dayData)

        return MutableLiveData(data)
    }
}

@Module
abstract class ContactDiaryOverviewFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun contactContactDiaryOverviewFragment(): ContactDiaryOverviewFragment
}
