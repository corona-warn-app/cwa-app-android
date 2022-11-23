package de.rki.coronawarnapp.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.flow.combine
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class OnboardingAnalyticsViewModel @AssistedInject constructor(
    private val settings: AnalyticsSettings,
    private val dispatcherProvider: DispatcherProvider,
    private val analytics: Analytics,
    val districts: Districts,
    @AppScope private val appScope: CoroutineScope
) : CWAViewModel() {

    val completedOnboardingEvent = SingleLiveEvent<Unit>()

    val ageGroup = settings.userInfoAgeGroup.asLiveData2()
    val federalState = settings.userInfoFederalState.asLiveData2()
    val district: LiveData<Districts.District?> = combine(
        flow { emit(districts.loadDistricts()) },
        settings.userInfoDistrict
    ) { districtsList, id ->
        districtsList.singleOrNull { it.districtId == id }
    }.asLiveData(dispatcherProvider.IO)

    fun onProceed(enable: Boolean) {
        appScope.launch(context = dispatcherProvider.IO) {
            analytics.setAnalyticsEnabled(enabled = enable)
            settings.updateLastOnboardingVersionCode(BuildConfigWrap.VERSION_CODE)
        }
        completedOnboardingEvent.postValue(Unit)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingAnalyticsViewModel>
}
