package de.rki.coronawarnapp.ui.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class OnboardingDeltaNotificationsViewModel @Inject constructor(
    private val settings: CWASettings,
    private val analyticsSettings: AnalyticsSettings,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<OnboardingDeltaNotificationsNavigationEvents> = SingleLiveEvent()

    fun onProceed() = launch {
        settings.updateLastNotificationsOnboardingVersionCode(BuildConfigWrap.VERSION_CODE)
        if (analyticsSettings.lastOnboardingVersionCode.first() == 0L) {
            routeToScreen.postValue(
                OnboardingDeltaNotificationsNavigationEvents.NavigateToOnboardingDeltaAnalyticsFragment
            )
        } else {
            routeToScreen.postValue(OnboardingDeltaNotificationsNavigationEvents.CloseScreen)
        }
    }
}
