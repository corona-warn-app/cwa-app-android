package de.rki.coronawarnapp.ui.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingTestViewModel @Inject constructor() : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() {
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingNotifications)
    }

    fun onBackButtonClick() {
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingTracing)
    }
}
