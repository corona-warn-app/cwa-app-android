package de.rki.coronawarnapp.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.flow

class OnboardingAnalyticsViewModel @AssistedInject constructor(
    private val settings: AnalyticsSettings,
    private val donorModules: Set<@JvmSuppressWildcards DonorModule>,
    val districts: Districts,
    dispatcherProvider: DispatcherProvider
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
        settings.analyticsEnabled.update {
            true
        }

        completedOnboardingEvent.postValue(Unit)
    }

    fun onDisableClick() {
        settings.analyticsEnabled.update {
            false
        }

        launch {
            donorModules.forEach {
                it.deleteData()
            }
        }

        completedOnboardingEvent.postValue(Unit)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingAnalyticsViewModel>
}
