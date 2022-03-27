package de.rki.coronawarnapp.familytest.ui.testlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.CoronaTest.State
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
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FamilyTestListViewModel(
    dispatcherProvider: DispatcherProvider,
    private val familyTestRepository: FamilyTestRepository,
    @AppScope private val appScope: CoroutineScope,
) : CWAViewModel(dispatcherProvider) {

    @AssistedFactory
    interface Factory : CWAViewModelFactory<FamilyTestListViewModel> {
        fun create(): FamilyTestListViewModel
    }

    val familyTests: LiveData<List<FamilyTestListItem>> = familyTestRepository.familyTests
        .map { familyTests ->
            familyTests
                .sortedBy {
                    // if (it.type == Type.RAPID_ANTIGEN) sampleCollectedAt TODO: how to get it?
                    it.registeredAt
                }
                .map {
                    when (it.coronaTest.type) {
                        Type.PCR -> it.toPCRTestCardItem()
                        Type.RAPID_ANTIGEN -> it.toRapidTestCardItem()
                    }
                }
        }.asLiveData(context = dispatcherProvider.Default)

    private fun FamilyCoronaTest.toPCRTestCardItem(): FamilyTestListItem =
        when (this.coronaTest.state) {
            State.PENDING -> FamilyPcrTestPendingCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} }
            )
            State.NEGATIVE -> FamilyPcrTestNegativeCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} }
            )
            State.POSITIVE -> FamilyPcrTestPositiveCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} }
            )
            State.INVALID -> FamilyPcrTestInvalidCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} }
            )
            State.REDEEMED -> FamilyPcrTestRedeemedCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} },
                onDeleteTest = {}
            )
            // Should not be possible
            State.RECYCLED -> FamilyPcrTestInvalidCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} }
            )
        }

    private fun FamilyCoronaTest.toRapidTestCardItem(): FamilyTestListItem =
        when (this.coronaTest.state) {
            State.PENDING -> FamilyRapidTestPendingCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} }
            )
            State.NEGATIVE -> FamilyRapidTestNegativeCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} }
            )
            State.POSITIVE -> FamilyRapidTestPositiveCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} }
            )
            State.INVALID -> FamilyRapidTestInvalidCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} }
            )
            State.REDEEMED -> FamilyRapidTestRedeemedCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} },
                onDeleteTest = {}
            )
            //State.OUTDATED -> FamilyRapidTestOutdatedCard.Item({}) // TODO: add OUTDATED state
            State.RECYCLED -> FamilyRapidTestInvalidCard.Item(familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position -> {} }
            ) // Should not be possible
        }

    companion object {
        private const val TAG = "FamilyTestListViewModel"
    }
}
