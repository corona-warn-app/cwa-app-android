package de.rki.coronawarnapp.ui.onboarding

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingNotificationsViewModel @AssistedInject constructor() : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() {
        routeToScreen.value = OnboardingNavigationEvents.NavigateToMainActivity
    }

    fun onBackButtonClick() {
        routeToScreen.value = OnboardingNavigationEvents.NavigateToOnboardingTracing
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<OnboardingNotificationsViewModel>
}
