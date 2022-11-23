package de.rki.coronawarnapp.datadonation.analytics.ui.input

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayViewModel
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.common.federalStateShortName
import de.rki.coronawarnapp.datadonation.analytics.common.labelStringRes
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber

class AnalyticsUserInputViewModel @AssistedInject constructor(
    @Assisted val type: AnalyticsUserInputFragment.InputType,
    private val settings: AnalyticsSettings,
    @AppContext private val context: Context,
    private val districtsSource: Districts,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel() {

    private val ageGroupSource: Flow<List<UserInfoItem>> = flowOf(PpaData.PPAAgeGroup.values())
        .map { ages ->
            val selected = settings.userInfoAgeGroup.first()
            ages.mapNotNull { age ->
                if (age == PpaData.PPAAgeGroup.UNRECOGNIZED) return@mapNotNull null
                UserInfoItem(
                    data = age,
                    isSelected = age == selected,
                    label = age.labelStringRes.toResolvingString()
                )
            }
        }

    private val federalStateSource: Flow<List<UserInfoItem>> = flowOf(PpaData.PPAFederalState.values())
        .map { states ->
            val selected = settings.userInfoFederalState.first()
            val items = states
                .mapNotNull { state ->
                    if (state == PpaData.PPAFederalState.UNRECOGNIZED) return@mapNotNull null
                    if (state == PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED) return@mapNotNull null
                    UserInfoItem(
                        data = state,
                        isSelected = state == selected,
                        label = state.labelStringRes.toResolvingString()
                    )
                }
                .sortedBy { it.label.get(context).lowercase() }

            val unspecified = UserInfoItem(
                data = PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED,
                isSelected = selected == PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED,
                label = PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED.labelStringRes.toResolvingString()
            )
            listOf(unspecified) + items
        }

    private val districtSource: Flow<List<UserInfoItem>> = flow { emit(districtsSource.loadDistricts()) }
        .map { allDistricts ->
            val ourStateCode = settings.userInfoFederalState.first().federalStateShortName
            allDistricts.filter { it.federalStateShortName == ourStateCode }
        }
        .map { districts ->
            val selected = settings.userInfoDistrict.first()
            val items = districts
                .map { district ->
                    UserInfoItem(
                        data = district,
                        isSelected = district.districtId == selected,
                        label = district.districtName.toLazyString()
                    )
                }
                .sortedBy { it.label.get(context).lowercase() }
            val unspecified = UserInfoItem(
                data = Districts.District(),
                isSelected = 0 == selected,
                label = R.string.analytics_userinput_district_unspecified.toResolvingString()
            )
            listOf(unspecified) + items
        }

    val userInfoItems: LiveData<List<UserInfoItem>> = when (type) {
        AnalyticsUserInputFragment.InputType.AGE_GROUP -> ageGroupSource
        AnalyticsUserInputFragment.InputType.FEDERAL_STATE -> federalStateSource
        AnalyticsUserInputFragment.InputType.DISTRICT -> districtSource
    }
        .catch { Timber.e(it, "Error sourcing list items.") }
        .asLiveData(context = dispatcherProvider.Default)

    val finishEvent = SingleLiveEvent<Unit>()

    fun selectUserInfoItem(item: UserInfoItem) = launch {
        when (item.data) {
            is PpaData.PPAAgeGroup -> {
                settings.updateUserInfoAgeGroup(item.data)
            }
            is PpaData.PPAFederalState -> {
                settings.updateUserInfoFederalState(item.data)
                settings.updateUserInfoDistrict(0)
            }
            is Districts.District -> {
                settings.updateUserInfoDistrict(item.data.districtId)
            }
            else -> throw IllegalArgumentException()
        }
        finishEvent.postValue(Unit)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ContactDiaryDayViewModel> {
        fun create(type: AnalyticsUserInputFragment.InputType): AnalyticsUserInputViewModel
    }
}
