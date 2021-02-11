package de.rki.coronawarnapp.ui.settings.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class SettingsPrivacyPreservingAnalyticsViewModel @AssistedInject constructor(
    analyticsSettings: AnalyticsSettings,
    val districts: Districts,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel() {

    val ageGroup = analyticsSettings.userInfoAgeGroup.flow.asLiveData()
    val federalState = analyticsSettings.userInfoFederalState.flow.asLiveData()
    val district: LiveData<Districts.District?> = combine(
        flow { emit(districts.loadDistricts()) },
        analyticsSettings.userInfoDistrict.flow
    ) { districtsList, id ->
        districtsList.singleOrNull { it.districtId == id }
    }.asLiveData(dispatcherProvider.IO)

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SettingsPrivacyPreservingAnalyticsViewModel>

    fun analyticsToggleEnabled() {
        // TODO
    }
}
