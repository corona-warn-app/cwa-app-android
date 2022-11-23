package de.rki.coronawarnapp.statistics.ui.stateselection

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.common.federalStateShortName
import de.rki.coronawarnapp.datadonation.analytics.common.labelStringRes
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage
import de.rki.coronawarnapp.statistics.local.storage.SelectedStatisticsLocation
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.toLazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber

@SuppressLint("StaticFieldLeak")
class FederalStateSelectionViewModel @AssistedInject constructor(
    @Assisted val selectedFederalState: PpaData.PPAFederalState?,
    @AppContext private val context: Context,
    private val districtsSource: Districts,
    private val localStatisticsConfigStorage: LocalStatisticsConfigStorage,
    private val timeStamper: TimeStamper
) : CWAViewModel() {

    private val federalStateSource: Flow<List<ListItem>> = flowOf(PpaData.PPAFederalState.values())
        .map { states ->
            states.mapNotNull { state ->
                if (state == PpaData.PPAFederalState.UNRECOGNIZED) return@mapNotNull null
                if (state == PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED) return@mapNotNull null
                ListItem(
                    data = state,
                    label = state.labelStringRes.toResolvingString()
                )
            }.sortedBy { it.label.get(context).lowercase() }
        }

    private val districtSource: Flow<List<ListItem>> = flow { emit(districtsSource.loadDistricts()) }
        .map { allDistricts ->
            allDistricts.filter { it.federalStateShortName == selectedFederalState?.federalStateShortName }
        }
        .map { districts ->
            districts
                .map { district ->
                    ListItem(
                        data = district,
                        label = district.districtName.toLazyString()
                    )
                }
                .sortedBy { it.label.get(context).lowercase() }
        }
        .map { districts ->
            if (selectedFederalState != null) {
                listOf(
                    ListItem(
                        data = selectedFederalState,
                        label = R.string.statistics_local_incidence_whole_state_text.toResolvingString()
                    )
                ) + districts
            } else {
                districts
            }
        }

    val listItems: LiveData<List<ListItem>> = if (selectedFederalState != null) {
        districtSource
    } else {
        federalStateSource
    }.catch { Timber.e(it, "Error sourcing list items.") }
        .asLiveData2()

    val event = SingleLiveEvent<Events>()

    fun selectUserInfoItem(item: ListItem) = launch {
        when (item.data) {
            is PpaData.PPAFederalState -> {
                if (selectedFederalState == null) {
                    event.postValue(Events.OpenDistricts(item.data))
                } else {
                    localStatisticsConfigStorage.updateActiveSelections(
                        localStatisticsConfigStorage.activeSelections.first().withLocation(
                            SelectedStatisticsLocation.SelectedFederalState(item.data, timeStamper.nowUTC)
                        )
                    )
                    event.postValue(Events.FinishEvent)
                }
            }
            is Districts.District -> {
                localStatisticsConfigStorage.updateActiveSelections(
                    localStatisticsConfigStorage.activeSelections.first().withLocation(
                        SelectedStatisticsLocation.SelectedDistrict(item.data, timeStamper.nowUTC)
                    )
                )
                event.postValue(Events.FinishEvent)
            }
            else -> throw IllegalArgumentException()
        }
    }

    sealed class Events {
        data class OpenDistricts(val selectedFederalState: PpaData.PPAFederalState) : Events()
        object FinishEvent : Events()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<FederalStateSelectionViewModel> {
        fun create(selectedFederalState: PpaData.PPAFederalState?): FederalStateSelectionViewModel
    }
}
