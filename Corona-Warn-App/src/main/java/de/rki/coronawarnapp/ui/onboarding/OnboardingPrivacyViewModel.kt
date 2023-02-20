package de.rki.coronawarnapp.ui.onboarding

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eol.AppEol
import de.rki.coronawarnapp.util.coroutine.DefaultDispatcherProvider
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingPrivacyViewModel @AssistedInject constructor(
    eol: AppEol,
    dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()
    val isEol = eol.isEol.asLiveData(context = dispatcherProvider.Default)

    fun onNextButtonClick(eolSkip: Boolean) {
        if (eolSkip) {
            routeToScreen.postValue(OnboardingNavigationEvents.NavigateToMainActivity)
        } else {
            routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingTracing)
        }
    }

    fun onBackButtonClick() {
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingFragment)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingPrivacyViewModel>
}
