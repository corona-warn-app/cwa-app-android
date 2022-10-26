package de.rki.coronawarnapp.ui.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingNotificationsViewModel @AssistedInject constructor(
    private val settings: CWASettings
) : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() = launch {
        settings.updateLastNotificationsOnboardingVersionCode(BuildConfigWrap.VERSION_CODE)
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingAnalytics)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingNotificationsViewModel>
}
