package de.rki.coronawarnapp.familytest.ui.testlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type
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
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FamilyTestListViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    appConfigProvider: AppConfigProvider,
    private val familyTestRepository: FamilyTestRepository,
    private val timeStamper: TimeStamper,
    @AppScope private val appScope: CoroutineScope,
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<FamilyTestListEvent>()
    val refreshComplete = SingleLiveEvent<Unit>()

    fun onRemoveAllTests() {
        events.postValue(FamilyTestListEvent.ConfirmRemoveAllTests)
    }

    fun onBackPressed() {
        events.postValue(FamilyTestListEvent.NavigateBack)
    }

    fun markAllTestAsViewed() {
        launch(appScope) {
            familyTestRepository.familyTests.first().map { familyTest ->
                familyTestRepository.markBadgeAsViewed(familyTest.identifier)
            }
        }
    }

    fun onRemoveTestConfirmed(test: FamilyCoronaTest?) {
        launch(appScope) {
            if (test == null) {
                familyTestRepository.familyTests.first().map { familyTest ->
                    familyTestRepository.moveTestToRecycleBin(familyTest.identifier)
                }
            } else {
                familyTestRepository.moveTestToRecycleBin(test.identifier)
            }
        }
    }

    fun onRefreshTests() {
        launch(appScope) {
            familyTestRepository.refresh().also {
                refreshComplete.postValue(null)
            }
        }
    }

    val familyTests: LiveData<List<FamilyTestListItem>> = combine(
        familyTestRepository.familyTests,
        appConfigProvider.currentConfig.map { it.coronaTestParameters }.distinctUntilChanged()
    ) { familyTests, coronaTestParameters ->
        familyTests
            .sortedBy {
                if (it.type == Type.RAPID_ANTIGEN) it.coronaTest.testTakenAt else it.registeredAt
            }
            .map {
                when (it.coronaTest.type) {
                    Type.PCR -> it.toPCRTestCardItem(coronaTestParameters)
                    Type.RAPID_ANTIGEN -> it.toRapidTestCardItem(coronaTestParameters)
                }
            }.also {
                if (it.isEmpty()) {
                    onBackPressed()
                }
            }
    }.asLiveData(context = dispatcherProvider.Default)

    private fun FamilyCoronaTest.toPCRTestCardItem(coronaTestConfig: CoronaTestConfig): FamilyTestListItem =
        when (this.coronaTest.getState(timeStamper.nowUTC, coronaTestConfig)) {
            State.PENDING -> FamilyPcrTestPendingCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                }
            )
            State.NEGATIVE -> FamilyPcrTestNegativeCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                }
            )
            State.POSITIVE -> FamilyPcrTestPositiveCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                }
            )
            State.INVALID -> FamilyPcrTestInvalidCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                }
            )
            State.REDEEMED -> FamilyPcrTestRedeemedCard.Item(
                familyCoronaTest = this,
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                },
                onDeleteTest = { events.postValue(FamilyTestListEvent.ConfirmRemoveTest(this)) }
            )
            // Should not be possible
            State.OUTDATED,
            State.RECYCLED -> FamilyPcrTestInvalidCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { _, _ -> }
            )
        }

    private fun FamilyCoronaTest.toRapidTestCardItem(coronaTestConfig: CoronaTestConfig): FamilyTestListItem =
        when (this.coronaTest.getState(timeStamper.nowUTC, coronaTestConfig)) {
            State.PENDING -> FamilyRapidTestPendingCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                }
            )
            State.NEGATIVE -> FamilyRapidTestNegativeCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                }
            )
            State.POSITIVE -> FamilyRapidTestPositiveCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                }
            )
            State.INVALID -> FamilyRapidTestInvalidCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                }
            )
            State.REDEEMED -> FamilyRapidTestRedeemedCard.Item(
                familyCoronaTest = this,
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                },
                onDeleteTest = { events.postValue(FamilyTestListEvent.ConfirmRemoveTest(this)) }
            )
            State.OUTDATED -> FamilyRapidTestOutdatedCard.Item(
                familyCoronaTest = this,
                onSwipeItem = { familyCoronaTest, position ->
                    events.postValue(FamilyTestListEvent.ConfirmSwipeTest(familyCoronaTest, position))
                },
                onDeleteTest = { events.postValue(FamilyTestListEvent.ConfirmRemoveTest(this)) }
            )
            // Should not be possible
            State.RECYCLED -> FamilyRapidTestInvalidCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { _, _ -> }
            )
        }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<FamilyTestListViewModel>
}
