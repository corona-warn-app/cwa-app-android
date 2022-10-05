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
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestRedeemedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestOutdatedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestRedeemedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyTestListCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyTestListItem
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.flow.combine
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
    val error = SingleLiveEvent<Exception>()
    val refreshComplete = SingleLiveEvent<Unit>()

    fun onRemoveAllTests() {
        events.postValue(FamilyTestListEvent.ConfirmRemoveAllTests)
    }

    fun onBackPressed() {
        events.postValue(FamilyTestListEvent.NavigateBack)
    }

    fun markAllTestAsViewed() {
        launch(appScope) {
            familyTestRepository.familyTests.first().filter {
                it.hasBadge
            }.map { familyTest ->
                familyTest.identifier
            }.let {
                familyTestRepository.markAllBadgesAsViewed(it)
            }
        }
    }

    fun onRemoveTestConfirmed(test: FamilyCoronaTest?) {
        launch(appScope) {
            if (test == null) {
                familyTestRepository.familyTests.first().map { familyTest ->
                    familyTest.identifier
                }.let {
                    familyTestRepository.moveAllTestsToRecycleBin(it)
                }
            } else {
                familyTestRepository.moveTestToRecycleBin(test.identifier)
            }
        }
    }

    fun deleteTest(test: FamilyCoronaTest) {
        launch(appScope) {
            familyTestRepository.deleteTest(test.identifier)
        }
    }

    fun onRefreshTests() {
        launch(appScope) {
            val result = familyTestRepository.refresh().also {
                refreshComplete.postValue(Unit)
            }
            if (result.isNotEmpty()) {
                error.postValue(result.values.first())
            }
        }
    }

    val familyTests: LiveData<List<FamilyTestListItem>> = combine(
        familyTestRepository.familyTests,
        appConfigProvider.currentConfig.map { it.coronaTestParameters }.distinctUntilChanged()
    ) { familyTests, coronaTestParameters ->
        familyTests
            .sortedByDescending {
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
        when (this.coronaTest.getUiState(timeStamper.nowUTC, coronaTestConfig)) {
            State.PENDING,
            State.NEGATIVE,
            State.POSITIVE,
            State.INVALID -> FamilyTestListCard.Item(
                familyCoronaTest = this,
                onClickAction = {
                    events.postValue(FamilyTestListEvent.NavigateToDetails(familyCoronaTest = this))
                },
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
            State.RECYCLED -> FamilyTestListCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { _, _ -> }
            )
        }

    private fun FamilyCoronaTest.toRapidTestCardItem(coronaTestConfig: CoronaTestConfig): FamilyTestListItem =
        when (this.coronaTest.getUiState(timeStamper.nowUTC, coronaTestConfig)) {
            State.PENDING,
            State.NEGATIVE,
            State.POSITIVE,
            State.INVALID -> FamilyTestListCard.Item(
                familyCoronaTest = this,
                onClickAction = {
                    events.postValue(FamilyTestListEvent.NavigateToDetails(familyCoronaTest = this))
                },
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
                onDeleteTest = { events.postValue(FamilyTestListEvent.DeleteTest(this)) }
            )
            // Should not be possible
            State.RECYCLED -> FamilyTestListCard.Item(
                familyCoronaTest = this,
                onClickAction = {},
                onSwipeItem = { _, _ -> }
            )
        }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<FamilyTestListViewModel>
}
