package de.rki.coronawarnapp.datadonation.analytics.ui.input

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayViewModel
import de.rki.coronawarnapp.datadonation.analytics.AnalyticsSettings
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
import java.util.Locale

class AnalyticsUserInputViewModel @AssistedInject constructor(
    @Assisted val type: AnalyticsUserInputFragment.InputType,
    private val settings: AnalyticsSettings,
    @AppContext private val context: Context,
    private val districtsSource: Districts,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel() {

    private val ageGroupSource: Flow<List<UserInfoItem>> = flowOf(PpaData.PPAAgeGroup.values())
        .map { ages ->
            val selected = settings.userInfoAgeGroup.value
            ages.mapNotNull { age ->
                if (age == PpaData.PPAAgeGroup.UNRECOGNIZED) return@mapNotNull null
                UserInfoItem(
                    data = age,
                    isSelected = age == selected,
                    label = age.getStringLabel().toResolvingString()
                )
            }
        }

    private val federalStateSource: Flow<List<UserInfoItem>> = flowOf(PpaData.PPAFederalState.values())
        .map { states ->
            val selected = settings.userInfoFederalState.value
            val items = states
                .mapNotNull { state ->
                    if (state == PpaData.PPAFederalState.UNRECOGNIZED) return@mapNotNull null
                    if (state == PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED) return@mapNotNull null
                    UserInfoItem(
                        data = state,
                        isSelected = state == selected,
                        label = state.getStringLabel().toResolvingString()
                    )
                }
                .sortedBy { it.label.get(context).toLowerCase(Locale.ROOT) }

            val unspecified = UserInfoItem(
                data = PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED,
                isSelected = selected == PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED,
                label = PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED.getStringLabel().toResolvingString()
            )
            listOf(unspecified) + items
        }

    private val districtSource: Flow<List<UserInfoItem>> = flow { emit(districtsSource.loadDistricts()) }
        .map { allDistricts ->
            val ourStateCode = when (settings.userInfoFederalState.value) {
                PpaData.PPAFederalState.FEDERAL_STATE_BW -> "BW"
                PpaData.PPAFederalState.FEDERAL_STATE_BY -> "BY"
                PpaData.PPAFederalState.FEDERAL_STATE_BE -> "BE"
                PpaData.PPAFederalState.FEDERAL_STATE_BB -> "BB"
                PpaData.PPAFederalState.FEDERAL_STATE_HB -> "HB"
                PpaData.PPAFederalState.FEDERAL_STATE_HH -> "HH"
                PpaData.PPAFederalState.FEDERAL_STATE_HE -> "HE"
                PpaData.PPAFederalState.FEDERAL_STATE_MV -> "MV"
                PpaData.PPAFederalState.FEDERAL_STATE_NI -> "NI"
                PpaData.PPAFederalState.FEDERAL_STATE_NRW -> "NW"
                PpaData.PPAFederalState.FEDERAL_STATE_RP -> "RP"
                PpaData.PPAFederalState.FEDERAL_STATE_SL -> "SL"
                PpaData.PPAFederalState.FEDERAL_STATE_SN -> "SN"
                PpaData.PPAFederalState.FEDERAL_STATE_ST -> "ST"
                PpaData.PPAFederalState.FEDERAL_STATE_SH -> "SH"
                PpaData.PPAFederalState.FEDERAL_STATE_TH -> "TH"
                else -> null
            }
            allDistricts.filter { it.federalStateShortName == ourStateCode }
        }
        .map { districts ->
            val selected = settings.userInfoDistrict.value
            val items = districts
                .map { district ->
                    UserInfoItem(
                        data = district,
                        isSelected = district.districtId == selected,
                        label = district.districtName.toLazyString()
                    )
                }
                .sortedBy { it.label.get(context).toLowerCase(Locale.ROOT) }
            val unspecified = UserInfoItem(
                data = Districts.District(),
                isSelected = 0 == selected,
                label = R.string.analytics_userinput_district_unspecified.toResolvingString()
            )
            listOf(unspecified) + items
        }

    val userInfoItems: LiveData<List<UserInfoItem>> = when (type) {
        AnalyticsUserInputFragment.InputType.AGEGROUP -> ageGroupSource
        AnalyticsUserInputFragment.InputType.FEDERALSTATE -> federalStateSource
        AnalyticsUserInputFragment.InputType.DISTRICT -> districtSource
    }
        .catch { Timber.e(it, "Error sourcing list items.") }
        .asLiveData(context = dispatcherProvider.Default)

    val finishEvent = SingleLiveEvent<Unit>()

    fun selectUserInfoItem(item: UserInfoItem) {
        when (item.data) {
            is PpaData.PPAAgeGroup -> {
                settings.userInfoAgeGroup.update { item.data }
            }
            is PpaData.PPAFederalState -> {
                settings.userInfoFederalState.update { item.data }
                settings.userInfoDistrict.update { 0 }
            }
            is Districts.District -> {
                settings.userInfoDistrict.update { item.data.districtId }
            }
            else -> throw IllegalArgumentException()
        }
        finishEvent.postValue(Unit)
    }

    @StringRes
    private fun PpaData.PPAFederalState.getStringLabel(): Int = when (this) {
        PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED -> R.string.analytics_userinput_federalstate_unspecified
        PpaData.PPAFederalState.FEDERAL_STATE_BW -> R.string.analytics_userinput_federalstate_bw
        PpaData.PPAFederalState.FEDERAL_STATE_BY -> R.string.analytics_userinput_federalstate_by
        PpaData.PPAFederalState.FEDERAL_STATE_BE -> R.string.analytics_userinput_federalstate_be
        PpaData.PPAFederalState.FEDERAL_STATE_BB -> R.string.analytics_userinput_federalstate_bb
        PpaData.PPAFederalState.FEDERAL_STATE_HB -> R.string.analytics_userinput_federalstate_hb
        PpaData.PPAFederalState.FEDERAL_STATE_HH -> R.string.analytics_userinput_federalstate_hh
        PpaData.PPAFederalState.FEDERAL_STATE_HE -> R.string.analytics_userinput_federalstate_he
        PpaData.PPAFederalState.FEDERAL_STATE_MV -> R.string.analytics_userinput_federalstate_mv
        PpaData.PPAFederalState.FEDERAL_STATE_NI -> R.string.analytics_userinput_federalstate_ni
        PpaData.PPAFederalState.FEDERAL_STATE_NRW -> R.string.analytics_userinput_federalstate_nrw
        PpaData.PPAFederalState.FEDERAL_STATE_RP -> R.string.analytics_userinput_federalstate_rp
        PpaData.PPAFederalState.FEDERAL_STATE_SL -> R.string.analytics_userinput_federalstate_sl
        PpaData.PPAFederalState.FEDERAL_STATE_SN -> R.string.analytics_userinput_federalstate_sn
        PpaData.PPAFederalState.FEDERAL_STATE_ST -> R.string.analytics_userinput_federalstate_st
        PpaData.PPAFederalState.FEDERAL_STATE_SH -> R.string.analytics_userinput_federalstate_sh
        PpaData.PPAFederalState.FEDERAL_STATE_TH -> R.string.analytics_userinput_federalstate_th
        PpaData.PPAFederalState.UNRECOGNIZED -> throw IllegalArgumentException("PpaData.PPAFederalState.UNRECOGNIZED")
    }

    @StringRes
    private fun PpaData.PPAAgeGroup.getStringLabel(): Int = when (this) {
        PpaData.PPAAgeGroup.AGE_GROUP_UNSPECIFIED -> R.string.analytics_userinput_agegroup_unspecified
        PpaData.PPAAgeGroup.AGE_GROUP_0_TO_29 -> R.string.analytics_userinput_agegroup_0_to_29
        PpaData.PPAAgeGroup.AGE_GROUP_30_TO_59 -> R.string.analytics_userinput_agegroup_30_to_59
        PpaData.PPAAgeGroup.AGE_GROUP_FROM_60 -> R.string.analytics_userinput_agegroup_from_60
        PpaData.PPAAgeGroup.UNRECOGNIZED -> throw IllegalStateException("PpaData.PPAAgeGroup.UNRECOGNIZED")
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ContactDiaryDayViewModel> {
        fun create(type: AnalyticsUserInputFragment.InputType): AnalyticsUserInputViewModel
    }
}
