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
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestRedeemedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestOutdatedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestRedeemedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyTestListCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyTestListItem
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class FamilyTestListFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: FamilyTestListViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { viewModel.familyTests } returns testCards()
        every { viewModel.markAllTestAsViewed() } just Runs

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
            listOf<FamilyTestListItem>(
                FamilyTestListCard.Item(
                    familyCoronaTest = ratPositive,
                    onClickAction = {},
                    onSwipeItem = { _, _ -> }
                ),
                FamilyTestListCard.Item(
                    familyCoronaTest = pcrPositive,
                    onClickAction = {},
                    onSwipeItem = { _, _ -> }
                ),
                FamilyTestListCard.Item(
                    familyCoronaTest = ratNegative,
                    onClickAction = {},
                    onSwipeItem = { _, _ -> }
                ),
                FamilyTestListCard.Item(
                    familyCoronaTest = pcrNegative,
                    onClickAction = {},
                    onSwipeItem = { _, _ -> }
                ),
                FamilyTestListCard.Item(
                    familyCoronaTest = ratPending,
                    onClickAction = {},
                    onSwipeItem = { _, _ -> }
                ),
                FamilyTestListCard.Item(
                    familyCoronaTest = pcrPending,
                    onClickAction = {},
                    onSwipeItem = { _, _ -> }
                ),
                FamilyTestListCard.Item(
                    familyCoronaTest = ratInvalid,
                    onClickAction = {},
                    onSwipeItem = { _, _ -> }
                ),
                FamilyTestListCard.Item(
                    familyCoronaTest = pcrInvalid,
                    onClickAction = {},
                    onSwipeItem = { _, _ -> }
                ),
                FamilyRapidTestRedeemedCard.Item(
                    familyCoronaTest = ratRedeemed,
                    onSwipeItem = { _, _ -> },
                    onDeleteTest = {}
                ),
                FamilyPcrTestRedeemedCard.Item(
                    familyCoronaTest = pcrRedeemed,
                    onSwipeItem = { _, _ -> },
                    onDeleteTest = {}
                ),
                FamilyRapidTestOutdatedCard.Item(
                    familyCoronaTest = ratOutdated,
                    onSwipeItem = { _, _ -> },
                    onDeleteTest = {}
                )
            )
        )

    private val ratPositive: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.RAT_POSITIVE
        every { it.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { it.coronaTest } returns test
        every { it.personName } returns "Lara Schneider"
        every { it.hasBadge } returns true
        every { it.isNegative } returns false
        every { it.isPositive } returns true
        every { it.isPending } returns false
        every { it.isInvalid } returns false
    }

    private val pcrPositive: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { it.type } returns BaseCoronaTest.Type.PCR
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.PCR_POSITIVE

        every { it.coronaTest } returns test
        every { it.personName } returns "Oma"
        every { it.isNegative } returns false
        every { it.isPositive } returns true
        every { it.isPending } returns false
        every { it.isInvalid } returns false
    }

    private val ratNegative: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { it.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.RAT_NEGATIVE

        every { it.coronaTest } returns test
        every { it.personName } returns "Clara"
        every { it.isPositive } returns false
        every { it.isNegative } returns true
        every { it.isPending } returns false
        every { it.isInvalid } returns false
    }

    private val pcrNegative: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { it.type } returns BaseCoronaTest.Type.PCR
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.PCR_NEGATIVE

        every { it.coronaTest } returns test
        every { it.personName } returns "Hans-Dieter"
        every { it.isPositive } returns false
        every { it.isNegative } returns true
        every { it.isPending } returns false
        every { it.isInvalid } returns false
    }

    private val ratPending: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { it.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.RAT_PENDING
        every { test.testResult } returns CoronaTestResult.RAT_PENDING

        every { it.coronaTest } returns test
        every { it.personName } returns "Maximilian"
        every { it.isNegative } returns false
        every { it.isPositive } returns false
        every { it.isInvalid } returns false
        every { it.isPending } returns true
    }

    private val pcrPending: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { it.type } returns BaseCoronaTest.Type.PCR
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.PCR_OR_RAT_PENDING
        every { test.testResult } returns CoronaTestResult.PCR_OR_RAT_PENDING

        every { it.coronaTest } returns test
        every { it.personName } returns "Miriam"
        every { it.isNegative } returns false
        every { it.isPositive } returns false
        every { it.isInvalid } returns false
        every { it.isPending } returns true
    }

    private val ratInvalid: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { it.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.RAT_INVALID

        every { it.coronaTest } returns test
        every { it.personName } returns "Leroy"
        every { it.isNegative } returns false
        every { it.isPositive } returns false
        every { it.isPending } returns false
        every { it.isInvalid } returns true
    }

    private val pcrInvalid: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { it.type } returns BaseCoronaTest.Type.PCR
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.PCR_INVALID

        every { it.coronaTest } returns test
        every { it.personName } returns "Jenkins"
        every { it.isNegative } returns false
        every { it.isPositive } returns false
        every { it.isPending } returns false
        every { it.isInvalid } returns true
    }

    private val ratRedeemed: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { it.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.RAT_REDEEMED
        every { test.testResult } returns CoronaTestResult.RAT_REDEEMED

        every { it.coronaTest } returns test
        every { it.personName } returns "Sarah"
        every { it.isNegative } returns false
        every { it.isPositive } returns false
        every { it.isRedeemed } returns true
    }

    private val pcrRedeemed: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { it.type } returns BaseCoronaTest.Type.PCR
        every { test.type } returns BaseCoronaTest.Type.PCR
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.PCR_OR_RAT_REDEEMED
        every { test.testResult } returns CoronaTestResult.PCR_OR_RAT_REDEEMED

        every { it.coronaTest } returns test
        every { it.personName } returns "Parker"
        every { it.isNegative } returns false
        every { it.isPositive } returns false
        every { it.isRedeemed } returns true
    }

    private val ratOutdated: FamilyCoronaTest = mockk<FamilyCoronaTest>(relaxed = true).also {
        val test: CoronaTest = mockk(relaxed = true)
        every { it.type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { test.getFormattedRegistrationDate() } returns "26.03.21"
        every { it.testResult } returns CoronaTestResult.RAT_NEGATIVE
        every { test.getUiState(any(), any()) } returns CoronaTest.State.OUTDATED

        every { it.coronaTest } returns test
        every { it.personName } returns "Marianna"
    }
}

@Module
abstract class FamilyTestsListFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun familyTestListScreen(): FamilyTestListFragment
}
