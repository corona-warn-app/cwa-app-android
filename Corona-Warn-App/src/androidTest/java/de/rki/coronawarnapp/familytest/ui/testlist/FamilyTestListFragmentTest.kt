package de.rki.coronawarnapp.familytest.ui.testlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestInvalidCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestNegativeCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestPendingCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestPositiveCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestRedeemedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestInvalidCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestNegativeCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestOutdatedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestPendingCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestPositiveCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestRedeemedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyTestListItem
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class FamilyTestListFragmentTest : BaseUITest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var familyTestRepository: FamilyTestRepository
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var appScope: CoroutineScope

    private lateinit var viewModel: FamilyTestListViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            FamilyTestListViewModel(
                TestDispatcherProvider(),
                appConfigProvider,
                familyTestRepository,
                timeStamper,
                appScope
            )
        )
        every { viewModel.familyTests } returns testCards()

        setupMockViewModel(
            object : FamilyTestListViewModel.Factory {
                override fun create(): FamilyTestListViewModel = viewModel
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
        launchFragmentInContainer2<FamilyTestListFragment>()
        takeScreenshot<FamilyTestListFragment>("1")
        Espresso.onView(ViewMatchers.withId(R.id.refreshLayout)).perform(ViewActions.swipeUp())
        takeScreenshot<FamilyTestListFragment>("2")
        Espresso.onView(ViewMatchers.withId(R.id.refreshLayout)).perform(ViewActions.swipeUp())
        takeScreenshot<FamilyTestListFragment>("3")
    }

    private fun testCards(): LiveData<List<FamilyTestListItem>> =
        MutableLiveData(
            mutableListOf<FamilyTestListItem>().apply {
                add(
                    FamilyRapidTestPositiveCard.Item(
                        familyCoronaTest = ratPositive,
                        onClickAction = {},
                        onSwipeItem = { _, _ -> }
                    )
                )
                add(
                    FamilyPcrTestPositiveCard.Item(
                        familyCoronaTest = pcrPositive,
                        onClickAction = {},
                        onSwipeItem = { _, _ -> }
                    )
                )
                add(
                    FamilyRapidTestNegativeCard.Item(
                        familyCoronaTest = ratNegative,
                        onClickAction = {},
                        onSwipeItem = { _, _ -> }
                    )
                )
                add(
                    FamilyPcrTestNegativeCard.Item(
                        familyCoronaTest = pcrNegative,
                        onClickAction = {},
                        onSwipeItem = { _, _ -> }
                    )
                )
                add(
                    FamilyRapidTestPendingCard.Item(
                        familyCoronaTest = ratPending,
                        onClickAction = {},
                        onSwipeItem = { _, _ -> }
                    )
                )
                add(
                    FamilyPcrTestPendingCard.Item(
                        familyCoronaTest = pcrPending,
                        onClickAction = {},
                        onSwipeItem = { _, _ -> }
                    )
                )
                add(
                    FamilyRapidTestInvalidCard.Item(
                        familyCoronaTest = ratInvalid,
                        onClickAction = {},
                        onSwipeItem = { _, _ -> }
                    )
                )
                add(
                    FamilyPcrTestInvalidCard.Item(
                        familyCoronaTest = pcrInvalid,
                        onClickAction = {},
                        onSwipeItem = { _, _ -> }
                    )
                )
                add(
                    FamilyRapidTestRedeemedCard.Item(
                        familyCoronaTest = ratRedeemed,
                        onSwipeItem = { _, _ -> },
                        onDeleteTest = {}
                    )
                )
                add(
                    FamilyPcrTestRedeemedCard.Item(
                        familyCoronaTest = pcrRedeemed,
                        onSwipeItem = { _, _ -> },
                        onDeleteTest = {}
                    )
                )
                add(
                    FamilyRapidTestOutdatedCard.Item(
                        familyCoronaTest = ratOutdated,
                        onSwipeItem = { _, _ -> },
                        onDeleteTest = {}
                    )
                )
            }
        )

    private val ratPositive: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.RAT_POSITIVE

        every { it.coronaTest } returns test
        every { it.personName } returns "Lara Schneider"
        every { it.hasBadge } returns true
    }

    private val pcrPositive: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.PCR
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.PCR_POSITIVE

        every { it.coronaTest } returns test
        every { it.personName } returns "Oma"
    }

    private val ratNegative: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.RAT_NEGATIVE

        every { it.coronaTest } returns test
        every { it.personName } returns "Clara"
    }

    private val pcrNegative: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.PCR
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.PCR_NEGATIVE

        every { it.coronaTest } returns test
        every { it.personName } returns "Hans-Dieter"
    }

    private val ratPending: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.RAT_PENDING

        every { it.coronaTest } returns test
        every { it.personName } returns "Maximilian"
    }

    private val pcrPending: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.PCR
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.PCR_OR_RAT_PENDING

        every { it.coronaTest } returns test
        every { it.personName } returns "Miriam"
    }

    private val ratInvalid: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.RAT_INVALID

        every { it.coronaTest } returns test
        every { it.personName } returns "Leroy"
    }

    private val pcrInvalid: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.PCR
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.PCR_INVALID

        every { it.coronaTest } returns test
        every { it.personName } returns "Jenkins"
    }

    private val ratRedeemed: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.RAT_REDEEMED

        every { it.coronaTest } returns test
        every { it.personName } returns "Sarah"
    }

    private val pcrRedeemed: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.PCR
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.PCR_OR_RAT_REDEEMED

        every { it.coronaTest } returns test
        every { it.personName } returns "Parker"
    }

    private val ratOutdated: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { test.testResult } returns CoronaTestResult.RAT_NEGATIVE
        every { test.getState(any(), any()) } returns CoronaTest.State.OUTDATED

        every { it.coronaTest } returns test
        every { it.personName } returns "Marianna"
    }
}

@Module
abstract class FamilyTestsListFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun familyTestListScreen(): FamilyTestListFragment
}
