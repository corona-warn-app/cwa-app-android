package de.rki.coronawarnapp.ui.onboarding

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingTestViewModel @AssistedInject constructor() : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() {
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingNotifications)
    }

    fun onBackButtonClick() {
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingTracing)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<OnboardingTestViewModel>
}
