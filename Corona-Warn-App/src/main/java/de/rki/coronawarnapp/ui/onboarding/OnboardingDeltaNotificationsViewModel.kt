package de.rki.coronawarnapp.ui.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first

class OnboardingDeltaNotificationsViewModel @AssistedInject constructor(
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

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingDeltaNotificationsViewModel>
}
