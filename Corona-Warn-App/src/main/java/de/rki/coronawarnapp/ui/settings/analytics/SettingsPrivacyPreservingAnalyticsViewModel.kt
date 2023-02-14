package de.rki.coronawarnapp.ui.settings.analytics

import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.flow.combine
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class SettingsPrivacyPreservingAnalyticsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    analyticsSettings: AnalyticsSettings,
    districts: Districts,
    private val analytics: Analytics
) : CWAViewModel() {

    val settingsPrivacyPreservingAnalyticsState = combine(
        analyticsSettings.userInfoAgeGroup,
        analyticsSettings.userInfoFederalState,
        analytics.isAnalyticsEnabledFlow(),
        flow { emit(districts.loadDistricts()) },
        analyticsSettings.userInfoDistrict
    ) { ageGroup, federalState, analyticsEnabled, districtsList, districtId ->
        val selectedDistrict = districtsList.singleOrNull { it.districtId == districtId }

        SettingsPrivacyPreservingAnalyticsState(
            isAnalyticsEnabled = analyticsEnabled,
            ageGroup = ageGroup,
            federalState = federalState,
            district = selectedDistrict
        )
    }.asLiveData(dispatcherProvider.IO)

    fun analyticsToggleEnabled() = launch {
        val analyticsState = analytics.isAnalyticsEnabledFlow().first()
        analytics.setAnalyticsEnabled(!analyticsState)
    }
}
