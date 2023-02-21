package de.rki.coronawarnapp.ui.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.eol.AppEol
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@HiltViewModel
class OnboardingPrivacyViewModel @Inject constructor(
    eol: AppEol,
) : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()
    private val isEol = eol.isEol

    fun onNextButtonClick() = launch {
        routeToScreen.postValue(
            if (isEol.first()) {
                OnboardingNavigationEvents.NavigateToMainActivity
            } else {
                OnboardingNavigationEvents.NavigateToOnboardingTracing
            }
        )
    }

    fun onBackButtonClick() {
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingFragment)
    }
}
