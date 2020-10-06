package de.rki.coronawarnapp.ui.onboarding

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingPrivacyViewModel @AssistedInject constructor(): CWAViewModel() {
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() {
        routeToScreen.value = OnboardingNavigationEvents.NavigateToOnboardingTracing
    }

    fun onBackButtonClick() {
        routeToScreen.value = OnboardingNavigationEvents.NavigateToOnboardingFragment
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<OnboardingPrivacyViewModel>
}
