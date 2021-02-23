package de.rki.coronawarnapp.ui.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingFragmentViewModel @AssistedInject constructor() : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() {
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingPrivacy)
    }

    fun onEasyLanguageClick() {
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToEasyLanguageUrl)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingFragmentViewModel>
}
