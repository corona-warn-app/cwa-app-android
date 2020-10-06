package de.rki.coronawarnapp.ui.onboarding

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingFragmentViewModel @AssistedInject constructor(): CWAViewModel() {
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() {
        routeToScreen.value = OnboardingNavigationEvents.NavigateToOnboardingPrivacy
    }

    fun onEasyLanguageClick() {
        routeToScreen.value = OnboardingNavigationEvents.NavigateToEasyLanguageUrl
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<OnboardingFragmentViewModel>
}
