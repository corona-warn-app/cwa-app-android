package de.rki.coronawarnapp.statistics.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.common.federalStateShortName
import de.rki.coronawarnapp.datadonation.analytics.common.labelStringRes
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.toLazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber

class FederalStateSelectionViewModel @AssistedInject constructor(
    @Assisted val selectedFederalStateShortName: String?,
    @AppContext private val context: Context,
    private val districtsSource: Districts,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel() {

    private val federalStateSource: Flow<List<UserInfoItem>> = flowOf(PpaData.PPAFederalState.values())
        .map { states ->
            states.mapNotNull { state ->
                if (state == PpaData.PPAFederalState.UNRECOGNIZED) return@mapNotNull null
                if (state == PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED) return@mapNotNull null
                UserInfoItem(
                    data = state,
                    label = state.labelStringRes.toResolvingString()
                )
            }.sortedBy { it.label.get(context).lowercase() }
        }

    private val districtSource: Flow<List<UserInfoItem>> = flow { emit(districtsSource.loadDistricts()) }
        .map { allDistricts ->
            allDistricts.filter { it.federalStateShortName == selectedFederalStateShortName }
        }
        .map { districts ->
            districts
                .map { district ->
                    UserInfoItem(
                        data = district,
                        label = district.districtName.toLazyString()
                    )
                }
                .sortedBy { it.label.get(context).lowercase() }
        }

    val userInfoItems: LiveData<List<UserInfoItem>> = if (selectedFederalStateShortName != null) {
        districtSource
    } else {
        federalStateSource
    }.catch { Timber.e(it, "Error sourcing list items.") }
        .asLiveData(context = dispatcherProvider.Default)

    val event = SingleLiveEvent<Events>()

    fun selectUserInfoItem(item: UserInfoItem) {
        when (item.data) {
            is PpaData.PPAFederalState -> {
                event.postValue(Events.OpenDistricts(item.data.federalStateShortName))
            }
            is Districts.District -> {
                event.postValue(Events.FinishEvent)
            }
            else -> throw IllegalArgumentException()
        }
    }

    sealed class Events {
        data class OpenDistricts(val selectedFederalStateShortName: String) : Events()
        object FinishEvent : Events()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<FederalStateSelectionViewModel> {
        fun create(type: String?): FederalStateSelectionViewModel
    }
}
