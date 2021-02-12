package de.rki.coronawarnapp.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.flow

class OnboardingAnalyticsViewModel @AssistedInject constructor(
    settings: AnalyticsSettings,
    dispatcherProvider: DispatcherProvider,
    private val analytics: Analytics,
    val districts: Districts
) : CWAViewModel() {

    val completedOnboardingEvent = SingleLiveEvent<Unit>()

    val ageGroup = settings.userInfoAgeGroup.flow.asLiveData()
    val federalState = settings.userInfoFederalState.flow.asLiveData()
    val district: LiveData<Districts.District?> = combine(
        flow { emit(districts.loadDistricts()) },
        settings.userInfoDistrict.flow
    ) { districtsList, id ->
        districtsList.singleOrNull { it.districtId == id }
    }.asLiveData(dispatcherProvider.IO)

    fun onNextButtonClick() {
        launch {
            analytics.setAnalyticsEnabled(enabled = true)
        }

        completedOnboardingEvent.postValue(Unit)
    }

    fun onDisableClick() {
        launch {
            analytics.setAnalyticsEnabled(enabled = false)
        }

        completedOnboardingEvent.postValue(Unit)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingAnalyticsViewModel>
}
